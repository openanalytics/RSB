/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.openanalytics.rsb.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.si.HeaderSettingMessageSourceWrapper;

/**
 * Handles directory based R job and result exchanges.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@Component("directoryDepositHandler")
public class DirectoryDepositHandler extends AbstractComponent implements BeanFactoryAware {
    public static final String INBOX_DIRECTORY_META_NAME = "inboxDirectory";
    public static final String ORIGINAL_FILENAME_META_NAME = "originalFilename";
    public static final String DEPOSIT_ROOT_DIRECTORY_META_NAME = "depositRootDirectory";

    @Resource(name = "jobDirectoryDepositChannel")
    private MessageChannel jobDirectoryDepositChannel;

    @Resource
    private FileListFilter<File> zipJobFilter;

    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<SourcePollingChannelAdapter>();

    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setJobDirectoryDepositChannel(final MessageChannel jobDirectoryDepositChannel) {
        this.jobDirectoryDepositChannel = jobDirectoryDepositChannel;
    }

    void setZipJobFilter(final FileListFilter<File> zipJobFilter) {
        this.zipJobFilter = zipJobFilter;
    }

    @PostConstruct
    public void setupChannelAdapters() {
        final List<DepositDirectoryConfiguration> depositRootDirectories = getConfiguration().getDepositRootDirectories();

        if ((depositRootDirectories == null) || (depositRootDirectories.isEmpty())) {
            return;
        }

        final NioFileLocker nioFileLocker = new NioFileLocker();

        for (final DepositDirectoryConfiguration depositRootDirectoryConfig : depositRootDirectories) {
            final PeriodicTrigger fileTrigger = new PeriodicTrigger(depositRootDirectoryConfig.getPollingPeriod(), TimeUnit.MILLISECONDS);
            fileTrigger.setInitialDelay(5000L);

            final File depositRootDirectory = depositRootDirectoryConfig.getRootDirectory();

            final FileReadingMessageSource fileMessageSource = new FileReadingMessageSource();
            fileMessageSource.setAutoCreateDirectory(true);
            fileMessageSource.setBeanFactory(beanFactory);
            fileMessageSource.setBeanName("rsb-deposit-dir-ms-" + depositRootDirectory.getPath());
            fileMessageSource.setDirectory(new File(depositRootDirectory, Configuration.DEPOSIT_JOBS_SUBDIR));
            fileMessageSource.setFilter(zipJobFilter);
            fileMessageSource.setLocker(nioFileLocker);
            fileMessageSource.afterPropertiesSet();

            final HeaderSettingMessageSourceWrapper<File> messageSource = new HeaderSettingMessageSourceWrapper<File>(
                    fileMessageSource, Constants.APPLICATION_NAME_MESSAGE_HEADER, depositRootDirectoryConfig.getApplicationName());

            final SourcePollingChannelAdapter channelAdapter = new SourcePollingChannelAdapter();
            channelAdapter.setBeanFactory(beanFactory);
            channelAdapter.setBeanName("rsb-deposit-dir-ca-" + depositRootDirectory.getPath());
            channelAdapter.setOutputChannel(jobDirectoryDepositChannel);
            channelAdapter.setSource(messageSource);
            channelAdapter.setTrigger(fileTrigger);
            channelAdapter.afterPropertiesSet();
            channelAdapter.start();

            getLogger().info("Started channel adapter: " + channelAdapter);

            channelAdapters.add(channelAdapter);
        }
    }

    @PreDestroy
    public void closeChannelAdapters() {
        for (final SourcePollingChannelAdapter channelAdapter : channelAdapters) {
            channelAdapter.stop();
            getLogger().info("Stopped channel adapter: " + channelAdapter);
        }
    }

    public void handleZipJob(final Message<File> message) throws IOException {
        final String applicationName = message.getHeaders().get(Constants.APPLICATION_NAME_MESSAGE_HEADER, String.class);
        final File zipJobFile = message.getPayload();

        final File depositRootDirectory = zipJobFile.getParentFile().getParentFile();
        final File acceptedDirectory = new File(depositRootDirectory, Configuration.DEPOSIT_ACCEPTED_SUBDIR);

        final File acceptedFile = new File(acceptedDirectory, zipJobFile.getName());
        FileUtils.deleteQuietly(acceptedFile); // in case a similar job already exists
        FileUtils.moveFile(zipJobFile, acceptedFile);

        final Map<String, Serializable> meta = new HashMap<String, Serializable>();
        meta.put(DEPOSIT_ROOT_DIRECTORY_META_NAME, depositRootDirectory);
        meta.put(ORIGINAL_FILENAME_META_NAME, zipJobFile.getName());
        meta.put(INBOX_DIRECTORY_META_NAME, zipJobFile.getParent());

        final MultiFilesJob job = new MultiFilesJob(Source.DIRECTORY, applicationName, UUID.randomUUID(),
                (GregorianCalendar) GregorianCalendar.getInstance(), meta);

        try {
            MultiFilesJob.addZipFilesToJob(new FileInputStream(acceptedFile), job);
            getMessageDispatcher().dispatch(job);
        } catch (final Exception e) {
            final MultiFilesResult errorResult = job.buildErrorResult(e, getMessages());
            handleZipResult(errorResult);
        }

    }

    public void handleZipResult(final MultiFilesResult result) throws IOException {
        final File resultFile = MultiFilesResult.zipResultFilesIfNotError(result);
        final File resultsDirectory = new File((File) result.getMeta().get(DEPOSIT_ROOT_DIRECTORY_META_NAME),
                Configuration.DEPOSIT_RESULTS_SUBDIR);

        final File outboxResultFile = new File(resultsDirectory, "result-"
                + FilenameUtils.getBaseName((String) result.getMeta().get(ORIGINAL_FILENAME_META_NAME)) + "."
                + FilenameUtils.getExtension(resultFile.getName()));

        FileUtils.deleteQuietly(outboxResultFile); // in case a similar result already exists
        FileUtils.moveFile(resultFile, outboxResultFile);
        result.destroy();
    }
}

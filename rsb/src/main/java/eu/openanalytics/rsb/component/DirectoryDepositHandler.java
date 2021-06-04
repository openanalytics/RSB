/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb.component;

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;
import static org.eclipse.statet.jcommons.io.FileUtils.requireParent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.file.FileLocker;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.si.BasicFileLocker;
import eu.openanalytics.rsb.si.HeaderSettingMessageSourceWrapper;


/**
 * Handles directory based R job and result exchanges.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("directoryDepositHandler")
public class DirectoryDepositHandler extends AbstractResource implements BeanFactoryAware
{
    public static final String DIRECTORY_CONFIG_HEADER_NAME = DirectoryDepositHandler.class.getName();
    public static final String INBOX_DIRECTORY_META_NAME = "inboxDirectory";
    public static final String ORIGINAL_FILENAME_META_NAME = "originalFilename";
    public static final String DEPOSIT_ROOT_DIRECTORY_META_NAME = "depositRootDirectory";

    @Resource(name = "directoryDepositChannel")
    private MessageChannel directoryDepositChannel;

    @Resource
    private FileListFilter<File> zipJobFilter;
	
	private FileLocker fileLocker= new BasicFileLocker();
	
    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<>();

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setZipJobFilter(final FileListFilter<File> zipJobFilter)
    {
        this.zipJobFilter = zipJobFilter;
    }

    @PostConstruct
    public void setupChannelAdapters() throws Exception
    {
        final List<DepositDirectoryConfiguration> depositDirectoryConfigurations = getConfiguration().getDepositRootDirectories();

        if ((depositDirectoryConfigurations == null) || (depositDirectoryConfigurations.isEmpty()))
        {
            return;
        }

        for (final DepositDirectoryConfiguration depositDirectoryConfiguration : depositDirectoryConfigurations)
        {
            final PeriodicTrigger fileTrigger = new PeriodicTrigger(
                depositDirectoryConfiguration.getPollingPeriod(), TimeUnit.MILLISECONDS);
            fileTrigger.setInitialDelay(5000L);
			
			final File depositRootDirectory= depositDirectoryConfiguration.getRootDirectory();
			
			final var fileMessageSource= new FileReadingMessageSource();
			fileMessageSource.setAutoCreateDirectory(true);
			fileMessageSource.setBeanFactory(this.beanFactory);
			fileMessageSource.setBeanName("rsb-deposit-dir-ms-" + depositRootDirectory.getPath());
			fileMessageSource.setDirectory(new File(depositRootDirectory, Configuration.DEPOSIT_JOBS_SUBDIR));
			fileMessageSource.setFilter(this.zipJobFilter);
			fileMessageSource.setLocker(this.fileLocker);
			fileMessageSource.afterPropertiesSet();
			
			final var messageSource= new HeaderSettingMessageSourceWrapper<>(
					fileMessageSource, DIRECTORY_CONFIG_HEADER_NAME, depositDirectoryConfiguration );
			
			final var channelAdapter= new SourcePollingChannelAdapter() {
				@Override
				protected void handleMessage(final Message<?> messageArg) {
					try {
						super.handleMessage(messageArg);
					}
					finally {
						final Object payload= messageArg.getPayload();
						if (payload instanceof File) {
							try {
								DirectoryDepositHandler.this.fileLocker.unlock((File)payload);
							}
							catch (final MessagingException e) {}
						}
					}
				}
			};
            channelAdapter.setBeanFactory(this.beanFactory);
            channelAdapter.setBeanName("rsb-deposit-dir-ca-" + depositRootDirectory.getPath());
            channelAdapter.setOutputChannel(this.directoryDepositChannel);
            channelAdapter.setSource(messageSource);
            channelAdapter.setTrigger(fileTrigger);
            channelAdapter.afterPropertiesSet();
            channelAdapter.start();

            getLogger().info("Started channel adapter: " + channelAdapter);

            this.channelAdapters.add(channelAdapter);
        }
    }

    @PreDestroy
    public void closeChannelAdapters()
    {
        for (final SourcePollingChannelAdapter channelAdapter : this.channelAdapters)
        {
            channelAdapter.stop();
            getLogger().info("Stopped channel adapter: " + channelAdapter);
        }
    }
	
	
	/** via Spring Integration */
	public void handleJob(final Message<File> message) throws IOException {
		final DepositDirectoryConfiguration depositDirectoryConfiguration= message.getHeaders()
				.get(DIRECTORY_CONFIG_HEADER_NAME, DepositDirectoryConfiguration.class);
		final var depositRootDirectory= depositDirectoryConfiguration.getRootDirectory().toPath();
		final var applicationName= depositDirectoryConfiguration.getApplicationName();
		
		final var dataFile= message.getPayload().toPath();
		final var fileName= requireFileName(dataFile).toString();
		final var acceptedFile= depositRootDirectory.resolve(Configuration.DEPOSIT_ACCEPTED_SUBDIR)
				.resolve(fileName);
		
		Files.move(dataFile, acceptedFile, StandardCopyOption.REPLACE_EXISTING);
		this.fileLocker.unlock(dataFile.toFile());
		
		final Map<String, Serializable> meta= new HashMap<>();
		meta.put(DEPOSIT_ROOT_DIRECTORY_META_NAME, depositRootDirectory.toUri());
		meta.put(INBOX_DIRECTORY_META_NAME, requireParent(dataFile).toUri());
		meta.put(ORIGINAL_FILENAME_META_NAME, fileName);
		
		final MultiFilesJob job= new MultiFilesJob(Source.DIRECTORY, applicationName,
				getUserName(), UUID.randomUUID(),
				(GregorianCalendar)GregorianCalendar.getInstance(),
				meta );
		try {
			try (final var in= Files.newInputStream(acceptedFile)) {
				MultiFilesJob.addDataToJob(Util.getContentType(acceptedFile), fileName, in, job);
			}
			
			final String jobConfigurationFileName= depositDirectoryConfiguration.getJobConfigurationFileName();
			if (StringUtils.isNotBlank(jobConfigurationFileName)) {
				final Path jobConfigurationFile= getJobConfigurationFile(applicationName,
						jobConfigurationFileName );
				try (final var in= Files.newInputStream(jobConfigurationFile)) {
					job.addFile(Constants.MULTIPLE_FILES_JOB_CONFIGURATION, in);
				}
			}
			
			getMessageDispatcher().dispatch(job);
		}
		catch (final Exception e) {
			try {
				final Exception errorInfo= HandlerUtils.handleJobError(this, e);
				final MultiFilesResult errorResult= job.buildErrorResult(errorInfo, getMessages());
				handleResult(errorResult);
			}
			finally {
				job.destroy();
			}
		}
	}
	
	public void handleResult(final MultiFilesResult result) throws IOException {
		try {
			final var resultFile= MultiFilesResult.zipResultFilesIfNotError(result);
			final var resultsDirectory= toPath(result.getMeta().get(DEPOSIT_ROOT_DIRECTORY_META_NAME))
					.resolve(Configuration.DEPOSIT_RESULTS_SUBDIR);
			
			final var outboxResultFile= resultsDirectory.resolve("result-"
					+ FilenameUtils.getBaseName((String)result.getMeta().get(ORIGINAL_FILENAME_META_NAME))
					+ "." + FilenameUtils.getExtension(requireFileName(resultFile).toString()) );
			
			Files.move(resultFile, outboxResultFile, StandardCopyOption.REPLACE_EXISTING);
		}
		finally {
			result.destroy();
		}
	}
	
	private static Path toPath(final Object o) {
		if (o instanceof URI) {
			return Path.of((URI)o);
		}
		if (o instanceof File) {
			return ((File)o).toPath();
		}
		throw new IllegalArgumentException(o.getClass().getName());
	}
	
}

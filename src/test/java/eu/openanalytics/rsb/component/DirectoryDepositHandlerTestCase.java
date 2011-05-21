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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.support.MessageBuilder;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectoryDepositHandlerTestCase {
    private static final String TEST_APPLICATION_NAME = "test_app_name";
    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    MessageChannel jobDirectoryDepositChannel;

    @Mock
    FileListFilter<File> zipJobFilter;

    private DirectoryDepositHandler directoryDepositHandler;

    @Before
    public void prepareTest() throws UnknownHostException {
        directoryDepositHandler = new DirectoryDepositHandler();
        directoryDepositHandler.setConfiguration(configuration);
        directoryDepositHandler.setMessageDispatcher(messageDispatcher);
        directoryDepositHandler.setBeanFactory(beanFactory);
        directoryDepositHandler.setJobDirectoryDepositChannel(jobDirectoryDepositChannel);
        directoryDepositHandler.setZipJobFilter(zipJobFilter);
    }

    @Test
    public void setupChannelAdapters() {
        directoryDepositHandler.setupChannelAdapters();
    }

    @Test
    public void closeChannelAdapters() {
        directoryDepositHandler.closeChannelAdapters();
    }

    @Test
    public void handleZipJob() throws Exception {
        final URL jobSample = Thread.currentThread().getContextClassLoader().getResource("data/r-job-sample.zip");

        final File jobParentFile = mock(File.class);
        when(jobParentFile.getParentFile()).thenReturn(FileUtils.getTempDirectory());

        final File zipJobFile = mock(File.class);
        when(zipJobFile.getParentFile()).thenReturn(jobParentFile);
        when(zipJobFile.getName()).thenReturn("fake");
        when(zipJobFile.exists()).thenReturn(true);
        when(zipJobFile.getCanonicalPath()).thenReturn("fake_path");
        when(zipJobFile.length()).thenReturn(FileUtils.sizeOf(new File(jobSample.toURI())));
        when(zipJobFile.getPath()).thenReturn(jobSample.getPath());
        when(zipJobFile.delete()).thenReturn(true);

        final DepositDirectoryConfiguration depositRootDirectoryConfig = mock(DepositDirectoryConfiguration.class);
        when(depositRootDirectoryConfig.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        when(configuration.getDepositRootDirectories()).thenReturn(Collections.singletonList(depositRootDirectoryConfig));

        final Message<File> message = MessageBuilder.withPayload(zipJobFile)
                .setHeader(Constants.APPLICATION_NAME_MESSAGE_HEADER, TEST_APPLICATION_NAME).build();

        directoryDepositHandler.handleZipJob(message);

        final ArgumentCaptor<MultiFilesJob> jobCaptor = ArgumentCaptor.forClass(MultiFilesJob.class);
        verify(messageDispatcher).dispatch(jobCaptor.capture());

        final MultiFilesJob job = jobCaptor.getValue();
        assertThat(job.getApplicationName(), is(TEST_APPLICATION_NAME));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.INBOX_DIRECTORY_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.ORIGINAL_FILENAME_META_NAME), is(true));
        assertThat(job.getSource(), is(Source.DIRECTORY));
        job.destroy();
    }

    @Test
    public void handleZipResult() throws IOException {
        final Map<String, Serializable> meta = new HashMap<String, Serializable>();
        meta.put(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME, FileUtils.getTempDirectory());

        final MultiFilesResult multiFilesResult = mock(MultiFilesResult.class);
        when(multiFilesResult.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        when(multiFilesResult.getPayload()).thenReturn(new File[0]);
        when(multiFilesResult.getTemporaryDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(multiFilesResult.getMeta()).thenReturn(meta);

        directoryDepositHandler.handleZipResult(multiFilesResult);
    }
}

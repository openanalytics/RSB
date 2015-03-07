/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectoryDepositHandlerTestCase
{
    private static final String TEST_APPLICATION_NAME = "test_app_name";
    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private BeanFactory beanFactory;

    private DirectoryDepositHandler directoryDepositHandler;

    @Before
    public void prepareTest() throws UnknownHostException
    {
        directoryDepositHandler = new DirectoryDepositHandler();
        directoryDepositHandler.setConfiguration(configuration);
        directoryDepositHandler.setMessageDispatcher(messageDispatcher);
        directoryDepositHandler.setBeanFactory(beanFactory);
    }

    @Test
    public void setupChannelAdapters()
    {
        directoryDepositHandler.setupChannelAdapters();
    }

    @Test
    public void closeChannelAdapters()
    {
        directoryDepositHandler.closeChannelAdapters();
    }

    @Test
    public void handleJobWithZipFile() throws Exception
    {
        final File jobParentFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(jobParentFile);

        final File zipJobFile = File.createTempFile("test-", ".zip", jobParentFile);
        FileUtils.copyInputStreamToFile(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("data/r-job-sample.zip"),
            zipJobFile);

        testHandleJob(jobParentFile, zipJobFile);
    }

    @Test
    public void handleJobWithPlainFile() throws Exception
    {
        final File jobParentFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(jobParentFile);

        final File zipJobFile = File.createTempFile("test-", ".dat", jobParentFile);
        FileUtils.copyInputStreamToFile(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("data/fake_data.dat"),
            zipJobFile);

        testHandleJob(jobParentFile, zipJobFile);
    }

    private void testHandleJob(final File jobParentFile, final File zipJobFile) throws IOException
    {
        final DepositDirectoryConfiguration depositRootDirectoryConfig = mock(DepositDirectoryConfiguration.class);
        when(depositRootDirectoryConfig.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        when(configuration.getDepositRootDirectories()).thenReturn(
            Collections.singletonList(depositRootDirectoryConfig));

        final Message<File> message = MessageBuilder.withPayload(zipJobFile)
            .setHeader(DirectoryDepositHandler.DIRECTORY_CONFIG_HEADER_NAME, depositRootDirectoryConfig)
            .build();

        directoryDepositHandler.handleJob(message);

        final ArgumentCaptor<MultiFilesJob> jobCaptor = ArgumentCaptor.forClass(MultiFilesJob.class);
        verify(messageDispatcher).dispatch(jobCaptor.capture());

        final MultiFilesJob job = jobCaptor.getValue();
        assertThat(job.getApplicationName(), is(TEST_APPLICATION_NAME));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME),
            is(true));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.INBOX_DIRECTORY_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(DirectoryDepositHandler.ORIGINAL_FILENAME_META_NAME), is(true));
        assertThat(job.getSource(), is(Source.DIRECTORY));
        job.destroy();

        FileUtils.forceDelete(jobParentFile);
    }

    @Test
    public void handleResult() throws IOException
    {
        final Map<String, Serializable> meta = new HashMap<String, Serializable>();
        meta.put(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME, FileUtils.getTempDirectory());

        final MultiFilesResult multiFilesResult = mock(MultiFilesResult.class);
        when(multiFilesResult.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        when(multiFilesResult.getPayload()).thenReturn(new File[0]);
        when(multiFilesResult.getTemporaryDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(multiFilesResult.getMeta()).thenReturn(meta);

        directoryDepositHandler.handleResult(multiFilesResult);
    }
}

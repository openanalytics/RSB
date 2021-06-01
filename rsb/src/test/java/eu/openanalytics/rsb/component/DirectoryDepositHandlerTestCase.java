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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static eu.openanalytics.rsb.test.TestUtils.getTestDataFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.statet.jcommons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.ConfigurationFactory;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.test.TestUtils;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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
        this.directoryDepositHandler = new DirectoryDepositHandler();
        this.directoryDepositHandler.setConfiguration(this.configuration);
        this.directoryDepositHandler.setMessageDispatcher(this.messageDispatcher);
        this.directoryDepositHandler.setBeanFactory(this.beanFactory);
    }

    @Test
    public void setupChannelAdapters()
    {
        try {
          this.directoryDepositHandler.setupChannelAdapters();
        } catch (final Exception e) {
          e.printStackTrace();
          fail("Unexpected exception thrown in @PostConstruct method setupChannelAdapters() of DirectoryDepositHandler");
        }
    }

    @Test
    public void closeChannelAdapters()
    {
        this.directoryDepositHandler.closeChannelAdapters();
    }
	
	
	@Test
	public void handleJobWithZipFile() throws Exception {
		final Path testDirectory= TestUtils.createTestDirectory();
		ConfigurationFactory.createDepositeDirectories(testDirectory);
		
		final Path jobFile= Files.createTempFile(
				testDirectory.resolve(Configuration.DEPOSIT_JOBS_SUBDIR), "test-", ".zip");
		try (final var out= Files.newOutputStream(jobFile)) {
			Files.copy(getTestDataFile("r-job-sample.zip"), out);
		}
		
		testHandleJob(testDirectory, jobFile);
	}
	
	@Test
	public void handleJobWithPlainFile() throws Exception {
		final Path testDirectory= TestUtils.createTestDirectory();
		ConfigurationFactory.createDepositeDirectories(testDirectory);
		
		final Path jobFile= Files.createTempFile(
				testDirectory.resolve(Configuration.DEPOSIT_JOBS_SUBDIR), "test-", ".dat" );
		try (final var out= Files.newOutputStream(jobFile)) {
			Files.copy(getTestDataFile("fake_data.dat"), out);
		}
		
		testHandleJob(testDirectory, jobFile);
	}
	
	private void testHandleJob(final Path testDirectory, final Path jobFile) throws IOException {
		final DepositDirectoryConfiguration depositRootDirectoryConfig= mock(DepositDirectoryConfiguration.class);
		when(depositRootDirectoryConfig.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
		when(this.configuration.getDepositRootDirectories()).thenReturn(
				Collections.singletonList(depositRootDirectoryConfig));
		
		final Message<File> message= MessageBuilder.withPayload(jobFile.toFile())
				.setHeader(DirectoryDepositHandler.DIRECTORY_CONFIG_HEADER_NAME, depositRootDirectoryConfig)
				.build();
		
		this.directoryDepositHandler.handleJob(message);
		
		final ArgumentCaptor<MultiFilesJob> jobCaptor= ArgumentCaptor.forClass(MultiFilesJob.class);
		verify(this.messageDispatcher).dispatch(jobCaptor.capture());
		
		final MultiFilesJob job= jobCaptor.getValue();
		assertEquals(TEST_APPLICATION_NAME, job.getApplicationName());
		assertTrue(job.getMeta().containsKey(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME));
		assertTrue(job.getMeta().containsKey(DirectoryDepositHandler.INBOX_DIRECTORY_META_NAME));
		assertTrue(job.getMeta().containsKey(DirectoryDepositHandler.ORIGINAL_FILENAME_META_NAME));
		assertEquals(Source.DIRECTORY, job.getSource());
		
		job.destroy();
		
		FileUtils.deleteRecursively(testDirectory);
	}
	
	
	@Test
	public void handleResult() throws IOException {
		final Path testDirectory= TestUtils.createTestDirectory();
		ConfigurationFactory.createDepositeDirectories(testDirectory);
		
		final Map<String, Serializable> meta= new HashMap<>();
		meta.put(DirectoryDepositHandler.DEPOSIT_ROOT_DIRECTORY_META_NAME, testDirectory.toUri());
		
		final MultiFilesResult multiFilesResult= mock(MultiFilesResult.class);
		when(multiFilesResult.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
		when(multiFilesResult.getPayload()).thenReturn(List.of());
		when(multiFilesResult.getTemporaryDirectory()).thenReturn(testDirectory);
		when(multiFilesResult.getMeta()).thenReturn(meta);
		
		this.directoryDepositHandler.handleResult(multiFilesResult);
	}
	
}

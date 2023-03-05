/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.List;

import org.eclipse.statet.jcommons.status.ProgressMonitor;
import org.eclipse.statet.jcommons.status.StatusException;

import org.eclipse.statet.rj.data.RObject;
import org.eclipse.statet.rj.data.impl.RCharacter32Store;
import org.eclipse.statet.rj.data.impl.RVectorImpl;
import org.eclipse.statet.rj.servi.RServi;
import org.eclipse.statet.rj.services.FunctionCall;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;
import eu.openanalytics.rsb.rservi.RServiUriSelector;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;
import eu.openanalytics.rsb.test.TestUtils;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class JobProcessorTestCase
{
    private JobProcessor jobProcessor;

    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private RServiInstanceProvider rServiInstanceProvider;
    @Mock
    private JobStatisticsHandler jobStatisticsHandler;
    @Mock
    private RServiUriSelector rServiUriSelector;

    @Before
    public void prepareTest() throws URISyntaxException
    {
        jobProcessor = new JobProcessor();
        jobProcessor.setConfiguration(configuration);
        jobProcessor.setMessageDispatcher(messageDispatcher);
        jobProcessor.setRServiUriSelector(rServiUriSelector);
        jobProcessor.setRServiInstanceProvider(rServiInstanceProvider);
        jobProcessor.setJobStatisticsHandler(jobStatisticsHandler);

        when(rServiUriSelector.getUriForApplication(anyString())).thenReturn(new URI("fake://default"));
    }

    @Test(expected = RuntimeException.class)
    public void processFunctionCallJobRserviProviderError() throws Exception
    {
        when(
            rServiInstanceProvider.getRServiInstance(anyString(), anyString(),
                eq(PoolingStrategy.IF_POSSIBLE))).thenThrow(
            new RuntimeException("simulated RServi provider issue"));

        jobProcessor.process(mock(AbstractFunctionCallJob.class));
    }

    @Test
    public void processRserviError() throws Exception
    {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(
            rServiInstanceProvider.getRServiInstance(anyString(), anyString(),
                eq(PoolingStrategy.IF_POSSIBLE))).thenReturn(rServi);
        final Throwable exception = new RuntimeException("simulated RServi issue");
        when(rServi.createFunctionCall(anyString())).thenThrow(exception);
        final AbstractFunctionCallJob job = mock(AbstractFunctionCallJob.class);
        @SuppressWarnings("unchecked")
        final AbstractResult<Object> result = mock(AbstractResult.class);
        when(job.buildErrorResult(eq(exception), any(MessageSource.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return result;
            }
        });

        jobProcessor.process(job);

        verify(messageDispatcher).dispatch(eq(result));
    }

    @Test
    public void processDirect() throws Exception
    {
        final URI defaultPoolUri = new URI("fake://default");
        final AbstractFunctionCallJob job = setupMocksForProcessingFunctionCallJob(defaultPoolUri);
        final AbstractFunctionCallResult result = setupResultMockForFunctionCallJob(job);

        final AbstractFunctionCallResult processDirectResult = (AbstractFunctionCallResult) jobProcessor.processDirect(job);

        verify(jobStatisticsHandler).storeJobStatistics(eq(job), any(Calendar.class), anyLong(),
            eq(defaultPoolUri.toString()));
        verifyZeroInteractions(messageDispatcher);

        assertThat(processDirectResult, is(result));
    }

    @Test
    public void processFunctionCallJob() throws Exception
    {
        final URI defaultPoolUri = new URI("fake://default");
        final AbstractFunctionCallJob job = setupMocksForProcessingFunctionCallJob(defaultPoolUri);
        final AbstractFunctionCallResult result = setupResultMockForFunctionCallJob(job);

        jobProcessor.process(job);

        verify(jobStatisticsHandler).storeJobStatistics(eq(job), any(Calendar.class), anyLong(),
            eq(defaultPoolUri.toString()));
        verify(messageDispatcher).dispatch(eq(result));
    }

    @Test
    public void processMultiFilesJobNoRScript() throws Exception
    {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenReturn(
            rServi);

        final MultiFilesJob job = mock(MultiFilesJob.class);
        final MultiFilesResult result = mock(MultiFilesResult.class);
        when(job.buildErrorResult(any(IllegalArgumentException.class), any(MessageSource.class))).thenReturn(
            result);

        jobProcessor.process(job);

        verify(messageDispatcher).dispatch(eq(result));
    }
	
	@Test
	public void processMultiFilesJob() throws Exception {
		final URI defaultPoolUri= new URI("fake://default");
		
		when(this.configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
		final RServi rServi= mock(RServi.class);
		when(this.rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER)))
				.thenReturn(rServi);
		final FunctionCall functionCall= mock(FunctionCall.class);
		when(rServi.createFunctionCall(anyString())).thenReturn(functionCall);
		final RObject rObject= new RVectorImpl<>(new RCharacter32Store(new String[0]));
		when(rServi.evalData(anyString(), (ProgressMonitor)isNull())).thenReturn(rObject);
		final MultiFilesJob job= mock(MultiFilesJob.class);
		final var scriptFile= Files.createTempFile(TestUtils.getTempDirectory(), "rsb", "test");
		when(job.getRScriptFile()).thenReturn(scriptFile);
		when(job.getFiles()).thenReturn(List.of(scriptFile));
		final MultiFilesResult result= mock(MultiFilesResult.class);
		when(job.buildSuccessResult()).thenReturn(result);
		
		this.jobProcessor.process(job);
		
		verify(this.jobStatisticsHandler).storeJobStatistics(eq(job), any(Calendar.class), anyLong(),
				eq(defaultPoolUri.toString()));
		verify(this.messageDispatcher).dispatch(eq(result));
	}
	
    private AbstractFunctionCallResult setupResultMockForFunctionCallJob(final AbstractFunctionCallJob job)
        throws IOException
    {
        final AbstractFunctionCallResult result = mock(AbstractFunctionCallResult.class);
        when(job.buildSuccessResult("fake_result")).thenReturn(result);
        return result;
    }

    private AbstractFunctionCallJob setupMocksForProcessingFunctionCallJob(final URI defaultPoolUri)
        throws Exception, StatusException, IOException
    {
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(
            rServiInstanceProvider.getRServiInstance(anyString(), anyString(),
                eq(PoolingStrategy.IF_POSSIBLE))).thenReturn(rServi);
        final FunctionCall functionCall = mock(FunctionCall.class);
        when(rServi.createFunctionCall(anyString())).thenReturn(functionCall);
        final RObject rObject = new RVectorImpl<>(new RCharacter32Store(
            new String[]{"fake_result"}));
        when(functionCall.evalData(null)).thenReturn(rObject);
        final AbstractFunctionCallJob job = mock(AbstractFunctionCallJob.class);
        return job;
    }
}

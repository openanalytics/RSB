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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import de.walware.rj.data.RObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RVectorImpl;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class JobProcessorTestCase {

    private JobProcessor jobProcessor;

    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private RServiInstanceProvider rServiInstanceProvider;
    @Mock
    private JobStatisticsHandler jobStatisticsHandler;

    @Before
    public void prepareTest() throws UnknownHostException {
        jobProcessor = new JobProcessor();
        jobProcessor.setConfiguration(configuration);
        jobProcessor.setMessageDispatcher(messageDispatcher);
        jobProcessor.setRServiInstanceProvider(rServiInstanceProvider);
        jobProcessor.setJobStatisticsHandler(jobStatisticsHandler);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getRServiPoolUri() throws URISyntaxException {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);

        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(null);
        assertThat(jobProcessor.getRServiPoolUri("appname"), is(defaultPoolUri));

        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(Collections.EMPTY_MAP);
        assertThat(jobProcessor.getRServiPoolUri("appname"), is(defaultPoolUri));

        final URI appSpecificPoolUri = new URI("fake://appname");
        when(configuration.getApplicationSpecificRserviPoolUris()).thenReturn(Collections.singletonMap("appname", appSpecificPoolUri));
        assertThat(jobProcessor.getRServiPoolUri("appname"), is(appSpecificPoolUri));
    }

    @Test(expected = RuntimeException.class)
    public void processFunctionCallJobRserviProviderError() throws Exception {
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenThrow(
                new RuntimeException("simulated RServi provider issue"));

        jobProcessor.process(mock(AbstractFunctionCallJob.class));
    }

    @Test
    public void processRserviError() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);
        final Throwable exception = new RuntimeException("simulated RServi issue");
        when(rServi.createFunctionCall(anyString())).thenThrow(exception);
        final AbstractFunctionCallJob job = mock(AbstractFunctionCallJob.class);
        @SuppressWarnings("unchecked")
        final AbstractResult<Object> result = mock(AbstractResult.class);
        when(job.buildErrorResult(eq(exception), any(MessageSource.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return result;
            }
        });

        jobProcessor.process(job);

        verify(messageDispatcher).dispatch(eq(result));
    }

    @Test
    public void processFunctionCallJob() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);
        final FunctionCall functionCall = mock(FunctionCall.class);
        when(rServi.createFunctionCall(anyString())).thenReturn(functionCall);
        final RObject rObject = new RVectorImpl<RCharacterDataImpl>(new RCharacterDataImpl(new String[] { "fake_result" }));
        when(functionCall.evalData(null)).thenReturn(rObject);
        final AbstractFunctionCallJob job = mock(AbstractFunctionCallJob.class);
        final AbstractFunctionCallResult result = mock(AbstractFunctionCallResult.class);
        when(job.buildSuccessResult("fake_result")).thenReturn(result);

        jobProcessor.process(job);

        verify(jobStatisticsHandler).storeJobStatistics(anyString(), any(UUID.class), any(Calendar.class), anyLong(),
                eq(defaultPoolUri.toString()));
        verify(messageDispatcher).dispatch(eq(result));
    }

    @Test
    public void processMultiFilesJobNoRScript() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);

        final MultiFilesJob job = mock(MultiFilesJob.class);
        final MultiFilesResult result = mock(MultiFilesResult.class);
        when(job.buildErrorResult(any(IllegalArgumentException.class), any(MessageSource.class))).thenReturn(result);

        jobProcessor.process(job);

        verify(messageDispatcher).dispatch(eq(result));
    }

    @Test
    public void processMultiFilesJob() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);
        final FunctionCall functionCall = mock(FunctionCall.class);
        when(rServi.createFunctionCall(anyString())).thenReturn(functionCall);
        final RObject rObject = new RVectorImpl<RCharacterDataImpl>(new RCharacterDataImpl(new String[0]));
        when(rServi.evalData(anyString(), (IProgressMonitor) isNull())).thenReturn(rObject);
        final MultiFilesJob job = mock(MultiFilesJob.class);
        final File scriptFile = File.createTempFile("rsb", "test");
        scriptFile.deleteOnExit();
        when(job.getRScriptFile()).thenReturn(scriptFile);
        when(job.getFiles()).thenReturn(new File[] { scriptFile });
        final MultiFilesResult result = mock(MultiFilesResult.class);
        when(job.buildSuccessResult()).thenReturn(result);

        jobProcessor.process(job);

        verify(jobStatisticsHandler).storeJobStatistics(anyString(), any(UUID.class), any(Calendar.class), anyLong(),
                eq(defaultPoolUri.toString()));
        verify(messageDispatcher).dispatch(eq(result));
    }
}

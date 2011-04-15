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
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import de.walware.rj.data.RObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RVectorImpl;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class JobProcessorTestCase {
    private JobProcessor jobProcessor;
    @Mock
    private Configuration configuration;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private RServiInstanceProvider rServiInstanceProvider;

    @Before
    public void prepareTest() throws UnknownHostException {
        jobProcessor = new JobProcessor();
        jobProcessor.setConfiguration(configuration);
        jobProcessor.setJmsTemplate(jmsTemplate);
        jobProcessor.setRServiInstanceProvider(rServiInstanceProvider);
    }

    @Test
    public void destroyNullJobStatisticsHandler() {
        when(configuration.getJobStatisticsHandler()).thenReturn(null);

        jobProcessor.destroyJobStatisticsHandler();
    }

    @Test
    public void destroyJobStatisticsHandler() {
        final JobStatisticsHandler jobStatisticsHandler = mock(JobStatisticsHandler.class);
        when(configuration.getJobStatisticsHandler()).thenReturn(jobStatisticsHandler);

        jobProcessor.destroyJobStatisticsHandler();

        verify(jobStatisticsHandler).destroy();
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

    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void processFunctionCallJobRserviProviderError() throws Exception {
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenThrow(
                new RuntimeException("simulated RServi provider issue"));

        jobProcessor.process(mock(AbstractFunctionCallJob.class));
    }

    @Test
    public void processFunctionCallJobRserviError() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);
        when(rServi.createFunctionCall(anyString())).thenThrow(new RuntimeException("simulated RServi issue"));
        @SuppressWarnings("unchecked")
        final AbstractFunctionCallJob<AbstractFunctionCallResult> functionCallJob = mock(AbstractFunctionCallJob.class);
        final AbstractFunctionCallResult functionCallResult = mock(AbstractFunctionCallResult.class);
        when(functionCallJob.buildResult(false, "simulated RServi issue")).thenReturn(functionCallResult);

        jobProcessor.process(functionCallJob);

        verify(jmsTemplate).send(matches("r\\.results\\..*"), any(MessageCreator.class));
    }

    @Test
    public void processFunctionCall() throws Exception {
        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);
        @SuppressWarnings("unchecked")
        final AbstractFunctionCallJob<AbstractFunctionCallResult> functionCallJob = mock(AbstractFunctionCallJob.class);
        final FunctionCall functionCall = mock(FunctionCall.class);
        when(rServi.createFunctionCall(anyString())).thenReturn(functionCall);
        final RObject rObject = new RVectorImpl<RCharacterDataImpl>(new RCharacterDataImpl(new String[] { "fake_result" }));
        when(functionCall.evalData(null)).thenReturn(rObject);
        final AbstractFunctionCallResult functionCallResult = mock(AbstractFunctionCallResult.class);
        when(functionCallJob.buildResult(true, "fake_result")).thenReturn(functionCallResult);
        final JobStatisticsHandler jobStatisticsHandler = mock(JobStatisticsHandler.class);
        when(configuration.getJobStatisticsHandler()).thenReturn(jobStatisticsHandler);

        jobProcessor.process(functionCallJob);

        verify(jobStatisticsHandler).storeJobStatistics(anyString(), any(UUID.class), any(Calendar.class), anyLong(),
                eq(defaultPoolUri.toString()));
        verify(jmsTemplate).send(matches("r\\.results\\..*"), any(MessageCreator.class));
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2020
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 */

package eu.openanalytics.rsb.message;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.JmsMessageDispatcher;
import eu.openanalytics.rsb.message.JmsMessageDispatcher.WorkItemMessagePostProcessor;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class JmsMessageDispatcherTestCase {
    private JmsMessageDispatcher jmsDispatcher;

    @Mock
    private Configuration configuration;
    @Mock
    private JmsTemplate jmsTemplate;

    @Before
    public void prepareTest() throws UnknownHostException {
        jmsDispatcher = new JmsMessageDispatcher();
        jmsDispatcher.setConfiguration(configuration);
        jmsDispatcher.setJmsTemplate(jmsTemplate);
    }

    @Test
    public void dispatchJob() {
        final AbstractJob job = mock(AbstractJob.class);
        jmsDispatcher.dispatch(job);
        verify(jmsTemplate).convertAndSend(matches("r\\.jobs\\..*"), any(AbstractJob.class), any(WorkItemMessagePostProcessor.class));
    }

    @Test
    public void dispatchResult() {
        final AbstractResult<?> result = mock(AbstractResult.class);
        jmsDispatcher.dispatch(result);
        verify(jmsTemplate).convertAndSend(matches("r\\.results\\..*"), any(AbstractResult.class), any(WorkItemMessagePostProcessor.class));
    }

    @Test
    public void process() {
        final AbstractJob job = mock(AbstractJob.class);
        when(job.getJobId()).thenReturn(UUID.randomUUID());
        final AbstractResult<?> result = mock(AbstractResult.class);
        when(jmsTemplate.receiveSelectedAndConvert(matches("r\\.results\\..*"), anyString())).thenReturn(result);

        assertEquals(jmsDispatcher.process(job), result);
        verify(jmsTemplate).convertAndSend(matches("r\\.jobs\\..*"), any(AbstractResult.class), any(WorkItemMessagePostProcessor.class));
    }
}

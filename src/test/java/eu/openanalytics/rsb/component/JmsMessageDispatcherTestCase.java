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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import eu.openanalytics.rsb.component.JmsMessageDispatcher.WorkItemMessagePostProcessor;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
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
}

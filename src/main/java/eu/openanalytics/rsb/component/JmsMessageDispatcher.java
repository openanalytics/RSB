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

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem;
import eu.openanalytics.rsb.message.MessageDispatcher;

/**
 * A JMS-backed a Job and Result message dispatcher.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("messageDispatcher")
public class JmsMessageDispatcher extends AbstractComponent implements MessageDispatcher {
    static final class WorkItemMessagePostProcessor implements MessagePostProcessor {
        private final AbstractWorkItem workItem;

        private WorkItemMessagePostProcessor(final AbstractWorkItem workItem) {
            this.workItem = workItem;
        }

        public Message postProcessMessage(final Message message) throws JMSException {
            message.setStringProperty(Constants.SOURCE_JMS_HEADER, workItem.getSource().toString());
            message.setStringProperty(Constants.APPLICATION_NAME_JMS_HEADER, workItem.getApplicationName());
            message.setStringProperty(Constants.JOB_ID_JMS_HEADER, workItem.getJobId().toString());
            return message;
        }
    }

    @Resource
    private JmsTemplate jmsTemplate;

    // exposed for unit tests
    void setJmsTemplate(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Dispatches an {@link AbstractJob} over JMS.
     * 
     * @param job
     * @param jmsTemplate
     */
    public void dispatch(final AbstractJob job) {
        jmsTemplate.convertAndSend(getQueueName(job), job, new WorkItemMessagePostProcessor(job));
    }

    /**
     * Dispatches an {@link AbstractResult} over JMS.
     * 
     * @param result
     * @param jmsTemplate
     */
    public void dispatch(final AbstractResult<?> result) {
        jmsTemplate.convertAndSend(getQueueName(result), result, new WorkItemMessagePostProcessor(result));
    }

    private static String getQueueName(final AbstractJob job) {
        return "r.jobs." + job.getApplicationName();
    }

    private static String getQueueName(final AbstractResult<?> result) {
        return "r.results." + result.getApplicationName();
    }
}

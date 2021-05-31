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

package eu.openanalytics.rsb.message;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.component.AbstractComponent;


/**
 * A JMS-backed a Job and Result message dispatcher.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("messageDispatcher")
public class JmsMessageDispatcher extends AbstractComponent implements MessageDispatcher
{
    static final class WorkItemMessagePostProcessor implements MessagePostProcessor
    {
        private final AbstractWorkItem workItem;

        private WorkItemMessagePostProcessor(final AbstractWorkItem workItem)
        {
            this.workItem = workItem;
        }

		@Override
		public Message postProcessMessage(final Message message) throws JMSException
        {
            message.setStringProperty(Constants.SOURCE_MESSAGE_HEADER, workItem.getSource().toString());
            message.setStringProperty(Constants.APPLICATION_NAME_MESSAGE_HEADER,
                workItem.getApplicationName());
            message.setStringProperty(Constants.JOB_ID_MESSAGE_HEADER, workItem.getJobId().toString());
            message.setJMSPriority(workItem.getPriority());
            return message;
        }
    }

    @Resource
    private JmsTemplate jmsTemplate;

    // exposed for unit tests
    void setJmsTemplate(final JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }

	@PreAuthorize("hasPermission(#job, 'APPLICATION_JOB')")
	@Override
	public void dispatch(final AbstractJob job)
    {
        jmsTemplate.convertAndSend(getJobQueueName(job), job, new WorkItemMessagePostProcessor(job));
    }

	@Override
	public void dispatch(final AbstractResult<?> result)
    {
        jmsTemplate.convertAndSend(getResultQueueName(result), result, new WorkItemMessagePostProcessor(
            result));
    }

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractResult<?>> T process(final AbstractJob job)
    {
        dispatch(job);
        final Object result = jmsTemplate.receiveSelectedAndConvert(getResultQueueName(job),
            Constants.JOB_ID_MESSAGE_HEADER + "='" + job.getJobId().toString() + "'");
        return (T) result;
    }

    private static String getJobQueueName(final AbstractWorkItem work)
    {
        return "r.jobs." + work.getApplicationName();
    }

    private static String getResultQueueName(final AbstractWorkItem work)
    {
        return "r.results." + work.getApplicationName();
    }
}

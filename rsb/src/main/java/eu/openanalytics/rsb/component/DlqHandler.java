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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem;


/**
 * Handles messages that end up in the dead letter queue.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("dlqHandler")
public class DlqHandler extends AbstractComponent {
    @Resource(name = "outboundEmailChannel")
    private MessageChannel outboundEmailChannel;

    // exposed for unit testing
    void setOutboundEmailChannel(final MessageChannel outboundEmailChannel) {
        this.outboundEmailChannel = outboundEmailChannel;
    }

    /**
     * Handles a job whose processing has failed repetitively.
     * 
     * @param job
     * @throws IOException
     */
    public void handle(final AbstractJob job) throws IOException {
        final String message = getMessages().getMessage(job.getAbortMessageId(), null, null);
        final ST template = Util.newStringTemplate(message);
        template.add("job", job);
        final String descriptiveMessage = template.render();

        logAndAlertFailure(job, descriptiveMessage);

        final AbstractResult<?> errorResult = job.buildErrorResult(new RuntimeException(descriptiveMessage), getMessages());
        getMessageDispatcher().dispatch(errorResult);
    }

    /**
     * Handles a result whose delivery has failed repetitively.
     * 
     * @param result
     */
    public void handle(final AbstractResult<?> result) {
        final String message = getMessages().getMessage("result.abort", null, null);
        final ST template = Util.newStringTemplate(message);
        template.add("result", result);
        final String descriptiveMessage = template.toString();

        logAndAlertFailure(result, descriptiveMessage);
    }

    private void logAndAlertFailure(final AbstractWorkItem workItem, final String descriptiveMessage) {
        // do not call workItem.destroy() to keep faulty file in the file system for inspection
        getLogger().error("Abandonning processing of: " + workItem);

        if (StringUtils.isNotBlank(getConfiguration().getAdministratorEmail())) {
            final Map<String, Object> headers = new HashMap<>();
            headers.put(MailHeaders.FROM, getConfiguration().getAdministratorEmail());
            headers.put(MailHeaders.TO, getConfiguration().getAdministratorEmail());
            headers.put(MailHeaders.SUBJECT, "Notification of RSB Fatal Error");

            final Message<String> message = new GenericMessage<>(descriptiveMessage, headers);
            outboundEmailChannel.send(message);
        }
    }
}

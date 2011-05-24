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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.lang.StringUtils;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.message.GenericMessage;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem;

/**
 * Handles messages that end up in the dead letter queue.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
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
        final StringTemplate template = new StringTemplate(message, DefaultTemplateLexer.class);
        template.setAttribute("job", job);
        final String descriptiveMessage = template.toString();

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
        final StringTemplate template = new StringTemplate(message, DefaultTemplateLexer.class);
        template.setAttribute("result", result);
        final String descriptiveMessage = template.toString();

        logAndAlertFailure(result, descriptiveMessage);
    }

    private void logAndAlertFailure(final AbstractWorkItem workItem, final String descriptiveMessage) {
        // do not call workItem.destroy() to keep faulty file in the file system for inspection
        getLogger().error("Abandonning processing of: " + workItem);

        if (StringUtils.isNotBlank(getConfiguration().getAdministratorEmail())) {
            final Map<String, Object> headers = new HashMap<String, Object>();
            headers.put(MailHeaders.FROM, getConfiguration().getAdministratorEmail());
            headers.put(MailHeaders.TO, getConfiguration().getAdministratorEmail());
            headers.put(MailHeaders.SUBJECT, "Notification of RSB Fatal Error");

            final Message<String> message = new GenericMessage<String>(descriptiveMessage, headers);
            outboundEmailChannel.send(message);
        }
    }
}

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

package eu.openanalytics.rsb.si;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;

import eu.openanalytics.rsb.Constants;

/**
 * A message source that wraps an existing one and adds an application name to the message
 * properties.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class ApplicationNameAwareMessageSourceWrapper<T> implements MessageSource<T> {
    // TODO unit test
    private final MessageSource<T> wrappedMessageSource;
    private final String applicationName;

    public ApplicationNameAwareMessageSourceWrapper(final MessageSource<T> wrappedMessageSource, final String applicationName) {
        this.wrappedMessageSource = wrappedMessageSource;
        this.applicationName = applicationName;
    }

    public Message<T> receive() {
        final Message<T> receivedMessage = wrappedMessageSource.receive();

        if (receivedMessage == null) {
            return null;
        }

        return MessageBuilder.fromMessage(receivedMessage).setHeader(Constants.APPLICATION_NAME_MESSAGE_HEADER, applicationName).build();
    }
}

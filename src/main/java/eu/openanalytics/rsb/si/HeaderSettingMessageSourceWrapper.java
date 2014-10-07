/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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

/**
 * A message source that wraps an existing one and adds an arbitrary header to the in-flight message.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class HeaderSettingMessageSourceWrapper<T> implements MessageSource<T> {
    private final MessageSource<T> wrappedMessageSource;
    private final String headerName;
    private final Object headerValue;

    public HeaderSettingMessageSourceWrapper(final MessageSource<T> wrappedMessageSource, final String headerName, final Object headerValue) {
        this.wrappedMessageSource = wrappedMessageSource;
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    public Message<T> receive() {
        final Message<T> receivedMessage = wrappedMessageSource.receive();

        if (receivedMessage == null) {
            return null;
        }

        final MessageBuilder<T> messageBuilder = MessageBuilder.fromMessage(receivedMessage).setHeader(headerName, headerValue);
        return messageBuilder.build();
    }
}

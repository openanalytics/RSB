/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.MessageDispatcher;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class AbstractComponent
{
    private final Log logger = LogFactory.getLog(getClass());

    @Resource
    private Configuration configuration;

    @Resource
    private MessageSource messages;

    @Resource
    private MessageDispatcher messageDispatcher;

    public void setMessageDispatcher(final MessageDispatcher messageDispatcher)
    {
        this.messageDispatcher = messageDispatcher;
    }

    public void setConfiguration(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setMessages(final MessageSource messages)
    {
        this.messages = messages;
    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    protected MessageSource getMessages()
    {
        return messages;
    }

    protected Log getLogger()
    {
        return logger;
    }

    protected MessageDispatcher getMessageDispatcher()
    {
        return messageDispatcher;
    }
}

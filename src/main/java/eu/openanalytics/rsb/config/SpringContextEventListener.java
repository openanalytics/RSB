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

package eu.openanalytics.rsb.config;

import java.io.IOException;

import javax.annotation.Resource;

import mx4j.tools.adaptor.http.HttpAdaptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class SpringContextEventListener implements ApplicationListener<ContextRefreshedEvent>
{
    private static final Log LOGGER = LogFactory.getLog(SpringContextEventListener.class);

    private static final String SPLASH = "\n\n____________________________\n"
                                         + "    ____       __     ____\n" + "    /    )   /    )   /   )\n"
                                         + "---/___ /----\\-------/__ /--\n"
                                         + "  /    |      \\     /    )\n"
                                         + "_/_____|__(____/___/____/___\n\n";

    @Resource
    private HttpAdaptor httpAdaptor;

    /**
     * Perform post init operations.
     */
    public void onApplicationEvent(final ContextRefreshedEvent event)
    {
        try
        {
            httpAdaptor.start();
        }
        catch (final IOException ioe)
        {
            LOGGER.error("Failed to start MX4J HTTP adaptor", ioe);
        }

        // who doesn't like ASCII art?
        LOGGER.info(SPLASH);
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

package eu.openanalytics.rsb.security;

import mx4j.tools.adaptor.http.HttpAdaptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.JmxConfiguration;

/**
 * Sub-class of {@link HttpAdaptor} that reads security configuration from RSB configuration and
 * sets it on the adaptor.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class SecurableMx4JHttpAdaptor extends HttpAdaptor
{
    private final Log LOGGER = LogFactory.getLog(SecurableMx4JHttpAdaptor.class);

    public SecurableMx4JHttpAdaptor(final Configuration configuration)
    {
        super();

        final JmxConfiguration jmxConfiguration = configuration.getJmxConfiguration();

        if (jmxConfiguration != null)
        {
            if (StringUtils.isNotBlank(jmxConfiguration.getHttpAuthenticationUsername()))
            {
                addAuthorization(jmxConfiguration.getHttpAuthenticationUsername(),
                    jmxConfiguration.getHttpAuthenticationPassword());

                setAuthenticationMethod("basic");

                LOGGER.info("Basic authentication active");
            }
        }
    }
}

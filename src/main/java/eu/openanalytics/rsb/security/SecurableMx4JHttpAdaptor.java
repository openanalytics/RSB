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

import java.io.IOException;
import java.security.GeneralSecurityException;

import mx4j.tools.adaptor.http.HttpAdaptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.OpenSSL;

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
    private static final Log LOGGER = LogFactory.getLog(SecurableMx4JHttpAdaptor.class);

    private static final String BASIC_AUTHENTICATION_METHOD = "basic";

    private final String httpAuthenticationUsername, httpAuthenticationEncryptedPassword;

    public SecurableMx4JHttpAdaptor(final Configuration configuration)
        throws IOException, GeneralSecurityException
    {
        super();

        final JmxConfiguration jmxConfiguration = configuration.getJmxConfiguration();

        if (jmxConfiguration != null)
        {
            if (StringUtils.isNotBlank(jmxConfiguration.getHttpAuthenticationUsername()))
            {
                httpAuthenticationUsername = jmxConfiguration.getHttpAuthenticationUsername();
                httpAuthenticationEncryptedPassword = jmxConfiguration.getHttpAuthenticationPassword();

                // decrypt the configured password using the username as the decryption password
                final byte[] decryptedPasswordBytes = OpenSSL.decrypt("des3",
                    httpAuthenticationUsername.toCharArray(),
                    httpAuthenticationEncryptedPassword.getBytes("UTF-8"));

                addAuthorization(httpAuthenticationUsername, new String(decryptedPasswordBytes, "UTF-8"));

                setAuthenticationMethod(BASIC_AUTHENTICATION_METHOD);

                LOGGER.info("Basic authentication active");
                return;
            }
        }

        httpAuthenticationUsername = null;
        httpAuthenticationEncryptedPassword = null;
    }
}

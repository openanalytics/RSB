/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2020
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
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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

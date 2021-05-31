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

package eu.openanalytics.rsb.security;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.CollectionUtils;

import eu.openanalytics.rsb.config.Configuration;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class JmxSecurityAuthenticator implements JMXAuthenticator
{
    private final Log LOGGER = LogFactory.getLog(JmxSecurityAuthenticator.class);

    @Resource(name = "authenticationManager")
    private AuthenticationManager authenticationManager;

    private final Configuration configuration;

    public JmxSecurityAuthenticator(final Configuration configuration)
    {
        Validate.notNull(configuration, "configuration can't be null");
        this.configuration = configuration;
    }

    @Override
    public Subject authenticate(final Object credentials)
    {
        try
        {
            final String[] info = (String[]) credentials;

            final Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                info[0], info[1]));

            final User authenticatedUser = (User) authentication.getPrincipal();

            if ((isRsbAdminPrincipal(authenticatedUser)) || (isRsbAdminRole(authenticatedUser)))
            {
                final Subject s = new Subject();
                s.getPrincipals().add(new JMXPrincipal(authentication.getName()));
                return s;
            }
            else
            {
                throw new SecurityException("Authenticated user " + authenticatedUser
                                            + " is not an RSB admin");
            }
        }
        catch (final Exception e)
        {
            LOGGER.error(
                "Error when trying to authenticate JMX credentials of type: " + credentials.getClass(), e);

            throw new SecurityException(e);
        }
    }

    private boolean isRsbAdminPrincipal(final User authenticatedUser)
    {
        return configuration.getRsbSecurityConfiguration() != null
               && configuration.getRsbSecurityConfiguration().getAdminPrincipals() != null
               && configuration.getRsbSecurityConfiguration()
                   .getAdminPrincipals()
                   .contains(authenticatedUser.getUsername());
    }

    private boolean isRsbAdminRole(final User authenticatedUser)
    {
        if ((configuration.getRsbSecurityConfiguration() == null)
            || (configuration.getRsbSecurityConfiguration().getAdminRoles() == null))
        {
            return false;
        }

        final Set<String> authoritiesNames = new HashSet<>();
        for (final GrantedAuthority authority : authenticatedUser.getAuthorities())
        {
            authoritiesNames.add(authority.getAuthority());
        }

        return CollectionUtils.containsAny(configuration.getRsbSecurityConfiguration().getAdminRoles(),
            authoritiesNames);
    }
}

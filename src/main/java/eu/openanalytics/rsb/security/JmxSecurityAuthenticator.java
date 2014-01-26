/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2014
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
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
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

        final Set<String> authoritiesNames = new HashSet<String>();
        for (final GrantedAuthority authority : authenticatedUser.getAuthorities())
        {
            authoritiesNames.add(authority.getAuthority());
        }

        return CollectionUtils.containsAny(configuration.getRsbSecurityConfiguration().getAdminRoles(),
            authoritiesNames);
    }
}

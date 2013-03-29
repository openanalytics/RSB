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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.ApplicationSecurityAuthorization;
import eu.openanalytics.rsb.config.Configuration.SecurityAuthorization;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.MultiFilesJob;

/**
 * Defines a {@link PermissionEvaluator} that considers the applications a user is granted to use.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ApplicationPermissionEvaluator implements PermissionEvaluator
{
    public static final String NO_AUTHENTICATED_USERNAME = null;

    @Resource
    private Configuration configuration;

    @Override
    public boolean hasPermission(final Authentication authentication,
                                 final Object targetDomainObject,
                                 final Object permission)
    {
        if (targetDomainObject == null)
        {
            return false;
        }

        if ("APPLICATION_JOB".equals(permission))
        {
            final AbstractJob job = (AbstractJob) targetDomainObject;
            return hasApplicationUserPermission(authentication, job);
        }
        else if ("RSB_RESOURCE".equals(permission))
        {
            final String resourceName = targetDomainObject.toString();
            return hasRsbResourcePermission(authentication, resourceName);
        }
        else
        {
            throw new SecurityException("Unknown permission: " + permission);
        }
    }

    private boolean hasApplicationUserPermission(final Authentication authentication, final AbstractJob job)
    {
        final Map<String, ApplicationSecurityAuthorization> applicationSecurityConfigurations = configuration.getApplicationSecurityConfiguration();

        if (applicationSecurityConfigurations != null)
        {
            final String applicationName = job.getApplicationName();
            final ApplicationSecurityAuthorization applicationSecurityConfiguration = applicationSecurityConfigurations.get(applicationName);
            return isAuthenticationAuthorized(authentication, applicationSecurityConfiguration)
                   && isJobAuthorized(job, applicationSecurityConfiguration);
        }
        else
        {
            return false;
        }
    }

    private boolean isJobAuthorized(final AbstractJob job,
                                    final ApplicationSecurityAuthorization applicationSecurityConfiguration)
    {
        if (applicationSecurityConfiguration == null)
        {
            return false;
        }

        if (job instanceof AbstractFunctionCallJob)
        {
            return applicationSecurityConfiguration.isFunctionCallAllowed();
        }
        else
        {
            final MultiFilesJob multiFilesJob = (MultiFilesJob) job;
            if (multiFilesJob.getRScriptFile() != null)
            {
                return applicationSecurityConfiguration.isScriptSubmissionAllowed();
            }
            else
            {
                return true;
            }
        }
    }

    private boolean hasRsbResourcePermission(final Authentication authentication, final String resourceName)
    {
        if (Constants.ADMIN_PATH.equals(resourceName))
        {
            return isAuthenticationAuthorized(authentication, configuration.getRsbSecurityConfiguration());
        }
        else
        {
            return false;
        }
    }

    private boolean isAuthenticationAuthorized(final Authentication authentication,
                                               final SecurityAuthorization securityAuthorization)
    {
        if (securityAuthorization == null)
        {
            return false;
        }

        final String userName = getUserName(authentication);

        if ((StringUtils.isNotBlank(userName))
            && (!CollectionUtils.isEmpty(securityAuthorization.getAuthorizedPrincipals()))
            && (securityAuthorization.getAuthorizedPrincipals().contains(userName)))
        {
            return true;
        }

        final Set<String> roles = new HashSet<String>();
        for (final GrantedAuthority authority : authentication.getAuthorities())
        {
            roles.add(authority.getAuthority());
        }

        return CollectionUtils.containsAny(securityAuthorization.getAuthorizedRoles(), roles);
    }

    private String getUserName(final Authentication authentication)
    {
        if (authentication.getPrincipal() instanceof UserDetails)
        {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean hasPermission(final Authentication authentication,
                                 final Serializable targetId,
                                 final String targetType,
                                 final Object permission)
    {
        throw new UnsupportedOperationException(
            "Application permission verification can only be done on objects");
    }
}

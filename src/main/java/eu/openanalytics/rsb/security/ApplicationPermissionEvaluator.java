/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2020
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 */

package eu.openanalytics.rsb.security;

import static eu.openanalytics.rsb.Constants.ADMIN_PATH;
import static eu.openanalytics.rsb.component.AdminResource.ADMIN_CATALOG_PATH;
import static eu.openanalytics.rsb.component.AdminResource.ADMIN_SYSTEM_PATH;

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

import eu.openanalytics.rsb.component.AdminResource;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.AdminSecurityAuthorization;
import eu.openanalytics.rsb.config.Configuration.ApplicationSecurityAuthorization;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.MultiFilesJob;

/**
 * Defines a {@link PermissionEvaluator} that considers the applications a user is granted to use.
 *
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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
        if ("CATALOG_USER".equals(permission))
        {
            return hasCatalogUserPermission(authentication, targetDomainObject);
        }
        else if ("CATALOG_ADMIN".equals(permission))
        {
            return hasCatalogAdminPermission(authentication, targetDomainObject);
        }

        if (targetDomainObject == null)
        {
            return false;
        }

        if ("APPLICATION_JOB".equals(permission))
        {
            final AbstractJob job = (AbstractJob) targetDomainObject;
            return hasApplicationJobPermission(authentication, job);
        }
        else if ("APPLICATION_USER".equals(permission))
        {
            final String applicationName = (String) targetDomainObject;
            return hasApplicationUserOrAdminPermission(authentication, applicationName);
        }
        else if ("APPLICATION_ADMIN".equals(permission))
        {
            final String applicationName = (String) targetDomainObject;
            return hasApplicationAdminPermission(authentication, applicationName);
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

    private boolean hasCatalogAdminPermission(final Authentication authentication,
                                              final Object targetDomainObject)
    {
        if (configuration.isApplicationAwareCatalog())
        {
            // in secure-mode with an application aware catalog, only admins of a specific
            // application can modify the application's catalog
            return hasPermission(authentication, targetDomainObject, "APPLICATION_ADMIN");
        }
        else
        {
            // in secure-mode with a non-application aware catalog, only RSB admins can
            // modify the catalog
            return hasRsbResourcePermission(authentication, AdminResource.ADMIN_CATALOG_PATH);
        }
    }

    private boolean hasCatalogUserPermission(final Authentication authentication,
                                             final Object targetDomainObject)
    {
        if (configuration.isApplicationAwareCatalog())
        {
            // in secure-mode with an application aware catalog, only users of a specific
            // application can read the application's catalog
            return hasPermission(authentication, targetDomainObject, "APPLICATION_USER");
        }
        else
        {
            // in secure-mode with a non-application aware catalog, anyone authenticated can
            // read the catalog
            return true;
        }
    }

    private boolean hasApplicationUserOrAdminPermission(final Authentication authentication,
                                                        final String applicationName)
    {
        return hasApplicationUserPermission(authentication, applicationName)
               || hasApplicationAdminPermission(authentication, applicationName);
    }

    private boolean hasApplicationJobPermission(final Authentication authentication, final AbstractJob job)
    {
        return hasApplicationAdminPermission(authentication, job.getApplicationName())
               || (hasApplicationUserPermission(authentication, job.getApplicationName()) && isJobAuthorized(job));
    }

    public boolean hasApplicationUserPermission(final Authentication authentication,
                                                final String applicationName)
    {
        final Map<String, ApplicationSecurityAuthorization> applicationSecurityConfigurations = configuration.getApplicationSecurityConfiguration();

        if (applicationSecurityConfigurations != null)
        {
            final ApplicationSecurityAuthorization applicationSecurityConfiguration = applicationSecurityConfigurations.get(applicationName);
            return isAuthenticationUser(authentication, applicationSecurityConfiguration);
        }
        else
        {
            return false;
        }
    }

    public boolean hasApplicationAdminPermission(final Authentication authentication,
                                                 final String applicationName)
    {
        // RSB admins are application admins
        if (isAuthenticationAdmin(authentication, configuration.getRsbSecurityConfiguration()))
        {
            return true;
        }

        final Map<String, ApplicationSecurityAuthorization> applicationSecurityConfigurations = configuration.getApplicationSecurityConfiguration();

        if (applicationSecurityConfigurations != null)
        {
            final ApplicationSecurityAuthorization applicationSecurityConfiguration = applicationSecurityConfigurations.get(applicationName);
            return isAuthenticationAdmin(authentication, applicationSecurityConfiguration);
        }
        else
        {
            return false;
        }
    }

    private boolean isJobAuthorized(final AbstractJob job)
    {
        final Map<String, ApplicationSecurityAuthorization> applicationSecurityConfigurations = configuration.getApplicationSecurityConfiguration();
        if (applicationSecurityConfigurations == null)
        {
            return false;
        }

        final String applicationName = job.getApplicationName();

        final ApplicationSecurityAuthorization applicationSecurityConfiguration = applicationSecurityConfigurations.get(applicationName);
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
        if (ADMIN_SYSTEM_PATH.equals(resourceName) || ADMIN_CATALOG_PATH.equals(resourceName)
            || ADMIN_PATH.equals(resourceName))
        {
            return isAuthenticationAdmin(authentication, configuration.getRsbSecurityConfiguration());
        }
        else
        {
            return false;
        }
    }

    private boolean isAuthenticationAdmin(final Authentication authentication,
                                          final AdminSecurityAuthorization adminSecurityAuthorization)
    {
        if (adminSecurityAuthorization == null)
        {
            return false;
        }

        return isAuthenticationAuthorized(authentication, adminSecurityAuthorization.getAdminPrincipals(),
            adminSecurityAuthorization.getAdminRoles());
    }

    private boolean isAuthenticationUser(final Authentication authentication,
                                         final ApplicationSecurityAuthorization applicationSecurityAuthorization)
    {
        if (applicationSecurityAuthorization == null)
        {
            return false;
        }

        return isAuthenticationAuthorized(authentication,
            applicationSecurityAuthorization.getUserPrincipals(),
            applicationSecurityAuthorization.getUserRoles());
    }

    private boolean isAuthenticationAuthorized(final Authentication authentication,
                                               final Set<String> authorizedPrincipals,
                                               final Set<String> authorizedRoles)
    {
        final String userName = getUserName(authentication);

        if ((StringUtils.isNotBlank(userName)) && (!CollectionUtils.isEmpty(authorizedPrincipals))
            && (authorizedPrincipals.contains(userName)))
        {
            return true;
        }

        final Set<String> roles = new HashSet<String>();
        for (final GrantedAuthority authority : authentication.getAuthorities())
        {
            roles.add(authority.getAuthority());
        }

        return CollectionUtils.containsAny(authorizedRoles, roles);
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

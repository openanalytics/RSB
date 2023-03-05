/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.openanalytics.rsb.Util;


/**
 * A {@link UserDetailsService} that accepts any user presented to it, which makes it
 * usable in scenarios where it is enough to merely identify a user.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class IdentifiedUserDetailsService implements UserDetailsService
{
    private static final String NO_PASSWORD = StringUtils.EMPTY;

    private String configuredAuthorities;
    private Set<GrantedAuthority> grantedAuthorities;

    @PostConstruct
    public void initializeGrantedAuthorities()
    {
        grantedAuthorities = new HashSet<>();

        if (StringUtils.isBlank(configuredAuthorities))
        {
            return;
        }

        for (final String configuredAuthority : StringUtils.split(configuredAuthorities, ", "))
        {
            final String strippedAuthority = StringUtils.stripToEmpty(configuredAuthority);
            if (StringUtils.isNotEmpty(strippedAuthority))
            {
                grantedAuthorities.add(new SimpleGrantedAuthority(strippedAuthority));
            }
        }
    }

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException
    {
        return new User(Util.replaceNonWordChars(username, "_"), NO_PASSWORD, grantedAuthorities);
    }

    public void setConfiguredAuthorities(final String configuredAuthorities)
    {
        this.configuredAuthorities = configuredAuthorities;
    }

    public String getConfiguredAuthorities()
    {
        return configuredAuthorities;
    }
}

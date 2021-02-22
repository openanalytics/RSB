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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.util.Assert;

/**
 * An {@link AbstractPreAuthenticatedProcessingFilter} that is designed to work behind Nginx dealing
 * with X.509 validation.
 * <p>
 * Expected headers are <code>X-SSL-Verified</code> (with value of <code>SUCCESS</code>) and
 * <code>X-SSL-Client-DN</code> (with DN as extracted from the client certificate).
 * <p>
 * By default, the client CN is used as the authentication principal.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 * @author Luke Taylor (for some code lifted from {@link SubjectDnX509PrincipalExtractor}).
 */
public class X509AuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter
{
    private static final String SSL_VERIFIED_HEADER = "X-SSL-Verified";
    private static final String SSL_CLIENT_DN_HEADER = "X-SSL-Client-DN";
    private Pattern subjectDnPattern;

    public X509AuthenticationFilter()
    {
        setSubjectDnRegex("CN=(.*?)(?:/|$)");
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request)
    {
        final String clientDN = (String) getPreAuthenticatedCredentials(request);
        if (clientDN == null)
        {
            return null;
        }

        logger.debug("Client DN is '" + clientDN + "'");

        final Matcher matcher = subjectDnPattern.matcher(clientDN);

        if (!matcher.find())
        {
            throw new BadCredentialsException("No matching pattern was found in client DN: " + clientDN);
        }

        if (matcher.groupCount() != 1)
        {
            throw new IllegalArgumentException("Regular expression must contain a single group ");
        }

        final String username = matcher.group(1);

        logger.debug("Extracted Principal name is '" + username + "'");

        return username;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request)
    {
        if (!StringUtils.equals(request.getHeader(SSL_VERIFIED_HEADER), "SUCCESS"))
        {
            return null;
        }

        return StringUtils.trimToNull(request.getHeader(SSL_CLIENT_DN_HEADER));
    }

    /**
     * Sets the regular expression which will by used to extract the user name from the
     * certificate's Subject DN.
     * <p>
     * It should contain a single group; for example the default expression "CN=(.*?)(?:/|$)"
     * matches the common name field. So "CN=Jimi Hendrix/OU=..." will give a user name of
     * "Jimi Hendrix".
     * <p>
     * The matches are case insensitive. So "emailAddress=(.?)/" will match
     * "EMAILADDRESS=jimi@hendrix.org/CN=..." giving a user name "jimi@hendrix.org"
     * 
     * @param subjectDnRegex the regular expression to find in the subject
     */
    public void setSubjectDnRegex(final String subjectDnRegex)
    {
        Assert.hasText(subjectDnRegex, "Regular expression may not be null or empty");
        subjectDnPattern = Pattern.compile(subjectDnRegex, Pattern.CASE_INSENSITIVE);
    }
}

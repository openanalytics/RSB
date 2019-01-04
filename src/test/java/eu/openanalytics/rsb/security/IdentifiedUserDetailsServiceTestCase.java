/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class IdentifiedUserDetailsServiceTestCase
{
    private static final String TEST_USER_NAME = "test_123";
    private static final String TEST_RAW_USER_NAME = "test@123";

    private IdentifiedUserDetailsService identifiedUserDetailsService;

    @Before
    public void setUp()
    {
        identifiedUserDetailsService = new IdentifiedUserDetailsService();
    }

    @Test
    public void noGrantedAuthorities()
    {
        identifiedUserDetailsService.initializeGrantedAuthorities();

        final UserDetails userDetails = identifiedUserDetailsService.loadUserByUsername(TEST_RAW_USER_NAME);

        assertThat(userDetails.getUsername(), is(TEST_USER_NAME));
        assertThat(userDetails.getAuthorities().size(), is(0));
    }

    @Test
    public void oneGrantedAuthorities()
    {
        identifiedUserDetailsService.setConfiguredAuthorities("auth1");
        identifiedUserDetailsService.initializeGrantedAuthorities();

        final UserDetails userDetails = identifiedUserDetailsService.loadUserByUsername(TEST_RAW_USER_NAME);

        assertThat(userDetails.getUsername(), is(TEST_USER_NAME));
        assertThat(userDetails.getAuthorities().size(), is(1));
    }

    @Test
    public void moreThanOneGrantedAuthorities()
    {
        identifiedUserDetailsService.setConfiguredAuthorities("auth1,auth2  auth3 , auth4");
        identifiedUserDetailsService.initializeGrantedAuthorities();

        final UserDetails userDetails = identifiedUserDetailsService.loadUserByUsername(TEST_RAW_USER_NAME);

        assertThat(userDetails.getUsername(), is(TEST_USER_NAME));
        assertThat(userDetails.getAuthorities().size(), is(4));
    }
}

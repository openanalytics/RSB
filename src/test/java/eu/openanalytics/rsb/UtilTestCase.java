/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
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
package eu.openanalytics.rsb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Test;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class UtilTestCase {
    @Test
    public void isValidApplicationName() {
        assertThat(Util.isValidApplicationName("test123"), is(true));
        assertThat(Util.isValidApplicationName("123ABC"), is(true));
        assertThat(Util.isValidApplicationName("123_ABC"), is(true));
        assertThat(Util.isValidApplicationName("123-ABC"), is(false));
        assertThat(Util.isValidApplicationName(null), is(false));
        assertThat(Util.isValidApplicationName(""), is(false));
        assertThat(Util.isValidApplicationName("-test"), is(false));
        assertThat(Util.isValidApplicationName("1 2 3"), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getSingleHeader() {
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        assertThat(Util.getSingleHeader(httpHeaders, "missing"), is(nullValue()));

        when(httpHeaders.getRequestHeader("missing_too")).thenReturn(Collections.EMPTY_LIST);
        assertThat(Util.getSingleHeader(httpHeaders, "missing_too"), is(nullValue()));

        when(httpHeaders.getRequestHeader("single_value")).thenReturn(Collections.singletonList("bingo"));
        assertThat(Util.getSingleHeader(httpHeaders, "single_value"), is("bingo"));

        when(httpHeaders.getRequestHeader("multi_value")).thenReturn(Arrays.asList("bingo_too", "ignored"));
        assertThat(Util.getSingleHeader(httpHeaders, "multi_value"), is("bingo_too"));
    }
}

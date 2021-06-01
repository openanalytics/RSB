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

package eu.openanalytics.rsb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class UtilTestCase
{
    @Test
    public void isValidApplicationName()
    {
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
    public void getSingleHeader()
    {
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        assertThat(Util.getSingleHeader(httpHeaders, "missing"), is(nullValue()));

        when(httpHeaders.getRequestHeader("missing_too")).thenReturn(Collections.EMPTY_LIST);
        assertThat(Util.getSingleHeader(httpHeaders, "missing_too"), is(nullValue()));

        when(httpHeaders.getRequestHeader("single_value")).thenReturn(Collections.singletonList("bingo"));
        assertThat(Util.getSingleHeader(httpHeaders, "single_value"), is("bingo"));

        when(httpHeaders.getRequestHeader("multi_value")).thenReturn(Arrays.asList("bingo_too", "ignored"));
        assertThat(Util.getSingleHeader(httpHeaders, "multi_value"), is("bingo_too"));
    }
	
	@Test
	public void getMimeType() {
		assertEquals(Constants.ZIP_MIME_TYPE.toString(), Util.getMimeType(Path.of("test.zip")).toString());
		assertEquals(Constants.TEXT_MIME_TYPE.toString(), Util.getMimeType(Path.of("test.err.txt")).toString());
		assertEquals(Constants.PDF_MIME_TYPE.toString(), Util.getMimeType(Path.of("test.pdf")).toString());
		assertEquals("application/octet-stream", Util.getMimeType(Path.of("test.foo")).toString());
	}
	
    @Test
    public void getResourceType()
    {
        assertThat(Util.getResourceType(Constants.ZIP_MIME_TYPE), is("zip"));
        assertThat(Util.getResourceType(Constants.PDF_MIME_TYPE), is("pdf"));
        assertThat(Util.getResourceType(Constants.TEXT_MIME_TYPE), is("txt"));
        assertThat(Util.getResourceType(Constants.DEFAULT_MIME_TYPE), is("dat"));
    }

    @Test
    public void convertToXmlDate()
    {
        final GregorianCalendar gmtMinus8Calendar = new GregorianCalendar(
            TimeZone.getTimeZone(TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000)[0]));
        gmtMinus8Calendar.set(2010, Calendar.JULY, 21, 11, 35, 48);
        gmtMinus8Calendar.set(GregorianCalendar.MILLISECOND, 456);

        final XMLGregorianCalendar xmlDate = Util.convertToXmlDate(gmtMinus8Calendar);
        assertThat(xmlDate.getTimezone(), is(0));
        assertThat(xmlDate.toXMLFormat(), is("2010-07-21T18:35:48.456Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newURIFailure()
    {
        Util.newURI(" a b c ");
    }

    @Test
    public void newURISuccess() throws URISyntaxException
    {
        assertThat(Util.newURI("foo://bar"), is(new URI("foo://bar")));
    }

    @Test
    public void normalizeJobMeta()
    {
        final Map<String, Serializable> source = new HashMap<>();
        source.put("foo", "bar");
        source.put(Constants.R_SCRIPT_CONFIGURATION_KEY.toLowerCase(Locale.ROOT), "r");
        source.put(Constants.SWEAVE_FILE_CONFIGURATION_KEY, "sweave");

        final Map<String, Serializable> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put(Constants.R_SCRIPT_CONFIGURATION_KEY, "r");
        expected.put(Constants.SWEAVE_FILE_CONFIGURATION_KEY, "sweave");

        assertThat(Util.normalizeJobMeta(source), is(expected));
    }

    @Test
    public void safeUuidFromString()
    {
        assertThat(Util.safeUuidFromString(null), is(nullValue()));
        assertThat(Util.safeUuidFromString(""), is(nullValue()));
        assertThat(Util.safeUuidFromString("bad"), is(nullValue()));

        final UUID testUuid = UUID.randomUUID();
        assertThat(Util.safeUuidFromString(testUuid.toString()), is(testUuid));
    }

    @Test
    public void replaceNonWordChars()
    {
        assertThat(Util.replaceNonWordChars(null, "_"), is(nullValue()));
        assertThat(Util.replaceNonWordChars("", "_"), is(""));
        assertThat(Util.replaceNonWordChars("  ", "_"), is("__"));
        assertThat(Util.replaceNonWordChars("abc_123", "_"), is("abc_123"));
        assertThat(Util.replaceNonWordChars("http://test.com", "_"), is("http___test_com"));
    }
}

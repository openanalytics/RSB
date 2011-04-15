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
package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.StartsWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.JobToken;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class JobsResourceTestCase {
    private static final String TEST_APP_NAME = "appName";

    private JobsResource jobsResource;

    @Mock
    private Configuration configuration;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void prepareTest() throws UnknownHostException {
        jobsResource = new JobsResource();
        jobsResource.setConfiguration(configuration);
        jobsResource.setJmsTemplate(jmsTemplate);
    }

    @Test
    public void testHandleJsonFunctionCallJob() throws URISyntaxException {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final JobToken jobToken = jobsResource.handleJsonFunctionCallJob("fake_json", httpHeaders, uriInfo);

        assertSuccessfullHandling(jobToken);
    }

    @Test
    public void testHandleXmlFunctionCallJob() throws URISyntaxException {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final JobToken jobToken = jobsResource.handleXmlFunctionCallJob("fake_xml", httpHeaders, uriInfo);

        assertSuccessfullHandling(jobToken);
    }

    @Test(expected = WebApplicationException.class)
    public void testHandleBadApplicationName() throws URISyntaxException {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(Collections.singletonList("_bad:app!$name"));

        jobsResource.handleXmlFunctionCallJob("fake_xml", httpHeaders, uriInfo);
    }

    @Test
    public void testHandleJobWithUriOverride() throws URISyntaxException {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(Collections.singletonList(TEST_APP_NAME));
        when(httpHeaders.getRequestHeader(Constants.URI_OVERRIDE_HTTP_HEADER)).thenReturn(Collections.singletonList("foo://bar"));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final JobToken jobToken = jobsResource.handleXmlFunctionCallJob("fake_xml", httpHeaders, uriInfo);

        assertSuccessfullHandling(jobToken);
        assertThat(jobToken.getResultUri(), is(new StartsWith("foo://bar")));
    }

    private void assertSuccessfullHandling(final JobToken jobToken) {
        assertThat(jobToken, notNullValue());
        assertThat(jobToken.getApplicationName(), is(TEST_APP_NAME));
        assertThat(jobToken.getJobId(), notNullValue());
        assertThat(jobToken.getResultUri(), notNullValue());
        verify(jmsTemplate).send(matches("r\\.jobs\\..*"), any(MessageCreator.class));
    }
}

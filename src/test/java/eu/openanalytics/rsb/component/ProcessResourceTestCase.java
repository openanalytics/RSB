/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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


package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProcessResourceTestCase
{
    private static final String FAKE_RESULT = "fake_result";

    private static final String TEST_APP_NAME = "appName";

    private ProcessResource processResource;

    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private JobProcessor jobProcessor;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private AbstractResult result;

    @Before
    public void prepareTest() throws Exception
    {
        processResource = new ProcessResource();
        processResource.setConfiguration(configuration);
        processResource.setMessageDispatcher(messageDispatcher);
        processResource.setJobProcessor(jobProcessor);

        when(result.getPayload()).thenReturn(FAKE_RESULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleBadApplicationName() throws Exception
    {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(
            Collections.singletonList("_bad:app!$name"));
        processResource.processXmlFunctionCallJob("fake_xml", httpHeaders);
        verifyZeroInteractions(messageDispatcher);
    }

    @Test
    public void handleJsonFunctionCallJob() throws Exception
    {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        when(result.isSuccess()).thenReturn(true);
        when(jobProcessor.processDirect(any(JsonFunctionCallJob.class))).thenReturn(result);

        assertSuccessfullProcessing(processResource.processJsonFunctionCallJob("fake_json", httpHeaders));
        verifyZeroInteractions(messageDispatcher);
    }

    @Test
    public void handleFailedJsonFunctionCallJob() throws Exception
    {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        when(result.isSuccess()).thenReturn(false);
        when(jobProcessor.processDirect(any(JsonFunctionCallJob.class))).thenReturn(result);

        assertFailedProcessing(processResource.processJsonFunctionCallJob("fake_json", httpHeaders));
        verifyZeroInteractions(messageDispatcher);
    }

    @Test
    public void handleXmlFunctionCallJob() throws Exception
    {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        when(result.isSuccess()).thenReturn(true);
        when(jobProcessor.processDirect(any(XmlFunctionCallJob.class))).thenReturn(result);

        assertSuccessfullProcessing(processResource.processXmlFunctionCallJob("fake_xml", httpHeaders));
        verifyZeroInteractions(messageDispatcher);
    }

    @Test
    public void handleFailedXmlFunctionCallJob() throws Exception
    {
        when(httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_HTTP_HEADER)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        when(result.isSuccess()).thenReturn(false);
        when(jobProcessor.processDirect(any(XmlFunctionCallJob.class))).thenReturn(result);

        assertFailedProcessing(processResource.processXmlFunctionCallJob("fake_xml", httpHeaders));
        verifyZeroInteractions(messageDispatcher);
    }

    private void assertSuccessfullProcessing(final Response response)
    {
        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity().toString(), is(FAKE_RESULT));
    }

    private void assertFailedProcessing(final Response response)
    {
        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity().toString(), is(FAKE_RESULT));
    }
}

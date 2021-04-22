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

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.matchers.StartsWith;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.rest.types.JobToken;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class JobsResourceTestCase
{
    private static final String TEST_APP_NAME = "appName";

    private JobsResource jobsResource;

    @Mock
    private Configuration configuration;
    @Mock
    private MessageDispatcher messageDispatcher;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void prepareTest() throws UnknownHostException
    {
        this.jobsResource = new JobsResource();
        this.jobsResource.setConfiguration(this.configuration);
        this.jobsResource.setMessageDispatcher(this.messageDispatcher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleBadApplicationName() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList("_bad:app!$name"));
        this.jobsResource.handleXmlFunctionCallJob("fake_xml", this.httpHeaders, this.uriInfo);
        verifyZeroInteractions(this.messageDispatcher);
    }

    @Test
    public void handleJobWithProtocolOverride() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(this.httpHeaders.getRequestHeader(Constants.FORWARDED_PROTOCOL_HTTP_HEADER)).thenReturn(
            Collections.singletonList("foo"));
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final JobToken jobToken = assertSuccessfullHandling(this.jobsResource.handleXmlFunctionCallJob("fake_xml",
            this.httpHeaders, this.uriInfo));
        assertThat(jobToken.getResultUri(), is(new StartsWith("foo:/")));
    }
	
    @Test
    public void handleJsonFunctionCallJob() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        assertSuccessfullHandling(this.jobsResource.handleJsonFunctionCallJob("fake_json", this.httpHeaders, this.uriInfo));
    }

    @Test
    public void handleXmlFunctionCallJob() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        assertSuccessfullHandling(this.jobsResource.handleXmlFunctionCallJob("fake_xml", this.httpHeaders, this.uriInfo));
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInvalidZipJob() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(this.httpHeaders.getRequestHeaders()).thenReturn(new MetadataMap<String, String>());
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        assertSuccessfullHandling(this.jobsResource.handleZipJob(getTestDataAsStream("invalid-job-subdir.zip"),
            this.httpHeaders, this.uriInfo));
    }

    @Test
    public void handleZipJob() throws Exception
    {
        when(this.httpHeaders.getRequestHeader(Constants.APPLICATION_NAME_FIELD_NAME)).thenReturn(
            Collections.singletonList(TEST_APP_NAME));
        when(this.httpHeaders.getRequestHeaders()).thenReturn(new MetadataMap<String, String>());
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
        assertSuccessfullHandling(this.jobsResource.handleZipJob(getTestDataAsStream("r-job-sample.zip"),
            this.httpHeaders, this.uriInfo));
    }
	
	
	@Test
	public void addJobMetaA() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaA(jobMeta, Constants.RSB_META_A_FIELD_NAME_PREFIX + "par1", "foo");
		
		assertEquals("foo", jobMeta.get("par1"));
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void addJobMetaA_missingName() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaA(jobMeta, Constants.RSB_META_A_FIELD_NAME_PREFIX, "foo");
	}
	
	@Test
	public void addJobMetaB() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaB(jobMeta, "par1=foo");
		
		assertEquals("foo", jobMeta.get("par1"));
	}
	
	@Test
	public void addJobMetaB_ignoreWhitespace() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaB(jobMeta, "par1 = \tfoo");
		
		assertEquals("foo", jobMeta.get("par1"));
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void addJobMetaB_invalid() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaB(jobMeta, "par1: foo");
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void addJobMetaB_missingName() throws Exception {
		final Map<String, Serializable>jobMeta= new HashMap<>();
		this.jobsResource.addJobMetaB(jobMeta, "= foo");
	}
	
	
    @Test
    public void handleMultipartFormJob() throws Exception
    {
        final Attachment applicationNamePart = mock(Attachment.class);
        final ContentDisposition applicationContentDisposition = mock(ContentDisposition.class);
        when(applicationNamePart.getContentDisposition()).thenReturn(applicationContentDisposition);
        when(applicationContentDisposition.getParameter(eq("name"))).thenReturn(
            Constants.APPLICATION_NAME_FIELD_NAME);
        when(applicationNamePart.getObject(eq(String.class))).thenReturn(TEST_APP_NAME);
        when(this.uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final List<Attachment> parts = Arrays.asList(applicationNamePart);
        assertSuccessfullHandling(this.jobsResource.handleMultipartFormJob(parts, this.httpHeaders, this.uriInfo));
    }

    private JobToken assertSuccessfullHandling(final Response response)
    {
        assertThat(response.getStatus(), is(Status.ACCEPTED.getStatusCode()));

        final JobToken jobToken = (JobToken) response.getEntity();
        assertThat(jobToken, notNullValue());
        assertThat(jobToken.getApplicationName(), is(TEST_APP_NAME));
        assertThat(jobToken.getJobId(), notNullValue());
        assertThat(jobToken.getSubmissionTime(), notNullValue());
        assertThat(jobToken.getResultUri(), notNullValue());
        assertThat(jobToken.getApplicationResultsUri(), notNullValue());

        final ArgumentCaptor<AbstractJob> jobCaptor = ArgumentCaptor.forClass(AbstractJob.class);
        verify(this.messageDispatcher).dispatch(jobCaptor.capture());
        jobCaptor.getValue().destroy();

        return jobToken;
    }

    public static InputStream getTestDataAsStream(final String payloadResourceFile)
    {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("data/" + payloadResourceFile);
    }
}

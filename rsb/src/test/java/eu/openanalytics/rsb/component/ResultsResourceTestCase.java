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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.NotFoundException;
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

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.data.SecureResultStore;
import eu.openanalytics.rsb.rest.types.Result;
import eu.openanalytics.rsb.rest.types.Results;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultsResourceTestCase
{
    private ResultsResource resultsResource;
    @Mock
    private SecureResultStore resultStore;
    @Mock
    private Configuration configuration;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void prepareTest()
    {
        resultsResource = new ResultsResource();
        resultsResource.setConfiguration(configuration);
        resultsResource.setResultStore(resultStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllResultsInvalidApplicationName() throws URISyntaxException
    {
        resultsResource.getAllResults("I'm bad :)", httpHeaders, uriInfo);
    }

    @Test
    public void getAllResults() throws URISyntaxException
    {
        final Results allResults = resultsResource.getAllResults(ResultResourceTestCase.TEST_APP_NAME,
            httpHeaders, uriInfo);
        assertThat(allResults, is(notNullValue()));
    }

    @Test(expected = NotFoundException.class)
    public void getSingleResultNotFound() throws URISyntaxException, IOException
    {
        resultsResource.getSingleResult(ResultResourceTestCase.TEST_APP_NAME,
            ResultResourceTestCase.TEST_JOB_ID.toString(), httpHeaders, uriInfo);
    }

    public void deleteSingleResultNotFound() throws URISyntaxException, IOException
    {
        final Response response = resultsResource.deleteSingleResult(ResultResourceTestCase.TEST_APP_NAME,
            ResultResourceTestCase.TEST_JOB_ID.toString());
        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void buildResult() throws URISyntaxException
    {
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final Result result = resultsResource.buildResult(ResultResourceTestCase.TEST_APP_NAME, httpHeaders,
            uriInfo, ResultResourceTestCase.buildPersistedResult("fake data"));
        assertThat(result, is(notNullValue()));
        assertThat(result.getApplicationName(), is(ResultResourceTestCase.TEST_APP_NAME));
        assertThat(result.getJobId(), is(ResultResourceTestCase.TEST_JOB_ID.toString()));
        assertThat(result.getType(), is("dat"));
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getDataUri(), is(notNullValue()));
        assertThat(result.getSelfUri(), is(notNullValue()));
    }
}

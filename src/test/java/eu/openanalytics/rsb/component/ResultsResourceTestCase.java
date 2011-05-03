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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.Result;
import eu.openanalytics.rsb.rest.types.Results;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultsResourceTestCase {
    private static final String TEST_JOB_ID = "job_id_123";
    private static final String TEST_APP_NAME = "app_name";
    private ResultsResource resultsResource;
    @Mock
    private Configuration configuration;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void prepareTest() {
        resultsResource = new ResultsResource();
        resultsResource.setConfiguration(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllResultsInvalidApplicationName() throws URISyntaxException {
        resultsResource.getAllResults("I'm bad :)", httpHeaders, uriInfo);
    }

    @Test
    public void getAllResults() throws URISyntaxException {
        final Results allResults = resultsResource.getAllResults(TEST_APP_NAME, httpHeaders, uriInfo);
        assertThat(allResults, is(notNullValue()));
    }

    @Test(expected = WebApplicationException.class)
    public void getSingleResultNotFound() throws URISyntaxException, IOException {
        resultsResource.getSingleResult(TEST_APP_NAME, TEST_JOB_ID, httpHeaders, uriInfo);
    }

    @Test(expected = WebApplicationException.class)
    public void deleteSingleResultNotFound() throws URISyntaxException, IOException {
        resultsResource.deleteSingleResult(TEST_APP_NAME, TEST_JOB_ID, httpHeaders, uriInfo);
    }

    @Test
    public void buildResult() throws URISyntaxException {
        final File resultFile = mock(File.class);
        when(resultFile.getName()).thenReturn(TEST_JOB_ID + ".job_type");
        when(resultFile.lastModified()).thenReturn(123L);
        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        final Result result = resultsResource.buildResult(TEST_APP_NAME, httpHeaders, uriInfo, resultFile);
        assertThat(result, is(notNullValue()));
        assertThat(result.getApplicationName(), is(TEST_APP_NAME));
        assertThat(result.getJobId(), is(TEST_JOB_ID));
        assertThat(result.getType(), is("job_type"));
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getDataUri(), is(notNullValue()));
        assertThat(result.getSelfUri(), is(notNullValue()));
    }
}

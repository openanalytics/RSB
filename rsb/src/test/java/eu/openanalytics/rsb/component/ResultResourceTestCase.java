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

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.data.PersistedResult;
import eu.openanalytics.rsb.data.SecureResultStore;
import eu.openanalytics.rsb.security.ApplicationPermissionEvaluator;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultResourceTestCase
{
    public static final String TEST_APP_NAME = "app_name";
    public static final UUID TEST_JOB_ID = UUID.randomUUID();
    public static final String TEST_RESULT_RESOURCE = TEST_JOB_ID.toString() + ".tst";

    @Mock
    private SecureResultStore resultStore;

    private ResultResource resultResource;
    private String testResultPayload;

    @Before
    public void prepareTest()
    {
        testResultPayload = RandomStringUtils.randomAlphanumeric(25 + new Random().nextInt(25));

        resultResource = new ResultResource();
        resultResource.setResultStore(resultStore);
    }

    @Test(expected = WebApplicationException.class)
    public void getResultNotFound() throws IOException
    {
        resultResource.getResult(TEST_APP_NAME, TEST_RESULT_RESOURCE);
    }

    @Test
    public void getResult() throws IOException
    {
        setupMockResultStore();

        final Response response = resultResource.getResult(TEST_APP_NAME, TEST_RESULT_RESOURCE);
        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        final StreamingOutput result = (StreamingOutput) response.getEntity();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.write(baos);

        assertThat(baos.toByteArray(), is(testResultPayload.getBytes()));
        assertThat(response.getMetadata().get(HttpHeaders.ETAG), notNullValue());
    }

    @Test(expected = WebApplicationException.class)
    public void getResultMetaNotFound() throws IOException
    {
        resultResource.getResultMeta(TEST_APP_NAME, TEST_RESULT_RESOURCE);
    }

    @Test
    public void getResultMeta() throws IOException
    {
        setupMockResultStore();

        final Response response = resultResource.getResultMeta(TEST_APP_NAME, TEST_RESULT_RESOURCE);
        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        assertThat(response.getMetadata().get(HttpHeaders.CONTENT_LENGTH), notNullValue());
        assertThat(response.getMetadata().get(HttpHeaders.ETAG), notNullValue());
    }

    private void setupMockResultStore()
    {
        final PersistedResult persistedResult = buildPersistedResult(testResultPayload);
        when(
            resultStore.findByApplicationNameAndJobId(TEST_APP_NAME,
                ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, TEST_JOB_ID)).thenReturn(
            persistedResult);
    }
	
	public static PersistedResult buildPersistedResult(final String resultPayload) {
		return new PersistedResult(TEST_APP_NAME,
				ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, TEST_JOB_ID,
				(GregorianCalendar)GregorianCalendar.getInstance(),
				true,
				Constants.DEFAULT_MIME_TYPE ) {
			
			@Override
			public long getDataLength() throws IOException {
				return 1;
			}
			
			@Override
			public InputStream getData() throws IOException {
				return new ByteArrayInputStream(resultPayload.getBytes());
			}
			
		};
	}
	
}

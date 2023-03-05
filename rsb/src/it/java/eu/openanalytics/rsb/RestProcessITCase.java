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

package eu.openanalytics.rsb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.meterware.httpunit.DeleteMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.apache.activemq.util.ByteArrayInputStream;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RestProcessITCase extends AbstractITCase {
    private String restProcessUri;

    @Before
    public void prepareTests() throws IOException {
        this.restProcessUri = RSB_BASE_URI + "/api/rest/process";
    }

    @Test
    public void processBadMethod() throws Exception {
        doTestUnsupportedDeleteMethod(this.restProcessUri);
    }

    @Test
    public void processBadApplicationName() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new PostMethodWebRequest(this.restProcessUri, new ByteArrayInputStream("ignored".getBytes()),
                "application/xml");
        request.setHeaderField("X-RSB-Application-Name", ":bad_app$name!");

        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(400, he.getResponseCode());
        }
    }

    @Test
    public void processBadContentType() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new PostMethodWebRequest(this.restProcessUri, new ByteArrayInputStream("ignored".getBytes()),
                "application/unsupported");
        request.setHeaderField("X-RSB-Application-Name", "myApp");

        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(415, he.getResponseCode());
        }
    }

    @Test
    public void submitValidXmlJob() throws Exception {
        final String applicationName = newTestApplicationName();
		final WebResponse response = sendPostRequest(applicationName, "application/xml",
				getTestDataStream("r-job-sample.xml"), 200 );
        final Document resultDoc = XMLUnit.buildTestDocument(response.getText());
        assertEquals("statDataPackage", resultDoc.getDocumentElement().getNodeName());
    }

    @Test
    public void submitInvalidXmlJob() throws Exception {
        final String applicationName = newTestApplicationName();
        final WebResponse response = sendPostRequest(applicationName, "application/xml", new ByteArrayInputStream("<bad/>".getBytes()), 400);
        final Document resultDoc = XMLUnit.buildTestDocument(response.getText());
        assertEquals("errorResult", resultDoc.getDocumentElement().getNodeName());
    }

    @Test
    public void submitValidJsonJob() throws Exception {
        final String applicationName = newTestApplicationName();
		final WebResponse response = sendPostRequest(applicationName, "application/json",
				getTestDataStream("r-job-sample.json"), 200 );
        final List<?> responseObject = Util.fromJson(response.getText(), List.class);
        assertEquals(10, responseObject.size());
    }

    @Test
    public void submitInvalidJsonJob() throws Exception {
        final String applicationName = newTestApplicationName();
        final WebResponse response = sendPostRequest(applicationName, "application/json", new ByteArrayInputStream("not*json".getBytes()),
                400);
        final Map<?, ?> responseObject = Util.fromJson(response.getText(), Map.class);
        assertEquals(applicationName, responseObject.get("applicationName"));
    }

    private WebResponse sendPostRequest(final String applicationName, final String contentType, final InputStream job,
            final int expectedResponseStatusCode) throws IOException, SAXException {
        final WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus(false);
        final PostMethodWebRequest request = new PostMethodWebRequest(this.restProcessUri, job, contentType);
        request.setHeaderField("X-RSB-Application-Name", applicationName);

        final WebResponse response = wc.sendRequest(request);
        assertEquals(expectedResponseStatusCode, response.getResponseCode());
        assertEquals(contentType, response.getHeaderField("Content-Type"));
        return response;
    }

    private static void doTestUnsupportedDeleteMethod(final String requestUri) throws IOException, SAXException {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new DeleteMethodWebRequest(requestUri);
        try {
            wc.sendRequest(request);
            fail("an exception should have been raised");
        } catch (final HttpException he) {
            assertEquals(405, he.getResponseCode());
        }
    }
}

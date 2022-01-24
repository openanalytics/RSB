/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertEquals;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.junit.Test;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RestMiscITCase extends AbstractITCase {
    @Test
    public void systemInfo() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/system/info");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("application/vnd.rsb+xml", response.getHeaderField("Content-Type"));

        final String xml = response.getText();
        assertXpathExists("/rsb:nodeInformation", xml);
        assertXpathExists("/rsb:nodeInformation/@name", xml);
        assertXpathExists("/rsb:nodeInformation/@healthy", xml);
        assertXpathExists("/rsb:nodeInformation/@uptime", xml);
        assertXpathExists("/rsb:nodeInformation/@uptimeText", xml);
        assertXpathExists("/rsb:nodeInformation/@servletContainerInfo", xml);
        assertXpathExists("/rsb:nodeInformation/@jvmMaxMemory", xml);
        assertXpathExists("/rsb:nodeInformation/@jvmFreeMemory", xml);
        assertXpathExists("/rsb:nodeInformation/@osLoadAverage", xml);
    }

    @Test
    public void systemHealthCheck() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/system/health/check");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("OK", response.getText());
    }

    @Test
    public void dataDirectories() throws Exception {
        final WebConversation wc = new WebConversation();
        final String dataUrl = RSB_BASE_URI + "/api/rest/data/";
        final WebRequest request = new GetMethodWebRequest(dataUrl);
        request.setHeaderField("Accept", Constants.RSB_XML_CONTENT_TYPE);
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("application/vnd.rsb+xml", response.getHeaderField("Content-Type"));

        final String xml = response.getText();
        assertXpathExists("/rsb:directory", xml);
        assertXpathExists("/rsb:directory/@path", xml);
        assertXpathExists("/rsb:directory/@name", xml);
        assertXpathExists("/rsb:directory/@uri", xml);
    }
}

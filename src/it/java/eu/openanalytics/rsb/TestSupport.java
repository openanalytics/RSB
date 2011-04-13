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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class TestSupport {
    public final static String RSB_BASE_URI;

    static {
        final String rsbUriSystemProperty = System.getProperty("rsb.uri");

        if (StringUtils.isNotBlank(rsbUriSystemProperty)) {
            RSB_BASE_URI = rsbUriSystemProperty;
        } else {
            // FIXME use value configured on Jetty Plugin
            RSB_BASE_URI = "http://localhost:8888";
        }
    }

    public final static String RSB_USERNAME = System.getProperty("rsb.username");
    public final static String RSB_PASSWORD = System.getProperty("rsb.password");
    public final static String RSB_REALM = System.getProperty("rsb.realm");

    public final static String RSB_REST_JOBS_URI = RSB_BASE_URI + "/api/rest/jobs";
    public final static String RSB_REST_RESULTS_URI = RSB_BASE_URI + "/api/rest/results";
    public final static String RSB_JOB_UPLOAD_FORM_URI = RSB_BASE_URI + "/ui/rsb.html";
    public final static String RSB_SOAP_JOBS_URI = RSB_BASE_URI + "/api/soap/mtom-jobs";

    static {
        System.out
                .printf("\n\nRunning RSB Integration Tests on:\n  Jobs URI: %s\n  Results URI: %s\n  Job upload form URI: %s\n  SOAP URI: %s\n  Username:%s\n  Password:%s\n  Realm: %s\n\n",
                        RSB_REST_JOBS_URI, RSB_REST_RESULTS_URI, RSB_JOB_UPLOAD_FORM_URI, RSB_SOAP_JOBS_URI, RSB_USERNAME,
                        StringUtils.repeat("*", StringUtils.length(RSB_PASSWORD)), RSB_REALM);
    }

    private TestSupport() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    public static void validateZipResult(final InputStream responseStream) throws IOException {
        final ZipInputStream result = new ZipInputStream(responseStream);
        ZipEntry ze = null;

        while ((ze = result.getNextEntry()) != null) {
            if (ze.getName().endsWith(".pdf"))
                return;
        }

        Assert.fail("No PDF file found in Zip result");
    }

    public static void validateErrorResult(final InputStream responseStream) throws IOException {
        final String response = IOUtils.toString(responseStream);
        Assert.assertTrue(response + " should contain 'error'", response.contains("error"));
    }
}

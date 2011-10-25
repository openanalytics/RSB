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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.PersistedConfiguration;
import eu.openanalytics.rsb.rest.types.Catalog;
import eu.openanalytics.rsb.rest.types.CatalogDirectory;
import eu.openanalytics.rsb.rest.types.CatalogFileType;
import eu.openanalytics.rsb.rest.types.RServiPools;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RestAdminITCase extends AbstractITCase {
    @Test
    public void getSystemConfiguration() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/system/configuration");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertThat(Util.fromJson(response.getText(), PersistedConfiguration.class), is(instanceOf(PersistedConfiguration.class)));
    }

    @Test
    public void putSystemConfiguration() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new PutMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/system/configuration", Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("rsb-configuration.json"), "application/json");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(204, response.getResponseCode());
    }

    @Test
    public void restart() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new PostMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/system/restart");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("RESTARTED", response.getText());
    }

    @Test
    public void getRServiPools() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/system/rservi_pools");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        final RServiPools rServiPools = JAXB.unmarshal(new StringReader(response.getText()), RServiPools.class);
        assertFalse(rServiPools.getRServiPools().isEmpty());
    }

    @Test
    public void getCatalog() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/catalog");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());

        final Catalog catalog = JAXB.unmarshal(new StringReader(response.getText()), Catalog.class);
        final List<CatalogDirectory> directories = catalog.getDirectories();

        // check all catalog dirs
        assertEquals(4, directories.size());
        for (final CatalogDirectory cd : directories) {
            assertEquals(cd.getType(), Configuration.Catalog.valueOf(cd.getType()).toString());
            assertTrue(StringUtils.isNotBlank(cd.getPath()));
            assertFalse(cd.getFiles().isEmpty());
        }

        // check one catalog file
        final CatalogFileType catalogFile = directories.get(0).getFiles().get(0);
        assertTrue(StringUtils.isNotBlank(catalogFile.getName()));
        assertTrue(StringUtils.isNotBlank(catalogFile.getDataUri()));
    }

    @Test
    public void getCatalogFile() throws Exception {
        final WebConversation wc = new WebConversation();
        final WebRequest request = new GetMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/catalog/R_SCRIPTS/test.R");
        final WebResponse response = wc.sendRequest(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("text/plain", response.getContentType());
        assertEquals(IOUtils.toString(getTestData("test.R")), response.getText());
    }

    @Test
    public void putCatalogFile() throws Exception {
        final String testFileName = "fake-" + RandomStringUtils.randomAlphanumeric(20) + ".R";

        WebConversation wc = new WebConversation();
        WebRequest request = new PutMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/catalog/R_SCRIPTS/" + testFileName,
                IOUtils.toInputStream("fake R script content for " + testFileName), "text/plain");
        WebResponse response = wc.sendRequest(request);
        assertEquals(201, response.getResponseCode());
        assertTrue(StringUtils.isNotBlank(response.getHeaderField("Location")));

        SuiteITCase.registerCreatedCatalogFile(Configuration.Catalog.R_SCRIPTS, testFileName);

        wc = new WebConversation();
        request = new PutMethodWebRequest(RSB_BASE_URI + "/api/rest/admin/catalog/R_SCRIPTS/" + testFileName,
                IOUtils.toInputStream("fake R script replacement content for " + testFileName), "text/plain");
        response = wc.sendRequest(request);
        assertEquals(204, response.getResponseCode());
        assertTrue(StringUtils.isBlank(response.getHeaderField("Location")));
    }
}

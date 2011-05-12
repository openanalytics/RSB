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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.ConfigurationFactory;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@RunWith(JUnit4.class)
public abstract class AbstractITCase extends XMLTestCase {
    protected final static String RSB_BASE_URI = "http://localhost:8888/rsb";

    private Configuration configuration;

    @Before
    public void setupCatalog() throws IOException {
        configuration = ConfigurationFactory.loadJsonConfiguration(Configuration.DEFAULT_JSON_CONFIGURATION_FILE);

        putTestScriptInCatalog(new File(configuration.getRScriptsCatalogDirectory(), "test.R"));
        putTestScriptInCatalog(new File(configuration.getRScriptsCatalogDirectory(), "testSweave.R"));
        putTestScriptInCatalog(new File(configuration.getSweaveFilesCatalogDirectory(), "testSweave.Rnw"));
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public static InputStream getTestData(final String payloadResourceFile) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("data/" + payloadResourceFile);
    }

    public static File getTestFile(final String payloadResourceFile) {
        try {
            return new File(Thread.currentThread().getContextClassLoader().getResource("data/" + payloadResourceFile).toURI());
        } catch (final URISyntaxException urise) {
            throw new RuntimeException(urise);
        }
    }

    public static void validateZipResult(final InputStream responseStream) throws IOException {
        final ZipInputStream result = new ZipInputStream(responseStream);
        ZipEntry ze = null;

        while ((ze = result.getNextEntry()) != null) {
            if (ze.getName().endsWith(".pdf")) {
                return;
            }
        }

        fail("No PDF file found in Zip result");
    }

    public static void validateErrorResult(final InputStream responseStream) throws IOException {
        final String response = IOUtils.toString(responseStream);
        assertTrue(response + " should contain 'error'", StringUtils.containsIgnoreCase(response, "error"));
    }

    private void putTestScriptInCatalog(final File testScript) throws FileNotFoundException, IOException {
        if (!testScript.isFile()) {
            final FileOutputStream fos = new FileOutputStream(testScript);
            IOUtils.copy(getTestData(testScript.getName()), fos);
            IOUtils.closeQuietly(fos);
        }
    }
}

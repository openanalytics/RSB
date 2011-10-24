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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.UnknownHostException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.Catalog;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminResourceTestCase {
    @Mock
    private ConfigurableApplicationContext applicationContext;
    @Mock
    private Configuration configuration;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;
    private AdminResource adminResource;

    @Before
    public void prepareTest() throws UnknownHostException {
        adminResource = new AdminResource();
        adminResource.setApplicationContext(applicationContext);
        adminResource.setConfiguration(configuration);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
    }

    @Test
    public void restart() throws Exception {
        final Response response = adminResource.restart();
        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity().toString(), is("RESTARTED"));
    }

    @Test
    public void getCatalog() throws Exception {
        when(configuration.getRScriptsCatalogDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(configuration.getSweaveFilesCatalogDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(configuration.getJobConfigurationCatalogDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(configuration.getEmailRepliesCatalogDirectory()).thenReturn(FileUtils.getTempDirectory());

        final Catalog catalog = adminResource.getCatalog(httpHeaders, uriInfo);
        assertThat(catalog, is(not(nullValue())));
        assertThat(catalog.getDirectories().size(), is(4));
    }

    @Test
    public void getCatalogFile() throws Exception {
        final File testFile = new File(Thread.currentThread().getContextClassLoader().getResource("data/test.R").toURI());
        when(configuration.getRScriptsCatalogDirectory()).thenReturn(testFile.getParentFile());

        final Response response = adminResource.getCatalogFile("R_SCRIPTS", "test.R", httpHeaders, uriInfo);
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void putCatalogFile() throws Exception {
        when(configuration.getRScriptsCatalogDirectory()).thenReturn(FileUtils.getTempDirectory());

        FileUtils.deleteQuietly(new File(FileUtils.getTempDirectory(), "fake.R"));

        final Response firstResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", IOUtils.toInputStream("fake script"),
                httpHeaders, uriInfo);
        assertThat(firstResponse.getStatus(), is(201));

        final Response secondResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", IOUtils.toInputStream("fake script"),
                httpHeaders, uriInfo);
        assertThat(secondResponse.getStatus(), is(204));
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Arrays;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.Directory;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class DataDirectoriesResourceTestCase
{
    @Mock
    private Configuration configuration;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;

    private DataDirectoriesResource dataDirectoriesResource;

    @Before
    public void prepareTest() throws UnknownHostException
    {
        dataDirectoriesResource = new DataDirectoriesResource();
        dataDirectoriesResource.setConfiguration(configuration);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
    }

    @Test
    public void setupChannelAdaptersNoDataDirectory() throws Exception
    {
        dataDirectoriesResource.setupRootMap();
    }

    @Test
    public void browseRoots() throws Exception
    {
        when(configuration.getDataDirectories()).thenReturn(Arrays.asList(FileUtils.getTempDirectory()));
        dataDirectoriesResource.setupRootMap();

        final Directory result = dataDirectoriesResource.browseRoots(httpHeaders, uriInfo);
        assertThat(result, is(notNullValue()));
        assertThat(result.getPath(), is(notNullValue()));
        assertThat(result.getUri(), is(notNullValue()));
    }

    @Test(expected = NotFoundException.class)
    public void browsePathBadRoot() throws Exception
    {
        dataDirectoriesResource.setupRootMap();
        dataDirectoriesResource.browsePath("not_found", null, httpHeaders, uriInfo);
    }

    @Test(expected = NotFoundException.class)
    public void browsePathBadExtension() throws Exception
    {
        when(configuration.getDataDirectories()).thenReturn(Arrays.asList(FileUtils.getTempDirectory()));
        dataDirectoriesResource.setupRootMap();
        dataDirectoriesResource.browsePath(dataDirectoriesResource.getRootMap().keySet().iterator().next(),
            "bad_extension", httpHeaders, uriInfo);
    }

    @Test(expected = AccessDeniedException.class)
    public void browsePathCraftyExtension() throws Exception
    {
        when(configuration.getDataDirectories()).thenReturn(Arrays.asList(FileUtils.getTempDirectory()));
        dataDirectoriesResource.setupRootMap();
        dataDirectoriesResource.browsePath(dataDirectoriesResource.getRootMap().keySet().iterator().next(),
            Base64.encodeBase64URLSafeString("../opt".getBytes()), httpHeaders, uriInfo);
    }

    @Test
    public void browsePath() throws Exception
    {
        when(configuration.getDataDirectories()).thenReturn(Arrays.asList(FileUtils.getTempDirectory()));
        dataDirectoriesResource.setupRootMap();
        final Directory result = dataDirectoriesResource.browsePath(dataDirectoriesResource.getRootMap()
            .keySet()
            .iterator()
            .next(), null, httpHeaders, uriInfo);
        assertThat(result, is(notNullValue()));
        assertThat(result.getPath(), is(notNullValue()));
        assertThat(result.getUri(), is(notNullValue()));
    }
}

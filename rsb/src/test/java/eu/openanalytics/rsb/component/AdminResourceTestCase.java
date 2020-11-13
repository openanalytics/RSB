/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2020
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.ConfigurationFactory;
import eu.openanalytics.rsb.config.PersistedConfiguration;
import eu.openanalytics.rsb.data.CatalogManager;
import eu.openanalytics.rsb.data.CatalogManager.PutCatalogFileResult;
import eu.openanalytics.rsb.rest.types.Catalog;
import eu.openanalytics.rsb.rest.types.RServiPools;
import eu.openanalytics.rsb.rservi.RServiPackageManager;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminResourceTestCase
{
    @Mock
    private ConfigurableApplicationContext applicationContext;
    @Mock
    private Configuration configuration;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private CatalogManager catalogManager;

    private AdminResource adminResource;

    @Before
    public void prepareTest() throws UnknownHostException
    {
        adminResource = new AdminResource();
        adminResource.setApplicationContext(applicationContext);
        adminResource.setConfiguration(configuration);
        adminResource.setCatalogManager(catalogManager);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
    }

    @Test
    public void getSystemConfiguration() throws Exception
    {
        adminResource.setConfiguration(ConfigurationFactory.loadJsonConfiguration());
        final Response response = adminResource.getSystemConfiguration();
        assertThat(response.getStatus(), is(200));
        assertThat(Util.fromJson(response.getEntity().toString(), PersistedConfiguration.class),
            is(instanceOf(PersistedConfiguration.class)));
    }

    @Test
    public void putSystemConfiguration() throws Exception
    {
        adminResource.setConfiguration(ConfigurationFactory.loadJsonConfiguration());
        final Response response = adminResource.putSystemConfiguration(Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("rsb-configuration.json"));
        assertThat(response.getStatus(), is(204));
    }

    @Test
    public void restart() throws Exception
    {
        final Response response = adminResource.restart();
        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity().toString(), is("RESTARTED"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void installRPackageBadChecksum() throws Exception
    {
        final byte[] fakePackageBytes = "fake_package".getBytes();
        adminResource.installRPackage("ignored", "bad", "bad.tar.gz", new ByteArrayInputStream(
            fakePackageBytes));
    }

    @Test
    public void installRPackageSuccess() throws Exception
    {
        adminResource.setrServiPackageManager(mock(RServiPackageManager.class));

        final byte[] fakePackageBytes = IOUtils.toByteArray(JobsResourceTestCase.getTestDataAsStream("fake-package.tar.gz"));

        adminResource.installRPackage("ignored", DigestUtils.sha1Hex(fakePackageBytes),
            "fake_package.tar.gz", new ByteArrayInputStream(fakePackageBytes));
    }

    @Test
    public void getRServiPools() throws Exception
    {
        when(configuration.getDefaultRserviPoolUri()).thenReturn(new URI("fake://pool_uri"));

        final RServiPools rServiPools = adminResource.getRServiPools();
        assertThat(rServiPools.getContents().size(), is(1));
    }

    @Test
    public void getApplicationUnawareCatalog() throws Exception
    {
        final File fakeCatalogDir = new File(FileUtils.getTempDirectory(), "rsb_test_catalog");
        FileUtils.forceMkdir(fakeCatalogDir);
        final Map<Pair<CatalogSection, File>, List<File>> fakeCatalog = Collections.singletonMap(
            Pair.of(CatalogSection.R_SCRIPTS, fakeCatalogDir), Collections.<File> emptyList());
        when(catalogManager.getCatalog(null)).thenReturn(fakeCatalog);

        final Catalog catalog = adminResource.getCatalogIndex(null, httpHeaders, uriInfo);
        assertThat(catalog, is(not(nullValue())));
        assertThat(catalog.getDirectories().size(), is(1));
    }

    @Test
    public void getApplicationAwareCatalog() throws Exception
    {
        when(configuration.isApplicationAwareCatalog()).thenReturn(true);

        final File fakeCatalogDir = new File(FileUtils.getTempDirectory(), "rsb_test_catalog");
        FileUtils.forceMkdir(fakeCatalogDir);
        final Map<Pair<CatalogSection, File>, List<File>> fakeCatalog = Collections.singletonMap(
            Pair.of(CatalogSection.R_SCRIPTS, fakeCatalogDir), Collections.<File> emptyList());
        when(catalogManager.getCatalog("TEST_APP")).thenReturn(fakeCatalog);

        final Catalog catalog = adminResource.getCatalogIndex("TEST_APP", httpHeaders, uriInfo);
        assertThat(catalog, is(not(nullValue())));
        assertThat(catalog.getDirectories().size(), is(1));
    }

    @Test
    public void getApplicationUnawareCatalogFile() throws Exception
    {
        final File testFile = new File(Thread.currentThread()
            .getContextClassLoader()
            .getResource("data/test.R")
            .toURI());

        when(catalogManager.getCatalogFile(CatalogSection.R_SCRIPTS, null, "test.R")).thenReturn(testFile);

        final Response response = adminResource.getCatalogFile("R_SCRIPTS", "test.R", null);
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void getApplicationAwareCatalogFile() throws Exception
    {
        when(configuration.isApplicationAwareCatalog()).thenReturn(true);

        final File testFile = new File(Thread.currentThread()
            .getContextClassLoader()
            .getResource("data/test.R")
            .toURI());

        when(catalogManager.getCatalogFile(CatalogSection.R_SCRIPTS, "TEST_APP", "test.R")).thenReturn(
            testFile);

        final Response response = adminResource.getCatalogFile("R_SCRIPTS", "test.R", "TEST_APP");
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void putApplicationUnawareCatalogFile() throws Exception
    {
        final File testFile = new File(FileUtils.getTempDirectory(), "fake.R");

        when(
            catalogManager.putCatalogFile(eq(CatalogSection.R_SCRIPTS), isNull(String.class), eq("fake.R"),
                any(InputStream.class))).thenReturn(Pair.of(PutCatalogFileResult.CREATED, testFile));

        final Response firstResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", null,
            IOUtils.toInputStream("fake script", Charset.defaultCharset()), httpHeaders, uriInfo);
        assertThat(firstResponse.getStatus(), is(201));

        when(
            catalogManager.putCatalogFile(eq(CatalogSection.R_SCRIPTS), isNull(String.class), eq("fake.R"),
                any(InputStream.class))).thenReturn(Pair.of(PutCatalogFileResult.UPDATED, testFile));

        final Response secondResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", null,
            IOUtils.toInputStream("fake script", Charset.defaultCharset()), httpHeaders, uriInfo);
        assertThat(secondResponse.getStatus(), is(204));
    }

    @Test
    public void putApplicationAwareCatalogFile() throws Exception
    {
        when(configuration.isApplicationAwareCatalog()).thenReturn(true);

        when(httpHeaders.getHeaderString("X-RSB-Application-Name")).thenReturn("TEST_APP");

        final File testFile = new File(FileUtils.getTempDirectory(), "fake.R");

        when(
            catalogManager.putCatalogFile(eq(CatalogSection.R_SCRIPTS), eq("TEST_APP"), eq("fake.R"),
                any(InputStream.class))).thenReturn(Pair.of(PutCatalogFileResult.CREATED, testFile));

        final Response firstResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", "TEST_APP",
            IOUtils.toInputStream("fake script", Charset.defaultCharset()), httpHeaders, uriInfo);
        assertThat(firstResponse.getStatus(), is(201));

        when(
            catalogManager.putCatalogFile(eq(CatalogSection.R_SCRIPTS), eq("TEST_APP"), eq("fake.R"),
                any(InputStream.class))).thenReturn(Pair.of(PutCatalogFileResult.UPDATED, testFile));

        final Response secondResponse = adminResource.putCatalogFile("R_SCRIPTS", "fake.R", "TEST_APP",
            IOUtils.toInputStream("fake script", Charset.defaultCharset()), httpHeaders, uriInfo);
        assertThat(secondResponse.getStatus(), is(204));
    }
}

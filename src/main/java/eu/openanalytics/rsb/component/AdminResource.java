/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.ConfigurationFactory;
import eu.openanalytics.rsb.config.PersistedConfiguration;
import eu.openanalytics.rsb.data.CatalogManager.PutCatalogFileResult;
import eu.openanalytics.rsb.rest.types.Catalog;
import eu.openanalytics.rsb.rest.types.CatalogDirectory;
import eu.openanalytics.rsb.rest.types.CatalogFileType;
import eu.openanalytics.rsb.rest.types.RServiPoolType;
import eu.openanalytics.rsb.rest.types.RServiPools;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("adminResource")
@Path("/" + Constants.ADMIN_PATH)
public class AdminResource extends AbstractResource implements ApplicationContextAware
{
    private static final String CATALOG_SUBPATH = "catalog";
    private static final String SYSTEM_SUBPATH = "system";

    public static final String ADMIN_SYSTEM_PATH = Constants.ADMIN_PATH + "/" + SYSTEM_SUBPATH;
    public static final String ADMIN_CATALOG_PATH = Constants.ADMIN_PATH + "/" + CATALOG_SUBPATH;

    private static final Pattern TAR_CATALOG_FILE_PATTERN = Pattern.compile(".*/inst/rsb/catalog/(.*)");

    private ConfigurableApplicationContext applicationContext;

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Path("/" + SYSTEM_SUBPATH + "/configuration")
    @GET
    @Produces(Constants.JSON_CONTENT_TYPE)
    public Response getSystemConfiguration()
    {
        return Response.ok(Util.toJson(new PersistedConfiguration(getConfiguration()))).build();
    }

    @Path("/" + SYSTEM_SUBPATH + "/configuration")
    @PUT
    @Consumes(Constants.JSON_CONTENT_TYPE)
    public Response putSystemConfiguration(final InputStream in) throws IOException, URISyntaxException
    {
        Validate.notNull(getConfiguration().getConfigurationUrl(),
            "Transient configuration can't be PUT over the API");
        final Configuration newConfiguration = ConfigurationFactory.loadJsonConfiguration(in);
        final String reformattedNewConfiguration = Util.toJson(new PersistedConfiguration(newConfiguration));
        final File newConfigurationFile = new File(getConfiguration().getConfigurationUrl().toURI());
        final FileWriter fw = new FileWriter(newConfigurationFile);
        IOUtils.copy(new StringReader(reformattedNewConfiguration), fw);
        IOUtils.closeQuietly(fw);
        getLogger().warn(
            "Configuration stored in: " + newConfigurationFile
                            + ". System needs restart in order to activate this new configuration!");
        return Response.noContent().build();
    }

    @Path("/" + SYSTEM_SUBPATH + "/restart")
    @POST
    public Response restart()
    {
        applicationContext.close();
        applicationContext.refresh();
        return Response.ok("RESTARTED").build();
    }

    @Path("/" + SYSTEM_SUBPATH + "/rservi_pools")
    @GET
    @Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
    public RServiPools getRServiPools()
    {
        final Map<URI, Set<String>> pools = new HashMap<URI, Set<String>>();

        addToPools(getConfiguration().getDefaultRserviPoolUri(), null, pools);

        final Map<String, Set<URI>> applicationSpecificRserviPoolUris = getConfiguration().getApplicationSpecificRserviPoolUris();
        if (applicationSpecificRserviPoolUris != null)
        {
            for (final Entry<String, Set<URI>> pool : applicationSpecificRserviPoolUris.entrySet())
            {
                for (final URI uri : pool.getValue())
                {
                    addToPools(uri, pool.getKey(), pools);
                }
            }
        }

        final RServiPools result = Util.REST_OBJECT_FACTORY.createRServiPools();
        for (final Entry<URI, Set<String>> pool : pools.entrySet())
        {
            final RServiPoolType rServiPool = Util.REST_OBJECT_FACTORY.createRServiPoolType();
            rServiPool.setPoolUri(pool.getKey().toString());
            rServiPool.setApplicationNames(StringUtils.join(pool.getValue(), ','));
            rServiPool.setDefault(pool.getKey().equals(getConfiguration().getDefaultRserviPoolUri()));
            result.getContents().add(rServiPool);
        }
        return result;
    }

    @Path("/" + SYSTEM_SUBPATH + "/r_packages")
    @POST
    @Consumes({Constants.GZIP_CONTENT_TYPE})
    public void installRPackage(final InputStream input, @QueryParam("sha1hexsum") final String sha1HexSum
    // , @QueryParam("rServiPoolUri") final String rServiPoolUri
    ) throws IOException
    {
        // store the package and tar files in temporary files
        final File packageSourceFile = File.createTempFile("rsb-install.", ".tar.gz");
        File packageTarFile = null;

        try
        {
            final FileOutputStream output = new FileOutputStream(packageSourceFile);
            IOUtils.copyLarge(input, output);
            IOUtils.closeQuietly(output);

            // validate the checksum
            final FileInputStream packageSourceInputStream = new FileInputStream(packageSourceFile);
            final String calculatedSha1HexSum = DigestUtils.shaHex(packageSourceInputStream);
            IOUtils.closeQuietly(packageSourceInputStream);
            Validate.isTrue(calculatedSha1HexSum.equals(sha1HexSum), "Invalid SHA-1 HEX checksum");

            // TODO upload to RServi

            // extract catalog files from $PKG_ROOT/inst/rsb/catalog
            packageTarFile = extractCatalogFiles(packageSourceFile);
        }
        finally
        {
            FileUtils.deleteQuietly(packageSourceFile);
            FileUtils.deleteQuietly(packageTarFile);
        }
    }

    private File extractCatalogFiles(final File packageSourceFile) throws IOException, FileNotFoundException
    {
        File packageTarFile;
        FileOutputStream output;
        // 1) extract TAR
        packageTarFile = File.createTempFile("rsb-install.", ".tar");
        final GzipCompressorInputStream gzIn = new GzipCompressorInputStream(new FileInputStream(
            packageSourceFile));
        output = new FileOutputStream(packageTarFile);
        IOUtils.copyLarge(gzIn, output);
        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(gzIn);

        // 2) parse TAR and drop files in catalog
        final TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(packageTarFile));
        TarArchiveEntry tarEntry = null;
        while ((tarEntry = tarIn.getNextTarEntry()) != null)
        {
            if (!tarEntry.isFile())
            {
                continue;
            }

            final Matcher matcher = TAR_CATALOG_FILE_PATTERN.matcher(tarEntry.getName());
            if (matcher.matches())
            {
                final byte[] data = IOUtils.toByteArray(tarIn, tarEntry.getSize());

                final String catalogFile = matcher.group(1);
                final File targetCatalogFile = new File(getConfiguration().getCatalogRootDirectory(),
                    catalogFile);
                output = new FileOutputStream(targetCatalogFile);
                IOUtils.write(data, output);
                IOUtils.closeQuietly(output);

                getLogger().info("Wrote " + data.length + " bytes in catalog file: " + targetCatalogFile);
            }
        }
        IOUtils.closeQuietly(tarIn);
        return packageTarFile;
    }

    @Path("/" + CATALOG_SUBPATH)
    @GET
    @Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
    public Catalog getCatalogIndex(@HeaderParam(Constants.APPLICATION_NAME_HTTP_HEADER) final String applicationName,
                                   @Context final HttpHeaders httpHeaders,
                                   @Context final UriInfo uriInfo) throws IOException, URISyntaxException
    {
        final Catalog result = Util.REST_OBJECT_FACTORY.createCatalog();

        for (final Entry<Pair<CatalogSection, File>, List<File>> catalogSectionFiles : getCatalogManager().getCatalog(
            applicationName)
            .entrySet())
        {
            final CatalogDirectory catalogDirectory = createCatalogDirectory(catalogSectionFiles.getKey()
                .getLeft(), catalogSectionFiles.getKey().getRight(), catalogSectionFiles.getValue(),
                httpHeaders, uriInfo);
            result.getDirectories().add(catalogDirectory);
        }

        return result;
    }

    @Path("/" + CATALOG_SUBPATH + "/{catalogName}/{fileName}")
    @GET
    public Response getCatalogFile(@PathParam("catalogName") final String catalogName,
                                   @PathParam("fileName") final String fileName,
                                   @HeaderParam(Constants.APPLICATION_NAME_HTTP_HEADER) final String applicationName)
        throws IOException, URISyntaxException
    {
        final File catalogFile = getCatalogManager().getCatalogFile(CatalogSection.valueOf(catalogName),
            applicationName, fileName);

        if (!catalogFile.isFile())
        {
            return Response.status(Status.NOT_FOUND).build();
        }

        final ResponseBuilder rb = Response.ok();
        rb.type(Util.getContentType(catalogFile));
        rb.entity(new StreamingOutput()
        {
            public void write(final OutputStream output) throws IOException
            {
                final FileInputStream fis = new FileInputStream(catalogFile);
                IOUtils.copy(fis, output);
                IOUtils.closeQuietly(fis);
                IOUtils.closeQuietly(output);
            }
        });
        return rb.build();
    }

    @Path("/" + CATALOG_SUBPATH + "/{catalogName}/{fileName}")
    @PUT
    @Consumes(Constants.TEXT_CONTENT_TYPE)
    public Response putCatalogFile(@PathParam("catalogName") final String catalogName,
                                   @PathParam("fileName") final String fileName,
                                   @HeaderParam(Constants.APPLICATION_NAME_HTTP_HEADER) final String applicationName,
                                   final InputStream in,
                                   @Context final HttpHeaders httpHeaders,
                                   @Context final UriInfo uriInfo) throws IOException, URISyntaxException
    {
        final CatalogSection catalogSection = CatalogSection.valueOf(catalogName);
        final Pair<PutCatalogFileResult, File> result = getCatalogManager().putCatalogFile(catalogSection,
            applicationName, fileName, in);

        if (result.getLeft() == PutCatalogFileResult.UPDATED)
        {
            return Response.noContent().build();
        }

        final URI location = buildCatalogFileUri(catalogSection, result.getRight(), httpHeaders, uriInfo);
        return Response.created(location).build();
    }

    private void addToPools(final URI rServiPoolUri,
                            final String applicationName,
                            final Map<URI, Set<String>> pools)
    {
        Set<String> applicationNames = pools.get(rServiPoolUri);
        if (applicationNames == null)
        {
            applicationNames = new HashSet<String>();
        }
        if (StringUtils.isNotBlank(applicationName))
        {
            applicationNames.add(applicationName);
        }
        pools.put(rServiPoolUri, applicationNames);
    }

    private CatalogDirectory createCatalogDirectory(final CatalogSection catalogSection,
                                                    final File catalogSectionDirectory,
                                                    final List<File> catalogFiles,
                                                    final HttpHeaders httpHeaders,
                                                    final UriInfo uriInfo)
        throws URISyntaxException, IOException
    {

        final String catalogTypeAsString = catalogSection.toString();

        final CatalogDirectory catalogDirectory = Util.REST_OBJECT_FACTORY.createCatalogDirectory();
        catalogDirectory.setType(catalogTypeAsString);
        catalogDirectory.setPath(catalogSectionDirectory.getCanonicalPath());

        for (final File file : catalogFiles)
        {
            final URI dataUri = buildCatalogFileUri(catalogSection, file, httpHeaders, uriInfo);
            final CatalogFileType catalogFile = Util.REST_OBJECT_FACTORY.createCatalogFileType();
            catalogFile.setName(file.getName());
            catalogFile.setDataUri(dataUri.toString());
            catalogDirectory.getFiles().add(catalogFile);
        }

        return catalogDirectory;
    }

    private URI buildCatalogFileUri(final CatalogSection catalogSection,
                                    final File file,
                                    final HttpHeaders httpHeaders,
                                    final UriInfo uriInfo) throws URISyntaxException
    {

        return Util.getUriBuilder(uriInfo, httpHeaders)
            .path(Constants.ADMIN_PATH)
            .path(CATALOG_SUBPATH)
            .path(catalogSection.toString())
            .path(file.getName())
            .build();
    }
}

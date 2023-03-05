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

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;
import static org.eclipse.statet.jcommons.io.FileUtils.requireParent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
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

import org.eclipse.statet.jcommons.io.FileUtils;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
import eu.openanalytics.rsb.rest.types.Catalog;
import eu.openanalytics.rsb.rest.types.CatalogDirectory;
import eu.openanalytics.rsb.rest.types.CatalogFileType;
import eu.openanalytics.rsb.rest.types.RServiPoolType;
import eu.openanalytics.rsb.rest.types.RServiPools;
import eu.openanalytics.rsb.rservi.RServiPackageManager;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("adminResource")
@Path("/" + Constants.ADMIN_PATH)
@NonNullByDefault
public class AdminResource extends AbstractResource implements ApplicationContextAware {
	
	private static final String SYSTEM_SUBPATH= "system";
	private static final String CATALOG_SUBPATH= "catalog";
	
	public static final String ADMIN_SYSTEM_PATH= Constants.ADMIN_PATH + "/" + SYSTEM_SUBPATH;
	public static final String ADMIN_CATALOG_PATH= Constants.ADMIN_PATH + "/" + CATALOG_SUBPATH;
	
	private static final Pattern TAR_CATALOG_FILE_PATTERN= Pattern.compile(".*/inst/rsb/catalog/(.*)");
	
	
    private ConfigurableApplicationContext applicationContext;

    @Resource
    private RServiPackageManager rServiPackageManager;

    // exposed for unit testing
    public void setrServiPackageManager(final RServiPackageManager rServiPackageManager)
    {
        this.rServiPackageManager = rServiPackageManager;
    }

    @Override
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
        try(FileWriter fw = new FileWriter(newConfigurationFile)) {
          IOUtils.copy(new StringReader(reformattedNewConfiguration), fw);
        }
        getLogger().warn(
            "Configuration stored in: " + newConfigurationFile
                            + ". System needs restart in order to activate this new configuration!");
        return Response.noContent().build();
    }

    @Path("/" + SYSTEM_SUBPATH + "/restart")
    @POST
    public Response restart()
    {
        this.applicationContext.close();
        this.applicationContext.refresh();
        return Response.ok("RESTARTED").build();
    }

    @Path("/" + SYSTEM_SUBPATH + "/rservi_pools")
    @GET
    @Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
    public RServiPools getRServiPools()
    {
        final Map<URI, Set<String>> pools = new HashMap<>();

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
    
    private void addToPools(final URI rServiPoolUri,
                            final String applicationName,
                            final Map<URI, Set<String>> pools)
    {
        Set<String> applicationNames= pools.get(rServiPoolUri);
        if (applicationNames == null)
        {
            applicationNames= new HashSet<>();
        }
        if (StringUtils.isNotBlank(applicationName))
        {
            applicationNames.add(applicationName);
        }
        pools.put(rServiPoolUri, applicationNames);
    }
	
	
	@Path("/" + SYSTEM_SUBPATH + "/r_packages")
	@POST
	@Consumes({Constants.GZIP_CONTENT_TYPE})
	public void installRPackage(
			@QueryParam("rServiPoolUri") final String rServiPoolUri,
			@QueryParam("sha1hexsum") final String sha1HexSum,
			@QueryParam("packageName") final String packageName,
			final InputStream input)
			throws Exception {
		Validate.notBlank(rServiPoolUri, "missing query param: rServiPoolUri");
		Validate.notBlank(sha1HexSum, "missing query param: sha1hexsum");
		Validate.notBlank(packageName, "missing query param: packageName");
		
		// store the package and tar files in temporary files
		final var tempDirectory= Files.createTempDirectory("rsb-rpkg-");
		
		final var packageSourceFile= tempDirectory.resolve(packageName);
		
		try {
			Files.copy(input, packageSourceFile);
			
			// validate the checksum
			try (final var fileIn= Files.newInputStream(packageSourceFile)) {
				final String calculatedSha1HexSum= DigestUtils.sha1Hex(fileIn);
				Validate.isTrue(calculatedSha1HexSum.equals(sha1HexSum), "Invalid SHA-1 HEX checksum");
			}
			
			// upload to RServi
			this.rServiPackageManager.install(packageSourceFile, rServiPoolUri);
			
			// extract catalog files from $PKG_ROOT/inst/rsb/catalog
			extractCatalogFiles(packageSourceFile);
			
			getLogger().info("Package with checksum " + sha1HexSum + " installed to " + rServiPoolUri);
		}
		finally {
			try {
				FileUtils.deleteRecursively(tempDirectory);
			}
			catch (final Exception e) {
				getLogger().warn("Failed to delete temporary directory: " + tempDirectory, e);
			}
		}
	}
	
	private void extractCatalogFiles(final java.nio.file.Path packageSourceFile) throws IOException {
		final var tempDirectory= requireParent(packageSourceFile);
		
		// 1) extract TAR
		final var packageTarFile= Files.createTempFile(tempDirectory, "rsb-install.", ".tar");
		try (	final var fileIn= Files.newInputStream(packageSourceFile);
				final GzipCompressorInputStream gzIn= new GzipCompressorInputStream(fileIn);
				final var out= Files.newOutputStream(packageTarFile) ) {
			gzIn.transferTo(out);
		}
		
		// 2) parse TAR and drop files in catalog
		try (	final var fileIn= Files.newInputStream(packageTarFile);
				final TarArchiveInputStream tarIn= new TarArchiveInputStream(fileIn) ) {
			final Matcher matcher= TAR_CATALOG_FILE_PATTERN.matcher("");
			TarArchiveEntry tarEntry= null;
			while ((tarEntry= tarIn.getNextTarEntry()) != null) {
				if (!tarEntry.isFile()) {
					continue;
				}
				
				if (matcher.reset(tarEntry.getName()).matches()) {
					final byte[] data= IOUtils.toByteArray(tarIn, tarEntry.getSize());
					
					final String catalogFileName= matcher.group(1);
					final var targetCatalogFile= getConfiguration().getCatalogRootDirectory().toPath()
							.resolve(catalogFileName);
					Files.write(targetCatalogFile, data);
					
					getLogger().info("Wrote " + data.length + " bytes in catalog file: " + targetCatalogFile);
				}
			}
		}
	}
	
	
	@Path("/" + CATALOG_SUBPATH)
	@GET
	@Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
	public Catalog getCatalogIndex(
			@HeaderParam(Constants.APPLICATION_NAME_FIELD_NAME) final @Nullable String applicationName,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IOException, URISyntaxException {
		final Catalog result= Util.REST_OBJECT_FACTORY.createCatalog();
		
		for (final var catalogSectionFiles : getCatalogManager().getCatalog(applicationName)
				.entrySet() ) {
			final CatalogDirectory catalogDirectory= createCatalogDirectory(
					catalogSectionFiles.getKey(),
					catalogSectionFiles.getValue().getLeft(),
					catalogSectionFiles.getValue().getRight(),
					httpHeaders, uriInfo );
			result.getDirectories().add(catalogDirectory);
		}
		
		return result;
	}
	
	@Path("/" + CATALOG_SUBPATH + "/{catalogName}/{fileName}")
	@GET
	public Response getCatalogFile(
			@PathParam("catalogName") final String catalogName,
			@PathParam("fileName") final String fileName,
			@HeaderParam(Constants.APPLICATION_NAME_FIELD_NAME) final @Nullable String applicationName)
			throws IOException, URISyntaxException {
		final CatalogSection catalogSection= CatalogSection.valueOf(catalogName);
		final var catalogFile= getCatalogManager().getCatalogFile(catalogSection,
				applicationName, fileName );
		
		if (!Files.isRegularFile(catalogFile)) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		final ResponseBuilder rb= Response.ok();
		rb.type(Util.getContentType(catalogFile));
		rb.entity(new StreamingOutput() {
			@Override
			public void write(final OutputStream out) throws IOException {
				Files.copy(catalogFile, out);
			}
		});
		return rb.build();
	}
	
	@Path("/" + CATALOG_SUBPATH + "/{catalogName}/{fileName}")
	@PUT
	@Consumes(Constants.TEXT_CONTENT_TYPE)
	public Response putCatalogFile(
			@PathParam("catalogName") final String catalogName,
			@PathParam("fileName") final String fileName,
			@HeaderParam(Constants.APPLICATION_NAME_FIELD_NAME) final @Nullable String applicationName,
			final InputStream in,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IOException, URISyntaxException {
		final CatalogSection catalogSection= CatalogSection.valueOf(catalogName);
		final var result= getCatalogManager().putCatalogFile(catalogSection,
				applicationName, fileName, in );
		
		switch (result.getChangeType()) {
		case UPDATED:
			return Response.noContent().build();
//		case CREATED:
		default:
			final URI location= buildCatalogFileUri(catalogSection, result.getPath(),
					httpHeaders, uriInfo );
			return Response.created(location).build();
		}
	}
	
	private CatalogDirectory createCatalogDirectory(final CatalogSection catalogSection,
			final java.nio.file.Path catalogSectionDirectory,
			final List<java.nio.file.Path> catalogFiles,
			final HttpHeaders httpHeaders, final UriInfo uriInfo)
			throws URISyntaxException, IOException {
		final String catalogTypeAsString= catalogSection.toString();
		
		final CatalogDirectory catalogDirectory= Util.REST_OBJECT_FACTORY.createCatalogDirectory();
		catalogDirectory.setType(catalogTypeAsString);
		catalogDirectory.setPath(catalogSectionDirectory.toAbsolutePath().normalize().toString());
		
		for (final var file : catalogFiles) {
			final URI dataUri= buildCatalogFileUri(catalogSection, file, httpHeaders, uriInfo);
			final CatalogFileType catalogFile= Util.REST_OBJECT_FACTORY.createCatalogFileType();
			catalogFile.setName(requireFileName(file).toString());
			catalogFile.setDataUri(dataUri.toString());
			catalogDirectory.getFiles().add(catalogFile);
		}
		
		return catalogDirectory;
	}
	
	private URI buildCatalogFileUri(final CatalogSection catalogSection,
			final java.nio.file.Path file,
			final HttpHeaders httpHeaders, final UriInfo uriInfo)
			throws URISyntaxException {
		return Util.getUriBuilder(uriInfo, httpHeaders)
				.path(Constants.ADMIN_PATH)
				.path(CATALOG_SUBPATH)
				.path(catalogSection.toString())
				.path(requireFileName(file).toString())
				.build();
	}
	
}

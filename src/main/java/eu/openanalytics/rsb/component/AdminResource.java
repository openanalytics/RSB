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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.cxf.ReloadableCXFServlet;
import eu.openanalytics.rsb.rest.types.Catalog;
import eu.openanalytics.rsb.rest.types.CatalogDirectory;
import eu.openanalytics.rsb.rest.types.CatalogFileType;

//TODO unit test
/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("adminResource")
@Path("/" + Constants.ADMIN_PATH)
public class AdminResource extends AbstractComponent implements ApplicationContextAware {

    private ConfigurableWebApplicationContext applicationContext;

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableWebApplicationContext) applicationContext;
    }

    @Path("/restart")
    @POST
    @Produces({ Constants.TEXT_CONTENT_TYPE })
    public Response restart() {
        applicationContext.close();
        applicationContext.refresh();
        ReloadableCXFServlet.reloadAll();
        return Response.ok("RESTARTED").build();
    }

    // TODO integration test
    @Path("/catalog")
    @GET
    @Produces({ Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE })
    public Response getCatalog(@Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo) throws IOException,
            URISyntaxException {

        final Catalog catalog = Util.REST_OBJECT_FACTORY.createCatalog();

        // FIXME support all 4 types, generalize
        final File catalogDirectoryFile = getConfiguration().getEmailRepliesCatalogDirectory();
        final String catalogType = "EMAIL_REPLIES";

        final CatalogDirectory catalogDirectory = Util.REST_OBJECT_FACTORY.createCatalogDirectory();
        catalogDirectory.setType(catalogType);
        catalogDirectory.setPath(catalogDirectoryFile.getCanonicalPath());

        for (final File file : catalogDirectoryFile.listFiles(new FileFilter() {
            public boolean accept(final File f) {
                return f.isFile();
            }
        })) {
            final URI dataUri = Util.getUriBuilder(uriInfo, httpHeaders).path("catalog").path(catalogType).path(file.getName()).build();
            final CatalogFileType catalogFile = Util.REST_OBJECT_FACTORY.createCatalogFileType();
            catalogFile.setName(file.getName());
            catalogFile.setDataUri(dataUri.toString());
            catalogDirectory.getFiles().add(catalogFile);
        }

        catalog.getDirectories().add(catalogDirectory);

        return Response.ok(catalog).build();
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.util.Base64Utility;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.data.PersistedResult;
import eu.openanalytics.rsb.data.SecureResultStore;

/**
 * Serves R job process result files.<br/>
 * <i>NB. Could very well be replaced with a static file serving context on a frontal
 * web server.</i>
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("resultResource")
@Path("/" + Constants.RESULT_PATH + "/{applicationName}/{resourceName}")
public class ResultResource extends AbstractResource
{
    @Resource
    private SecureResultStore resultStore;

    // exposed for testing
    void setResultStore(final SecureResultStore resultStore)
    {
        this.resultStore = resultStore;
    }

    /**
     * Serves a single result file.
     * 
     * @param applicationName
     * @param resourceName
     * @return
     * @throws IOException
     */
    @GET
    public Response getResult(@PathParam("applicationName") final String applicationName,
                              @PathParam("resourceName") final String resourceName) throws IOException
    {

        final PersistedResult persistedResult = getPersistedResultOrDie(applicationName, resourceName);

        final ResponseBuilder rb = Response.ok();
        addContentTypeHeader(persistedResult, rb);
        addEtagHeader(persistedResult, rb);
        rb.entity(new StreamingOutput()
        {
            public void write(final OutputStream output) throws IOException
            {
                final InputStream data = persistedResult.getData();
                IOUtils.copy(data, output);
                IOUtils.closeQuietly(data);
                IOUtils.closeQuietly(output);
            }
        });
        return rb.build();
    }

    /**
     * Provides HTTP meta-information only for a single result file.
     * 
     * @param applicationName
     * @param resourceName
     * @return
     * @throws IOException
     */
    @HEAD
    public Response getResultMeta(@PathParam("applicationName") final String applicationName,
                                  @PathParam("resourceName") final String resourceName) throws IOException
    {

        final PersistedResult persistedResult = getPersistedResultOrDie(applicationName, resourceName);

        final ResponseBuilder rb = Response.noContent();
        addContentLengthHeader(persistedResult, rb);
        addContentTypeHeader(persistedResult, rb);
        addEtagHeader(persistedResult, rb);
        return rb.build();
    }

    private void addContentTypeHeader(final PersistedResult persistedResult, final ResponseBuilder rb)
    {
        String contentType = "application/octet-stream";
        if (persistedResult.getMimeType() != null)
        {
            contentType = persistedResult.getMimeType().toString();
        }
        rb.header(HttpHeaders.CONTENT_TYPE, contentType);
    }
    
    private void addEtagHeader(final PersistedResult persistedResult, final ResponseBuilder rb)
    {
        rb.header(HttpHeaders.ETAG, getEtag(persistedResult));
    }

    private void addContentLengthHeader(final PersistedResult persistedResult, final ResponseBuilder rb)
        throws IOException
    {
        rb.header(HttpHeaders.CONTENT_LENGTH, Long.toString(persistedResult.getDataLength()));
    }

    private PersistedResult getPersistedResultOrDie(final String applicationName, final String resourceName)
    {
        if (!Util.isValidApplicationName(applicationName))
        {
            throw new IllegalArgumentException("Invalid application name: " + applicationName);
        }

        final UUID jobId = UUID.fromString(FilenameUtils.getBaseName(resourceName));

        final PersistedResult persistedResult = resultStore.findByApplicationNameAndJobId(applicationName,
            getUserName(), jobId);

        if (persistedResult == null)
        {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
        }

        return persistedResult;
    }

    // exposed for unit testing
    static String getEtag(final PersistedResult persistedResult)
    {
        return Base64Utility.encode((persistedResult.getApplicationName() + "/" + persistedResult.getJobId()).getBytes());
    }
}

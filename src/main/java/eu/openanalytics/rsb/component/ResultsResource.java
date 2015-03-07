/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.data.PersistedResult;
import eu.openanalytics.rsb.data.SecureResultStore;
import eu.openanalytics.rsb.rest.types.Result;
import eu.openanalytics.rsb.rest.types.Results;

/**
 * Exposes results meta-information, allowing their deletion too.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("resultsResource")
@Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
@Path("/" + Constants.RESULTS_PATH + "/{applicationName}")
public class ResultsResource extends AbstractResource
{
    @Resource
    private SecureResultStore resultStore;

    // exposed for testing
    void setResultStore(final SecureResultStore resultStore)
    {
        this.resultStore = resultStore;
    }

    @GET
    public Results getAllResults(@PathParam("applicationName") final String applicationName,
                                 @Context final HttpHeaders httpHeaders,
                                 @Context final UriInfo uriInfo) throws URISyntaxException
    {
        validateApplicationName(applicationName);

        final Results results = Util.REST_OBJECT_FACTORY.createResults();

        for (final PersistedResult persistedResult : resultStore.findByApplicationName(applicationName,
            getUserName()))
        {
            final Result result = buildResult(applicationName, httpHeaders, uriInfo, persistedResult);
            results.getContents().add(result);
        }

        return results;
    }

    @Path("/{jobId}")
    @GET
    public Result getSingleResult(@PathParam("applicationName") final String applicationName,
                                  @PathParam("jobId") final String jobId,
                                  @Context final HttpHeaders httpHeaders,
                                  @Context final UriInfo uriInfo) throws URISyntaxException, IOException
    {

        validateApplicationName(applicationName);
        validateJobId(jobId);

        final PersistedResult persistedResult = resultStore.findByApplicationNameAndJobId(applicationName,
            getUserName(), UUID.fromString(jobId));
        if (persistedResult == null)
        {
            throw new NotFoundException();
        }

        return buildResult(applicationName, httpHeaders, uriInfo, persistedResult);
    }

    @Path("/{jobId}")
    @DELETE
    public Response deleteSingleResult(@PathParam("applicationName") final String applicationName,
                                       @PathParam("jobId") final String jobId)
        throws URISyntaxException, IOException
    {
        validateApplicationName(applicationName);
        validateJobId(jobId);

        if (!resultStore.deleteByApplicationNameAndJobId(applicationName, getUserName(),
            UUID.fromString(jobId)))
        {
            return Response.status(Status.NOT_FOUND).build();
        }
        else
        {
            return Response.noContent().build();
        }
    }

    Result buildResult(final String applicationName,
                       final HttpHeaders httpHeaders,
                       final UriInfo uriInfo,
                       final PersistedResult persistedResult) throws URISyntaxException
    {

        final String jobId = persistedResult.getJobId().toString();
        final URI selfUri = Util.buildResultUri(applicationName, jobId, httpHeaders, uriInfo);
        final String resourceName = jobId + "." + Util.getResourceType(persistedResult.getMimeType());
        final URI dataUri = Util.getUriBuilder(uriInfo, httpHeaders)
            .path(Constants.RESULT_PATH)
            .path(applicationName)
            .path(resourceName)
            .build();

        final Result result = Util.REST_OBJECT_FACTORY.createResult();
        result.setApplicationName(applicationName);
        result.setJobId(jobId);
        result.setResultTime(Util.convertToXmlDate(persistedResult.getResultTime()));
        result.setSelfUri(selfUri.toString());
        result.setDataUri(dataUri.toString());
        result.setSuccess(persistedResult.isSuccess());
        result.setType(Util.getResourceType(persistedResult.getMimeType()));
        return result;
    }

    private void validateApplicationName(final String applicationName)
    {
        if (!Util.isValidApplicationName(applicationName))
        {
            throw new IllegalArgumentException("Invalid application name: " + applicationName);
        }
    }

    private void validateJobId(final String jobId)
    {
        if (StringUtils.isEmpty(jobId))
        {
            throw new IllegalArgumentException("Job Id can't be empty");
        }
    }
}

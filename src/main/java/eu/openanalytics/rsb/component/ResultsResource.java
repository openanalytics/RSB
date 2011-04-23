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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.Result;
import eu.openanalytics.rsb.rest.types.Results;

/**
 * Exposes results meta-information, allowing their deletion too.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("resultsResource")
@Produces({ Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE })
@Path("/" + Constants.RESULTS_PATH + "/{applicationName}")
public class ResultsResource extends AbstractComponent {
    private interface SingleResultFileOperation<T> {
        T run(final File resultFile) throws URISyntaxException, IOException;
    }

    @GET
    public Results getAllResults(@PathParam("applicationName") final String applicationName, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException {

        if (!Util.isValidApplicationName(applicationName)) {
            throw new IllegalArgumentException("Invalid application name: " + applicationName);
        }

        final Results results = Util.REST_OBJECT_FACTORY.createResults();
        final File[] resultFiles = getApplicationResultDirectory(applicationName).listFiles();

        if (resultFiles != null) {
            for (final File resultFile : resultFiles) {
                final Result result = buildResult(applicationName, httpHeaders, uriInfo, resultFile);
                results.getContents().add(result);
            }
        }

        return results;
    }

    @Path("/{jobId}")
    @GET
    public Result getSingleResult(@PathParam("applicationName") final String applicationName, @PathParam("jobId") final String jobId,
            @Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo) throws URISyntaxException, IOException {
        return runSingleResultFileOperation(applicationName, jobId, httpHeaders, uriInfo, new SingleResultFileOperation<Result>() {
            public Result run(final File resultFile) throws URISyntaxException {
                return buildResult(applicationName, httpHeaders, uriInfo, resultFile);
            }
        });
    }

    @Path("/{jobId}")
    @DELETE
    public Response deleteSingleResult(@PathParam("applicationName") final String applicationName, @PathParam("jobId") final String jobId,
            @Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo) throws URISyntaxException, IOException {

        return runSingleResultFileOperation(applicationName, jobId, httpHeaders, uriInfo, new SingleResultFileOperation<Response>() {
            public Response run(final File resultFile) throws URISyntaxException, IOException {
                FileUtils.forceDelete(resultFile);
                return Response.noContent().build();
            }
        });
    }

    private <T> T runSingleResultFileOperation(final String applicationName, final String jobId, final HttpHeaders httpHeaders,
            final UriInfo uriInfo, final SingleResultFileOperation<T> operation) throws URISyntaxException, IOException {

        if (!Util.isValidApplicationName(applicationName)) {
            throw new IllegalArgumentException("Invalid application name: " + applicationName);
        }

        if (StringUtils.isEmpty(jobId)) {
            throw new IllegalArgumentException("Job Id can't be empty");
        }

        final File[] resultFiles = getApplicationResultDirectory(applicationName).listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return jobId.equals(StringUtils.substringBefore(name, "."));
            }
        });

        if ((resultFiles == null) || (resultFiles.length == 0)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if (resultFiles.length > 1) {
            throw new IllegalStateException("Found " + resultFiles.length + " results for job Id: " + jobId);
        }

        return operation.run(resultFiles[0]);
    }

    // exposed for unit tests
    Result buildResult(final String applicationName, final HttpHeaders httpHeaders, final UriInfo uriInfo, final File resultFile)
            throws URISyntaxException {

        final String fileName = resultFile.getName();
        final String jobId = StringUtils.substringBefore(fileName, ".");
        final GregorianCalendar resultTime = (GregorianCalendar) GregorianCalendar.getInstance();
        resultTime.setTimeInMillis(resultFile.lastModified());

        final URI selfUri = Util.buildResultUri(applicationName, jobId, httpHeaders, uriInfo);
        final URI dataUri = Util.getUriBuilder(uriInfo, httpHeaders).path(Constants.RESULT_PATH).path(applicationName).path(fileName)
                .build();

        final Result result = Util.REST_OBJECT_FACTORY.createResult();
        result.setApplicationName(applicationName);
        result.setJobId(jobId);
        result.setResultTime(Util.convert(resultTime));
        result.setSelfUri(selfUri.toString());
        result.setDataUri(dataUri.toString());
        result.setSuccess(!StringUtils.contains(fileName, ".err."));
        result.setType(FilenameUtils.getExtension(fileName));
        return result;
    }
}

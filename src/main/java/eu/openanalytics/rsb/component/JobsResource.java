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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.JobToken;
import eu.openanalytics.rsb.rest.types.ObjectFactory;

/**
 * Handles asynchronous R job processing requests.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("jobsResource")
@Path("/jobs")
public class JobsResource extends AbstractConfigurable {
    // FIXME unit test
    // FIXME integration test
    // FIXME support application/zip application/x-zip application/x-zip-compressed
    // FIXME support multipart/form-data
    private final static ObjectFactory restOF = new ObjectFactory();

    private final static class InvalidApplicationNameStatus implements StatusType {
        private final String applicationName;

        private InvalidApplicationNameStatus(final String applicationName) {
            this.applicationName = applicationName;
        }

        public int getStatusCode() {
            return Status.BAD_REQUEST.getStatusCode();
        }

        public Family getFamily() {
            return Status.BAD_REQUEST.getFamily();
        }

        public String getReasonPhrase() {
            return "Bad request - Invalid application name: " + applicationName;
        }
    };

    /**
     * Handles a function call job.
     * 
     * @param argument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     */
    @POST
    @Consumes({ Constants.XML_JOB_CONTENT_TYPE, Constants.JSON_JOB_CONTENT_TYPE })
    @Produces({ Constants.XML_JOB_CONTENT_TYPE, Constants.JSON_JOB_CONTENT_TYPE })
    public JobToken handleFunctionCallJob(final String argument, @Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo)
            throws URISyntaxException {

        final String applicationName = Util.getSingleHeader(httpHeaders, Constants.APPLICATION_NAME_HTTP_HEADER);
        if (!Util.isValidApplicationName(applicationName)) {
            throw new WebApplicationException(Response.status(new InvalidApplicationNameStatus(applicationName)).build());
        }

        // FIXME extract X-RSB-Meta- headers

        final String jobId = UUID.randomUUID().toString();
        final JobToken jobToken = restOF.createJobToken();
        jobToken.setApplicationName(applicationName);
        jobToken.setJobId(jobId);

        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path("results").path(applicationName);

        final String uriOverride = Util.getSingleHeader(httpHeaders, Constants.URI_OVERRIDE_HTTP_HEADER);
        if (StringUtils.isNotBlank(uriOverride)) {
            final URI override = new URI(uriOverride);
            uriBuilder.scheme(override.getScheme());
            uriBuilder.host(override.getHost());
            uriBuilder.port(override.getPort());
        }

        jobToken.setApplicationResultsUri(uriBuilder.build().toString());
        jobToken.setResultUri(uriBuilder.path(jobId).build().toString());

        return jobToken;
    }
}

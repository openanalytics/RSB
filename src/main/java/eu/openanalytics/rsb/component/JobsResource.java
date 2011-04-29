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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.rest.types.JobToken;

/**
 * Handles asynchronous R job processing requests.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("jobsResource")
@Path("/" + Constants.JOBS_PATH)
@Produces({ Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE })
public class JobsResource extends AbstractComponent {
    private interface JobBuilder {
        AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) throws IOException;
    }

    /**
     * Handles a function call job with a JSON payload.
     * 
     * @param jsonArgument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @POST
    @Consumes(Constants.JSON_CONTENT_TYPE)
    public Response handleJsonFunctionCallJob(final String jsonArgument, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException, IOException {
        return handleNewRestJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) {
                return new JsonFunctionCallJob(Source.REST, applicationName, jobId, submissionTime, jsonArgument);
            }
        });
    }

    /**
     * Handles a function call job with a XML payload.
     * 
     * @param xmlArgument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @POST
    @Consumes(Constants.XML_CONTENT_TYPE)
    public Response handleXmlFunctionCallJob(final String xmlArgument, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException, IOException {

        return handleNewRestJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) {
                return new XmlFunctionCallJob(Source.REST, applicationName, jobId, submissionTime, xmlArgument);
            }
        });
    }

    /**
     * Handles a function call job with a ZIP payload.
     * 
     * @param xmlArgument
     *            Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @POST
    @Consumes({ Constants.ZIP_CONTENT_TYPE, Constants.ZIP_CONTENT_TYPE2, Constants.ZIP_CONTENT_TYPE3 })
    public Response handleZipJob(final InputStream in, @Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo)
            throws URISyntaxException, IOException {

        return handleNewRestJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime)
                    throws IOException {

                final MultiFilesJob job = new MultiFilesJob(Source.REST, applicationName, jobId, submissionTime, getJobMeta(httpHeaders));
                MultiFilesJob.addZipFilesToJob(in, job);
                return job;
            }
        });
    }

    /**
     * Handles a multi-part form job upload.
     * 
     * @param parts
     * @param httpHeaders
     * @param uriInfo
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @POST
    @Consumes(Constants.MULTIPART_CONTENT_TYPE)
    // force content type to plain XML and JSON (browsers choke on subtypes)
    @Produces({ Constants.XML_CONTENT_TYPE, Constants.JSON_CONTENT_TYPE })
    // TODO unit test
    public Response handleMultipartFormJob(final List<Attachment> parts, @Context final HttpHeaders httpHeaders,
            @Context final UriInfo uriInfo) throws URISyntaxException, IOException {

        String applicationName = null;
        final Map<String, String> jobMeta = new HashMap<String, String>();

        for (final Attachment part : parts) {
            final String partName = getPartName(part);
            if (StringUtils.equals(partName, Constants.APPLICATION_NAME_HEADER)) {
                applicationName = part.getObject(String.class);
            } else if (StringUtils.startsWith(partName, Constants.RSB_META_HEADER_PREFIX)) {
                final String metaName = StringUtils.substringAfter(partName, Constants.RSB_META_HEADER_PREFIX);
                final String metaValue = part.getObject(String.class);
                if (StringUtils.isNotEmpty(metaValue)) {
                    jobMeta.put(metaName, metaValue);
                }
            }
        }

        final String finalApplicationName = applicationName;

        return handleNewJob(finalApplicationName, httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime)
                    throws IOException {

                final MultiFilesJob job = new MultiFilesJob(Source.REST, applicationName, jobId, submissionTime, jobMeta);

                for (final Attachment part : parts) {
                    if (StringUtils.equals(getPartName(part), Constants.JOB_FILES_MULTIPART_NAME)) {
                        final InputStream data = part.getDataHandler().getInputStream();
                        MultiFilesJob.addDataToJob(part.getContentType().toString(), getPartFileName(part), data, job);
                    }
                }

                return job;
            }
        });
    }

    private Response handleNewRestJob(final HttpHeaders httpHeaders, final UriInfo uriInfo, final JobBuilder jobBuilder)
            throws URISyntaxException, IOException {

        final String applicationName = Util.getSingleHeader(httpHeaders, Constants.APPLICATION_NAME_HEADER);
        return handleNewJob(applicationName, httpHeaders, uriInfo, jobBuilder);
    }

    private Response handleNewJob(final String applicationName, final HttpHeaders httpHeaders, final UriInfo uriInfo,
            final JobBuilder jobBuilder) throws IOException, URISyntaxException {

        final UUID jobId = UUID.randomUUID();
        final AbstractJob job = jobBuilder.build(applicationName, jobId, (GregorianCalendar) GregorianCalendar.getInstance());

        getMessageDispatcher().dispatch(job);

        final JobToken jobToken = buildJobToken(uriInfo, httpHeaders, job);

        return Response.status(Status.ACCEPTED).entity(jobToken).build();
    }

    private Map<String, String> getJobMeta(final HttpHeaders httpHeaders) {
        final Map<String, String> meta = new HashMap<String, String>();

        for (final Entry<String, List<String>> multiValues : httpHeaders.getRequestHeaders().entrySet()) {
            if (!StringUtils.startsWithIgnoreCase(multiValues.getKey(), Constants.RSB_META_HEADER_PREFIX)) {
                continue;
            }

            if (multiValues.getValue().size() > 1) {
                throw new IllegalArgumentException("Multiple values found for header: " + multiValues.getKey());
            }

            meta.put(StringUtils.substringAfter(multiValues.getKey(), Constants.RSB_META_HEADER_PREFIX), multiValues.getValue().get(0));
        }

        return meta;
    }

    private JobToken buildJobToken(final UriInfo uriInfo, final HttpHeaders httpHeaders, final AbstractJob job) throws URISyntaxException {
        final JobToken jobToken = Util.REST_OBJECT_FACTORY.createJobToken();
        jobToken.setApplicationName(job.getApplicationName());
        jobToken.setSubmissionTime(Util.convert(job.getSubmissionTime()));

        final String jobIdAsString = job.getJobId().toString();
        jobToken.setJobId(jobIdAsString);

        final URI uriBuilder = Util.getUriBuilder(uriInfo, httpHeaders).path(Constants.RESULTS_PATH).path(job.getApplicationName()).build();
        jobToken.setApplicationResultsUri(uriBuilder.toString());
        jobToken.setResultUri(Util.buildResultUri(job.getApplicationName(), jobIdAsString, httpHeaders, uriInfo).toString());
        return jobToken;
    }

    private static String getPartName(final Attachment part) {
        return part.getContentDisposition().getParameter("name");
    }

    private static String getPartFileName(final Attachment part) {
        return FilenameUtils.getName(part.getContentDisposition().getParameter("filename"));
    }
}

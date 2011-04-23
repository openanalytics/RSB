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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.message.ZipJob;
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
    // FIXME support multipart/form-data

    private interface JobBuilder {
        AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) throws IOException;
    }

    @Resource
    private JmsTemplate jmsTemplate;

    // exposed for unit testing
    void setJmsTemplate(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
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
        return handleNewJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) {
                return new JsonFunctionCallJob(applicationName, jobId, submissionTime, jsonArgument);
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

        return handleNewJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) {
                return new XmlFunctionCallJob(applicationName, jobId, submissionTime, xmlArgument);
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

        return handleNewJob(httpHeaders, uriInfo, new JobBuilder() {
            public AbstractJob build(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime)
                    throws IOException {

                final ZipJob zipJob = new ZipJob(applicationName, jobId, submissionTime, getJobMeta(httpHeaders));

                final ZipInputStream zis = new ZipInputStream(in);
                ZipEntry ze = null;

                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        zipJob.destroy();
                        throw new IllegalArgumentException("Invalid zip archive: nested directories are not supported");
                    }
                    zipJob.addFile(ze.getName(), zis);
                    zis.closeEntry();
                }

                IOUtils.closeQuietly(zis);
                return zipJob;
            }
        });
    }

    private Response handleNewJob(final HttpHeaders httpHeaders, final UriInfo uriInfo, final JobBuilder jobBuilder)
            throws URISyntaxException, IOException {

        final String applicationName = Util.getSingleHeader(httpHeaders, Constants.APPLICATION_NAME_HTTP_HEADER);
        if (!Util.isValidApplicationName(applicationName)) {
            throw new IllegalArgumentException("Invalid application name: " + applicationName);
        }

        final UUID jobId = UUID.randomUUID();
        final AbstractJob job = jobBuilder.build(applicationName, jobId, (GregorianCalendar) GregorianCalendar.getInstance());

        Util.dispatch(job, jmsTemplate);

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
}

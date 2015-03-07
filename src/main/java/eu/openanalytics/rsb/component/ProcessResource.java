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
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;

/**
 * Processes synchronous R job requests. Currently only supports XML and JSON function calls.<br/>
 * <b>This processor bypasses the messaging infrastructure so it is not a fair player compared to
 * the other REST/SOAP entry points, thus it should be used with care!</b>
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("processResource")
@Path("/" + Constants.PROCESS_PATH)
public class ProcessResource extends AbstractResource
{
    private interface FunctionCallJobBuilder
    {
        AbstractFunctionCallJob build(final String applicationName,
                                      final UUID jobId,
                                      final GregorianCalendar submissionTime) throws IOException;
    }

    @Resource
    private JobProcessor jobProcessor;

    // exposed for unit testing
    public void setJobProcessor(final JobProcessor jobProcessor)
    {
        this.jobProcessor = jobProcessor;
    }

    /**
     * Handles a function call job with a JSON payload.
     * 
     * @param jsonArgument Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(Constants.JSON_CONTENT_TYPE)
    @Produces(Constants.JSON_CONTENT_TYPE)
    public Response processJsonFunctionCallJob(final String jsonArgument,
                                               @Context final HttpHeaders httpHeaders) throws Exception
    {
        return handleNewRestJob(httpHeaders, new FunctionCallJobBuilder()
        {
            public AbstractFunctionCallJob build(final String applicationName,
                                                 final UUID jobId,
                                                 final GregorianCalendar submissionTime)
            {
                return new JsonFunctionCallJob(Source.REST_IMMEDIATE, applicationName, getUserName(), jobId,
                    submissionTime, jsonArgument);
            }
        });
    }

    /**
     * Handles a function call job with a XML payload.
     * 
     * @param xmlArgument Argument passed to the function called on RServi.
     * @param httpHeaders
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(Constants.XML_CONTENT_TYPE)
    @Produces(Constants.XML_CONTENT_TYPE)
    public Response processXmlFunctionCallJob(final String xmlArgument, @Context final HttpHeaders httpHeaders)
        throws Exception
    {

        return handleNewRestJob(httpHeaders, new FunctionCallJobBuilder()
        {
            public AbstractFunctionCallJob build(final String applicationName,
                                                 final UUID jobId,
                                                 final GregorianCalendar submissionTime)
            {
                return new XmlFunctionCallJob(Source.REST_IMMEDIATE, applicationName, getUserName(), jobId,
                    submissionTime, xmlArgument);
            }
        });
    }

    private Response handleNewRestJob(final HttpHeaders httpHeaders, final FunctionCallJobBuilder jobBuilder)
        throws Exception
    {

        final String applicationName = Util.getSingleHeader(httpHeaders,
            Constants.APPLICATION_NAME_HTTP_HEADER);
        return handleNewJob(applicationName, jobBuilder);
    }

    private Response handleNewJob(final String applicationName, final FunctionCallJobBuilder jobBuilder)
        throws Exception
    {

        final UUID jobId = UUID.randomUUID();
        final AbstractFunctionCallJob job = jobBuilder.build(applicationName, jobId,
            (GregorianCalendar) GregorianCalendar.getInstance());
        final AbstractResult<?> result = jobProcessor.processDirect(job);
        if (result.isSuccess())
        {
            return Response.ok(result.getPayload()).build();
        }
        else
        {
            return Response.status(Status.BAD_REQUEST).entity(result.getPayload()).build();
        }
    }
}

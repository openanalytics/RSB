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
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.JsonFunctionCallResult;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.message.XmlFunctionCallResult;
import eu.openanalytics.rsb.soap.jobs.MtomJobProcessor;
import eu.openanalytics.rsb.soap.types.JobType;
import eu.openanalytics.rsb.soap.types.JobType.Parameter;
import eu.openanalytics.rsb.soap.types.ObjectFactory;
import eu.openanalytics.rsb.soap.types.PayloadType;
import eu.openanalytics.rsb.soap.types.ResultType;

/**
 * Handles synchronous SOAP/MTOM R job processing requests.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@MTOM
@WebService(endpointInterface = "eu.openanalytics.rsb.soap.jobs.MtomJobProcessor", targetNamespace = "http://soap.rsb.openanalytics.eu/jobs", serviceName = "MtomJobService", portName = "MtomJobProcessorPort", wsdlLocation = "wsdl/mtom-jobs.wsdl")
@Component("soapMtomJobHandler")
// TODO unit test, integration test
public class SoapMtomJobHandler extends AbstractComponent implements MtomJobProcessor {
    private final static ObjectFactory soapOF = new ObjectFactory();

    /**
     * Processes a single R job.
     * 
     * @throws IOException
     */
    public ResultType process(final JobType job) {
        try {
            final String applicationName = job.getApplicationName();

            final Map<String, String> meta = new HashMap<String, String>();
            for (final Parameter parameter : job.getParameter()) {
                meta.put(parameter.getName(), parameter.getValue());
            }

            // FIXME refactor this code!

            // function call jobs have a single attachment and no meta
            if ((job.getPayload().size() == 1) && (meta.isEmpty())) {
                final PayloadType singlePayload = job.getPayload().get(0);
                if (Constants.XML_CONTENT_TYPE.equals(singlePayload.getContentType())) {
                    final String argument = IOUtils.toString(singlePayload.getData().getInputStream());
                    final XmlFunctionCallJob xmlFunctionCallJob = new XmlFunctionCallJob(Source.SOAP, applicationName, UUID.randomUUID(),
                            (GregorianCalendar) GregorianCalendar.getInstance(), argument);

                    final XmlFunctionCallResult xmlFunctionCallResult = getMessageDispatcher().process(xmlFunctionCallJob);

                    final ResultType response = createResponse(xmlFunctionCallResult);
                    final PayloadType payload = soapOF.createPayloadType();
                    payload.setContentType(Constants.XML_CONTENT_TYPE);
                    // FIXME use file name building logic
                    payload.setName("result.xml");
                    payload.setData(new DataHandler(new ByteArrayDataSource(xmlFunctionCallResult.getPayload(), Constants.XML_CONTENT_TYPE)));
                    response.getPayload().add(payload);

                    return response;
                } else if (Constants.JSON_CONTENT_TYPE.equals(job.getPayload().get(0).getContentType())) {
                    final String argument = IOUtils.toString(singlePayload.getData().getInputStream());
                    final JsonFunctionCallJob jsonFunctionCallJob = new JsonFunctionCallJob(Source.SOAP, applicationName,
                            UUID.randomUUID(), (GregorianCalendar) GregorianCalendar.getInstance(), argument);

                    final JsonFunctionCallResult jsonFunctionCallResult = getMessageDispatcher().process(jsonFunctionCallJob);

                    final ResultType response = createResponse(jsonFunctionCallResult);
                    final PayloadType payload = soapOF.createPayloadType();
                    payload.setContentType(Constants.JSON_CONTENT_TYPE);
                    // FIXME use file name building logic
                    payload.setName("result.json");
                    payload.setData(new DataHandler(new ByteArrayDataSource(jsonFunctionCallResult.getPayload(),
                            Constants.JSON_CONTENT_TYPE)));
                    response.getPayload().add(payload);

                    return response;
                }
            }

            final MultiFilesJob multiFilesJob = new MultiFilesJob(Source.SOAP, applicationName, UUID.randomUUID(),
                    (GregorianCalendar) GregorianCalendar.getInstance(), meta);

            final boolean isOneZipJob = job.getPayload().size() == 1
                    && Constants.ZIP_CONTENT_TYPES.contains(job.getPayload().get(0).getContentType());

            for (final PayloadType payload : job.getPayload()) {
                MultiFilesJob.addDataToJob(payload.getContentType(), payload.getName(), payload.getData().getInputStream(), multiFilesJob);
            }

            final MultiFilesResult multiFilesResult = getMessageDispatcher().process(multiFilesJob);

            final ResultType response = createResponse(multiFilesResult);

            final File[] resultFiles = isOneZipJob ? new File[] { MultiFilesResult.zipResultFilesIfNotError(multiFilesResult) }
                    : multiFilesResult.getPayload();

            for (final File resultFile : resultFiles) {
                final PayloadType payload = soapOF.createPayloadType();
                payload.setContentType(new MimetypesFileTypeMap().getContentType(resultFile));
                payload.setName(resultFile.getName());
                payload.setData(new DataHandler(new FileDataSource(resultFile)));
                response.getPayload().add(payload);
            }

            return response;
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private ResultType createResponse(final AbstractResult<?> result) {
        final ResultType response = soapOF.createResultType();
        response.setApplicationName(result.getApplicationName());
        response.setJobId(result.getJobId().toString());
        return response;
    }
}

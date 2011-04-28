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
import javax.xml.ws.soap.MTOM;

import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
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
public class SoapMtomJobHandler extends AbstractComponent implements MtomJobProcessor {
    // TODO unit test
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

            // FIXME support XML and JSON jobs
            final MultiFilesJob multiFilesJob = new MultiFilesJob(Source.SOAP, applicationName, UUID.randomUUID(),
                    (GregorianCalendar) GregorianCalendar.getInstance(), meta);

            for (final PayloadType payload : job.getPayload()) {
                Util.addDataToJob(payload.getContentType(), payload.getName(), payload.getData().getInputStream(), multiFilesJob);
            }

            final MultiFilesResult multiFilesResult = getMessageDispatcher().process(multiFilesJob);

            final ResultType response = soapOF.createResultType();
            response.setApplicationName(multiFilesResult.getApplicationName());
            response.setJobId(multiFilesResult.getJobId().toString());

            // FIXME support attaching n-files instead of one zip only
            final File resultFile = multiFilesResult.getPayload();

            final PayloadType payload = soapOF.createPayloadType();
            payload.setContentType(new MimetypesFileTypeMap().getContentType(resultFile));
            payload.setName(resultFile.getName());
            payload.setData(new DataHandler(new FileDataSource(resultFile)));
            response.getPayload().add(payload);
            return response;
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

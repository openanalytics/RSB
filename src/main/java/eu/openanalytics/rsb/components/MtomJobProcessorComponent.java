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

package eu.openanalytics.rsb.components;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.soap.jobs.MtomJobProcessor;
import eu.openanalytics.rsb.soap.types.JobType;
import eu.openanalytics.rsb.soap.types.PayloadType;
import eu.openanalytics.rsb.soap.types.ResultType;

/**
 * @author rsb.development@openanalytics.eu
 */
@MTOM
@WebService(endpointInterface = "eu.openanalytics.rsb.soap.jobs.MtomJobProcessor", targetNamespace = "http://soap.rsb.openanalytics.eu/jobs", serviceName = "MtomJobService", portName = "MtomJobProcessorPort", wsdlLocation = "wsdl/mtom-jobs.wsdl")
@Component("MtomJobProcessor")
public class MtomJobProcessorComponent implements MtomJobProcessor {
	private static final String JSON_SERVICE_CALL_JOB_CONTENT_TYPE = "application/vnd.rsb+json";
	private static final String XML_SERVICE_CALL_JOB_CONTENT_TYPE = "application/vnd.rsb+xml";
	private static final String APPLICATION_ZIP_CONTENT_TYPE = "application/zip";

	private final static Pattern APP_NAME_VALIDATOR = Pattern.compile("\\w+");

	public ResultType process(final JobType job) {
		final String applicationName = job.getApplicationName();

		if (StringUtils.isBlank(applicationName) || !APP_NAME_VALIDATOR.matcher(applicationName).matches()) {
			throw new IllegalArgumentException("Invalid application name: " + applicationName);
		}

		// TODO implement
		final ResultType response = new ResultType();
		response.setApplicationName(applicationName);
		response.setJobId(UUID.randomUUID().toString());
		final PayloadType payload = new PayloadType();
		payload.setContentType(XML_SERVICE_CALL_JOB_CONTENT_TYPE);
		payload.setName("result");
		try {
			payload.setData(new DataHandler(new ByteArrayDataSource("<fake_result />", XML_SERVICE_CALL_JOB_CONTENT_TYPE)));
		} catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
		response.getPayload().add(payload);
		return response;
	}
}

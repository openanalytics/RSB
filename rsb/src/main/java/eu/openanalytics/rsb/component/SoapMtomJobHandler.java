/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb.component;

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.IllegalJobDataException;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.JsonFunctionCallResult;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.message.XmlFunctionCallResult;
import eu.openanalytics.rsb.security.ApplicationPermissionEvaluator;
import eu.openanalytics.rsb.soap.jobs.MtomJobProcessor;
import eu.openanalytics.rsb.soap.types.JobType;
import eu.openanalytics.rsb.soap.types.JobType.Parameter;
import eu.openanalytics.rsb.soap.types.ObjectFactory;
import eu.openanalytics.rsb.soap.types.PayloadType;
import eu.openanalytics.rsb.soap.types.ResultType;


/**
 * Handles synchronous SOAP/MTOM R job processing requests.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@MTOM
@WebService(endpointInterface = "eu.openanalytics.rsb.soap.jobs.MtomJobProcessor", targetNamespace = "http://soap.rsb.openanalytics.eu/jobs", serviceName = "MtomJobService", portName = "MtomJobProcessorPort", wsdlLocation = "wsdl/mtom-jobs.wsdl")
@Component("soapMtomJobHandler")
public class SoapMtomJobHandler extends AbstractComponent implements MtomJobProcessor
{
    private static final String NULL_RESULT_RECEIVED = "Null result received: has the job timed out?";
    private final static ObjectFactory soapOF = new ObjectFactory();

    /**
     * Processes a single R job.
     * 
     * @throws IOException
     */
	@Override
	public ResultType process(final JobType job)
    {
        try
        {
            final String applicationName = job.getApplicationName();
            final Map<String, Serializable> meta = getMeta(job);

            final ResultType potentialFunctionCallResult = processPotentialFunctionCallJob(applicationName,
                job, meta);
            if (potentialFunctionCallResult != null)
            {
                return potentialFunctionCallResult;
            }

            return processMultiFilesJob(applicationName, job, meta);
        }
		catch (final IllegalJobDataException | IOException e) {
			throw new RuntimeException(e);
		}
	}

    private Map<String, Serializable> getMeta(final JobType job)
    {
        final Map<String, Serializable> meta = new HashMap<>();
        for (final Parameter parameter : job.getParameter())
        {
            meta.put(parameter.getName(), parameter.getValue());
        }
        return meta;
    }

    private ResultType processPotentialFunctionCallJob(final String applicationName,
                                                       final JobType job,
                                                       final Map<String, Serializable> meta)
        throws IOException
    {

        if (!isPotentiallyAFunctionCallJob(job, meta))
        {
            return null;
        }

        // we consider only the first payload
        final PayloadType payload = job.getPayload().get(0);
        final String contentType = payload.getContentType();

        if (Constants.XML_CONTENT_TYPE.equals(contentType))
        {
            final String argument = IOUtils.toString(payload.getData().getInputStream(), Charset.defaultCharset());
            final XmlFunctionCallJob xmlFunctionCallJob = new XmlFunctionCallJob(Source.SOAP,
                applicationName, ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, UUID.randomUUID(),
                (GregorianCalendar) GregorianCalendar.getInstance(), argument);

            final XmlFunctionCallResult xmlFunctionCallResult = getMessageDispatcher().process(
                xmlFunctionCallJob);
            return buildResult(xmlFunctionCallResult);
        }

        if (Constants.JSON_CONTENT_TYPE.equals(payload.getContentType()))
        {
            final String argument = IOUtils.toString(payload.getData().getInputStream(), Charset.defaultCharset());
            final JsonFunctionCallJob jsonFunctionCallJob = new JsonFunctionCallJob(Source.SOAP,
                applicationName, ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, UUID.randomUUID(),
                (GregorianCalendar) GregorianCalendar.getInstance(), argument);

            final JsonFunctionCallResult jsonFunctionCallResult = getMessageDispatcher().process(
                jsonFunctionCallJob);
            return buildResult(jsonFunctionCallResult);
        }

        // wasn't a function call after all...
        return null;
    }

    private boolean isPotentiallyAFunctionCallJob(final JobType job, final Map<String, Serializable> meta)
    {
        // function call jobs have a single attachment and no meta
        return (job.getPayload().size() == 1) && (meta.isEmpty());
    }
	
	private ResultType processMultiFilesJob(final String applicationName,
			final JobType soapJob, final Map<String, Serializable> meta)
			throws IllegalJobDataException, IOException {
		final MultiFilesJob job= new MultiFilesJob(Source.SOAP, applicationName,
				ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, UUID.randomUUID(),
				(GregorianCalendar)GregorianCalendar.getInstance(), meta);
		try {
			for (final PayloadType payload : soapJob.getPayload()) {
				try (final var data= payload.getData().getInputStream()) {
					MultiFilesJob.addDataToJob(payload.getContentType(), payload.getName(),
							data, job );
				}
			}
			
			final MultiFilesResult multiFilesResult= getMessageDispatcher().process(job);
			return buildResult(soapJob, multiFilesResult);
		}
		catch (final Exception e) {
			job.destroy();
			throw e;
		}
	}
	
    private ResultType buildResult(final AbstractFunctionCallResult functionCallResult) throws IOException
    {
        Validate.notNull(functionCallResult, NULL_RESULT_RECEIVED);

        final String resultContentType = functionCallResult.getMimeType().toString();

        final PayloadType payload = soapOF.createPayloadType();
        payload.setContentType(resultContentType);
        payload.setName(functionCallResult.getResultFileName());
        payload.setData(new DataHandler(new ByteArrayDataSource(functionCallResult.getPayload(),
            resultContentType)));

        final ResultType result = createResult(functionCallResult);
        result.getPayload().add(payload);
        return result;
    }
	
	private ResultType buildResult(final JobType job, final MultiFilesResult multiFilesResult)
			throws IOException {
		Validate.notNull(multiFilesResult, NULL_RESULT_RECEIVED);
		
		final boolean isOneZipJob= (job.getPayload().size() == 1
				&& Constants.ZIP_CONTENT_TYPES.contains(job.getPayload().get(0).getContentType()) );
		
		final var resultFiles= (isOneZipJob) ?
				List.of(MultiFilesResult.zipResultFilesIfNotError(multiFilesResult)) :
				multiFilesResult.getPayload();
		
		final ResultType result= createResult(multiFilesResult);
		
		for (final var resultFile : resultFiles) {
			final PayloadType payload= soapOF.createPayloadType();
			payload.setContentType(Util.getContentType(resultFile));
			payload.setName(requireFileName(resultFile).toString());
			payload.setData(new DataHandler(new FileDataSource(resultFile.toFile())));
			result.getPayload().add(payload);
		}
		return result;
	}
	
    private ResultType createResult(final AbstractResult<?> result)
    {
        final ResultType response = soapOF.createResultType();
        response.setApplicationName(result.getApplicationName());
        response.setJobId(result.getJobId().toString());
        response.setSuccess(result.isSuccess());
        return response;
    }
}

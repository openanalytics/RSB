/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.IllegalJobDataException;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.rest.types.JobToken;


/**
 * Handles asynchronous R job processing requests.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("jobsResource")
@Path("/" + Constants.JOBS_PATH)
@Produces({Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE})
public class JobsResource extends AbstractResource {
	
	
	private static interface JobBuilder {
		
		AbstractJob build(String applicationName, UUID jobId, GregorianCalendar submissionTime) 
				throws IllegalJobDataException, IOException;
	}
	
	
	/**
	 * Handles a function call job with a JSON payload.
	 * 
	 * @param jsonArgument Argument passed to the function called on RServi.
	 * @param httpHeaders
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Consumes(Constants.JSON_CONTENT_TYPE)
	public Response handleJsonFunctionCallJob(final String jsonArgument,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IllegalArgumentException, URISyntaxException, IOException {
		return handleNewRestJob(httpHeaders, uriInfo,
				(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) -> {
			return new JsonFunctionCallJob(Source.REST, applicationName, getUserName(), jobId,
					submissionTime, jsonArgument );
		});
	}
	
	/**
	 * Handles a function call job with a XML payload.
	 * 
	 * @param xmlArgument Argument passed to the function called on RServi.
	 * @param httpHeaders
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Consumes(Constants.XML_CONTENT_TYPE)
	public Response handleXmlFunctionCallJob(final String xmlArgument,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IllegalArgumentException, URISyntaxException, IOException {
		return handleNewRestJob(httpHeaders, uriInfo, 
				(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) -> {
			return new XmlFunctionCallJob(Source.REST, applicationName, getUserName(), jobId,
					submissionTime, xmlArgument );
		});
	}
	
	/**
	 * Handles a function call job with a ZIP payload.
	 * 
	 * @param in Input stream of the ZIP data.
	 * @param httpHeaders
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Consumes({Constants.ZIP_CONTENT_TYPE, Constants.ZIP_CONTENT_TYPE2, Constants.ZIP_CONTENT_TYPE3})
	public Response handleZipJob(final InputStream in,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IllegalArgumentException, URISyntaxException, IOException {
		return handleNewRestJob(httpHeaders, uriInfo,
				(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) -> {
			final MultiFilesJob job= new MultiFilesJob(Source.REST, applicationName,
					getUserName(), jobId, submissionTime, getJobMeta(httpHeaders) );
			try {
				job.addFilesFromZip(in);
				
				return job;
			}
			catch (final Exception e) {
				job.destroy();
				throw e;
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
	 */
	@POST
	@Consumes(Constants.MULTIPART_CONTENT_TYPE)
	// force content type to plain XML and JSON (browsers choke on subtypes)
	@Produces({Constants.XML_CONTENT_TYPE, Constants.JSON_CONTENT_TYPE})
	public Response handleMultipartFormJob(final List<Attachment> parts,
			@Context final HttpHeaders httpHeaders,
			@Context final UriInfo uriInfo)
			throws IllegalArgumentException, URISyntaxException, IOException {
		String jobApplicationName= null;
		final Map<String, Serializable> jobMeta= new HashMap<>();
		
		for (final Attachment part : parts) {
			final String partName= getPartName(part);
			if (partName == null) {
				continue;
			}
			final String fieldName= partName.toLowerCase(Locale.ROOT);
			if (fieldName.equals(Constants.APPLICATION_NAME_FIELD_NAME)) {
				jobApplicationName= part.getObject(String.class);
				continue;
			}
			if (fieldName.startsWith(Constants.RSB_META_A_FIELD_NAME_PREFIX)) {
				final String metaValue= part.getObject(String.class);
				if (metaValue != null && !metaValue.isEmpty()) {
					if (fieldName.startsWith(Constants.RSB_META_B_FIELD_NAME_PREFIX)) {
						addJobMetaB(jobMeta, metaValue);
					}
					else {
						addJobMetaA(jobMeta, partName, metaValue);
					}
				}
				continue;
			}
		}
		
		return handleNewJob(jobApplicationName, httpHeaders, uriInfo, 
				(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) -> {
			final MultiFilesJob job= new MultiFilesJob(Source.REST, applicationName,
					getUserName(), jobId, submissionTime, jobMeta);
			for (final Attachment part : parts) {
				final String partName= getPartName(part);
				if (partName == null) {
					continue;
				}
				final String fieldName= partName.toLowerCase(Locale.ROOT);
				if (fieldName.equals(Constants.JOB_FILES_FIELD_NAME)) {
					final InputStream data= part.getDataHandler().getInputStream();
					if (data.available() == 0) {
						// if the form is submitted with no file attached, we get an empty part
						continue;
					}
					MultiFilesJob.addDataToJob(part.getContentType().toString(), getPartFileName(part),
							data, job);
				}
			}
			return job;
		});
	}
	
	
	private Response handleNewRestJob(final HttpHeaders httpHeaders, final UriInfo uriInfo,
			final JobBuilder jobBuilder)
			throws IllegalArgumentException, URISyntaxException, IOException {
		final String applicationName= Util.getSingleHeader(httpHeaders,
				Constants.APPLICATION_NAME_FIELD_NAME );
		return handleNewJob(applicationName, httpHeaders, uriInfo, jobBuilder);
	}
	
	private Response handleNewJob(final String applicationName,
			final HttpHeaders httpHeaders, final UriInfo uriInfo,
			final JobBuilder jobBuilder)
			throws IllegalArgumentException, URISyntaxException, IOException {
		try {
			final UUID jobId= UUID.randomUUID();
			final AbstractJob job= jobBuilder.build(applicationName, jobId,
					(GregorianCalendar)GregorianCalendar.getInstance() );
			getMessageDispatcher().dispatch(job);
			final JobToken jobToken= buildJobToken(uriInfo, httpHeaders, job);
			return Response.status(Status.ACCEPTED).entity(jobToken).build();
		}
		catch (final IllegalJobDataException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	private Map<String, Serializable> getJobMeta(final HttpHeaders httpHeaders) {
		final Map<String, Serializable> jobMeta= new HashMap<>();
		
		for (final Entry<String, List<String>> field : httpHeaders.getRequestHeaders().entrySet()) {
			final String fieldName= field.getKey().toLowerCase(Locale.ROOT);
			if (fieldName.startsWith(Constants.RSB_META_A_FIELD_NAME_PREFIX)) {
				if (field.getValue().size() > 1) {
					throw new IllegalArgumentException(String.format("Multiple values found for header: %1$s",
							field.getKey() ));
				}
				if (fieldName.startsWith(Constants.RSB_META_B_FIELD_NAME_PREFIX)) {
					addJobMetaB(jobMeta, field.getValue().get(0));
				}
				else {
					addJobMetaA(jobMeta, fieldName, field.getValue().get(0)); // force lower case name
				}
				continue;
			}
		}
		
		return jobMeta;
	}
	
	protected void addJobMetaA(final Map<String, Serializable> jobMeta,
			final String fieldName, final String fieldValue) {
		String metaKey= fieldName.substring(Constants.RSB_META_A_FIELD_NAME_PREFIX.length());
		if (metaKey.isEmpty()) {
			throw new IllegalArgumentException("Invalid specification of meta information parameter (type A): parameter name is missing.");
		}
		metaKey= Util.normalizeJobMetaKey(metaKey);
		if (jobMeta.put(metaKey, fieldValue) != null) {
			throw new IllegalArgumentException(String.format("Duplicate specification of meta information parameter '%1$s'.",
					metaKey ));
		}
	}
	
	protected void addJobMetaB(final Map<String, Serializable> jobMeta, final String fieldValue) {
		final int sep= fieldValue.indexOf('=');
		if (sep == -1) {
			throw new IllegalArgumentException(String.format("Invalid specification of meta information parameter (type B): \"%1$s\".",
					fieldValue ));
		}
		String metaKey= substringTrim(fieldValue, 0, sep);
		if (metaKey.isEmpty()) {
			throw new IllegalArgumentException("Invalid specification of meta information parameter (type B): parameter name is missing.");
		}
		metaKey= Util.normalizeJobMetaKey(metaKey);
		if (jobMeta.put(metaKey, substringTrim(fieldValue, sep + 1, fieldValue.length())) != null) {
			throw new IllegalArgumentException(String.format("Duplicate specification of meta information parameter '%1$s'.",
					metaKey ));
		}
	}
	
	private JobToken buildJobToken(final UriInfo uriInfo, final HttpHeaders httpHeaders,
			final AbstractJob job)
			throws URISyntaxException {
		final JobToken jobToken = Util.REST_OBJECT_FACTORY.createJobToken();
		jobToken.setApplicationName(job.getApplicationName());
		jobToken.setSubmissionTime(Util.convertToXmlDate(job.getSubmissionTime()));
		
		final String jobIdAsString = job.getJobId().toString();
		jobToken.setJobId(jobIdAsString);
		
		final URI uriBuilder = Util.getUriBuilder(uriInfo, httpHeaders)
				.path(Constants.RESULTS_PATH)
				.path(job.getApplicationName())
				.build();
		jobToken.setApplicationResultsUri(uriBuilder.toString());
		jobToken.setResultUri(Util.buildResultUri(job.getApplicationName(), jobIdAsString, httpHeaders,
				uriInfo).toString());
		return jobToken;
	}
	
	
	private static @Nullable String getPartName(final Attachment part) {
		return part.getContentDisposition().getParameter("name");
	}
	
	private static @Nullable String getPartFileName(final Attachment part) {
		return FilenameUtils.getName(part.getContentDisposition().getParameter("filename"));
	}
	
	private static String substringTrim(final String s, int start, int end) {
		if (start == 0 && end == s.length()) {
			return s.trim();
		}
		while (start < end && s.charAt(start) <= ' ') {
			start++;
		}
		while (start < end && s.charAt(end - 1) <= ' ') {
			end--;
		}
		return s.substring(start, end);
	}
	
}

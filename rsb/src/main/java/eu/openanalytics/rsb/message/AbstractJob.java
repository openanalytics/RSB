/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
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

package eu.openanalytics.rsb.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.ErrorResult;


/**
 * Represents a generic RSB job.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractJob extends AbstractWorkItem implements Job {
	
	private static final long serialVersionUID= 1L;
	
	
	public AbstractJob(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final Map<String, Serializable> meta) {
		super(source, applicationName, userName, jobId, submissionTime, meta);
	}
	
	
	@Override
	public String getType() {
		return getClass().getSimpleName();
	}
	
	
	public abstract AbstractResult<?> buildErrorResult(Throwable t, MessageSource messageSource)
			throws IOException;
	
	/**
	 * Builds an {@link ErrorResult} for a job whose processing has failed.
	 * 
	 * @param job
	 * @param error
	 * @return
	 */
	public static ErrorResult buildJobProcessingErrorResult(final AbstractJob job,
			final Throwable error) {
		final ErrorResult errorResult= Util.REST_OBJECT_FACTORY.createErrorResult();
		errorResult.setApplicationName(job.getApplicationName());
		errorResult.setJobId(job.getJobId().toString());
		errorResult.setSubmissionTime(Util.convertToXmlDate(job.getSubmissionTime()));
		errorResult.setErrorMessage(error.getMessage());
		return errorResult;
	}
	
}

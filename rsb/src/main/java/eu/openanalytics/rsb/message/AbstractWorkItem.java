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

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import eu.openanalytics.rsb.Util;


/**
 * Parent of all the work item messages.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractWorkItem implements WorkItem, Serializable {
	
	public enum Source {
		
		REST("job.error", "job.abort", 4),
		REST_IMMEDIATE("job.error", "job.abort", 4),
		SOAP("job.error", "job.abort", 8),
		EMAIL("email.job.error", "email.job.abort", 4),
		DIRECTORY("directory.job.error", "directory.job.abort", 4);
		
		
		private final String errorMessageId;
		private final String abortMessageId;
		private final int priority;
		
		private Source(final String errorMessageId, final String abortMessageId, final int priority) {
			this.errorMessageId= errorMessageId;
			this.abortMessageId= abortMessageId;
			this.priority= priority;
		}
		
	}
	
	
	private static final long serialVersionUID= 1L;
	
	
	private final Source source;
	private final String applicationName;
	private final @Nullable String userName;
	private final UUID jobId;
	private final GregorianCalendar submissionTime;
	private final Map<String, Serializable> meta;
	
	public AbstractWorkItem(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final Map<String, Serializable> meta) {
		Validate.notNull(source, "source can't be null");
		Validate.notEmpty(applicationName, "applicationName can't be empty");
		Validate.notNull(jobId, "jobId can't be null");
		Validate.notNull(submissionTime, "submissionTime can't be null");
		Validate.notNull(meta, "meta can't be null");
		if (!Util.isValidApplicationName(applicationName)) {
			throw new IllegalArgumentException("Invalid application name: " + applicationName);
		}
		
		this.source= source;
		this.applicationName= applicationName;
		this.userName= userName;
		this.jobId= jobId;
		this.submissionTime= submissionTime;
		this.meta= meta;
	}
	
	public void destroy() {
		releaseResources();
	}
	
	protected abstract void releaseResources();
	
	
	@Override
	public Source getSource() {
		return this.source;
	}
	
	@Override
	public String getApplicationName() {
		return this.applicationName;
	}
	
	@Override
	public @Nullable String getUserName() {
		return this.userName;
	}
	
	@Override
	public UUID getJobId() {
		return this.jobId;
	}
	
	@Override
	public GregorianCalendar getSubmissionTime() {
		return this.submissionTime;
	}
	
	@Override
	public int getPriority() {
		// potentially support per application priority
		return getSource().priority;
	}
	
	@Override
	public String getErrorMessageId() {
		return getSource().errorMessageId;
	}
	
	@Override
	public String getAbortMessageId() {
		return getSource().abortMessageId;
	}
	
	
	@Override
	public Map<String, Serializable> getMeta() {
		return this.meta;
	}
	
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}

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

package eu.openanalytics.rsb.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.MimeType;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Defines what a result store should persist for job result.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class PersistedResult {
	
	
	private final String applicationName;
	private final @Nullable String userName;
	private final UUID jobId;
	private final GregorianCalendar resultTime;
	private final boolean success;
	private final MimeType mimeType;
	
	public PersistedResult(final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar resultTime,
			final boolean success,
			final MimeType mimeType) {
		this.applicationName= applicationName;
		this.userName= userName;
		this.jobId= jobId;
		this.resultTime= resultTime;
		this.success= success;
		this.mimeType= mimeType;
	}
	
	
	public String getApplicationName() {
		return this.applicationName;
	}
	
	public @Nullable String getUserName() {
		return this.userName;
	}
	
	public UUID getJobId() {
		return this.jobId;
	}
	
	public GregorianCalendar getResultTime() {
		return this.resultTime;
	}
	
	public boolean isSuccess() {
		return this.success;
	}
	
	public MimeType getMimeType() {
		return this.mimeType;
	}
	
	
	public abstract long getDataLength() throws IOException;
	
	public abstract InputStream getData() throws IOException;
	
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}

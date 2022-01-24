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

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.MimeType;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;


/**
 * Represents the result of a {@link AbstractFunctionCallJob}.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractFunctionCallResult extends AbstractResult<String> {
	
	private static final long serialVersionUID= 1L;
	
	
	private final String result;
	
	
	@SuppressWarnings("unchecked")
	public AbstractFunctionCallResult(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final boolean success, final String result) {
		// function call jobs and results have no meta
		super(source, applicationName, userName, jobId, submissionTime, Collections.EMPTY_MAP, success);
		this.result= result;
	}
	
	@Override
	protected void releaseResources() {
		// NOOP
	}
	
	
	public abstract MimeType getMimeType();
	
	@Override
	public String getPayload() {
		return this.result;
	}
	
	public String getResultFileName() {
		final String resultFileExtension= (isSuccess() ? "" : "err.") + getMimeType().getSubType();
		return getJobId() + "." + resultFileExtension;
	}
	
}

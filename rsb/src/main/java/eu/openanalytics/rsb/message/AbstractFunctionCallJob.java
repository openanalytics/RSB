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
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;


/**
 * Represents a RSB job that consists in calling a unary function on R.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractFunctionCallJob extends AbstractJob {
	
	private static final long serialVersionUID= 1L;
	
	
	private final String argument;
	
	
	@SuppressWarnings("unchecked")
	public AbstractFunctionCallJob(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final String argument) {
		// function call jobs and results have no meta
		super(source, applicationName, userName, jobId, submissionTime, Collections.EMPTY_MAP);
		this.argument= argument;
	}
	
	@Override
	protected void releaseResources() {
		// NOOP
	}
	
	
	public abstract String getFunctionName();
	
	public String getArgument() {
		return this.argument;
	}
	
	
	public abstract AbstractResult<String> buildSuccessResult(String result) throws IOException;
	
	
}

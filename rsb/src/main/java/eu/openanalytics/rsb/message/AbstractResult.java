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

package eu.openanalytics.rsb.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;


/**
 * Represents the result of a {@link AbstractJob}.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractResult<T> extends AbstractWorkItem implements Result<T> {
	
	private static final long serialVersionUID= 1L;
	
	
	private final boolean success;
	
	
	public AbstractResult(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final Map<String, Serializable> meta, final boolean success) {
		super(source, applicationName, userName, jobId, submissionTime, meta);
		this.success= success;
	}
	
	
	@Override
	public boolean isSuccess() {
		return this.success;
	}
	
	@Override
	public abstract T getPayload() throws IOException;
	
}

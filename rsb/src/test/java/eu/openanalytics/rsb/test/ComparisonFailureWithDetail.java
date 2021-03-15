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

package eu.openanalytics.rsb.test;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import junit.framework.ComparisonFailure;


@NonNullByDefault
public class ComparisonFailureWithDetail extends ComparisonFailure {
	
	
	private static final long serialVersionUID= 1L;
	
	
	private final @Nullable String detail;
	
	
	public ComparisonFailureWithDetail(final String message,
			final @Nullable String expected, final @Nullable String actual,
			final @Nullable String detail) {
		super(message, expected, actual);
		this.detail= detail;
	}
	
	
	@Override
	public String getMessage() {
		final String message= super.getMessage();
		final String detail= this.detail;
		if (detail != null) {
			return message + "\n=== error detail: ===\n" + detail;
		}
		return message;
	}
	
}

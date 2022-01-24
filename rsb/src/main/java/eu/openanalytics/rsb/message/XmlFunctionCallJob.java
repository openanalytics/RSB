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

import java.util.GregorianCalendar;
import java.util.UUID;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.Util;


/**
 * Represents a RSB job that consists in calling the RSBXmlService function on R.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class XmlFunctionCallJob extends AbstractFunctionCallJob {
	
	private static final long serialVersionUID= 1L;
	
	
	public XmlFunctionCallJob(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final String argument) {
		super(source, applicationName, userName, jobId, submissionTime, argument);
	}
	
	
	@Override
	public String getFunctionName() {
		return "RSBXmlService";
	}
	
	
	@Override
	public XmlFunctionCallResult buildSuccessResult(final String result) {
		// R response is JSON already, no conversion needed
		return buildResult(true, result);
	}
	
	@Override
	public XmlFunctionCallResult buildErrorResult(final Throwable error, final MessageSource messageSource) {
		return buildResult(false, Util.toXml(AbstractJob.buildJobProcessingErrorResult(this, error)));
	}
	
	private XmlFunctionCallResult buildResult(final boolean success, final String result) {
		return new XmlFunctionCallResult(getSource(), getApplicationName(),
				getUserName(), getJobId(), getSubmissionTime(),
				success, result );
	}
	
}

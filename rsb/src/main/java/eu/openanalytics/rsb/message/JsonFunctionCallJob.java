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

package eu.openanalytics.rsb.message;

import java.util.GregorianCalendar;
import java.util.UUID;

import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.Util;


/**
 * Represents a RSB job that consists in calling the RSBJsonService function on R.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class JsonFunctionCallJob extends AbstractFunctionCallJob
{
    private static final long serialVersionUID = 1L;

    public JsonFunctionCallJob(final Source source,
                               final String applicationName,
                               final String userName,
                               final UUID jobId,
                               final GregorianCalendar submissionTime,
                               final String argument)
    {
        super(source, applicationName, userName, jobId, submissionTime, argument);
    }

    @Override
    public String getFunctionName()
    {
        return "RSBJsonService";
    }

    @Override
    public JsonFunctionCallResult buildSuccessResult(final String result)
    {
        // R response is JSON already, no conversion needed
        return buildResult(true, result);
    }

    @Override
    public JsonFunctionCallResult buildErrorResult(final Throwable error, final MessageSource messageSource)
    {
        return buildResult(false, Util.toJson(AbstractJob.buildJobProcessingErrorResult(this, error)));
    }

    private JsonFunctionCallResult buildResult(final boolean success, final String result)
    {
        return new JsonFunctionCallResult(getSource(), getApplicationName(), getUserName(), getJobId(),
            getSubmissionTime(), success, result);
    }
}

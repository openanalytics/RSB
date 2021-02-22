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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

import javax.activation.MimeType;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.data.PersistedResult;
import eu.openanalytics.rsb.data.SecureResultStore;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * Processes results of jobs that have submitted over the REST API.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("restResultProcessor")
public class RestResultProcessor extends AbstractComponent
{
    @Resource
    private SecureResultStore resultStore;

    // exposed for testing
    void setResultStore(final SecureResultStore resultStore)
    {
        this.resultStore = resultStore;
    }

    public void process(final AbstractFunctionCallResult result) throws IOException
    {
        persistResult(result, result.getMimeType(), new ByteArrayInputStream(result.getPayload().getBytes()));
    }

    public void process(final MultiFilesResult result) throws IOException
    {
        final File resultFile = MultiFilesResult.zipResultFilesIfNotError(result);
        persistResult(result, Util.getMimeType(resultFile), new FileInputStream(resultFile));
    }

    private <T> void persistResult(final AbstractResult<?> result,
                                   final MimeType resultMimeType,
                                   final InputStream resultData) throws IOException
    {

        final GregorianCalendar resultTime = (GregorianCalendar) GregorianCalendar.getInstance();

        final PersistedResult persistedResult = new PersistedResult(result.getApplicationName(),
            result.getUserName(), result.getJobId(), resultTime, result.isSuccess(), resultMimeType)
        {

            @Override
            public long getDataLength() throws IOException
            {
                return resultData.available();
            }

            @Override
            public InputStream getData()
            {
                return resultData;
            }
        };

        resultStore.store(persistedResult);
        result.destroy();
    }
}

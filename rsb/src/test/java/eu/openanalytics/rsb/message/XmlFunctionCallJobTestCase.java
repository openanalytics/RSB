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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.message.AbstractWorkItem.Source;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class XmlFunctionCallJobTestCase
{

    private XmlFunctionCallJob xmlFunctionCallJob;

    @Before
    public void prepareTest()
    {
        xmlFunctionCallJob = new XmlFunctionCallJob(Source.REST, "app_name", null, UUID.randomUUID(),
            (GregorianCalendar) GregorianCalendar.getInstance(), "<fake_job/>");
    }

    @Test
    public void getFunctionName()
    {
        assertThat(xmlFunctionCallJob.getFunctionName(), notNullValue());
    }

    @Test
    public void buildSuccessResultString()
    {
        final XmlFunctionCallResult xmlFunctionCallResult = xmlFunctionCallJob.buildSuccessResult("<fake_result/>");
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(true));
    }

    @Test
    public void buildErrorResultThrowable() throws Exception
    {
        final XmlFunctionCallResult xmlFunctionCallResult = xmlFunctionCallJob.buildErrorResult(
            new RuntimeException("simulated error"), null);
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(false));
        assertXpathEvaluatesTo("errorResult", "name(/node())", xmlFunctionCallResult.getPayload());
    }
}

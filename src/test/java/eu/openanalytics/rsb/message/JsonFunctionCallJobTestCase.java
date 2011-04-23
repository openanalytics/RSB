/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openanalytics.rsb.message;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.Util;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class JsonFunctionCallJobTestCase {

    private JsonFunctionCallJob jsonFunctionCallJob;

    @Before
    public void prepareTest() {
        jsonFunctionCallJob = new JsonFunctionCallJob("app_name", UUID.randomUUID(), (GregorianCalendar) GregorianCalendar.getInstance(),
                "\\\"fake_job\\\"");
    }

    @Test
    public void getFunctionName() {
        assertThat(jsonFunctionCallJob.getFunctionName(), notNullValue());
    }

    @Test
    public void buildSuccessResultString() {
        final JsonFunctionCallResult xmlFunctionCallResult = jsonFunctionCallJob.buildSuccessResult("\\\"fake_result\\\"");
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(true));
    }

    @Test
    public void buildErrorResultThrowable() throws Exception {
        final JsonFunctionCallResult xmlFunctionCallResult = jsonFunctionCallJob.buildErrorResult(new RuntimeException("simulated error"));
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(false));
        assertThat(Util.fromJson(xmlFunctionCallResult.getPayload(), Map.class), notNullValue());
    }
}

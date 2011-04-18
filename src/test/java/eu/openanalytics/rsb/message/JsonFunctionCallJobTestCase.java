package eu.openanalytics.rsb.message;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.Util;

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
        assertThat(Util.fromJson(xmlFunctionCallResult.getResult()), notNullValue());
    }
}

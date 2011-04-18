package eu.openanalytics.rsb.message;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class XmlFunctionCallJobTestCase {

    private XmlFunctionCallJob xmlFunctionCallJob;

    @Before
    public void prepareTest() {
        xmlFunctionCallJob = new XmlFunctionCallJob("app_name", UUID.randomUUID(), (GregorianCalendar) GregorianCalendar.getInstance(),
                "<fake_job/>");
    }

    @Test
    public void getFunctionName() {
        assertThat(xmlFunctionCallJob.getFunctionName(), notNullValue());
    }

    @Test
    public void buildSuccessResultString() {
        final XmlFunctionCallResult xmlFunctionCallResult = xmlFunctionCallJob.buildSuccessResult("<fake_result/>");
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(true));
    }

    @Test
    public void buildErrorResultThrowable() throws Exception {
        final XmlFunctionCallResult xmlFunctionCallResult = xmlFunctionCallJob.buildErrorResult(new RuntimeException("simulated error"));
        assertThat(xmlFunctionCallResult, notNullValue());
        assertThat(xmlFunctionCallResult.isSuccess(), is(false));
        assertXpathEvaluatesTo("errorResult", "name(/node())", xmlFunctionCallResult.getResult());
    }
}

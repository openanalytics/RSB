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

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.JsonFunctionCallJob;
import eu.openanalytics.rsb.message.JsonFunctionCallResult;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.message.XmlFunctionCallJob;
import eu.openanalytics.rsb.message.XmlFunctionCallResult;
import eu.openanalytics.rsb.soap.types.JobType;
import eu.openanalytics.rsb.soap.types.JobType.Parameter;
import eu.openanalytics.rsb.soap.types.PayloadType;
import eu.openanalytics.rsb.soap.types.ResultType;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class SoapMtomJobHandlerTestCase {
    private static final String TEST_APP_NAME = "appName";

    private SoapMtomJobHandler soapMtomJobHandler;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Before
    public void prepareTest() {
        final Configuration configuration = mock(Configuration.class);
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        when(configuration.getResultsDirectory()).thenReturn(tempDir);

        soapMtomJobHandler = new SoapMtomJobHandler();
        soapMtomJobHandler.setConfiguration(configuration);
        soapMtomJobHandler.setMessageDispatcher(messageDispatcher);
    }

    @Test
    public void processXmlFunctionCall() {
        final JobType job = Util.SOAP_OBJECT_FACTORY.createJobType();
        job.setApplicationName(TEST_APP_NAME);
        final PayloadType xmlFunctionCallPayload = Util.SOAP_OBJECT_FACTORY.createPayloadType();
        xmlFunctionCallPayload.setContentType(Constants.XML_CONTENT_TYPE);
        xmlFunctionCallPayload.setData(new DataHandler(new ByteArrayDataSource("<fake_job/>".getBytes(), xmlFunctionCallPayload
                .getContentType())));
        job.getPayload().add(xmlFunctionCallPayload);
        final XmlFunctionCallResult result = mock(XmlFunctionCallResult.class);
        when(result.getMimeType()).thenReturn(Constants.XML_MIME_TYPE);
        when(result.getPayload()).thenReturn("<fake_result/>");
        when(result.getJobId()).thenReturn(UUID.randomUUID());
        when(messageDispatcher.process(any(XmlFunctionCallJob.class))).thenAnswer(new Answer<AbstractResult<?>>() {
            public AbstractResult<?> answer(final InvocationOnMock invocation) throws Throwable {
                return result;
            }
        });

        final ResultType processResult = soapMtomJobHandler.process(job);

        assertThat(processResult.getPayload().size(), is(1));
    }

    @Test
    public void processJsonFunctionCall() {
        final JobType job = Util.SOAP_OBJECT_FACTORY.createJobType();
        job.setApplicationName(TEST_APP_NAME);
        final PayloadType jsonFunctionCallPayload = Util.SOAP_OBJECT_FACTORY.createPayloadType();
        jsonFunctionCallPayload.setContentType(Constants.JSON_CONTENT_TYPE);
        jsonFunctionCallPayload.setData(new DataHandler(new ByteArrayDataSource("\"fake_job\"".getBytes(), jsonFunctionCallPayload
                .getContentType())));
        job.getPayload().add(jsonFunctionCallPayload);
        final JsonFunctionCallResult result = mock(JsonFunctionCallResult.class);
        when(result.getMimeType()).thenReturn(Constants.JSON_MIME_TYPE);
        when(result.getPayload()).thenReturn("\"fake_result\"");
        when(result.getJobId()).thenReturn(UUID.randomUUID());
        when(messageDispatcher.process(any(JsonFunctionCallJob.class))).thenAnswer(new Answer<AbstractResult<?>>() {
            public AbstractResult<?> answer(final InvocationOnMock invocation) throws Throwable {
                return result;
            }
        });

        final ResultType processResult = soapMtomJobHandler.process(job);

        assertThat(processResult.getPayload().size(), is(1));
    }

    @Test
    public void processMultiFilesJobCall() throws IOException {
        final JobType job = Util.SOAP_OBJECT_FACTORY.createJobType();
        job.setApplicationName(TEST_APP_NAME);
        final Parameter meta = Util.SOAP_OBJECT_FACTORY.createJobTypeParameter();
        meta.setName("rScript");
        meta.setValue("test.R");
        job.getParameter().add(meta);
        // final PayloadType jsonFunctionCallPayload = Util.SOAP_OBJECT_FACTORY.createPayloadType();
        // jsonFunctionCallPayload.setContentType("application/octet-stream");
        // jsonFunctionCallPayload.setData(new DataHandler(new
        // ByteArrayDataSource("\"fake_job\"".getBytes(), jsonFunctionCallPayload
        // .getContentType())));
        // job.getPayload().add(jsonFunctionCallPayload);
        final MultiFilesResult result = mock(MultiFilesResult.class);
        when(result.getPayload()).thenReturn(new File[0]);
        when(result.getJobId()).thenReturn(UUID.randomUUID());
        when(messageDispatcher.process(any(MultiFilesJob.class))).thenAnswer(new Answer<AbstractResult<?>>() {
            public AbstractResult<?> answer(final InvocationOnMock invocation) throws Throwable {
                return result;
            }
        });

        final ResultType processResult = soapMtomJobHandler.process(job);

        assertThat(processResult.getPayload().size(), is(0));
    }
}

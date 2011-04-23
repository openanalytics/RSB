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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class RestResultProcessorTestCase {

    private RestResultProcessor restResultProcessor;

    @Mock
    private MessageChannel resultFilesChannel;
    @Mock
    private AbstractFunctionCallResult functionCallResult;

    @Before
    public void prepareTest() {
        final Configuration configuration = mock(Configuration.class);
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        when(configuration.getRsbResultsDirectory()).thenReturn(tempDir);

        restResultProcessor = new RestResultProcessor();
        restResultProcessor.setResultFilesChannel(resultFilesChannel);
        restResultProcessor.setConfiguration(configuration);

        when(functionCallResult.getApplicationName()).thenReturn("test_app_name");
        when(functionCallResult.getMimeType()).thenReturn(Constants.XML_MIME_TYPE);
        when(functionCallResult.getPayload()).thenReturn("<fake />");
    }

    @Test
    public void processSuccess() throws IOException {
        when(functionCallResult.isSuccess()).thenReturn(true);

        restResultProcessor.process(functionCallResult);

        verify(resultFilesChannel).send(any(Message.class), anyInt());
        verify(functionCallResult).destroy();
    }

    @Test
    public void processFailure() throws IOException {
        when(functionCallResult.isSuccess()).thenReturn(true);

        restResultProcessor.process(functionCallResult);

        verify(resultFilesChannel).send(any(Message.class), anyInt());
        verify(functionCallResult).destroy();
    }
}

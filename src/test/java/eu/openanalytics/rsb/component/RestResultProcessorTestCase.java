/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.data.PersistedResult;
import eu.openanalytics.rsb.data.SecureResultStore;
import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class RestResultProcessorTestCase
{

    private RestResultProcessor restResultProcessor;

    @Mock
    private SecureResultStore resultStore;

    @Before
    public void prepareTest()
    {
        restResultProcessor = new RestResultProcessor();
        restResultProcessor.setResultStore(resultStore);
    }

    @Test
    public void processFunctionCallResultSuccess() throws IOException
    {
        final AbstractFunctionCallResult functionCallResult = buildMockFunctionCallResult();
        when(functionCallResult.isSuccess()).thenReturn(true);

        restResultProcessor.process(functionCallResult);

        verify(resultStore).store(any(PersistedResult.class));
        verify(functionCallResult).destroy();
    }

    @Test
    public void processFunctionCallResultFailure() throws IOException
    {
        final AbstractFunctionCallResult functionCallResult = buildMockFunctionCallResult();
        when(functionCallResult.isSuccess()).thenReturn(false);

        restResultProcessor.process(functionCallResult);

        verify(resultStore).store(any(PersistedResult.class));
        verify(functionCallResult).destroy();
    }

    @Test
    public void processMultiFilesResultSuccess() throws IOException
    {
        final MultiFilesResult multiFilesResult = buildMockMultiFilesCallResult();
        when(multiFilesResult.isSuccess()).thenReturn(true);

        restResultProcessor.process(multiFilesResult);

        verify(resultStore).store(any(PersistedResult.class));
        verify(multiFilesResult).destroy();
    }

    @Test
    public void processMultiFilesResultFailure() throws IOException
    {
        final MultiFilesResult multiFilesResult = buildMockMultiFilesCallResult();
        when(multiFilesResult.isSuccess()).thenReturn(false);

        restResultProcessor.process(multiFilesResult);

        verify(resultStore).store(any(PersistedResult.class));
        verify(multiFilesResult).destroy();
    }

    private MultiFilesResult buildMockMultiFilesCallResult() throws IOException
    {
        final MultiFilesResult multiFilesResult = mock(MultiFilesResult.class);
        when(multiFilesResult.getApplicationName()).thenReturn("test_app_name");
        when(multiFilesResult.getPayload()).thenReturn(new File[0]);
        when(multiFilesResult.getTemporaryDirectory()).thenReturn(FileUtils.getTempDirectory());
        return multiFilesResult;
    }

    private AbstractFunctionCallResult buildMockFunctionCallResult()
    {
        final AbstractFunctionCallResult functionCallResult = mock(AbstractFunctionCallResult.class);
        when(functionCallResult.getApplicationName()).thenReturn("test_app_name");
        when(functionCallResult.getMimeType()).thenReturn(Constants.XML_MIME_TYPE);
        when(functionCallResult.getPayload()).thenReturn("<fake />");
        return functionCallResult;
    }
}

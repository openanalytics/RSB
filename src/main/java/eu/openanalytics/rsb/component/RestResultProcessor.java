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

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.message.GenericMessage;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.message.AbstractFunctionCallResult;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * Processes results of jobs that have submitted over the REST API.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("restResultProcessor")
public class RestResultProcessor extends AbstractComponent {
    @Resource(name = "resultFilesChannel")
    private MessageChannel resultFilesChannel;

    // exposed for unit tests
    void setResultFilesChannel(final MessageChannel resultFilesChannel) {
        this.resultFilesChannel = resultFilesChannel;
    }

    public void process(final AbstractFunctionCallResult result) throws IOException {
        ensureApplicationResultDir(result);
        final String resultFileName = getResultFileName(result);
        final Message<String> message = newResultMessage(result, resultFileName);
        resultFilesChannel.send(message, getConfiguration().getJobTimeOut());
        result.destroy();
    }

    public void process(final MultiFilesResult result) throws IOException {
        ensureApplicationResultDir(result);
        final String resultFileName = getResultFileName(result);
        final Message<File> message = newResultMessage(result, resultFileName);
        resultFilesChannel.send(message, getConfiguration().getJobTimeOut());
        result.destroy();
    }

    private <T> Message<T> newResultMessage(final AbstractResult<T> result, final String fileName) throws IOException {
        final String applicationAndResultFileName = result.getApplicationName() + File.separator + fileName;
        return new GenericMessage<T>(result.getPayload(), Collections.singletonMap(FileHeaders.FILENAME,
                (Object) applicationAndResultFileName));
    }

    private void ensureApplicationResultDir(final AbstractResult<?> result) throws IOException {
        final File applicationResultDir = new File(getConfiguration().getResultsDirectory(), result.getApplicationName());
        FileUtils.forceMkdir(applicationResultDir);
    }

    private String getResultFileName(final AbstractFunctionCallResult functionCallResult) {
        final String resultFileExtension = (functionCallResult.isSuccess() ? "" : "err.") + functionCallResult.getMimeType().getSubType();
        return functionCallResult.getJobId() + "." + resultFileExtension;
    }

    private String getResultFileName(final MultiFilesResult multiFilesResult) throws IOException {
        return multiFilesResult.getPayload().getName();
    }
}

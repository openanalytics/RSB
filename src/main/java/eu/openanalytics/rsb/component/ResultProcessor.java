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

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("resultProcessor")
public class ResultProcessor extends AbstractComponent {
    @Resource(name = "resultFilesChannel")
    private MessageChannel resultFilesChannel;

    // exposed for unit tests
    void setResultFilesChannel(final MessageChannel resultFilesChannel) {
        this.resultFilesChannel = resultFilesChannel;
    }

    public void process(final AbstractFunctionCallResult functionCallResult) throws IOException {
        final File applicationResultDir = new File(getConfiguration().getRsbResultsDirectory(), functionCallResult.getApplicationName());
        FileUtils.forceMkdir(applicationResultDir);

        final String resultFileName = getResultFileName(functionCallResult);

        final Message<String> message = new GenericMessage<String>(getResultPayload(functionCallResult), Collections.singletonMap(
                FileHeaders.FILENAME, (Object) resultFileName));

        resultFilesChannel.send(message, getConfiguration().getJobTimeOut());

        functionCallResult.destroy();
    }

    private String getResultFileName(final AbstractFunctionCallResult functionCallResult) {
        return functionCallResult.getApplicationName() + File.separator + functionCallResult.getJobId() + "."
                + getResultFileExtension(functionCallResult);
    }

    private String getResultFileExtension(final AbstractFunctionCallResult result) {
        // prepend error results extension with "err."
        return (result.isSuccess() ? "" : "err.") + result.getMimeType().getSubType();
    }

    private String getResultPayload(final AbstractFunctionCallResult functionCallResult) {
        return functionCallResult.getResult();
    }
}

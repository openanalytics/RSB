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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * @author rsb.development@openanalytics.eu
 */
@Component
@RequestMapping(value = "/result")
public class ResultFileServingComponent extends AbstractConfigurable {
    @RequestMapping(value = "/{applicationName}/{resultFile:.+}", method = RequestMethod.GET)
    public void getResult(@PathVariable final String applicationName, @PathVariable final String resultFile,
            final OutputStream responseStream) throws FileNotFoundException, IOException {
        FileCopyUtils.copy(
                new FileInputStream(new File(new File(getConfiguration().getRsbResultsDirectory(), applicationName), resultFile)),
                responseStream);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public void handleFileNotFoundException(final FileNotFoundException fnf, final WebRequest request, final HttpServletResponse response)
            throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), fnf.getMessage());
    }
}

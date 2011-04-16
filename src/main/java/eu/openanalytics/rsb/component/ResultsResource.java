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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.Result;
import eu.openanalytics.rsb.rest.types.Results;

/**
 * Exposes results meta-information, allowing their deletion too.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
@Component("resultsResource")
@Produces({ Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE })
@Path("/results/{applicationName}")
public class ResultsResource extends AbstractComponent {
    // TODO unit test
    // FIXME support GET one result
    // FIXME support DELETE one result

    @GET
    public Results getAllResults(@PathParam("applicationName") final String applicationName) {
        if (!Util.isValidApplicationName(applicationName)) {
            Util.throwCustomBadRequestException("Invalid application name: " + applicationName);
        }

        final Results results = Util.REST_OBJECT_FACTORY.createResults();

        for (final File resultFile : getApplicationResultDirectory(applicationName).listFiles()) {
            final String fileName = resultFile.getName();

            final Result result = Util.REST_OBJECT_FACTORY.createResult();
            result.setApplicationName(applicationName);
            result.setJobId(StringUtils.substringBefore(fileName, "."));
            // FIXME add URIs
            // result.setApplicationResultsUri(value);
            // result.setDataUri(value);
            // result.setSelfUri(value);
            result.setSuccess(!StringUtils.contains(fileName, ".err."));
            result.setType(FilenameUtils.getExtension(fileName));

            results.getResults().add(result);
        }

        return results;
    }
}

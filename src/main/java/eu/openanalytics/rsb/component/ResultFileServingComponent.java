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
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * @author rsb.development@openanalytics.eu
 */
@Component("resultFileServer")
@Path("/result")
public class ResultFileServingComponent extends AbstractConfigurable {
    @GET
    @Path("/{applicationName}/{result}")
    public StreamingOutput getResult(@PathParam("applicationName") final String applicationName, @PathParam("result") final String result) {

        final File resultFile = new File(new File(getConfiguration().getRsbResultsDirectory(), applicationName), result);

        if (!resultFile.exists()) {
            throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
        }

        return new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                FileCopyUtils.copy(new FileInputStream(resultFile), output);
            }
        };
    }
}

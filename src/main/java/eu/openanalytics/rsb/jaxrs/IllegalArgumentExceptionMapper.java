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

package eu.openanalytics.rsb.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;

import eu.openanalytics.rsb.Constants;

/**
 * Converts {@link IllegalArgumentException} into BAD_REQUEST HTTP responses.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    private final static class BadRequestStatus implements StatusType {
        private final String reasonPhrase;

        private BadRequestStatus(final String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public int getStatusCode() {
            return Status.BAD_REQUEST.getStatusCode();
        }

        public Family getFamily() {
            return Status.BAD_REQUEST.getFamily();
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }
    };

    @Override
    public Response toResponse(final IllegalArgumentException iae) {
        final BadRequestStatus status = new BadRequestStatus("Bad request - " + iae.getMessage());
        // JAX-RS doesn't seem to propagate the reason phrase to the ultimate HTTP response, hence
        // add it to a custom header too
        return Response.status(status).header(Constants.REASON_PHRASE_HEADER, status.getReasonPhrase()).build();
    }
}

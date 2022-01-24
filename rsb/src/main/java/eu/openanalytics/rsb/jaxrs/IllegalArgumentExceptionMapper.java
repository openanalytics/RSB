/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb.jaxrs;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;


/**
 * Converts {@link IllegalArgumentException} into BAD_REQUEST HTTP responses.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException>
{
    private final static class BadRequestStatus implements StatusType
    {
        private final String reasonPhrase;

        private BadRequestStatus(final String reasonPhrase)
        {
            this.reasonPhrase = reasonPhrase;
        }

		@Override
		public int getStatusCode()
        {
            return Status.BAD_REQUEST.getStatusCode();
        }

		@Override
		public Family getFamily()
        {
            return Status.BAD_REQUEST.getFamily();
        }

		@Override
		public String getReasonPhrase()
        {
            return reasonPhrase;
        }
    }

    @Override
    public Response toResponse(final IllegalArgumentException iae)
    {
        final BadRequestStatus status = new BadRequestStatus("Bad request - " + iae.getMessage());
        // JAX-RS doesn't seem to propagate the reason phrase to the ultimate HTTP
        // response status
        // line, hence add it to the response body too
        return Response.status(status).type(MediaType.TEXT_PLAIN).entity(status.getReasonPhrase()).build();
    }
}

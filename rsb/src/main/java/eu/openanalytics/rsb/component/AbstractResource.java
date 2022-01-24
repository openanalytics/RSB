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

package eu.openanalytics.rsb.component;

import java.security.Principal;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import eu.openanalytics.rsb.security.ApplicationPermissionEvaluator;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractResource extends AbstractComponentWithCatalog {
	
	
	@Context
	private @Nullable SecurityContext securityContext;
	
	
	// exposed for unit testing
	void setSecurityContext(final SecurityContext securityContext) {
		this.securityContext= securityContext;
	}
	
	
	protected String getUserName() {
		final var securityContext= this.securityContext;
		final Principal user;
		return (securityContext != null && (user= securityContext.getUserPrincipal()) != null) ?
				user.getName() :
				ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME;
	}
	
}

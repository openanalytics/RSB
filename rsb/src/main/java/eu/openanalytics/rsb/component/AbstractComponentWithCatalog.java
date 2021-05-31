/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
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

import java.nio.file.Path;

import javax.annotation.Resource;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.data.CatalogManager;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractComponentWithCatalog extends AbstractComponent {
	
	
	@Resource
	private CatalogManager catalogManager;
	
	
	protected AbstractComponentWithCatalog() {
	}
	
	
	public void setCatalogManager(final CatalogManager catalogManager) {
		this.catalogManager= catalogManager;
	}
	
	protected CatalogManager getCatalogManager() {
		return this.catalogManager;
	}
	
	protected Path getJobConfigurationFile(final @Nullable String applicationName,
			final String fileName) {
		return getCatalogManager().internalGetCatalogFile(CatalogSection.JOB_CONFIGURATIONS,
				applicationName, fileName );
	}
	
}

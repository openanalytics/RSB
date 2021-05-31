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

package eu.openanalytics.rsb.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import eu.openanalytics.rsb.config.Configuration.CatalogSection;


/**
 * The RSB catalog. The <code>applicationName</code> argument are mandatory if the catalog runs in
 * application-aware mode.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public interface CatalogManager {
	
	
	Map<CatalogSection, Pair<Path, List<Path>>> getCatalog(
			@Nullable String applicationName);
	
	
	Path getCatalogFile(CatalogSection catalogSection,
			@Nullable String applicationName,
			String fileName);
	
	/**
	 * This must only be called when it's impossible to have a security context (ie after going
	 * through JMS)
	 */
	Path internalGetCatalogFile(CatalogSection catalogSection,
			@Nullable String applicationName,
			String fileName);
	
	static final class PutCatalogFileResult {
		
		public static enum ChangeType {
			CREATED, UPDATED;
		}
		
		private final ChangeType changeType;
		
		private final Path path;
		
		public PutCatalogFileResult(final ChangeType changeType, final Path path) {
			this.changeType= changeType;
			this.path= path;
		}
		
		public ChangeType getChangeType() {
			return this.changeType;
		}
		
		public Path getPath() {
			return this.path;
		}
		
	}
	
	PutCatalogFileResult putCatalogFile(CatalogSection catalogSection,
			@Nullable String applicationName,
			String fileName, InputStream in) throws IOException;
	
}

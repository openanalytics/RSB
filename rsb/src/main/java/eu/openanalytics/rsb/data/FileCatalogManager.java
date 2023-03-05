/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.component.AbstractComponent;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;


/**
 * A file-based optionally-aware file catalog.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component
@NonNullByDefault
public class FileCatalogManager extends AbstractComponent implements CatalogManager {
	
	
	@PostConstruct
	public void createCatalogTree() throws IOException {
		final Set<String> applicationNames= (getConfiguration().isApplicationAwareCatalog()) ?
				collectionAllApplicationNames() : Set.of("ignored");
		for (final String applicationName : applicationNames) {
			for (final CatalogSection catalogSection : CatalogSection.values()) {
				Files.createDirectories(getCatalogSectionDirectory(catalogSection, applicationName));
			}
		}
	}
	
	private Set<String> collectionAllApplicationNames() {
		final Set<String> applicationNames= new HashSet<>();
		
		if (getConfiguration().getApplicationSecurityConfiguration() != null) {
			applicationNames.addAll(getConfiguration().getApplicationSecurityConfiguration().keySet());
		}
		
		if (getConfiguration().getDepositRootDirectories() != null) {
			for (final DepositDirectoryConfiguration ddc : getConfiguration().getDepositRootDirectories()) {
				applicationNames.add(ddc.getApplicationName());
			}
		}
		
		if (getConfiguration().getDepositEmailAccounts() != null) {
			for (final DepositEmailConfiguration dec : getConfiguration().getDepositEmailAccounts()) {
				applicationNames.add(dec.getApplicationName());
			}
		}
		
		if (getConfiguration().getApplicationSecurityConfiguration() != null) {
			applicationNames.addAll(getConfiguration().getApplicationSecurityConfiguration().keySet());
		}
		
		return applicationNames;
	}
	
	
	@PreAuthorize("hasPermission(#applicationName, 'CATALOG_USER')")
	@Override
	public Map<CatalogSection, Pair<Path, List<Path>>> getCatalog(
			final @Nullable String applicationName) {
		final Map<CatalogSection, Pair<Path, List<Path>>> catalog= new HashMap<>();
		
		for (final CatalogSection catalogSection : CatalogSection.values()) {
			final Path catalogSectionDirectory= getCatalogSectionDirectory(catalogSection,
					applicationName );
			
			final var files= new ArrayList<Path>();
			try (final var directoryStream= Files.newDirectoryStream(catalogSectionDirectory,
					Files::isRegularFile )) {
				for (final var path : directoryStream) {
					files.add(path);
				}
			}
			catch (final IOException e) {
				getLogger().error(String.format("Failed to load entries for catalog section %1$s.",
								catalogSection ),
						e );
				files.clear();
			}
			
			catalog.put(catalogSection, Pair.of(catalogSectionDirectory, files));
		}
		
		return catalog;
	}
	
	@PreAuthorize("hasPermission(#applicationName, 'CATALOG_USER')")
	@Override
	public Path getCatalogFile(final CatalogSection catalogSection,
			final @Nullable String applicationName,
			final String fileName) {
		return internalGetCatalogFile(catalogSection, applicationName, fileName);
	}
	
	@Override
	public Path internalGetCatalogFile(final CatalogSection catalogSection,
			final @Nullable String applicationName,
			final String fileName) {
		final Path catalogSectionDirectory= getCatalogSectionDirectory(catalogSection,
				applicationName );
		return catalogSectionDirectory.resolve(fileName);
	}
	
	@PreAuthorize("hasPermission(#applicationName, 'CATALOG_ADMIN')")
	@Override
	public PutCatalogFileResult putCatalogFile(final CatalogSection catalogSection,
			final @Nullable String applicationName,
			final String fileName, final InputStream in) throws IOException {
		final Path catalogSectionDirectory= getCatalogSectionDirectory(catalogSection, applicationName);
		
		final var catalogFile= catalogSectionDirectory.resolve(fileName);
		
		final PutCatalogFileResult.ChangeType changeType;
		if (Files.isRegularFile(catalogFile)) {
			Files.copy(in, catalogFile, StandardCopyOption.REPLACE_EXISTING);
			changeType= PutCatalogFileResult.ChangeType.UPDATED;
		}
		else {
			Files.copy(in, catalogFile);
			changeType= PutCatalogFileResult.ChangeType.CREATED;
		}
		
		getLogger().info(StringUtils.capitalize(changeType.toString().toLowerCase()) 
				+ " " + fileName
				+ " in catalog section " + catalogSection + " as file: " + catalogFile );
		
		return new PutCatalogFileResult(changeType, catalogFile);
	}
	
	private Path getCatalogSectionDirectory(final CatalogSection catalogSection,
			final @Nullable String applicationName) {
		final Path actualCatalogRoot;
		
		if (getConfiguration().isApplicationAwareCatalog()) {
			if (applicationName == null || applicationName.isEmpty()) {
				throw new IllegalArgumentException("Failed to access the catalog because no application name has been provided but RSB is running with an application aware catalog.");
			}
			actualCatalogRoot= getConfiguration().getCatalogRootDirectory().toPath()
					.resolve(applicationName);
		}
		else {
			actualCatalogRoot= getConfiguration().getCatalogRootDirectory().toPath();
		}
		
		return actualCatalogRoot.resolve(catalogSection.getSubDir());
	}
	
}

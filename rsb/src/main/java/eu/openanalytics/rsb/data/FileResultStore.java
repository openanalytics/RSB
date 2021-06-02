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

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.MimeType;

import org.eclipse.statet.jcommons.io.FileUtils;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.component.AbstractComponent;


/**
 * A file-based result store.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("secureResultStore")
@NonNullByDefault
public class FileResultStore extends AbstractComponent implements SecureResultStore {
	
	
	private static final String ERROR_MESSAGE= "This method shouldn't have been called: please report the issue.";
	private static final String ERROR_FILE_INFIX_EXTENSION= ".err";
	
	private static final Comparator<Path> PATH_ORDER_COMPARATOR= new Comparator<>() {
		@Override
		public int compare(final Path o1, final Path o2) {
			final int compare= FileUtils.getLastModifiedTimeComparator().compare(o1, o2);
			if (compare != 0) {
				return -compare;
			}
			return o1.compareTo(o2);
		}
	};
	
	
	@Override
	public void store(final PersistedResult result) throws IOException {
		final String resultFileName= result.getJobId().toString()
				+ (result.isSuccess() ? "" : ERROR_FILE_INFIX_EXTENSION) + "."
				+ Util.getResourceType(result.getMimeType());
		
		final var resultsDirectory= getResultsDirectory(result.getApplicationName(),
				result.getUserName() );
		Files.createDirectories(resultsDirectory);
		final var resultFile= resultsDirectory.resolve(resultFileName);
		
		try (final InputStream resultData= result.getData()) {
			Files.copy(resultData, resultFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	
	@Override
	public boolean deleteByApplicationNameAndJobId(final String applicationName, 
			final UUID jobId) throws IOException {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}
	
	@Override
	public Collection<PersistedResult> findByApplicationName(final String applicationName) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}
	
	@Override
	public PersistedResult findByApplicationNameAndJobId(final String applicationName,
			final UUID jobId) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}
	
	
	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public boolean deleteByApplicationNameAndJobId(final String applicationName,
			final @Nullable String userName, final UUID jobId)
			throws IOException {
		final var resultFile= getResultFile(applicationName, userName, jobId);
		
		if (resultFile == null) {
			return false;
		}
		
		FileUtils.deleteRecursively(resultFile);
		return true;
	}
	
	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public Collection<PersistedResult> findByApplicationName(final String applicationName,
			final @Nullable String userName) {
		try {
			final var resultsDirectory= getResultsDirectory(applicationName, userName);
			
			if (!Files.isDirectory(resultsDirectory)) {
				return Collections.emptyList();
			}
			
			final var files= new ArrayList<Path>();
			try (final var directoryStream= Files.newDirectoryStream(resultsDirectory)) {
				for (final Path path : directoryStream) {
					files.add(path);
				}
			}
			files.sort(PATH_ORDER_COMPARATOR);
			
			final Collection<PersistedResult> persistedResults= new ArrayList<>();
			for (final var resultFile : files) {
				final UUID jobId= Util.safeUuidFromString(
						StringUtils.substringBefore(requireFileName(resultFile).toString(), ".") );
				if (jobId != null) {
					persistedResults.add(
							buildPersistedResult(applicationName, userName, jobId, resultFile) );
				}
			}
			
			return persistedResults;
		}
		catch (final IOException e) {
			getLogger().warn(String.format("Failed to collect persisted results for '%1$s'.",
							applicationName ),
					e );
			return Collections.emptyList();
		}
	}
	
	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public @Nullable PersistedResult findByApplicationNameAndJobId(final String applicationName,
			final @Nullable String userName, final UUID jobId) {
		try {
			final Path resultFile= getResultFile(applicationName, userName, jobId);
			return (resultFile != null) ?
					buildPersistedResult(applicationName, userName, jobId, resultFile) :
					null;
		}
		catch (final IOException e) {
			getLogger().warn(String.format("Failed to collect persisted results for '%1$s'.",
							applicationName ),
					e );
			return null;
		}
	}
	
	
	private @Nullable Path getResultFile(final String applicationName,
			final @Nullable String userName, final UUID jobId)
			throws IOException {
		final var resultsDirectory= getResultsDirectory(applicationName, userName);
		
		if (!Files.isDirectory(resultsDirectory)) {
			return null;
		}
		
		final var files= new ArrayList<Path>();
		try (final var directoryStream= Files.newDirectoryStream(resultsDirectory,
				jobId.toString() + ".*" )) {
			for (final Path path : directoryStream) {
				files.add(path);
			}
		}
		
		if (files.isEmpty()) {
			return null;
		}
		if (files.size() > 1) {
			throw new IllegalStateException("Found " + files.size() + " results for job Id: " + jobId);
		}
		return files.get(0);
	}
	
	private PersistedResult buildPersistedResult(final String applicationName,
			final @Nullable String userName,final UUID jobId,
			final Path resultFile)
			throws IOException {
		final GregorianCalendar resultTime= (GregorianCalendar)GregorianCalendar.getInstance();
		
		resultTime.setTimeInMillis(Files.getLastModifiedTime(resultFile).toMillis());
		
		final boolean success= !StringUtils.contains(requireFileName(resultFile).toString(),
				ERROR_FILE_INFIX_EXTENSION + "." );
		final MimeType mimeType= Util.getMimeType(resultFile);
		
		return new PersistedResult(applicationName, userName, jobId, resultTime, success, mimeType) {
			
			private final long length= Files.size(resultFile);
			
			@Override
			public long getDataLength() {
				return this.length;
			}
			
			@Override
			public InputStream getData() {
				try {
					return Files.newInputStream(resultFile);
				}
				catch (final IOException e) {
					throw new IllegalStateException(e);
				}
			}
			
		};
	}
	
	private Path getResultsDirectory(final String applicationName,
			final @Nullable String userName) {
		// this is to prevent trying to use application names with / or \ in order to reach
		// disallowed directories
		Validate.isTrue(Util.isValidApplicationName(applicationName),
				"Invalid application name: " + applicationName );
		
		final var applicationResultsDirectory= getConfiguration().getResultsDirectory().toPath()
				.resolve(applicationName);
		
		return (userName != null && !userName.isEmpty()) ?
				applicationResultsDirectory.resolve(userName) :
				applicationResultsDirectory;
	}
	
}

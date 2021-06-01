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

package eu.openanalytics.rsb.message;

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.statet.jcommons.io.FileUtils;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;


/**
 * Represents a RSB result that consists of multiple files.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class MultiFilesResult extends AbstractResult<List<Path>> {
	
	private static final long serialVersionUID= 1L;
	
	
	private final File temporaryDirectory;
	
	
	public MultiFilesResult(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final Map<String, Serializable> meta, final boolean success)
			throws IOException {
		super(source, applicationName, userName, jobId, submissionTime, meta, success);
		this.temporaryDirectory= Files.createTempDirectory("rsb-result-").toFile();
	}
	
	
	@Override
	protected void releaseResources() {
		try {
			FileUtils.deleteRecursively(getTemporaryDirectory());
		}
		catch (final IOException e) {
			throw new RuntimeException("Can't release resources of: " + this, e);
		}
	}
	
	
	// exposed only for unit tests
	public Path getTemporaryDirectory() {
		return this.temporaryDirectory.toPath();
	}
	
	@Override
	public List<Path> getPayload() throws IOException {
		final var payload= new ArrayList<Path>();
		try (final var directoryStream= Files.newDirectoryStream(getTemporaryDirectory())) {
			for (final Path path : directoryStream) {
				payload.add(path);
			}
		}
		return payload;
	}
	
	public Path createNewResultFile(final String name) throws IOException {
		return getTemporaryDirectory().resolve(name);
	}
	
	
	/**
	 * Zips all the files contained in a multifiles result except if the result is
	 * not successful, in that case returns the first file (which should be the only
	 * one and contain a plain text error message).
	 * 
	 * @param result
	 * @return
	 * @throws IOException
	 */
	public static Path zipResultFilesIfNotError(final MultiFilesResult result)
			throws IOException {
		final var resultFiles= result.getPayload();
		
		if (!result.isSuccess() && resultFiles.size() == 1) {
			return resultFiles.get(0);
		}
		
		final var resultZipFile= result.getTemporaryDirectory().resolve(result.getJobId() + ".zip");
		try (	final var fileOut= Files.newOutputStream(resultZipFile);
				final ZipOutputStream resultZipOut= new ZipOutputStream(fileOut) ) {
			for (final var resultFile : resultFiles) {
				resultZipOut.putNextEntry(new ZipEntry(requireFileName(resultFile).toString()));
				Files.copy(resultFile, resultZipOut);
				resultZipOut.closeEntry();
			}
		}
		
		return resultZipFile;
	}
	
}

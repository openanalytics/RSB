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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.statet.jcommons.lang.NonNull;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.stringtemplate.v4.ST;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;


/**
 * Represents a RSB job that consists of multiple files.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class MultiFilesJob extends AbstractJob {
	
	private static final long serialVersionUID= 1L;
	
	
	private final File temporaryDirectory;
	
	private @Nullable File rScriptFile;
	
	
	public MultiFilesJob(final Source source, final String applicationName,
			final @Nullable String userName, final UUID jobId,
			final GregorianCalendar submissionTime,
			final Map<String, Serializable> meta)
			throws IOException {
		super(source, applicationName, userName, jobId, submissionTime, meta);
		this.temporaryDirectory= Util.createTemporaryDirectory("job");
	}
	
	@Override
	protected void releaseResources() {
		try {
			if (this.temporaryDirectory.isDirectory()) {
				FileUtils.forceDelete(this.temporaryDirectory);
			}
		}
		catch (final IOException e) {
			throw new RuntimeException("Can't release resources of: " + this, e);
		}
	}
	
	
	public @Nullable File getRScriptFile() {
		return this.rScriptFile;
	}
	
	public @NonNull File[] getFiles() {
		return this.temporaryDirectory.listFiles();
	}
	
	public void addFile(final String name, final InputStream is)
			throws IllegalJobDataException, IOException {
		if (Constants.MULTIPLE_FILES_JOB_CONFIGURATION.equals(name)) {
			loadJobConfiguration(is);
		}
		else {
			addJobFile(name, is);
		}
	}
	
	/**
	 * Adds all the files contained in a Zip archive to a job. Rejects Zips that contain
	 * sub-directories.
	 * 
	 * @param data input stream of zip file
	 */
	public void addFilesFromZip(final InputStream data)
			throws IllegalJobDataException, IOException {
		try (final ZipInputStream zis= new ZipInputStream(data)) {
			ZipEntry ze= null;
			
			while ((ze= zis.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					destroy();
					throw new IllegalJobDataException("Invalid zip archive: nested directories are not supported");
				}
				addFile(ze.getName(), zis);
				zis.closeEntry();
			}
		}
	}
	
	private void addJobFile(final String name, final InputStream is)
			throws IllegalJobDataException, FileNotFoundException, IOException {
		final File jobFile= new File(this.temporaryDirectory, name);
		
		if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(name), Constants.R_SCRIPT_FILE_EXTENSION)) {
			if (this.rScriptFile != null) {
				throw new IllegalJobDataException("Only one R script is allowed per job");
			}
			this.rScriptFile= jobFile;
		}
		
		try (final FileOutputStream fos= new FileOutputStream(jobFile)) {
			IOUtils.copy(is, fos);
		}
	}
	
	
	private void loadJobConfiguration(final InputStream is) throws IOException {
		final Properties jobConfiguration= new Properties();
		jobConfiguration.load(is);
		
		final Map<String, Serializable> mergedMeta= new HashMap<>();
		for (final Entry<?, ?> e : jobConfiguration.entrySet()) {
			mergedMeta.put(e.getKey().toString(), e.getValue().toString());
		}
		
		// give priority to pre-existing metas by overriding the ones from the config file
		mergedMeta.putAll(getMeta());
		getMeta().clear();
		getMeta().putAll(mergedMeta);
	}
	
	
	public MultiFilesResult buildSuccessResult() throws IOException {
		return new MultiFilesResult(getSource(), getApplicationName(),
				getUserName(), getJobId(), getSubmissionTime(),
				getMeta(), true );
	}
	
	@Override
	public MultiFilesResult buildErrorResult(final Throwable t, final MessageSource messageSource)
			throws IOException {
		final String message= messageSource.getMessage(getErrorMessageId(), null, null);
		final ST template= Util.newStringTemplate(message);
		template.add("job", this);
		template.add("throwable", t);
		
		final MultiFilesResult result= new MultiFilesResult(getSource(), getApplicationName(),
				getUserName(), getJobId(), getSubmissionTime(),
				getMeta(), false );
		final File resultFile= result.createNewResultFile(getJobId()
				+ "." + Util.getResourceType(Constants.TEXT_MIME_TYPE) );
		Files.writeString(resultFile.toPath(), template.render(), StandardCharsets.UTF_8);
		return result;
	}
	
	
	/**
	 * Add a stream to a job, exploding it if it is a Zip input. Closes the provided
	 * data stream.
	 * 
	 * @param contentType
	 * @param name
	 * @param data
	 * @param job
	 * @throws IOException
	 * @throws IllegalJobDataException 
	 */
	public static void addDataToJob(final String contentType,
			final String name,
			final InputStream data, final MultiFilesJob job)
			throws IOException, IllegalJobDataException {
		// some browsers send zip file as application/octet-stream, forcing a fallback to an
		// extension check
		if (Constants.ZIP_CONTENT_TYPES.contains(contentType)
				|| StringUtils.endsWithIgnoreCase(
						FilenameUtils.getExtension(name), Constants.ZIP_MIME_TYPE.getSubType()) ) {
			job.addFilesFromZip(data);
		}
		else {
			job.addFile(name, data);
		}
	}
	
}

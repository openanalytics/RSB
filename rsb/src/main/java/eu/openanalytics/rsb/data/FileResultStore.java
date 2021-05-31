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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
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
public class FileResultStore extends AbstractComponent implements SecureResultStore
{
    private static final String ERROR_MESSAGE = "This method shouldn't have been called: please report the issue.";
    private static final String ERROR_FILE_INFIX_EXTENSION = ".err";

	@Override
	public boolean deleteByApplicationNameAndJobId(final String applicationName, final UUID jobId)
        throws IOException
    {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

	@Override
	public Collection<PersistedResult> findByApplicationName(final String applicationName)
    {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

	@Override
	public PersistedResult findByApplicationNameAndJobId(final String applicationName, final UUID jobId)
    {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

	@Override
	public void store(final PersistedResult result) throws IOException
    {
        final String resultFileName = result.getJobId().toString()
                                      + (result.isSuccess() ? "" : ERROR_FILE_INFIX_EXTENSION) + "."
                                      + Util.getResourceType(result.getMimeType());

        final File resultsDirectory = getResultsDirectory(result.getApplicationName(), result.getUserName());
        FileUtils.forceMkdir(resultsDirectory);
        final File resultFile = new File(resultsDirectory, resultFileName);

        try(final InputStream resultData = result.getData(); final FileOutputStream fos = new FileOutputStream(resultFile)) {
          IOUtils.copy(resultData, fos);
        }
    }

	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public boolean deleteByApplicationNameAndJobId(final String applicationName,
                                                   final String userName,
                                                   final UUID jobId) throws IOException
    {
        final File resultFile = getResultFile(applicationName, userName, jobId);

        if (resultFile == null)
        {
            return false;
        }

        FileUtils.forceDelete(resultFile);
        return true;
    }

	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public Collection<PersistedResult> findByApplicationName(final String applicationName,
                                                             final String userName)
    {
        final File[] resultFiles = getResultsDirectory(applicationName, userName).listFiles();

        if ((resultFiles == null) || (resultFiles.length == 0))
        {
            return Collections.emptyList();
        }

        final Collection<PersistedResult> persistedResults = new ArrayList<>();

        final List<File> sortedFiles = new ArrayList<>(Arrays.asList(resultFiles));
        Collections.sort(sortedFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        for (final File resultFile : sortedFiles)
        {
            final UUID jobId = Util.safeUuidFromString(StringUtils.substringBefore(resultFile.getName(), "."));
            if (jobId != null)
            {
                persistedResults.add(buildPersistedResult(applicationName, userName, jobId, resultFile));
            }
        }

        return persistedResults;
    }

	@PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
	@Override
	public PersistedResult findByApplicationNameAndJobId(final String applicationName,
                                                         final String userName,
                                                         final UUID jobId)
    {
        final File resultFile = getResultFile(applicationName, userName, jobId);
        return resultFile == null ? null : buildPersistedResult(applicationName, userName, jobId, resultFile);
    }

    private File getResultFile(final String applicationName, final String userName, final UUID jobId)
    {
        final String jobIdAsString = jobId.toString();
        final File[] resultFiles = getResultsDirectory(applicationName, userName).listFiles(
            new FilenameFilter()
            {
				@Override
				public boolean accept(final File dir, final String name)
                {
                    return jobIdAsString.equals(StringUtils.substringBefore(name, "."));
                }
            });

        if ((resultFiles == null) || (resultFiles.length == 0))
        {
            return null;
        }

        if (resultFiles.length > 1)
        {
            throw new IllegalStateException("Found " + resultFiles.length + " results for job Id: " + jobId);
        }

        return resultFiles[0];
    }

    private PersistedResult buildPersistedResult(final String applicationName,
                                                 final String userName,
                                                 final UUID jobId,
                                                 final File resultFile)
    {
        final GregorianCalendar resultTime = (GregorianCalendar) GregorianCalendar.getInstance();
        resultTime.setTimeInMillis(resultFile.lastModified());

        final boolean success = !StringUtils.contains(resultFile.getName(), ERROR_FILE_INFIX_EXTENSION + ".");
        final MimeType mimeType = Util.getMimeType(resultFile);

        return new PersistedResult(applicationName, userName, jobId, resultTime, success, mimeType)
        {
            @Override
            public InputStream getData()
            {
                try
                {
                    return new FileInputStream(resultFile);
                }
                catch (final FileNotFoundException fnfe)
                {
                    throw new IllegalStateException(fnfe);
                }
            }

            @Override
            public long getDataLength()
            {
                return resultFile.length();
            }
        };
    }

    private File getResultsDirectory(final String applicationName, final String userName)
    {
        // this is to prevent trying to use application names with / or \ in order to
        // reach disallowed directories
        Validate.isTrue(Util.isValidApplicationName(applicationName), "Invalid application name: "
                                                                      + applicationName);

        final File applicationResultsDirectory = new File(getConfiguration().getResultsDirectory(),
            applicationName);

        return StringUtils.isBlank(userName) ? applicationResultsDirectory : new File(
            applicationResultsDirectory, userName);
    }
}

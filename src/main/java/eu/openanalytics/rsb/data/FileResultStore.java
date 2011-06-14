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
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.component.AbstractComponent;

/**
 * A file-based result store.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("resultStore")
public class FileResultStore extends AbstractComponent implements ResultStore {

    private static final String ERROR_FILE_INFIX_EXTENSION = ".err";

    public void store(final PersistedResult result) throws IOException {
        final String resultFileName = result.getJobId().toString() + (result.isSuccess() ? "" : ERROR_FILE_INFIX_EXTENSION) + "."
                + Util.getResourceType(result.getMimeType());

        final File applicationResultDirectory = getApplicationResultDirectory(result.getApplicationName());
        FileUtils.forceMkdir(applicationResultDirectory);
        final File resultFile = new File(applicationResultDirectory, resultFileName);

        final InputStream resultData = result.getData();
        final FileOutputStream fos = new FileOutputStream(resultFile);
        IOUtils.copy(resultData, fos);
        IOUtils.closeQuietly(result.getData());
        IOUtils.closeQuietly(fos);
    }

    @Override
    public boolean deleteByApplicationNameAndJobId(final String applicationName, final UUID jobId) throws IOException {
        final File resultFile = getResultFile(applicationName, jobId);

        if (resultFile == null) {
            return false;
        }

        FileUtils.forceDelete(resultFile);
        return true;
    }

    public Collection<PersistedResult> findByApplicationName(final String applicationName) {
        final File[] resultFiles = getApplicationResultDirectory(applicationName).listFiles();

        if ((resultFiles == null) || (resultFiles.length == 0)) {
            return Collections.emptyList();
        }

        final Collection<PersistedResult> persistedResults = new ArrayList<PersistedResult>();

        final List<File> sortedFiles = new ArrayList<File>(Arrays.asList(resultFiles));
        Collections.sort(sortedFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        for (final File resultFile : sortedFiles) {
            final UUID jobId = UUID.fromString(StringUtils.substringBefore(resultFile.getName(), "."));
            persistedResults.add(buildPersistedResult(applicationName, jobId, resultFile));
        }

        return persistedResults;
    }

    public PersistedResult findByApplicationNameAndJobId(final String applicationName, final UUID jobId) {
        final File resultFile = getResultFile(applicationName, jobId);
        return resultFile == null ? null : buildPersistedResult(applicationName, jobId, resultFile);
    }

    private File getResultFile(final String applicationName, final UUID jobId) {
        final String jobIdAsString = jobId.toString();
        final File[] resultFiles = getApplicationResultDirectory(applicationName).listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return jobIdAsString.equals(StringUtils.substringBefore(name, "."));
            }
        });

        if ((resultFiles == null) || (resultFiles.length == 0)) {
            return null;
        }

        if (resultFiles.length > 1) {
            throw new IllegalStateException("Found " + resultFiles.length + " results for job Id: " + jobId);
        }

        return resultFiles[0];
    }

    private PersistedResult buildPersistedResult(final String applicationName, final UUID jobId, final File resultFile) {
        final GregorianCalendar resultTime = (GregorianCalendar) GregorianCalendar.getInstance();
        resultTime.setTimeInMillis(resultFile.lastModified());

        final boolean success = !StringUtils.contains(resultFile.getName(), ERROR_FILE_INFIX_EXTENSION + ".");
        final MimeType mimeType = Util.getMimeType(resultFile);

        return new PersistedResult(applicationName, jobId, resultTime, success, mimeType) {
            @Override
            public InputStream getData() {
                try {
                    return new FileInputStream(resultFile);
                } catch (final FileNotFoundException fnfe) {
                    throw new IllegalStateException(fnfe);
                }
            }

            @Override
            public long getDataLength() {
                return resultFile.length();
            }
        };
    }

    private File getApplicationResultDirectory(final String applicationName) {
        return new File(getConfiguration().getResultsDirectory(), applicationName);
    }
}

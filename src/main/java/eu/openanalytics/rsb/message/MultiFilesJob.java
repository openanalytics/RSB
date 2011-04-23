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
package eu.openanalytics.rsb.message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.util.FileCopyUtils;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;

/**
 * Represents a RSB job that consists of multiple files.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class MultiFilesJob extends AbstractJobWithMeta {
    private static final long serialVersionUID = 1L;

    private final File temporaryDirectory;
    private File rScriptFile;

    public MultiFilesJob(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime,
            final Map<String, String> meta) throws IOException {
        super(applicationName, jobId, submissionTime, meta);
        this.temporaryDirectory = Util.createTemporaryDirectory("job");
    }

    public void addFile(final String name, final InputStream is) throws IOException {
        if (Constants.MULTIPLE_FILES_JOB_CONFIGURATION.equals(name)) {
            loadJobConfiguration(is);
        } else {
            addJobFile(name, is);
        }
    }

    private void addJobFile(final String name, final InputStream is) throws FileNotFoundException, IOException {
        final File jobFile = new File(temporaryDirectory, name);

        if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(name), Constants.R_SCRIPT_FILE_EXTENSION)) {
            if (rScriptFile != null) {
                throw new IllegalArgumentException("Only one R script is allowed per job");
            }
            rScriptFile = jobFile;
        }

        final FileOutputStream fos = new FileOutputStream(jobFile);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(fos);
    }

    private void loadJobConfiguration(final InputStream is) throws IOException {
        final Properties jobConfiguration = new Properties();
        jobConfiguration.load(is);

        final Map<String, String> mergedMeta = new HashMap<String, String>();
        for (final Entry<?, ?> e : jobConfiguration.entrySet()) {
            mergedMeta.put(e.getKey().toString(), e.getValue().toString());
        }

        // give priority to pre-existing metas by overriding the ones from the config file
        mergedMeta.putAll(getMeta());
        getMeta().clear();
        getMeta().putAll(mergedMeta);
    }

    @Override
    protected void releaseResources() {
        try {
            FileUtils.forceDelete(temporaryDirectory);
        } catch (final IOException ioe) {
            throw new RuntimeException("Can't release resources of: " + this, ioe);
        }
    }

    public MultiFilesResult buildSuccessResult() throws IOException {
        return new MultiFilesResult(getApplicationName(), getJobId(), getSubmissionTime(), true);
    }

    @Override
    public MultiFilesResult buildErrorResult(final Throwable t, final MessageSource messageSource) throws IOException {
        final String cause = t.getMessage();
        final String errorText = messageSource.getMessage("job.error", new Object[] { getJobId(), getSubmissionTime().getTime(), cause },
                cause, null);

        final MultiFilesResult result = new MultiFilesResult(getApplicationName(), getJobId(), getSubmissionTime(), false);
        final File resultFile = result.createNewResultFile(getJobId() + "." + Constants.MULTIPLE_FILES_ERROR_FILE_EXTENSION);
        FileCopyUtils.copy(errorText, new FileWriter(resultFile));
        return result;
    }

    public File getRScriptFile() {
        return rScriptFile;
    }

    public File[] getFiles() {
        return temporaryDirectory.listFiles();
    }
}

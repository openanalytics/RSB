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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;

/**
 * Represents a RSB result that consists of multiple files.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class MultiFilesResult extends AbstractResult<File> {
    private static final long serialVersionUID = 1L;

    private final File temporaryDirectory;

    private File payload = null;

    public MultiFilesResult(final Source source, final String applicationName, final UUID jobId, final GregorianCalendar submissionTime,
            final Map<String, String> meta, final boolean success) throws IOException {
        super(source, applicationName, jobId, submissionTime, meta, success);
        this.temporaryDirectory = Util.createTemporaryDirectory("job");
    }

    public File createNewResultFile(final String name) throws IOException {
        return new File(temporaryDirectory, name);
    }

    @Override
    protected void releaseResources() {
        try {
            FileUtils.forceDelete(temporaryDirectory);
        } catch (final IOException ioe) {
            throw new RuntimeException("Can't release resources of: " + this, ioe);
        }
    }

    @Override
    public File getPayload() throws IOException {
        if (payload == null) {
            payload = buildPayload();
        }

        return payload;
    }

    private File buildPayload() throws FileNotFoundException, IOException {
        final File[] resultFiles = temporaryDirectory.listFiles();
        if ((resultFiles.length == 1) && (StringUtils.endsWith(resultFiles[0].getName(), Constants.MULTIPLE_FILES_ERROR_FILE_EXTENSION))) {
            return resultFiles[0];
        }

        final File resultZipFile = new File(temporaryDirectory, getJobId() + ".zip");
        final ZipOutputStream resultZOS = new ZipOutputStream(new FileOutputStream(resultZipFile));

        for (final File resultFile : resultFiles) {
            resultZOS.putNextEntry(new ZipEntry(resultFile.getName()));

            final FileInputStream fis = new FileInputStream(resultFile);
            IOUtils.copy(fis, resultZOS);
            IOUtils.closeQuietly(fis);

            resultZOS.closeEntry();
        }

        IOUtils.closeQuietly(resultZOS);
        return resultZipFile;
    }
}

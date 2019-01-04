/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import eu.openanalytics.rsb.Util;

/**
 * Represents a RSB result that consists of multiple files.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class MultiFilesResult extends AbstractResult<File[]>
{
    private static final long serialVersionUID = 1L;

    private final File temporaryDirectory;

    public MultiFilesResult(final Source source,
                            final String applicationName,
                            final String userName,
                            final UUID jobId,
                            final GregorianCalendar submissionTime,
                            final Map<String, Serializable> meta,
                            final boolean success) throws IOException
    {
        super(source, applicationName, userName, jobId, submissionTime, meta, success);
        this.temporaryDirectory = Util.createTemporaryDirectory("job");
    }

    public File createNewResultFile(final String name) throws IOException
    {
        return new File(temporaryDirectory, name);
    }

    @Override
    protected void releaseResources()
    {
        try
        {
            FileUtils.forceDelete(temporaryDirectory);
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException("Can't release resources of: " + this, ioe);
        }
    }

    @Override
    public File[] getPayload() throws IOException
    {
        final File[] resultFiles = temporaryDirectory.listFiles();
        return resultFiles == null ? new File[0] : resultFiles;
    }

    // exposed only for unit tests
    public File getTemporaryDirectory()
    {
        return temporaryDirectory;
    }

    /**
     * Zips all the files contained in a multifiles result except if the result is
     * not successful, in that case returns the first file (which should be the only
     * one and contain a plain text error message).
     * 
     * @param result
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static File zipResultFilesIfNotError(final MultiFilesResult result)
        throws FileNotFoundException, IOException
    {
        final File[] resultFiles = result.getPayload();

        if ((!result.isSuccess()) && (resultFiles.length == 1))
        {
            return resultFiles[0];
        }

        final File resultZipFile = new File(result.getTemporaryDirectory(), result.getJobId() + ".zip");
        final ZipOutputStream resultZOS = new ZipOutputStream(new FileOutputStream(resultZipFile));

        for (final File resultFile : resultFiles)
        {
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

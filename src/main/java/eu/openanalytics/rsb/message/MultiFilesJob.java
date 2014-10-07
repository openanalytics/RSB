/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.util.FileCopyUtils;
import org.stringtemplate.v4.ST;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;

/**
 * Represents a RSB job that consists of multiple files.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class MultiFilesJob extends AbstractJob
{
    private static final long serialVersionUID = 1L;

    private final File temporaryDirectory;
    private File rScriptFile;

    public MultiFilesJob(final Source source,
                         final String applicationName,
                         final String userName,
                         final UUID jobId,
                         final GregorianCalendar submissionTime,
                         final Map<String, Serializable> meta) throws IOException
    {
        super(source, applicationName, userName, jobId, submissionTime, meta);
        this.temporaryDirectory = Util.createTemporaryDirectory("job");
    }

    public void addFile(final String name, final InputStream is) throws IOException
    {
        if (Constants.MULTIPLE_FILES_JOB_CONFIGURATION.equals(name))
        {
            loadJobConfiguration(is);
        }
        else
        {
            addJobFile(name, is);
        }
    }

    private void addJobFile(final String name, final InputStream is)
        throws FileNotFoundException, IOException
    {
        final File jobFile = new File(temporaryDirectory, name);

        if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(name), Constants.R_SCRIPT_FILE_EXTENSION))
        {
            if (rScriptFile != null)
            {
                throw new IllegalArgumentException("Only one R script is allowed per job");
            }
            rScriptFile = jobFile;
        }

        final FileOutputStream fos = new FileOutputStream(jobFile);
        IOUtils.copy(is, fos);
        IOUtils.closeQuietly(fos);
    }

    private void loadJobConfiguration(final InputStream is) throws IOException
    {
        final Properties jobConfiguration = new Properties();
        jobConfiguration.load(is);

        final Map<String, Serializable> mergedMeta = new HashMap<String, Serializable>();
        for (final Entry<?, ?> e : jobConfiguration.entrySet())
        {
            mergedMeta.put(e.getKey().toString(), e.getValue().toString());
        }

        // give priority to pre-existing metas by overriding the ones from the config
        // file
        mergedMeta.putAll(getMeta());
        getMeta().clear();
        getMeta().putAll(mergedMeta);
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

    public MultiFilesResult buildSuccessResult() throws IOException
    {
        return new MultiFilesResult(getSource(), getApplicationName(), getUserName(), getJobId(),
            getSubmissionTime(), getMeta(), true);
    }

    @Override
    public MultiFilesResult buildErrorResult(final Throwable t, final MessageSource messageSource)
        throws IOException
    {
        final String message = messageSource.getMessage(getErrorMessageId(), null, null);
        final ST template = Util.newStringTemplate(message);
        template.add("job", this);
        template.add("throwable", t);

        final MultiFilesResult result = new MultiFilesResult(getSource(), getApplicationName(),
            getUserName(), getJobId(), getSubmissionTime(), getMeta(), false);
        final File resultFile = result.createNewResultFile(getJobId() + "."
                                                           + Util.getResourceType(Constants.TEXT_MIME_TYPE));
        FileCopyUtils.copy(template.render(), new FileWriter(resultFile));
        return result;
    }

    public File getRScriptFile()
    {
        return rScriptFile;
    }

    public File[] getFiles()
    {
        return temporaryDirectory.listFiles();
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
     */
    public static void addDataToJob(final String contentType,
                                    final String name,
                                    final InputStream data,
                                    final MultiFilesJob job) throws IOException
    {
        // some browsers send zip file as application/octet-stream, forcing a
        // fallback to an
        // extension check
        if ((Constants.ZIP_CONTENT_TYPES.contains(contentType) || (StringUtils.endsWithIgnoreCase(
            FilenameUtils.getExtension(name), Constants.ZIP_MIME_TYPE.getSubType()))))
        {
            addZipFilesToJob(data, job);
        }
        else
        {
            job.addFile(name, data);
        }
    }

    /**
     * Adds all the files contained in a Zip archive to a job. Rejects Zips that
     * contain sub-directories.
     * 
     * @param data
     * @param job
     * @throws IOException
     */
    public static void addZipFilesToJob(final InputStream data, final MultiFilesJob job) throws IOException
    {
        final ZipInputStream zis = new ZipInputStream(data);
        ZipEntry ze = null;

        while ((ze = zis.getNextEntry()) != null)
        {
            if (ze.isDirectory())
            {
                job.destroy();
                throw new IllegalArgumentException(
                    "Invalid zip archive: nested directories are not supported");
            }
            job.addFile(ze.getName(), zis);
            zis.closeEntry();
        }

        IOUtils.closeQuietly(zis);
    }
}

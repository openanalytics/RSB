/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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

package eu.openanalytics.rsb.component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.Job;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.rservi.ErrorableRServi;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;
import eu.openanalytics.rsb.rservi.RServiUriSelector;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Processes job requests and builds job responses.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("jobProcessor")
public class JobProcessor extends AbstractComponentWithCatalog
{
    private interface JobRunner
    {
        AbstractResult<?> runOn(RServi rServi) throws Exception;
    }

    @Resource
    private RServiInstanceProvider rServiInstanceProvider;

    @Resource
    private JobStatisticsHandler jobStatisticsHandler;

    @Resource
    private RServiUriSelector rServiUriSelector;

    @PreAuthorize("hasPermission(#job, 'APPLICATION_JOB')")
    public AbstractResult<?> processDirect(final AbstractFunctionCallJob job) throws Exception
    {
        return process(job, new JobRunner()
        {
            @Override
            public AbstractResult<String> runOn(final RServi rServi) throws CoreException, IOException
            {
                final String resultPayload = callFunctionOnR(rServi, job);
                return job.buildSuccessResult(resultPayload);
            }
        }, true);
    }

    public void process(final AbstractFunctionCallJob job) throws Exception
    {
        process(job, new JobRunner()
        {
            @Override
            public AbstractResult<String> runOn(final RServi rServi) throws CoreException, IOException
            {
                final String resultPayload = callFunctionOnR(rServi, job);
                return job.buildSuccessResult(resultPayload);
            }
        }, false);
    }

    public void process(final MultiFilesJob job) throws Exception
    {
        process(job, new JobRunner()
        {
            @Override
            public AbstractResult<File[]> runOn(final RServi rServi) throws Exception
            {
                final Set<String> filesUploadedToR = new HashSet<String>();

                // locate and upload the R script
                final File rScriptFile = getRScriptFile(job);

                uploadFileToR(rServi, rScriptFile, filesUploadedToR);

                // optionally uploads a Sweave file
                final String sweaveFileFromCatalog = (String) getUploadableJobMeta(job).get(
                    Constants.SWEAVE_FILE_CONFIGURATION_KEY);

                if (sweaveFileFromCatalog != null)
                {
                    final File sweaveFile = getCatalogManager().internalGetCatalogFile(
                        CatalogSection.SWEAVE_FILES, job.getApplicationName(), sweaveFileFromCatalog);

                    if (!sweaveFile.isFile())
                    {
                        throw new IllegalArgumentException("Invalid catalog Sweave file reference in job: "
                                                           + job);
                    }

                    uploadFileToR(rServi, sweaveFile, filesUploadedToR);
                }

                // upload the job files (except the R Script which has already been
                // taken care of)
                for (final File jobFile : job.getFiles())
                {
                    if (!jobFile.equals(rScriptFile))
                    {
                        uploadFileToR(rServi, jobFile, filesUploadedToR);
                    }
                }

                // upload the configuration file to R
                uploadPropertiesToR(rServi, getUploadableJobMeta(job), filesUploadedToR);

                // hit R
                executeScriptOnR(rServi, rScriptFile.getName());

                final MultiFilesResult result = job.buildSuccessResult();

                // download the result files but not the uploaded ones nor the log
                // file
                final Set<String> filesToDownload = getFilesInRWorkspace(rServi);
                filesToDownload.removeAll(filesUploadedToR);
                filesToDownload.remove(Constants.DEFAULT_R_LOG_FILE);
                for (final String fileToDownload : filesToDownload)
                {
                    final File resultFile = result.createNewResultFile(fileToDownload);
                    final FileOutputStream fos = new FileOutputStream(resultFile);
                    rServi.downloadFile(fos, fileToDownload, 0, null);
                    IOUtils.closeQuietly(fos);
                }

                return result;
            }

            private Map<String, Serializable> getUploadableJobMeta(final Job job)
            {
                final Map<String, Serializable> meta = new HashMap<String, Serializable>(job.getMeta());

                if ((JobProcessor.this.getConfiguration().isPropagateSecurityContext())
                    && (StringUtils.isNotBlank(job.getUserName())))
                {
                    meta.put("rsbSecure", true);
                    meta.put("rsbUserPrincipal", job.getUserName());
                }

                return meta;
            }

            private File getRScriptFile(final MultiFilesJob job)
            {
                final String rScriptFromCatalog = (String) getUploadableJobMeta(job).get(
                    Constants.R_SCRIPT_CONFIGURATION_KEY);

                return rScriptFromCatalog != null
                                                 ? getRScriptFileFromCatalog(rScriptFromCatalog, job)
                                                 : getRScriptFileFromJob(job);
            }

            private File getRScriptFileFromCatalog(final String rScriptFromCatalog, final MultiFilesJob job)
            {
                final File rScriptFile = getCatalogManager().internalGetCatalogFile(CatalogSection.R_SCRIPTS,
                    job.getApplicationName(), rScriptFromCatalog);

                if ((rScriptFile == null) || (!rScriptFile.isFile()))
                {
                    throw new IllegalArgumentException("No R script has been found for job: " + job
                                                       + ", in the catalog under the name: "
                                                       + rScriptFromCatalog);
                }
                else
                {
                    return rScriptFile;
                }
            }

            private File getRScriptFileFromJob(final MultiFilesJob job)
            {
                if ((job.getRScriptFile() == null) || (!job.getRScriptFile().isFile()))
                {
                    throw new IllegalArgumentException("No R script has been found for job: " + job);
                }
                else
                {
                    return job.getRScriptFile();
                }
            }
        }, false);
    }

    // setters exposed for unit testing
    void setRServiInstanceProvider(final RServiInstanceProvider rServiInstanceProvider)
    {
        this.rServiInstanceProvider = rServiInstanceProvider;
    }

    void setJobStatisticsHandler(final JobStatisticsHandler jobStatisticsHandler)
    {
        this.jobStatisticsHandler = jobStatisticsHandler;
    }

    void setRServiUriSelector(final RServiUriSelector rServiUriSelector)
    {
        this.rServiUriSelector = rServiUriSelector;
    }

    private AbstractResult<?> process(final AbstractJob job, final JobRunner jobRunner, final boolean direct)
        throws Exception
    {
        AbstractResult<?> result = null;
        final long startTime = System.currentTimeMillis();
        final URI rserviPoolAddress = rServiUriSelector.getUriForApplication(job.getApplicationName());

        // using instanceof of is not OO-friendly but defining pooling strategy is none of
        // AbstractWorkItem business
        final PoolingStrategy poolingStrategy = job instanceof AbstractFunctionCallJob
                                                                                      ? PoolingStrategy.IF_POSSIBLE
                                                                                      : PoolingStrategy.NEVER;

        // don't catch RServi pool here so the error is propagated and the job can be
        // retried
        final RServi rServi = rServiInstanceProvider.getRServiInstance(rserviPoolAddress.toString(),
            Constants.RSERVI_CLIENT_ID, poolingStrategy);

        try
        {
            result = jobRunner.runOn(rServi);

            final long processTime = System.currentTimeMillis() - startTime;

            jobStatisticsHandler.storeJobStatistics(job, new GregorianCalendar(), processTime,
                rserviPoolAddress.toString());

            if (getLogger().isInfoEnabled())
            {
                getLogger().info(
                    String.format("Successfully processed %s %s for %s on %s in %dms", job.getType(),
                        job.getJobId(), job.getApplicationName(), rserviPoolAddress, processTime));
            }
        }
        // catch wide to prevent disrupting the main flow
        catch (final Throwable t)
        {
            if (rServi instanceof ErrorableRServi)
            {
                ((ErrorableRServi) rServi).markError();
            }

            final long processTime = System.currentTimeMillis() - startTime;
            getLogger().error(
                String.format("Failed to process %s %s for %s on %s in %dms", job.getType(), job.getJobId(),
                    job.getApplicationName(), rserviPoolAddress, processTime), t);
            result = job.buildErrorResult(t, getMessages());
        }
        finally
        {
            rServi.close();

            if ((!direct) && (result != null))
            {
                getMessageDispatcher().dispatch(result);
            }

            job.destroy();
        }

        return result;
    }

    private String callFunctionOnR(final RServi rServi, final AbstractFunctionCallJob job)
        throws CoreException
    {
        final FunctionCall functionCall = rServi.createFunctionCall(job.getFunctionName());
        functionCall.addChar(job.getArgument());

        if ((getConfiguration().isPropagateSecurityContext()) && (isNotBlank(job.getUserName())))
        {
            functionCall.addLogi("rsbSecure", true);
            functionCall.addChar("rsbUserPrincipal", job.getUserName());
        }

        final RObject result = functionCall.evalData(null);
        if (!RDataUtil.isSingleString(result))
        {
            throw new RuntimeException("Unexpected return value for function: " + job.getFunctionName());
        }

        return result.getData().getChar(0);
    }

    private static void uploadFileToR(final RServi rServi, final File file, final Set<String> filesUploadedToR)
        throws FileNotFoundException, CoreException
    {
        final FileInputStream fis = new FileInputStream(file);
        rServi.uploadFile(fis, file.length(), file.getName(), 0, null);
        IOUtils.closeQuietly(fis);
        filesUploadedToR.add(file.getName());
    }

    private static void uploadPropertiesToR(final RServi rServi,
                                            final Map<String, Serializable> metas,
                                            final Set<String> filesUploadedToR)
        throws CoreException, IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Properties properties = new Properties();
        for (final Entry<String, Serializable> meta : metas.entrySet())
        {
            properties.setProperty(meta.getKey(), meta.getValue().toString());
        }
        properties.store(baos, null);
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        rServi.uploadFile(bais, bais.available(), Constants.MULTIPLE_FILES_JOB_CONFIGURATION, 0, null);
        filesUploadedToR.add(Constants.MULTIPLE_FILES_JOB_CONFIGURATION);
    }

    private static void executeScriptOnR(final RServi rServi, final String rScriptName) throws CoreException
    {
        final FunctionCall sourceCall = rServi.createFunctionCall("source");
        sourceCall.addChar("file", rScriptName);
        sourceCall.evalVoid(null);
    }

    private static HashSet<String> getFilesInRWorkspace(final RServi rServi)
        throws UnexpectedRDataException, CoreException
    {
        final RObject evalResult = rServi.evalData("dir()", null);
        return new HashSet<String>(Arrays.asList(RDataUtil.checkRCharVector(evalResult).getData().toArray()));
    }
}

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
package eu.openanalytics.rsb.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.springframework.stereotype.Component;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.servi.RServi;
import de.walware.rj.services.FunctionCall;
import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.message.AbstractFunctionCallJob;
import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Processes job requests and builds job responses.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@Component("jobProcessor")
public class JobProcessor extends AbstractComponent {
    private interface JobRunner {
        AbstractResult<?> runOn(RServi rServi) throws Exception;
    }

    @Resource
    private RServiInstanceProvider rServiInstanceProvider;

    @Resource
    private JobStatisticsHandler jobStatisticsHandler;

    private final String rServiClientId;

    public JobProcessor() throws UnknownHostException {
        rServiClientId = "rsb@" + InetAddress.getLocalHost().getHostName();
    }

    public void process(final AbstractFunctionCallJob job) throws Exception {
        process(job, new JobRunner() {
            public AbstractResult<String> runOn(final RServi rServi) throws CoreException, IOException {
                final String resultPayload = callFunctionOnR(rServi, job.getFunctionName(), job.getArgument());
                return job.buildSuccessResult(resultPayload);
            }
        });
    }

    public void process(final MultiFilesJob job) throws Exception {
        process(job, new JobRunner() {
            public AbstractResult<File[]> runOn(final RServi rServi) throws Exception {
                final Set<String> filesUploadedToR = new HashSet<String>();

                // locate and upload the R script
                final String rScriptFromCatalog = job.getMeta().get(Constants.R_SCRIPT_CONFIGURATION_KEY);
                final File rScriptFile = rScriptFromCatalog != null ? new File(getConfiguration().getRScriptsCatalogDirectory(),
                        rScriptFromCatalog) : job.getRScriptFile();

                if ((rScriptFile == null) || (!rScriptFile.isFile())) {
                    throw new IllegalArgumentException("No R script has been found for job: " + job + " using catalog: "
                            + getConfiguration().getRScriptsCatalogDirectory());
                }

                uploadFileToR(rServi, rScriptFile, filesUploadedToR);

                // optionally uploads a Sweave file
                final String sweaveFileFromCatalog = job.getMeta().get(Constants.SWEAVE_FILE_CONFIGURATION_KEY);

                if (sweaveFileFromCatalog != null) {
                    final File sweaveFile = new File(getConfiguration().getSweaveFilesCatalogDirectory(), sweaveFileFromCatalog);

                    if (!sweaveFile.isFile()) {
                        throw new IllegalArgumentException("Invalid catalog Sweave file reference in job: " + job);
                    }

                    uploadFileToR(rServi, sweaveFile, filesUploadedToR);
                }

                // upload the job files (except the R Script which has already been taken care of)
                for (final File jobFile : job.getFiles()) {
                    if (!jobFile.equals(rScriptFile)) {
                        uploadFileToR(rServi, jobFile, filesUploadedToR);
                    }
                }

                // upload the configuration file to R
                uploadPropertiesToR(rServi, job.getMeta(), filesUploadedToR);

                // hit R
                executeScriptOnR(rServi, rScriptFile.getName());

                final MultiFilesResult result = job.buildSuccessResult();

                // download the result files but not the uploaded ones nor the log file
                final Set<String> filesToDownload = getFilesInRWorkspace(rServi);
                filesToDownload.removeAll(filesUploadedToR);
                filesToDownload.remove(Constants.DEFAULT_R_LOG_FILE);
                for (final String fileToDownload : filesToDownload) {
                    final File resultFile = result.createNewResultFile(fileToDownload);
                    final FileOutputStream fos = new FileOutputStream(resultFile);
                    rServi.downloadFile(fos, fileToDownload, 0, null);
                    IOUtils.closeQuietly(fos);
                }

                return result;
            }
        });
    }

    // exposed for unit testing
    void setRServiInstanceProvider(final RServiInstanceProvider rServiInstanceProvider) {
        this.rServiInstanceProvider = rServiInstanceProvider;
    }

    void setJobStatisticsHandler(final JobStatisticsHandler jobStatisticsHandler) {
        this.jobStatisticsHandler = jobStatisticsHandler;
    }

    URI getRServiPoolUri(final String applicationName) {
        final Map<String, URI> applicationSpecificRserviPoolUris = getConfiguration().getApplicationSpecificRserviPoolUris();

        if (applicationSpecificRserviPoolUris == null) {
            return getConfiguration().getDefaultRserviPoolUri();
        }

        final URI applicationRserviPoolUri = applicationSpecificRserviPoolUris.get(applicationName);

        return applicationRserviPoolUri == null ? getConfiguration().getDefaultRserviPoolUri() : applicationRserviPoolUri;
    }

    private void process(final AbstractJob job, final JobRunner jobRunner) throws LoginException, CoreException, IOException {
        AbstractResult<?> result = null;
        final long startTime = System.currentTimeMillis();
        final URI rserviPoolAddress = getRServiPoolUri(job.getApplicationName());

        // don't catch RServi pool here so the error is propagated and the job can be retried
        final RServi rServi = rServiInstanceProvider.getRServiInstance(rserviPoolAddress.toString(), rServiClientId);

        try {
            result = jobRunner.runOn(rServi);

            final long processTime = System.currentTimeMillis() - startTime;

            jobStatisticsHandler.storeJobStatistics(job.getApplicationName(), job.getJobId(), new GregorianCalendar(), processTime,
                    rserviPoolAddress.toString());

            getLogger().info(
                    String.format("Successfully processed %s %s on %s in %dms", job.getType(), job.getJobId(), rserviPoolAddress,
                            processTime));
        } catch (final Throwable t) {
            // catch wide to prevent disrupting the main flow
            final long processTime = System.currentTimeMillis() - startTime;
            getLogger().error(
                    String.format("Failed to process %s %s on %s in %dms", job.getType(), job.getJobId(), rserviPoolAddress, processTime),
                    t);
            result = job.buildErrorResult(t, getMessages());
        } finally {
            rServi.close();

            if (result != null) {
                getMessageDispatcher().dispatch(result);
            }

            job.destroy();
        }
    }

    private static String callFunctionOnR(final RServi rServi, final String functionName, final String argument) throws CoreException {
        final FunctionCall functionCall = rServi.createFunctionCall(functionName);
        functionCall.addChar(argument);
        final RObject result = functionCall.evalData(null);
        if (!RDataUtil.isSingleString(result)) {
            throw new RuntimeException("Unexpected return value for function: " + functionName);
        }

        return result.getData().getChar(0);
    }

    private static void uploadFileToR(final RServi rServi, final File file, final Set<String> filesUploadedToR)
            throws FileNotFoundException, CoreException {
        final FileInputStream fis = new FileInputStream(file);
        rServi.uploadFile(fis, file.length(), file.getName(), 0, null);
        IOUtils.closeQuietly(fis);
        filesUploadedToR.add(file.getName());
    }

    private static void uploadPropertiesToR(final RServi rServi, final Map<String, String> metas, final Set<String> filesUploadedToR)
            throws CoreException, IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Properties properties = new Properties();
        properties.putAll(metas);
        properties.store(baos, null);
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        rServi.uploadFile(bais, bais.available(), Constants.MULTIPLE_FILES_JOB_CONFIGURATION, 0, null);
        filesUploadedToR.add(Constants.MULTIPLE_FILES_JOB_CONFIGURATION);
    }

    private static void executeScriptOnR(final RServi rServi, final String rScriptName) throws CoreException {
        final FunctionCall sourceCall = rServi.createFunctionCall("source");
        sourceCall.addChar("file", rScriptName);
        sourceCall.evalVoid(null);
    }

    private static HashSet<String> getFilesInRWorkspace(final RServi rServi) throws UnexpectedRDataException, CoreException {
        final RObject evalResult = rServi.evalData("dir()", null);
        return new HashSet<String>(Arrays.asList(RDataUtil.checkRCharVector(evalResult).getData().toArray()));
    }
}

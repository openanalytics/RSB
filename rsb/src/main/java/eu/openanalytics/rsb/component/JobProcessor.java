/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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

package eu.openanalytics.rsb.component;

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.statet.jcommons.status.StatusException;

import org.eclipse.statet.rj.data.RDataUtils;
import org.eclipse.statet.rj.data.RObject;
import org.eclipse.statet.rj.data.UnexpectedRDataException;
import org.eclipse.statet.rj.servi.RServi;
import org.eclipse.statet.rj.services.FunctionCall;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

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
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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
            public AbstractResult<String> runOn(final RServi rServi) throws StatusException, IOException
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
            public AbstractResult<String> runOn(final RServi rServi) throws StatusException, IOException
            {
                final String resultPayload = callFunctionOnR(rServi, job);
                return job.buildSuccessResult(resultPayload);
            }
        }, false);
    }
	
	public void process(final MultiFilesJob job) throws Exception {
		process(job, new JobRunner() {
			@Override
			public AbstractResult<List<Path>> runOn(final RServi rServi) throws Exception {
				final Set<String> filesUploadedToR= new HashSet<>();
				
				// locate and upload the R script
				final Path rScriptFile= getRScriptFile(job);
				final String rScriptFileName= uploadFileToR(rServi, rScriptFile, filesUploadedToR);
				
				// optionally uploads a Sweave file
				final String sweaveFileFromCatalog= (String)getUploadableJobMeta(job)
						.get(Constants.SWEAVE_FILE_CONFIGURATION_KEY);
				if (sweaveFileFromCatalog != null) {
					final Path sweaveFile= getCatalogManager().internalGetCatalogFile(
							CatalogSection.SWEAVE_FILES,
							job.getApplicationName(), sweaveFileFromCatalog );
					if (sweaveFile == null || !Files.exists(sweaveFile)) {
						throw new IllegalArgumentException("Invalid catalog Sweave file reference in job: " + job);
					}
					
					uploadFileToR(rServi, sweaveFile, filesUploadedToR);
				}
				
				// upload the job files (except the R Script which has already been taken care of)
				for (final Path jobFile : job.getFiles()) {
					if (!jobFile.equals(rScriptFile)) {
						uploadFileToR(rServi, jobFile, filesUploadedToR);
					}
				}
				
				// upload the configuration file to R
				uploadPropertiesToR(rServi, getUploadableJobMeta(job), filesUploadedToR);
				
				// hit R
				executeScriptOnR(rServi, rScriptFileName);
				
				final MultiFilesResult result= job.buildSuccessResult();
				
				// download the result files but not the uploaded ones nor the log file
				final Set<String> filesToDownload= getFilesInRWorkspace(rServi);
				filesToDownload.removeAll(filesUploadedToR);
				filesToDownload.remove(Constants.DEFAULT_R_LOG_FILE);
				for (final String fileToDownload : filesToDownload) {
					final Path resultFile= result.createNewResultFile(fileToDownload);
					try (final var out= Files.newOutputStream(resultFile, StandardOpenOption.CREATE_NEW)) {
						rServi.downloadFile(out, fileToDownload, 0, null);
					}
				}
				
				return result;
			}
			
			private Map<String, Serializable> getUploadableJobMeta(final Job job) {
				final Map<String, Serializable> meta= new HashMap<>(job.getMeta());
				
				if (JobProcessor.this.getConfiguration().isPropagateSecurityContext()
						&& StringUtils.isNotBlank(job.getUserName()) ) {
					meta.put("rsbSecure", true);
					meta.put("rsbUserPrincipal", job.getUserName());
				}
				
				return meta;
			}
			
			private Path getRScriptFile(final MultiFilesJob job) {
				final String rScriptFromCatalog= (String)getUploadableJobMeta(job)
						.get(Constants.R_SCRIPT_CONFIGURATION_KEY);
				
				return (rScriptFromCatalog != null) ?
						getRScriptFileFromCatalog(rScriptFromCatalog, job) :
						getRScriptFileFromJob(job);
			}
			
			private Path getRScriptFileFromCatalog(final String rScriptFromCatalog, final MultiFilesJob job) {
				final Path rScriptFile= getCatalogManager().internalGetCatalogFile(
						CatalogSection.R_SCRIPTS,
						job.getApplicationName(), rScriptFromCatalog );
				if (rScriptFile == null || !Files.isRegularFile(rScriptFile)) {
					throw new IllegalArgumentException("No R script has been found for job: " + job
							+ ", in the catalog under the name: " + rScriptFromCatalog );
				}
				else {
					return rScriptFile;
				}
			}
			
			private Path getRScriptFileFromJob(final MultiFilesJob job) {
				final var rScriptFile= job.getRScriptFile();
				if (rScriptFile == null || !Files.exists(rScriptFile)) {
					throw new IllegalArgumentException("No R script has been found for job: " + job);
				}
				return rScriptFile;
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
        final URI rserviPoolAddress = this.rServiUriSelector.getUriForApplication(job.getApplicationName());

        // using instanceof of is not OO-friendly but defining pooling strategy is none of
        // AbstractWorkItem business
        final PoolingStrategy poolingStrategy = job instanceof AbstractFunctionCallJob
                                                                                      ? PoolingStrategy.IF_POSSIBLE
                                                                                      : PoolingStrategy.NEVER;

        // don't catch RServi pool here so the error is propagated and the job can be
        // retried
        final RServi rServi = this.rServiInstanceProvider.getRServiInstance(rserviPoolAddress.toString(),
            Constants.RSERVI_CLIENT_ID, poolingStrategy);

        try
        {
            result = jobRunner.runOn(rServi);

            final long processTime = System.currentTimeMillis() - startTime;

            this.jobStatisticsHandler.storeJobStatistics(job, new GregorianCalendar(), processTime,
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
            try
            {
                rServi.close();
            }
            catch (final StatusException e)
            {
                getLogger().error(e.getMessage(), e);
            }

            try
            {
                if ((!direct) && (result != null))
                {
                    getMessageDispatcher().dispatch(result);
                }
            }
            finally
            {
                job.destroy();
            }
        }

        return result;
    }

    private String callFunctionOnR(final RServi rServi, final AbstractFunctionCallJob job)
        throws StatusException
    {
        final FunctionCall functionCall = rServi.createFunctionCall(job.getFunctionName());
        functionCall.addChar(job.getArgument());

        if ((getConfiguration().isPropagateSecurityContext()) && (isNotBlank(job.getUserName())))
        {
            functionCall.addLogi("rsbSecure", true);
            functionCall.addChar("rsbUserPrincipal", job.getUserName());
        }

        final RObject result = functionCall.evalData(null);
        if (!RDataUtils.isSingleChar(result))
        {
            throw new RuntimeException("Unexpected return value for function: " + job.getFunctionName());
        }

        return result.getData().getChar(0);
    }
	
	private static String uploadFileToR(final RServi rServi, final Path file,
			final Set<String> filesUploadedToR)
			throws StatusException, IOException {
		final String fileName= requireFileName(file).toString();
		
		try (final var in= Files.newInputStream(file)) {
			rServi.uploadFile(in, Files.size(file), fileName, 0, null);
		}
		
		filesUploadedToR.add(fileName);
		return fileName;
	}
	
    private static void uploadPropertiesToR(final RServi rServi,
                                            final Map<String, Serializable> metas,
                                            final Set<String> filesUploadedToR)
        throws StatusException, IOException
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

    private static void executeScriptOnR(final RServi rServi, final String rScriptName) throws StatusException
    {
        final FunctionCall sourceCall = rServi.createFunctionCall("source");
        sourceCall.addChar("file", rScriptName);
        sourceCall.evalVoid(null);
    }

    private static HashSet<String> getFilesInRWorkspace(final RServi rServi)
        throws UnexpectedRDataException, StatusException
    {
        final RObject evalResult = rServi.evalData("dir()", null);
        return new HashSet<>(Arrays.asList(RDataUtils.checkRCharVector(evalResult).getData().toArray()));
    }
}

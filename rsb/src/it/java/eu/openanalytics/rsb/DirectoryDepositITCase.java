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

package eu.openanalytics.rsb;

import static org.eclipse.statet.jcommons.lang.ObjectUtils.nonNullLateInit;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.statet.jcommons.io.FileUtils;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class DirectoryDepositITCase extends AbstractITCase {
	
	
	protected Path jobsDirectory= nonNullLateInit();
	protected Path acceptedDirectory= nonNullLateInit();
	protected Path resultsDirectory= nonNullLateInit();
	
	
	public DirectoryDepositITCase() {
	}
	
	@Before
	public void prepareTests() throws IOException {
		final var depositRootDirectory= findDepositDirectoryConfiguration(getApplicationName())
				.getRootDirectory().toPath();
		this.jobsDirectory= depositRootDirectory.resolve(Configuration.DEPOSIT_JOBS_SUBDIR);
		this.acceptedDirectory= depositRootDirectory.resolve(Configuration.DEPOSIT_ACCEPTED_SUBDIR);
		this.resultsDirectory= depositRootDirectory.resolve(Configuration.DEPOSIT_RESULTS_SUBDIR);
		
		FileUtils.cleanDirectory(this.jobsDirectory);
		FileUtils.cleanDirectory(this.acceptedDirectory);
		FileUtils.cleanDirectory(this.resultsDirectory);
	}
	
	
    protected String getApplicationName()
    {
        return "lab1";
    }

    private DepositDirectoryConfiguration findDepositDirectoryConfiguration(final String applicationName)
    {
        for (final DepositDirectoryConfiguration depositDirectoryConfiguration : getConfiguration().getDepositRootDirectories())
        {
            if (depositDirectoryConfiguration.getApplicationName().equals(applicationName))
            {
                return depositDirectoryConfiguration;
            }
        }

        throw new IllegalArgumentException(applicationName + " not found!");
    }

    @Test
    public void validZipDeposit() throws IOException, InterruptedException
    {
        doTestValidZipDeposit("r-job-sample.zip");
    }

    @Test
    public void invalidZipDeposit() throws IOException, InterruptedException
    {
        doInvalidZipDeposit("invalid-job-subdir.zip");
    }

    @Test
    public void dataOnlyZipDeposit() throws IOException, InterruptedException
    {
        doInvalidZipDeposit("r-job-data-only.zip");
    }
	
	
	protected void doTestValidZipDeposit(final String jobFileName)
			throws IOException, InterruptedException {
		Files.copy(getTestDataFile(jobFileName), this.jobsDirectory.resolve(jobFileName));
		
		final var acceptedFile= ponderUntilJobAccepted(jobFileName);
		assertTrue("file was not created: " + acceptedFile, Files.isRegularFile(acceptedFile));
		
		final var resultFile= ponderUntilJobResult(jobFileName);
		assertTrue("file was not created: " + resultFile, Files.isRegularFile(resultFile));
		
		try (final var in= Files.newInputStream(resultFile)) {
			validateZipResult(in);
		}
	}
	
	protected void doInvalidZipDeposit(final String jobFileName)
			throws IOException, InterruptedException {
		Files.copy(getTestDataFile(jobFileName), this.jobsDirectory.resolve(jobFileName));
		
		final var acceptedFile= ponderUntilJobAccepted(jobFileName);
		assertTrue("file was not created: " + acceptedFile, Files.isRegularFile(acceptedFile));
		
		final var resultFile= ponderUntilJobError(jobFileName);
		assertTrue("file was not created: " + resultFile, Files.isRegularFile(resultFile));
		
		try (final var in= Files.newInputStream(resultFile)) {
			validateErrorResult(in);
		}
	}
	
	protected Path ponderUntilJobAccepted(final String jobFileName) throws InterruptedException {
		final var expectedAcceptedFile= this.acceptedDirectory.resolve(jobFileName);
		ponderForFile(expectedAcceptedFile);
		return expectedAcceptedFile;
	}
	
	protected Path ponderUntilJobResult(final String jobFileName) throws InterruptedException {
		final var expectedAcceptedFile= this.resultsDirectory.resolve("result-" + jobFileName);
		ponderForFile(expectedAcceptedFile);
		return expectedAcceptedFile;
	}
	
	protected Path ponderUntilJobError(final String jobFileName) throws InterruptedException {
		final var expectedAcceptedFile= this.resultsDirectory.resolve("result-"
				+ FilenameUtils.getBaseName(jobFileName)
				+ ".txt" );
		ponderForFile(expectedAcceptedFile);
		return expectedAcceptedFile;
	}
	
	private void ponderForFile(final Path expectedFile) throws InterruptedException {
		int attempts= 0;
		while (attempts++ < 120) {
			Thread.sleep(500L);
			if (Files.isRegularFile(expectedFile)) {
				break;
			}
		}
	}
	
}

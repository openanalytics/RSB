/*
 *   R Service Bus
 *
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

package eu.openanalytics.rsb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class DirectoryDepositITCase extends AbstractITCase
{
    protected File jobsDirectory;
    protected File acceptedDirectory;
    protected File resultsDirectory;

    @Before
    public void prepareTests() throws IOException
    {
        final File depositRootDirectory = findDepositDirectoryConfiguration(getApplicationName()).getRootDirectory();
        jobsDirectory = new File(depositRootDirectory, Configuration.DEPOSIT_JOBS_SUBDIR);
        acceptedDirectory = new File(depositRootDirectory, Configuration.DEPOSIT_ACCEPTED_SUBDIR);
        resultsDirectory = new File(depositRootDirectory, Configuration.DEPOSIT_RESULTS_SUBDIR);

        FileUtils.cleanDirectory(jobsDirectory);
        FileUtils.cleanDirectory(acceptedDirectory);
        FileUtils.cleanDirectory(resultsDirectory);
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
        throws IOException, InterruptedException, FileNotFoundException
    {
        FileUtils.copyFileToDirectory(getTestFile(jobFileName), jobsDirectory);

        final File acceptedFile = ponderUntilJobAccepted(jobFileName);
        assertThat("file was not created: " + acceptedFile, acceptedFile.isFile(), is(true));

        final File resultFile = ponderUntilJobResult(jobFileName);
        assertThat("file was not created: " + resultFile, resultFile.isFile(), is(true));

        validateZipResult(new FileInputStream(resultFile));
    }

    protected void doInvalidZipDeposit(final String jobFileName)
        throws IOException, InterruptedException, FileNotFoundException
    {
        FileUtils.copyFileToDirectory(getTestFile(jobFileName), jobsDirectory);

        final File acceptedFile = ponderUntilJobAccepted(jobFileName);
        assertThat("file was not created: " + acceptedFile, acceptedFile.isFile(), is(true));

        final File resultFile = ponderUntilJobError(jobFileName);
        assertThat("file was not created: " + resultFile, resultFile.isFile(), is(true));

        validateErrorResult(new FileInputStream(resultFile));
    }

    protected File ponderUntilJobAccepted(final String jobFileName) throws InterruptedException
    {
        final File expectedAcceptedFile = new File(acceptedDirectory, jobFileName);
        ponderForFile(expectedAcceptedFile);
        return expectedAcceptedFile;
    }

    protected File ponderUntilJobResult(final String jobFileName) throws InterruptedException
    {
        final File expectedAcceptedFile = new File(resultsDirectory, "result-" + jobFileName);
        ponderForFile(expectedAcceptedFile);
        return expectedAcceptedFile;
    }

    protected File ponderUntilJobError(final String jobFileName) throws InterruptedException
    {
        final File expectedAcceptedFile = new File(resultsDirectory, "result-"
                                                                     + FilenameUtils.getBaseName(jobFileName)
                                                                     + ".txt");
        ponderForFile(expectedAcceptedFile);
        return expectedAcceptedFile;
    }

    private void ponderForFile(final File expectedFile) throws InterruptedException
    {
        int attempts = 0;
        while (attempts++ < 120)
        {
            Thread.sleep(500L);
            if (expectedFile.isFile())
            {
                break;
            }
        }
    }
}

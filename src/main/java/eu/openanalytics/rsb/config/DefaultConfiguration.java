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
package eu.openanalytics.rsb.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Provides default configuration values: it is strongly suggested that all concrete implementations
 * of {@link Configuration} extend this class.
 * 
 * @author rsb.development@openanalytics.eu
 */
public class DefaultConfiguration implements Configuration {
    protected final File userHomeDirectory;
    private final File rsbHomeDirectory;
    private final File rScriptsCatalogDirectory;
    private final File sweaveFilesCatalogDirectory;
    private final File emailRepliesCatalogDirectory;
    private final File rsbResultsDirectory;
    private final File activeMqWorkDirectory;
    private final URI defaultRserviPoolUri;
    private final int jobTimeOut;
    private final int numberOfConcurrentJobWorkersPerQueue;

    public DefaultConfiguration() throws URISyntaxException {
        userHomeDirectory = new File(System.getProperty("user.home"));
        rsbHomeDirectory = new File(userHomeDirectory, ".rsb");

        final File rsbCatalogRootDirectory = new File(rsbHomeDirectory, "catalog");
        rScriptsCatalogDirectory = new File(rsbCatalogRootDirectory, "r_scripts");
        sweaveFilesCatalogDirectory = new File(rsbCatalogRootDirectory, "sweave_files");
        emailRepliesCatalogDirectory = new File(rsbCatalogRootDirectory, "email_replies");

        rsbResultsDirectory = new File(rsbHomeDirectory, "results");
        activeMqWorkDirectory = new File(rsbHomeDirectory, "activemq");
        defaultRserviPoolUri = new URI("rmi://127.0.0.1/rservi-pool");
        jobTimeOut = 600000; // 10 minutes
        numberOfConcurrentJobWorkersPerQueue = 5;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void validate() {
        Validate.notNull(getUserHomeDirectory(), "userHomeDirectory can't be null");
        Validate.notNull(getResultsDirectory(), "rsbResultsDirectory can't be null");
        Validate.notNull(getActiveMqWorkDirectory(), "activeMqWorkDirectory can't be null");
        Validate.notNull(getDefaultRserviPoolUri(), "defaultRserviPoolUri can't be null");
    }

    protected File getUserHomeDirectory() {
        return userHomeDirectory;
    }

    @Override
    public File getRScriptsCatalogDirectory() {
        return rScriptsCatalogDirectory;
    }

    @Override
    public File getSweaveFilesCatalogDirectory() {
        return sweaveFilesCatalogDirectory;
    }

    @Override
    public File getEmailRepliesCatalogDirectory() {
        return emailRepliesCatalogDirectory;
    }

    public File getResultsDirectory() {
        return rsbResultsDirectory;
    }

    public File getActiveMqWorkDirectory() {
        return activeMqWorkDirectory;
    }

    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return null;
    }

    public int getJobTimeOut() {
        return jobTimeOut;
    }

    public int getNumberOfConcurrentJobWorkersPerQueue() {
        return numberOfConcurrentJobWorkersPerQueue;
    }

    public JobStatisticsHandler getJobStatisticsHandler() {
        return null;
    }
}

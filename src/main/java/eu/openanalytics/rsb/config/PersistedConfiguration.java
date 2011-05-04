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
import java.util.Map;

/**
 * Defines the persisted configuration of RSB, from which the actual {@link Configuration} is
 * derived.
 * 
 * @see Configuration
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class PersistedConfiguration {
    private File activeMqWorkDirectory;
    private URI defaultRserviPoolUri;
    private int jobTimeOut;
    private int numberOfConcurrentJobWorkersPerQueue;
    private File catalogRootDirectory;
    private File resultsDirectory;
    private Map<String, URI> applicationSpecificRserviPoolUris;
    private String jobStatisticsHandlerClass;
    private Map<String, Object> jobStatisticsHandlerConfiguration;

    /**
     * Directory under which RSB catalogs are located. The catalogs are:
     * <ul>
     * <li>r_scripts: catalog of R scripts</li>
     * <li>sweave_files: catalog of Sweave files</li>
     * <li>email_replies: catalog of Email replies</li>
     * </ul>
     * If any of these sub-directories do not pre-exist, RSB will try to create it.
     */
    public File getCatalogRootDirectory() {
        return catalogRootDirectory;
    }

    public void setCatalogRootDirectory(final File catalogRootDirectory) {
        this.catalogRootDirectory = catalogRootDirectory;
    }

    /**
     * Directory where ActiveMQ stores its persisted data.
     */
    public File getActiveMqWorkDirectory() {
        return activeMqWorkDirectory;
    }

    public void setActiveMqWorkDirectory(final File activeMqWorkDirectory) {
        this.activeMqWorkDirectory = activeMqWorkDirectory;
    }

    /**
     * URI of the RServi RMI pool.
     */
    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public void setDefaultRserviPoolUri(final URI defaultRserviPoolUri) {
        this.defaultRserviPoolUri = defaultRserviPoolUri;
    }

    /**
     * Maximum time a job request can be pending its response (in milliseconds).
     */
    public int getJobTimeOut() {
        return jobTimeOut;
    }

    public void setJobTimeOut(final int jobTimeOut) {
        this.jobTimeOut = jobTimeOut;
    }

    /**
     * Number of concurrent job workers per queue, which must be computed based on the number of
     * nodes in the RServi pool and the number of job queues (one global plus one per "boosted"
     * application).
     */
    public int getNumberOfConcurrentJobWorkersPerQueue() {
        return numberOfConcurrentJobWorkersPerQueue;
    }

    public void setNumberOfConcurrentJobWorkersPerQueue(final int numberOfConcurrentJobWorkersPerQueue) {
        this.numberOfConcurrentJobWorkersPerQueue = numberOfConcurrentJobWorkersPerQueue;
    }

    /**
     * Directory where result files are written.
     */
    public File getResultsDirectory() {
        return resultsDirectory;
    }

    public void setResultsDirectory(final File resultsDirectory) {
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Mapping of application names and RServi RMI pool URIs.
     */
    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return applicationSpecificRserviPoolUris;
    }

    public void setApplicationSpecificRserviPoolUris(final Map<String, URI> applicationSpecificRserviPoolUris) {
        this.applicationSpecificRserviPoolUris = applicationSpecificRserviPoolUris;
    }

    /**
     * The job statistics handler class to instantiate.
     */
    public String getJobStatisticsHandlerClass() {
        return jobStatisticsHandlerClass;
    }

    public void setJobStatisticsHandlerClass(final String jobStatisticsHandlerClass) {
        this.jobStatisticsHandlerClass = jobStatisticsHandlerClass;
    }

    /**
     * The configuration specific to the job statistics handler.
     */
    public Map<String, Object> getJobStatisticsHandlerConfiguration() {
        return jobStatisticsHandlerConfiguration;
    }

    public void setJobStatisticsHandlerConfiguration(final Map<String, Object> jobStatisticsHandlerConfiguration) {
        this.jobStatisticsHandlerConfiguration = jobStatisticsHandlerConfiguration;
    }
}

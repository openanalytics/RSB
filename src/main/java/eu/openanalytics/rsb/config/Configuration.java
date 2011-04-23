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

import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Defines the configuration of RSB, which is injected in all components in order to support runtime
 * changes via a hit-reloadable Groovy concrete implementation.
 * 
 * @see DefaultConfiguration
 * 
 * @author rsb.development@openanalytics.eu
 */
public interface Configuration {

    /**
     * Validates the configuration, throwing runtime exceptions in case of problems.
     */
    void validate();

    /**
     * Directory where a catalog of R scripts, Sweave files and Email responses are stored.
     */
    File getRsbCatalogDirectory();

    /**
     * Directory where result files are written.
     */
    File getRsbResultsDirectory();

    /**
     * Directory where ActiveMQ stores its persisted data.
     */
    File getActiveMqWorkDirectory();

    /**
     * URI of the RServi RMI pool.
     */
    URI getDefaultRserviPoolUri();

    /**
     * Optional mapping of application names and RServi RMI pool URIs.
     */
    Map<String, URI> getApplicationSpecificRserviPoolUris();

    /**
     * Maximum time a job request can be pending its response (in milliseconds).
     */
    int getJobTimeOut();

    /**
     * Number of concurrent job workers per queue, which must be computed based on the number of
     * nodes in the RServi pool and the number of job queues (one global plus one per "boosted"
     * application).
     */
    int getNumberOfConcurrentJobWorkersPerQueue();

    /**
     * Optional RSB job statistics handler.
     */
    JobStatisticsHandler getJobStatisticsHandler();
}

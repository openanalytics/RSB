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
import java.util.Collections;
import java.util.Map;

/**
 * Provides default configuration values: it is strongly suggested that all concrete implementations
 * of {@link Configuration} extend this abstract class.
 * 
 * @author rsb.development@openanalytics.eu
 */
public abstract class AbstractDefaultConfiguration implements Configuration {
    protected final File userHomeDirectory;
    private final File rsbHomeDirectory;
    private final File rsbResultsDirectory;
    private final File activeMqWorkDirectory;
    private final URI defaultRserviPoolUri;
    private final Map<String, URI> applicationSpecificRserviPoolUris;
    private final int jobTimeOut;

    public AbstractDefaultConfiguration() throws URISyntaxException {
        userHomeDirectory = new File(System.getProperty("user.home"));
        rsbHomeDirectory = new File(userHomeDirectory, ".rsb");
        rsbResultsDirectory = new File(rsbHomeDirectory, "results");
        activeMqWorkDirectory = new File(rsbHomeDirectory, "activemq");
        defaultRserviPoolUri = new URI("rmi://127.0.0.1/rservi-pool");
        applicationSpecificRserviPoolUris = Collections.emptyMap();
        jobTimeOut = 600000; // 10 minutes
    }

    protected File getUserHomeDirectory() {
        return userHomeDirectory;
    }

    public File getRsbResultsDirectory() {
        return rsbResultsDirectory;
    }

    public File getActiveMqWorkDirectory() {
        return activeMqWorkDirectory;
    }

    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return applicationSpecificRserviPoolUris;
    }

    public int getJobTimeOut() {
        return jobTimeOut;
    }
}

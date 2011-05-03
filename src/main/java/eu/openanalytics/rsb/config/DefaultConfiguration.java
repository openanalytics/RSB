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
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Provides default configuration values: it is strongly suggested that all concrete implementations
 * of {@link Configuration} extend this class.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class DefaultConfiguration implements Configuration {
    private static final Log LOGGER = LogFactory.getLog(DefaultConfiguration.class);

    private File activeMqWorkDirectory;
    private File defaultRsbHomeDirectory;
    private final URI defaultRserviPoolUri;

    private File emailRepliesCatalogDirectory;
    private final int jobTimeOut;

    private final int numberOfConcurrentJobWorkersPerQueue;
    private File rsbCatalogRootDirectory;
    private File rsbResultsDirectory;
    private File rScriptsCatalogDirectory;
    private File sweaveFilesCatalogDirectory;

    public DefaultConfiguration() throws URISyntaxException {
        defaultRserviPoolUri = new URI("rmi://127.0.0.1/rservi-pool");
        jobTimeOut = 600000; // 10 minutes
        numberOfConcurrentJobWorkersPerQueue = 5;
    }

    public File getActiveMqWorkDirectory() {
        if (activeMqWorkDirectory == null) {
            activeMqWorkDirectory = new File(getDefaultRsbHomeDirectory(), "activemq");
        }
        return activeMqWorkDirectory;
    }

    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return null;
    }

    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public File getEmailRepliesCatalogDirectory() {
        if (emailRepliesCatalogDirectory == null) {
            emailRepliesCatalogDirectory = new File(getRsbCatalogRootDirectory(), "email_replies");
        }
        return emailRepliesCatalogDirectory;
    }

    public JobStatisticsHandler getJobStatisticsHandler() {
        return null;
    }

    public int getJobTimeOut() {
        return jobTimeOut;
    }

    public int getNumberOfConcurrentJobWorkersPerQueue() {
        return numberOfConcurrentJobWorkersPerQueue;
    }

    public File getResultsDirectory() {
        if (rsbResultsDirectory == null) {
            rsbResultsDirectory = new File(getDefaultRsbHomeDirectory(), "results");
        }
        return rsbResultsDirectory;
    }

    public File getRScriptsCatalogDirectory() {
        if (rScriptsCatalogDirectory == null) {
            rScriptsCatalogDirectory = new File(getRsbCatalogRootDirectory(), "r_scripts");
        }

        return rScriptsCatalogDirectory;
    }

    public File getSweaveFilesCatalogDirectory() {
        if (sweaveFilesCatalogDirectory == null) {
            sweaveFilesCatalogDirectory = new File(getRsbCatalogRootDirectory(), "sweave_files");
        }
        return sweaveFilesCatalogDirectory;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // by default create all the directories required by RSB under a single parent home
    // directory
    // note that this is not a requirement: each directory can be located in different locations
    private File getDefaultRsbHomeDirectory() {
        if (defaultRsbHomeDirectory != null) {
            return defaultRsbHomeDirectory;
        }

        // try in different potential locations
        final String[] potentialRsbHomeParentDirectories = { System.getProperty("user.home"), getWebInfPath(),
                System.getProperty("user.dir"), System.getProperty("java.io.tmpdir") };

        for (final String potentialRsbHomeParentDirectory : potentialRsbHomeParentDirectories) {
            if (StringUtils.isBlank(potentialRsbHomeParentDirectory)) {
                continue;
            }
            final File result = getOrCreateDefaultRsbHomeDirectory(potentialRsbHomeParentDirectory);
            if (result != null) {
                defaultRsbHomeDirectory = result;
                return defaultRsbHomeDirectory;
            }
        }

        throw new IllegalStateException("Failed to create RSB directories in all attempted locations");
    }

    private File getOrCreateDefaultRsbHomeDirectory(final String rsbHomeParentDirectory) {
        final File rsbHomeDirectory = new File(new File(rsbHomeParentDirectory), ".rsb");
        if (rsbHomeDirectory.isDirectory()) {
            return rsbHomeDirectory;
        }
        if (rsbHomeDirectory.mkdir()) {
            return rsbHomeDirectory;
        }
        LOGGER.info("Failed to create default RSB home directory under: " + rsbHomeDirectory);
        return null;
    }

    private File getRsbCatalogRootDirectory() {
        if (rsbCatalogRootDirectory == null) {
            rsbCatalogRootDirectory = new File(getDefaultRsbHomeDirectory(), "catalog");
        }
        return rsbCatalogRootDirectory;
    }

    private static String getWebInfPath() {
        final URL classUrl = DefaultConfiguration.class
                .getResource("/" + DefaultConfiguration.class.getName().replace('.', '/') + ".class");

        if (classUrl == null) {
            return null;
        }

        return StringUtils.substringBefore(classUrl.getFile(), "classes");
    }
}

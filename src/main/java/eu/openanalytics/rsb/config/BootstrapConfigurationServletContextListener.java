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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.stats.NoopJobStatisticsHandler;

/**
 * Verifies that the RSB configuration is loadable and, if not, creates a default one so RSB can
 * start anyway.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class BootstrapConfigurationServletContextListener implements ServletContextListener {
    private static final Log LOGGER = LogFactory.getLog(BootstrapConfigurationServletContextListener.class);

    // TODO unit test
    public void contextInitialized(final ServletContextEvent sce) {
        if (isConfigurationPresent()) {
            return;
        }

        final File directoryOnClassPath = getDirectoryOnClassPath(sce);

        if ((directoryOnClassPath != null) && (directoryOnClassPath.isDirectory())) {
            try {
                createDefaultConfigurationFile(new File(directoryOnClassPath, Configuration.DEFAULT_JSON_CONFIGURATION_FILE),
                        getWebInfDirectory(sce));
            } catch (final URISyntaxException urie) {
                LOGGER.error("Failed to create a default configuration file", urie);
            }
        } else {
            LOGGER.error("Configuration file " + Configuration.DEFAULT_JSON_CONFIGURATION_FILE
                    + " not found and no way to create a default configuration on the classpath: RSB will not start properly!");
        }
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        // NOOP
    }

    private static boolean isConfigurationPresent() {
        return Thread.currentThread().getContextClassLoader().getResource(Configuration.DEFAULT_JSON_CONFIGURATION_FILE) != null;
    }

    private File getWebInfDirectory(final ServletContextEvent sce) {
        final String path = sce.getServletContext().getRealPath("/WEB-INF");
        if (StringUtils.isNotBlank(path)) {
            return new File(path);
        }
        return null;
    }

    private File getDirectoryOnClassPath(final ServletContextEvent sce) {
        final String path = sce.getServletContext().getRealPath("/WEB-INF/classes");
        if (StringUtils.isNotBlank(path)) {
            return new File(path);
        }
        return null;
    }

    private static void createDefaultConfigurationFile(final File defaultConfigurationFile, final File webInfDirectory)
            throws URISyntaxException {
        final PersistedConfiguration defaultConfiguration = new PersistedConfiguration();

        final File defaultRsbHomeDirectory = getDefaultRsbHomeDirectory(webInfDirectory);
        defaultConfiguration.setActiveMqWorkDirectory(new File(defaultRsbHomeDirectory, "activemq"));
        defaultConfiguration.setApplicationSpecificRserviPoolUris(null);
        defaultConfiguration.setCatalogRootDirectory(new File(defaultRsbHomeDirectory, "catalog"));
        defaultConfiguration.setDefaultRserviPoolUri(new URI("rmi://127.0.0.1/rservi-pool"));
        defaultConfiguration.setJobStatisticsHandlerClass(NoopJobStatisticsHandler.class.getName());
        defaultConfiguration.setJobTimeOut(600000);// 10 minutes
        defaultConfiguration.setNumberOfConcurrentJobWorkersPerQueue(5);
        defaultConfiguration.setResultsDirectory(new File(defaultRsbHomeDirectory, "results"));

        FileWriter fw = null;
        try {
            fw = new FileWriter(defaultConfigurationFile);
            IOUtils.copy(new StringReader(Util.toJson(defaultConfiguration)), fw);
            LOGGER.warn("Created default RSB configuration: "
                    + defaultConfigurationFile
                    + ". It is not production grade and will be wiped out in case of RSB redeployment. Please configure properly and move to another non-transient location on the classpath (for example TOMCAT_HOME/lib).");
        } catch (final IOException ioe) {
            LOGGER.error("Failed to create default RSB configuration file!", ioe);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    // by default create all the directories required by RSB under a single parent home
    // directory
    // note that this is not a requirement: each directory can be located in different locations
    private static File getDefaultRsbHomeDirectory(final File webInfDirectory) {
        // try in different potential locations
        final File[] potentialRsbHomeParentDirectories = { FileUtils.getUserDirectory(), webInfDirectory, FileUtils.getTempDirectory() };

        for (final File potentialRsbHomeParentDirectory : potentialRsbHomeParentDirectories) {
            if (potentialRsbHomeParentDirectory == null) {
                continue;
            }
            final File result = getOrCreateDefaultRsbHomeDirectory(potentialRsbHomeParentDirectory);
            if (result != null) {
                return result;
            }
        }

        throw new IllegalStateException("Failed to create RSB directories in all attempted locations");
    }

    private static File getOrCreateDefaultRsbHomeDirectory(final File rsbHomeParentDirectory) {
        final File rsbHomeDirectory = new File(rsbHomeParentDirectory, ".rsb");
        if (rsbHomeDirectory.isDirectory()) {
            return rsbHomeDirectory;
        }
        if (rsbHomeDirectory.mkdir()) {
            return rsbHomeDirectory;
        }
        LOGGER.info("Failed to create default RSB home directory under: " + rsbHomeDirectory);
        return null;
    }
}

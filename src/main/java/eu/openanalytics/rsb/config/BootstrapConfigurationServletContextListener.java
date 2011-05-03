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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        if (isGroovyConfigurationPresent()) {
            return;
        }

        final File directoryOnClassPath = getDirectoryOnClassPath(sce);

        if ((directoryOnClassPath != null) && (directoryOnClassPath.isDirectory())) {
            createDefaultConfigurationFile(new File(directoryOnClassPath, Configuration.GROOVY_CONFIGURATION_FILE));
        } else {
            LOGGER.error("Configuration file " + Configuration.GROOVY_CONFIGURATION_FILE
                    + " not found and no way to create a default configuration on the classpath: RSB will not start properly!");
        }
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        // NOOP
    }

    private static boolean isGroovyConfigurationPresent() {
        return Thread.currentThread().getContextClassLoader().getResource(Configuration.GROOVY_CONFIGURATION_FILE) != null;
    }

    private File getDirectoryOnClassPath(final ServletContextEvent sce) {
        final String path = sce.getServletContext().getRealPath("/WEB-INF/classes");
        if (StringUtils.isNotBlank(path)) {
            return new File(path);
        }
        return null;
    }

    private static void createDefaultConfigurationFile(final File defaultConfigurationFile) {
        FileWriter fw = null;

        try {
            fw = new FileWriter(defaultConfigurationFile);
            IOUtils.copy(new StringReader(Configuration.DEFAULT_CONFIGURATION_CONTENT), fw);
            LOGGER.warn("Created default RSB configuration: "
                    + defaultConfigurationFile
                    + ". It is not production grade and will be wiped out in case of RSB redeployment. Please configure properly and move to another non-transient location on the classpath (for example TOMCAT_HOME/lib).");
        } catch (final IOException ioe) {
            LOGGER.error("Failed to create default RSB configuration file!", ioe);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }
}

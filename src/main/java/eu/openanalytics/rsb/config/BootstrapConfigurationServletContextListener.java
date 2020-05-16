/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2020
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 */

package eu.openanalytics.rsb.config;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.PersistedConfiguration.PersistedSmtpConfiguration;

/**
 * Verifies that the RSB configuration is loadable and, if not, creates a default one so RSB can
 * start anyway.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class BootstrapConfigurationServletContextListener implements ServletContextListener
{
    public static final String RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM = "rsbConfiguration";

    private static final Log LOGGER = LogFactory.getLog(BootstrapConfigurationServletContextListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        copyUiFragments(sce.getServletContext());

        if (isConfigurationPresent(sce.getServletContext()))
        {
            return;
        }

        final File configurationDirectory = getWritableConfigurationDirectory(sce);

        if (configurationDirectory != null)
        {
            try
            {
                createDefaultConfigurationFile(new File(configurationDirectory,
                    Configuration.DEFAULT_JSON_CONFIGURATION_FILE), getWebInfDirectory(sce));
            }
            catch (final URISyntaxException urie)
            {
                LOGGER.error("Failed to create a default configuration file", urie);
            }
        }
        else
        {
            LOGGER.error("Configuration file " + Configuration.DEFAULT_JSON_CONFIGURATION_FILE
                         + " not found and no way to create a default configuration in "
                         + ConfigurationFactory.RSB_CONFIGURATION_DIRECTORY
                         + " or on the classpath: RSB will not start properly!");
        }
    }

    private void copyUiFragments(final ServletContext servletContext)
    {
        final File fragmentsSourceDirectory = new File(ConfigurationFactory.RSB_CONFIGURATION_DIRECTORY,
            "fragments");
        if (!fragmentsSourceDirectory.isDirectory())
        {
            return;
        }

        final String fragmentsDestinationPath = servletContext.getRealPath("/fragments");
        if (StringUtils.isBlank(fragmentsDestinationPath))
        {
            return;
        }

        final File fragmentsDestinationDirectory = new File(fragmentsDestinationPath);

        final File[] sourceFragments = fragmentsSourceDirectory.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(final File dir, final String name)
            {
                return StringUtils.endsWith(name, ".html");
            }
        });

        if (sourceFragments == null)
        {
            return;
        }

        for (final File sourceFragment : sourceFragments)
        {
            final File destinationFragment = new File(fragmentsDestinationDirectory, sourceFragment.getName());

            try
            {
                FileCopyUtils.copy(sourceFragment, destinationFragment);
                LOGGER.info("Installed UI fragment: " + sourceFragment.getName());
            }
            catch (final IOException ioe)
            {
                LOGGER.error("Failed to copy UI fragment from: " + sourceFragment + " to: "
                             + destinationFragment, ioe);
            }
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        // NOOP
    }

    private static boolean isConfigurationPresent(final ServletContext servletContext)
    {
        final String configurationFile = getConfiguredConfigurationFileName(servletContext);
        Validate.notEmpty(configurationFile,
            "No configuration specified in web.xml: servlet context parameter "
                            + RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM + " is missing");

        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_NAME_SYSTEM_PROPERTY, configurationFile);

        final boolean configurationPresent = ConfigurationFactory.isConfigurationPresent();

        if (configurationPresent)
        {
            LOGGER.info("Successfully located configured configuration file: " + configurationFile);
        }

        return configurationPresent;
    }

    private static String getConfiguredConfigurationFileName(final ServletContext servletContext)
    {
        return servletContext.getInitParameter(RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM);
    }

    private File getWebInfDirectory(final ServletContextEvent sce)
    {
        final String path = sce.getServletContext().getRealPath("/WEB-INF");
        if (StringUtils.isNotBlank(path))
        {
            return new File(path);
        }
        return null;
    }

    File getWritableConfigurationDirectory(final ServletContextEvent sce)
    {
        File rsbConfigurationDirectory = new File(ConfigurationFactory.RSB_CONFIGURATION_DIRECTORY);
        if (rsbConfigurationDirectory.isDirectory() && rsbConfigurationDirectory.canWrite())
        {
            return rsbConfigurationDirectory;
        }

        final String path = sce.getServletContext().getRealPath("/WEB-INF/classes");
        if (StringUtils.isNotBlank(path))
        {
            rsbConfigurationDirectory = new File(path);
            if (rsbConfigurationDirectory.isDirectory() && rsbConfigurationDirectory.canWrite())
            {
                return rsbConfigurationDirectory;
            }
        }

        return null;
    }

    // exposed for testing
    private static void createDefaultConfigurationFile(final File defaultConfigurationFile,
                                                       final File webInfDirectory) throws URISyntaxException
    {

        final PersistedConfiguration defaultConfiguration = createDefaultConfiguration(webInfDirectory);

        try
        {
            Util.toPrettyJsonFile(defaultConfiguration, defaultConfigurationFile);
            LOGGER.warn("Created default RSB configuration: "
                        + defaultConfigurationFile
                        + ". It is not production grade and will be wiped out in case of RSB redeployment. Please configure properly and move to another non-transient location on the classpath (for example TOMCAT_HOME/lib).");
        }
        catch (final IOException ioe)
        {
            LOGGER.error("Failed to create default RSB configuration file!", ioe);
        }
    }

    // exposed for unit testing
    static PersistedConfiguration createDefaultConfiguration(final File webInfDirectory)
        throws URISyntaxException
    {
        final File defaultRsbHomeDirectory = getDefaultRsbHomeDirectory(webInfDirectory);
        final PersistedConfiguration defaultConfiguration = new PersistedConfiguration();
        defaultConfiguration.setActiveMqWorkDirectory(new File(defaultRsbHomeDirectory, "activemq"));
        defaultConfiguration.setCatalogRootDirectory(new File(defaultRsbHomeDirectory, "catalog"));
        defaultConfiguration.setDefaultRserviPoolUri(new URI("rmi://127.0.0.1/rpooli-pool"));
        defaultConfiguration.setJobTimeOut(600000);// 10 minutes
        defaultConfiguration.setNumberOfConcurrentJobWorkersPerQueue(5);
        defaultConfiguration.setResultsDirectory(new File(defaultRsbHomeDirectory, "results"));
        defaultConfiguration.setSmtpConfiguration(new PersistedSmtpConfiguration("localhost", 25, "", ""));
        return defaultConfiguration;
    }

    // exposed for unit testing
    // by default create all the directories required by RSB under a single parent home directory
    // note that this is not a requirement: each directory can be located in different locations
    static File getDefaultRsbHomeDirectory(final File webInfDirectory)
    {
        // try in different potential locations
        final File[] potentialRsbHomeParentDirectories = {FileUtils.getUserDirectory(), webInfDirectory,
            FileUtils.getTempDirectory()};

        for (final File potentialRsbHomeParentDirectory : potentialRsbHomeParentDirectories)
        {
            if (potentialRsbHomeParentDirectory == null)
            {
                continue;
            }
            final File result = getOrCreateDefaultRsbHomeDirectory(potentialRsbHomeParentDirectory);
            if (result != null)
            {
                return result;
            }
        }

        throw new IllegalStateException("Failed to create RSB directories in all attempted locations");
    }

    private static File getOrCreateDefaultRsbHomeDirectory(final File rsbHomeParentDirectory)
    {
        final File rsbHomeDirectory = new File(rsbHomeParentDirectory, ".rsb");
        if (rsbHomeDirectory.isDirectory())
        {
            return rsbHomeDirectory;
        }
        if (rsbHomeDirectory.mkdir())
        {
            return rsbHomeDirectory;
        }
        LOGGER.info("Failed to create default RSB home directory under: " + rsbHomeDirectory);
        return null;
    }
}

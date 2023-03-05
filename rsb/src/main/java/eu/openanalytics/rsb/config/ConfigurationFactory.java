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

package eu.openanalytics.rsb.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration.ApplicationSecurityAuthorization;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.config.Configuration.JmxConfiguration;
import eu.openanalytics.rsb.data.FileCatalogManager;


/**
 * Loads and validated an RSB configuration file and builds a configuration object out of it.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class ConfigurationFactory
{
    public static final String RSB_CONFIGURATION_DIRECTORY = "/etc/rsb";

    static final String CONFIGURATION_FILE_NAME_SYSTEM_PROPERTY = Configuration.class.getName();

    private static final Log LOGGER = LogFactory.getLog(ConfigurationFactory.class);

    private ConfigurationFactory()
    {
        throw new UnsupportedOperationException("do not instantiate");
    }

    static boolean isConfigurationPresent()
    {
        return getConfigurationUrl() != null;
    }

	public static Configuration loadJsonConfiguration() throws IOException {	
		final URL configurationUrl= Validate.notNull(getConfigurationUrl(), String.format(
				"The configuration file ('%1$s') is not present.", getConfigurationFileName() ));
		
		final PersistedConfigurationAdapter pca= load(configurationUrl);
        validateConfiguration(pca);
        createMissingDirectories(pca);
        return pca;
    }

    private static String getConfigurationFileName()
    {
        return System.getProperty(CONFIGURATION_FILE_NAME_SYSTEM_PROPERTY,
            Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
    }

    public static Configuration loadJsonConfiguration(final InputStream is) throws IOException
    {
        final PersistedConfigurationAdapter pca = loadConfigurationStream(null, is);
        validateConfiguration(pca);
        createMissingDirectories(pca);
        return pca;
    }

    private static void validateConfiguration(final PersistedConfigurationAdapter pca) throws IOException
    {
        if (BooleanUtils.toBoolean(System.getProperty(ConfigurationFactory.class.getName()
                                                      + ".skipValidation")))
        {
            LOGGER.info("Skipped validation of: " + pca);
            return;
        }

        final Set<String> validationErrors = validate(pca);
        Validate.isTrue(validationErrors.isEmpty(),
            "Validation error(s):\n" + StringUtils.join(validationErrors, "\n")
                            + "\nfound in configuration:\n" + pca);
        LOGGER.info("Successfully validated: " + pca.exportAsJson());
    }

    // exposed for testing
    static PersistedConfigurationAdapter load(final URL configurationUrl) throws IOException
    {
        final InputStream is = configurationUrl.openStream();
        return loadConfigurationStream(configurationUrl, is);
    }

    private static URL getConfigurationUrl()
    {
        final File configurationFile = new File(RSB_CONFIGURATION_DIRECTORY, getConfigurationFileName());
        if (configurationFile.isFile() && configurationFile.canRead())
        {
            try
            {
                return configurationFile.toURI().toURL();
            }
            catch (final MalformedURLException murle)
            {
                throw new IllegalStateException("Unexpected toURL failure for file: " + configurationFile,
                    murle);
            }
        }
        else
        {
            return Thread.currentThread().getContextClassLoader().getResource(getConfigurationFileName());
        }
    }

    private static PersistedConfigurationAdapter loadConfigurationStream(final URL configurationUrl,
                                                                         final InputStream is)
        throws IOException
    {
        try(final InputStream autoCloseIs = is) {
          final String json = IOUtils.toString(is, Charset.defaultCharset());

          final PersistedConfiguration pc = Util.fromJson(json, PersistedConfiguration.class);
          final PersistedConfigurationAdapter pca = new PersistedConfigurationAdapter(configurationUrl, pc);
          return pca;
        }
    }

    static Set<String> validate(final PersistedConfigurationAdapter pca) throws IOException
    {
        final Set<String> validationErrors = new HashSet<>();

        LOGGER.info("Validating configuration: " + pca.getConfigurationUrl());
        validateNotNull(pca.getActiveMqWorkDirectory(), "activeMqWorkDirectory", validationErrors);
        validateNotNull(pca.getResultsDirectory(), "rsbResultsDirectory", validationErrors);
        validateNotNull(pca.getDefaultRserviPoolUri(), "defaultRserviPoolUri", validationErrors);
        validateNotNull(pca.getSmtpConfiguration(), "smtpConfiguration", validationErrors);

        if (StringUtils.isNotEmpty(pca.getAdministratorEmail()))
        {
            validateIsTrue(EmailValidator.getInstance().isValid(pca.getAdministratorEmail()),
                "if present, the administrator email must be valid", validationErrors);
        }

        if (pca.getDepositRootDirectories() != null)
        {

            for (final DepositDirectoryConfiguration depositRootDirectoryConfig : pca.getDepositRootDirectories())
            {
                final String depositApplicationName = depositRootDirectoryConfig.getApplicationName();
                validateIsTrue(Util.isValidApplicationName(depositApplicationName),
                    "invalid deposit directory application name: " + depositApplicationName, validationErrors);
            }
        }

        if (pca.getDepositEmailAccounts() != null)
        {
            final FileCatalogManager fileCatalogManager = new FileCatalogManager();
            fileCatalogManager.setConfiguration(pca);

            for (final DepositEmailConfiguration depositEmailAccount : pca.getDepositEmailAccounts())
            {
                final String applicationName = depositEmailAccount.getApplicationName();
				
				validateIsTrue(Util.isValidApplicationName(applicationName),
						"invalid deposit email application name: " + applicationName,
						validationErrors );
				
				if (depositEmailAccount.getResponseFileName() != null) {
					final Path responseFile= fileCatalogManager.internalGetCatalogFile(
							CatalogSection.EMAIL_REPLIES,
							applicationName, depositEmailAccount.getResponseFileName() );
					
					validateIsTrue(Files.isRegularFile(responseFile),
							"missing response file: " + responseFile,
							validationErrors );
				}
				
				if (depositEmailAccount.getJobConfigurationFileName() != null) {
					final Path jobConfigurationFile= fileCatalogManager.internalGetCatalogFile(
							CatalogSection.JOB_CONFIGURATIONS,
							applicationName, depositEmailAccount.getJobConfigurationFileName() );
					
					validateIsTrue(Files.isRegularFile(jobConfigurationFile),
							"missing job configuration file: " + jobConfigurationFile,
							validationErrors );
                }
            }
        }

        if (pca.getDataDirectories() != null)
        {
            for (final File dataDirectoryRoot : pca.getDataDirectories())
            {
                validateIsTrue(dataDirectoryRoot.isDirectory(), "invalid data directory: "
                                                                + dataDirectoryRoot, validationErrors);
            }
        }

        if (pca.getApplicationSecurityConfiguration() != null)
        {
            for (final Entry<String, ApplicationSecurityAuthorization> applicationSecurityConfiguration : pca.getApplicationSecurityConfiguration()
                .entrySet())
            {
                validateIsTrue(Util.isValidApplicationName(applicationSecurityConfiguration.getKey()),
                    "invalid deposit application security authorization application name: "
                                    + applicationSecurityConfiguration.getKey(), validationErrors);
            }
        }

        if (pca.getJmxConfiguration() != null)
        {
            final JmxConfiguration jmxConfiguration = pca.getJmxConfiguration();

            if (StringUtils.isNotBlank(jmxConfiguration.getHttpAuthenticationUsername()))
            {
                validateIsTrue(StringUtils.isNotBlank(jmxConfiguration.getHttpAuthenticationPassword()),
                    "Both username and password must be provided when securing the JMX HTTP console",
                    validationErrors);
            }
        }

        return validationErrors;
    }

    private static void validateNotNull(final Object o, final String field, final Set<String> validationErrors)
    {
        if (o == null)
        {
            validationErrors.add(field + " can't be null");
        }
    }

    private static void validateIsTrue(final boolean b,
                                       final String message,
                                       final Set<String> validationErrors)
    {
        if (!b)
        {
            validationErrors.add(message);
        }
    }

    private static void createMissingDirectories(final PersistedConfigurationAdapter pca) throws IOException
    {
        if (pca.getDepositRootDirectories() != null)
        {
			for (final DepositDirectoryConfiguration depositRootDirectoryConfig : pca.getDepositRootDirectories()) {
				createDepositeDirectories(depositRootDirectoryConfig.getRootDirectory().toPath());
			}
		}
	}
	
	
	public static void createDepositeDirectories(final Path root) throws IOException {
		Files.createDirectories(root);
		Files.createDirectories(root.resolve(Configuration.DEPOSIT_ACCEPTED_SUBDIR));
		Files.createDirectories(root.resolve(Configuration.DEPOSIT_JOBS_SUBDIR));
		Files.createDirectories(root.resolve(Configuration.DEPOSIT_RESULTS_SUBDIR));
	}
	
}

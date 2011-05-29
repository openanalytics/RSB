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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;

/**
 * Loads and validated an RSB configuration file and builds a configuration object out of it.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public abstract class ConfigurationFactory {
    private static final Log LOGGER = LogFactory.getLog(ConfigurationFactory.class);

    private ConfigurationFactory() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    public static Configuration loadJsonConfiguration() throws IOException {
        final String configurationFile = System.getProperty(Configuration.class.getName(), Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
        final PersistedConfigurationAdapter pca = loadAndValidateJsonConfigurationFile(configurationFile);
        createMissingDirectories(pca);
        return pca;
    }

    private static PersistedConfigurationAdapter loadAndValidateJsonConfigurationFile(final String configurationFile) throws IOException {
        final PersistedConfigurationAdapter pca = load(configurationFile);
        final Set<String> validationErrors = validate(pca);
        Validate.isTrue(validationErrors.isEmpty(), "Validation error(s):\n" + StringUtils.join(validationErrors, "\n")
                + "\nfound in configuration:\n" + pca);
        LOGGER.info("Successfully validated: " + pca);
        return pca;
    }

    // exposed for testing
    static PersistedConfigurationAdapter load(final String configurationFile) throws IOException {
        final URL configurationUrl = Thread.currentThread().getContextClassLoader().getResource(configurationFile);
        Validate.notNull(configurationUrl, "Impossible to find " + configurationFile + " on the classpath");

        final InputStream is = configurationUrl.openStream();
        final String json = IOUtils.toString(is);
        IOUtils.closeQuietly(is);

        final PersistedConfiguration pc = Util.fromJson(json, PersistedConfiguration.class);
        final PersistedConfigurationAdapter pca = new PersistedConfigurationAdapter(configurationUrl, pc);
        return pca;
    }

    static Set<String> validate(final PersistedConfigurationAdapter pca) throws IOException {
        final Set<String> validationErrors = new HashSet<String>();

        LOGGER.info("Validating configuration: " + pca.getConfigurationUrl());
        validateNotNull(pca.getActiveMqWorkDirectory(), "activeMqWorkDirectory", validationErrors);
        validateNotNull(pca.getResultsDirectory(), "rsbResultsDirectory", validationErrors);
        validateNotNull(pca.getDefaultRserviPoolUri(), "defaultRserviPoolUri", validationErrors);
        validateNotNull(pca.getSmtpConfiguration(), "smtpConfiguration", validationErrors);

        if (StringUtils.isNotEmpty(pca.getAdministratorEmail())) {
            validateIsTrue(EmailValidator.getInstance().isValid(pca.getAdministratorEmail()),
                    "if present, the administrator email must be valid", validationErrors);
        }

        if (pca.getDepositRootDirectories() != null) {

            for (final DepositDirectoryConfiguration depositRootDirectoryConfig : pca.getDepositRootDirectories()) {
                final String depositApplicationName = depositRootDirectoryConfig.getApplicationName();
                validateIsTrue(Util.isValidApplicationName(depositApplicationName), "invalid deposit directory application name: "
                        + depositApplicationName, validationErrors);
            }
        }

        if (pca.getDepositEmailAccounts() != null) {
            for (final DepositEmailConfiguration depositEmailAccount : pca.getDepositEmailAccounts()) {
                validateIsTrue(Util.isValidApplicationName(depositEmailAccount.getApplicationName()),
                        "invalid deposit email application name: " + depositEmailAccount.getApplicationName(), validationErrors);

                if (depositEmailAccount.getResponseFileName() != null) {
                    final File responseFile = new File(pca.getEmailRepliesCatalogDirectory(), depositEmailAccount.getResponseFileName());
                    validateIsTrue(responseFile.exists(), "missing response file: " + responseFile, validationErrors);
                }

                if (depositEmailAccount.getJobConfigurationFileName() != null) {
                    final File jobConfigurationFile = new File(pca.getJobConfigurationCatalogDirectory(),
                            depositEmailAccount.getJobConfigurationFileName());
                    validateIsTrue(jobConfigurationFile.exists(), "missing job configuration file: " + jobConfigurationFile,
                            validationErrors);
                }
            }
        }
        return validationErrors;
    }

    private static void validateNotNull(final Object o, final String field, final Set<String> validationErrors) {
        if (o == null) {
            validationErrors.add(field + " can't be null");
        }
    }

    private static void validateIsTrue(final boolean b, final String message, final Set<String> validationErrors) {
        if (!b) {
            validationErrors.add(message);
        }
    }

    private static void createMissingDirectories(final PersistedConfigurationAdapter pca) throws IOException {
        FileUtils.forceMkdir(pca.getRScriptsCatalogDirectory());
        FileUtils.forceMkdir(pca.getSweaveFilesCatalogDirectory());
        FileUtils.forceMkdir(pca.getJobConfigurationCatalogDirectory());
        FileUtils.forceMkdir(pca.getEmailRepliesCatalogDirectory());

        if (pca.getDepositRootDirectories() != null) {
            for (final DepositDirectoryConfiguration depositRootDirectoryConfig : pca.getDepositRootDirectories()) {
                final File depositRootDir = depositRootDirectoryConfig.getRootDirectory();
                FileUtils.forceMkdir(depositRootDir);
                FileUtils.forceMkdir(new File(depositRootDir, Configuration.DEPOSIT_ACCEPTED_SUBDIR));
                FileUtils.forceMkdir(new File(depositRootDir, Configuration.DEPOSIT_JOBS_SUBDIR));
                FileUtils.forceMkdir(new File(depositRootDir, Configuration.DEPOSIT_RESULTS_SUBDIR));
            }
        }
    }
}

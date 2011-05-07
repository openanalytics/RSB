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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.openanalytics.rsb.Util;

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

    public static Configuration loadJsonConfiguration(final String configurationFile) throws IOException {
        final URL configurationUrl = Thread.currentThread().getContextClassLoader().getResource(configurationFile);
        Validate.notNull(configurationUrl, "Impossible to find " + configurationFile + " on the classpath");

        final InputStream is = configurationUrl.openStream();
        final String json = IOUtils.toString(is);
        IOUtils.closeQuietly(is);

        final PersistedConfiguration pc = Util.fromJson(json, PersistedConfiguration.class);
        final PersistedConfigurationAdapter pca = new PersistedConfigurationAdapter(configurationUrl, pc);
        validate(pca);
        return pca;
    }

    // exposed for testing
    static void validate(final PersistedConfigurationAdapter pca) throws IOException {
        LOGGER.info("Validating configuration: " + pca.getConfigurationUrl());

        Validate.notNull(pca.getActiveMqWorkDirectory(), "activeMqWorkDirectory can't be null: " + pca);

        Validate.notNull(pca.getResultsDirectory(), "rsbResultsDirectory can't be null: " + pca);

        Validate.notNull(pca.getDefaultRserviPoolUri(), "defaultRserviPoolUri can't be null: " + pca);

        Validate.notNull(pca.getSmtpConfiguration(), "smtpConfiguration can't be null: " + pca);

        if (pca.getRScriptsCatalogDirectory() != null) {
            FileUtils.forceMkdir(pca.getRScriptsCatalogDirectory());
        }

        if (pca.getSweaveFilesCatalogDirectory() != null) {
            FileUtils.forceMkdir(pca.getSweaveFilesCatalogDirectory());
        }

        if (pca.getEmailRepliesCatalogDirectory() != null) {
            FileUtils.forceMkdir(pca.getEmailRepliesCatalogDirectory());
        }

        LOGGER.info("Successfully validated: " + pca);
    }
}

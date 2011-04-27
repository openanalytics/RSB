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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Configuration validator.
 * 
 * @author rsb.development@openanalytics.eu
 */
public class ConfigurationValidator {
    private ConfigurationValidator() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    /**
     * Validates the configuration, throwing exceptions in case of problems.
     * 
     * @throws IOException
     */
    public static void validate(final Configuration configuration) throws IOException {
        Validate.notNull(configuration.getResultsDirectory(), "rsbResultsDirectory can't be null: " + configurationAsString(configuration));

        Validate.notNull(configuration.getActiveMqWorkDirectory(), "activeMqWorkDirectory can't be null: "
                + configurationAsString(configuration));

        Validate.notNull(configuration.getDefaultRserviPoolUri(), "defaultRserviPoolUri can't be null: "
                + configurationAsString(configuration));

        if (configuration.getRScriptsCatalogDirectory() != null) {
            FileUtils.forceMkdir(configuration.getRScriptsCatalogDirectory());
            // Validate.isTrue(configuration.getRScriptsCatalogDirectory().isDirectory(),
            // "if configured, rScriptsCatalogDirectory must be an existing directory: " +
            // configurationAsString(configuration));
        }

        if (configuration.getSweaveFilesCatalogDirectory() != null) {
            FileUtils.forceMkdir(configuration.getSweaveFilesCatalogDirectory());
            // Validate.isTrue(configuration.getSweaveFilesCatalogDirectory().isDirectory(),
            // "if configured, sweaveFilesCatalogDirectory must be an existing directory: " +
            // configurationAsString(configuration));
        }

        if (configuration.getEmailRepliesCatalogDirectory() != null) {
            FileUtils.forceMkdir(configuration.getEmailRepliesCatalogDirectory());
            // Validate.isTrue(configuration.getEmailRepliesCatalogDirectory().isDirectory(),
            // "if configured, emailRepliesCatalogDirectory must be an existing directory: " +
            // configurationAsString(configuration));
        }
    }

    private static String configurationAsString(final Configuration configuration) {
        return ToStringBuilder.reflectionToString(configuration, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

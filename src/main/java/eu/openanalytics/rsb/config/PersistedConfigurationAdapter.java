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
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Adapts a {@link PersistedConfiguration} into a {@link Configuration}.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class PersistedConfigurationAdapter implements Configuration {
    private final transient PersistedConfiguration persistedConfiguration;
    private final URL configurationUrl;

    private final File rScriptsCatalogDirectory;
    private final File sweaveFilesCatalogDirectory;
    private final File emailRepliesCatalogDirectory;

    public PersistedConfigurationAdapter(final URL configurationUrl, final PersistedConfiguration persistedConfiguration) {
        this.configurationUrl = configurationUrl;
        this.persistedConfiguration = persistedConfiguration;

        rScriptsCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(), Configuration.R_SCRIPTS_CATALOG_SUBDIR);
        sweaveFilesCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(), Configuration.SWEAVE_FILE_CATALOG_SUBDIR);
        emailRepliesCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(),
                Configuration.EMAIL_REPLIES_CATALOG_SUBDIR);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE) + "\n"
                + ToStringBuilder.reflectionToString(persistedConfiguration, ToStringStyle.MULTI_LINE_STYLE);
    }

    public URL getConfigurationUrl() {
        return configurationUrl;
    }

    public File getActiveMqWorkDirectory() {
        return persistedConfiguration.getActiveMqWorkDirectory();
    }

    public int getJobTimeOut() {
        return persistedConfiguration.getJobTimeOut();
    }

    public int getNumberOfConcurrentJobWorkersPerQueue() {
        return persistedConfiguration.getNumberOfConcurrentJobWorkersPerQueue();
    }

    public File getResultsDirectory() {
        return persistedConfiguration.getResultsDirectory();
    }

    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return persistedConfiguration.getApplicationSpecificRserviPoolUris();
    }

    public URI getDefaultRserviPoolUri() {
        return persistedConfiguration.getDefaultRserviPoolUri();
    }

    public String getJobStatisticsHandlerClass() {
        return persistedConfiguration.getJobStatisticsHandlerClass();
    }

    public Map<String, Object> getJobStatisticsHandlerConfiguration() {
        return persistedConfiguration.getJobStatisticsHandlerConfiguration();
    }

    public File getRScriptsCatalogDirectory() {
        return rScriptsCatalogDirectory;
    }

    public File getSweaveFilesCatalogDirectory() {
        return sweaveFilesCatalogDirectory;
    }

    public File getEmailRepliesCatalogDirectory() {
        return emailRepliesCatalogDirectory;
    }
}

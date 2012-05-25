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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.config.PersistedConfiguration.PersistedJobStatisticsHandlerConfiguration;

/**
 * Adapts a {@link PersistedConfiguration} into a {@link Configuration}.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class PersistedConfigurationAdapter implements Configuration {
    private static final long serialVersionUID = 1L;

    public static final JmxConfiguration DEFAULT_JMX_RMI_CONFIGURATION = new PersistedConfiguration.PersistedJmxConfiguration(9098, 9099,
            8889);
    private final PersistedConfiguration persistedConfiguration;
    private final URL configurationUrl;
    private final String nodeName;
    private final File rScriptsCatalogDirectory;
    private final File sweaveFilesCatalogDirectory;
    private final File jobConfigurationCatalogDirectory;
    private final File emailRepliesCatalogDirectory;

    public PersistedConfigurationAdapter(final URL configurationUrl, final PersistedConfiguration persistedConfiguration) {
        this.persistedConfiguration = persistedConfiguration;
        this.configurationUrl = configurationUrl;

        nodeName = StringUtils.isNotBlank(persistedConfiguration.getNodeName()) ? persistedConfiguration.getNodeName()
                : getDefaultNodeName();

        rScriptsCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(), Configuration.Catalog.R_SCRIPTS.getSubDir());
        sweaveFilesCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(),
                Configuration.Catalog.SWEAVE_FILES.getSubDir());
        jobConfigurationCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(),
                Configuration.Catalog.JOB_CONFIGURATIONS.getSubDir());
        emailRepliesCatalogDirectory = new File(persistedConfiguration.getCatalogRootDirectory(),
                Configuration.Catalog.EMAIL_REPLIES.getSubDir());
    }

    private String getDefaultNodeName() {
        // find something unique about the running node like the location of resource
        final URL resourceUrl = getClass().getResource("/META-INF/spring/core-beans.xml");
        final String uniqueId = Long.toHexString(Math.abs((long) resourceUrl.toExternalForm().hashCode()));
        return StringUtils.lowerCase(Constants.HOST_NAME + "-" + uniqueId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE) + "\n" + persistedConfiguration.toString();
    }

    // for JMX access
    public String exportAsString() {
        return toString();
    }

    // for JMX access
    public String exportAsJson() {
        return Util.toJson(persistedConfiguration);
    }

    public URL getConfigurationUrl() {
        return configurationUrl;
    }

    public String getNodeName() {
        return nodeName;
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

    public String getAdministratorEmail() {
        return persistedConfiguration.getAdministratorEmail();
    }

    public SmtpConfiguration getSmtpConfiguration() {
        return persistedConfiguration.getSmtpConfiguration();
    }

    public JmxConfiguration getJmxConfiguration() {
        if (persistedConfiguration.getJmxConfiguration() == null) {
            return DEFAULT_JMX_RMI_CONFIGURATION;
        }

        return persistedConfiguration.getJmxConfiguration();
    }

    public JobStatisticsHandlerConfiguration getJobStatisticsHandlerConfiguration() {
        final PersistedJobStatisticsHandlerConfiguration persisted = persistedConfiguration.getJobStatisticsHandlerConfiguration();
        return persisted != null ? persisted : new PersistedJobStatisticsHandlerConfiguration();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<DepositDirectoryConfiguration> getDepositRootDirectories() {
        return (List) persistedConfiguration.getDepositRootDirectories();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<DepositEmailConfiguration> getDepositEmailAccounts() {
        return (List) persistedConfiguration.getDepositEmailAccounts();
    }

    public File getRScriptsCatalogDirectory() {
        return rScriptsCatalogDirectory;
    }

    public File getSweaveFilesCatalogDirectory() {
        return sweaveFilesCatalogDirectory;
    }

    public File getJobConfigurationCatalogDirectory() {
        return jobConfigurationCatalogDirectory;
    }

    public File getEmailRepliesCatalogDirectory() {
        return emailRepliesCatalogDirectory;
    }

    public List<File> getDataDirectories() {
        return persistedConfiguration.getDataDirectories();
    }

    @Override
    public Config getRServiClientPoolConfig() {
        return persistedConfiguration.getrServiClientPoolConfig();
    }
}

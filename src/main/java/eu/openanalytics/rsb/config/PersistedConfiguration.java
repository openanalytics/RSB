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
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.config.Configuration.JmxConfiguration;
import eu.openanalytics.rsb.config.Configuration.JobStatisticsHandlerConfiguration;
import eu.openanalytics.rsb.config.Configuration.SmtpConfiguration;

/**
 * Defines the persisted configuration of RSB, from which the actual {@link Configuration} is derived.
 * 
 * @see Configuration
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class PersistedConfiguration implements Serializable {
    public static class PersistedSmtpConfiguration implements SmtpConfiguration {
        private static final long serialVersionUID = 1L;
        private String host;
        private int port;
        private String username;
        private String password;

        public PersistedSmtpConfiguration(final String host, final int port, final String username, final String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public PersistedSmtpConfiguration() {
            // NOOP
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }
    }

    public static class PersistedJmxConfiguration implements JmxConfiguration {
        private static final long serialVersionUID = 1L;
        private int stubPort;
        private int registryPort;
        private int httpPort;

        public PersistedJmxConfiguration(final int stubPort, final int registryPort, final int httpPort) {
            this.stubPort = stubPort;
            this.registryPort = registryPort;
            this.httpPort = httpPort;
        }

        public PersistedJmxConfiguration() {
            // NOOP
        }

        public int getStubPort() {
            return stubPort;
        }

        public void setStubPort(final int stubPort) {
            this.stubPort = stubPort;
        }

        public int getRegistryPort() {
            return registryPort;
        }

        public void setRegistryPort(final int registryPort) {
            this.registryPort = registryPort;
        }

        public int getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(final int httpPort) {
            this.httpPort = httpPort;
        }
    }

    public static class PersistedJobStatisticsHandlerConfiguration implements JobStatisticsHandlerConfiguration {
        private static final long serialVersionUID = 1L;
        private String className;
        private Map<String, Object> parameters;

        public PersistedJobStatisticsHandlerConfiguration(final String className, final Map<String, Object> parameters) {
            this.className = className;
            this.parameters = parameters;
        }

        public PersistedJobStatisticsHandlerConfiguration() {
            // NOOP
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(final String className) {
            this.className = className;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(final Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static class PersistedDepositDirectoryConfiguration implements DepositDirectoryConfiguration {
        private static final long serialVersionUID = 1L;
        private File rootDirectory;
        private String applicationName;
        private long pollingPeriod;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public File getRootDirectory() {
            return rootDirectory;
        }

        public void setRootDirectory(final File rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(final String applicationName) {
            this.applicationName = applicationName;
        }

        public long getPollingPeriod() {
            return pollingPeriod;
        }

        public void setPollingPeriod(final long pollingPeriod) {
            this.pollingPeriod = pollingPeriod;
        }
    }

    public static class PersistedDepositEmailConfiguration implements DepositEmailConfiguration {
        private static final long serialVersionUID = 1L;
        private URI accountURI;
        private String applicationName;
        private long pollingPeriod;
        private String responseFileName;
        private String jobConfigurationFileName;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public URI getAccountURI() {
            return accountURI;
        }

        public void setAccountURI(final URI accountURI) {
            this.accountURI = accountURI;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(final String applicationName) {
            this.applicationName = applicationName;
        }

        public long getPollingPeriod() {
            return pollingPeriod;
        }

        public void setPollingPeriod(final long pollingPeriod) {
            this.pollingPeriod = pollingPeriod;
        }

        public String getResponseFileName() {
            return responseFileName;
        }

        public void setResponseFileName(final String responseFileName) {
            this.responseFileName = responseFileName;
        }

        public String getJobConfigurationFileName() {
            return jobConfigurationFileName;
        }

        public void setJobConfigurationFileName(final String jobConfigurationFileName) {
            this.jobConfigurationFileName = jobConfigurationFileName;
        }
    }

    private static final long serialVersionUID = 1L;
    private File activeMqWorkDirectory;
    private URI defaultRserviPoolUri;
    private int jobTimeOut;
    private int numberOfConcurrentJobWorkersPerQueue;
    private File catalogRootDirectory;
    private File resultsDirectory;
    private Map<String, URI> applicationSpecificRserviPoolUris;
    private PersistedJobStatisticsHandlerConfiguration jobStatisticsHandlerConfiguration;
    private String administratorEmail;
    private PersistedSmtpConfiguration smtpConfiguration;
    private PersistedJmxConfiguration jmxConfiguration;
    private List<PersistedDepositDirectoryConfiguration> depositRootDirectories;
    private List<PersistedDepositEmailConfiguration> depositEmailAccounts;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * Directory under which RSB catalogs are located. The catalogs are:
     * <ul>
     * <li>{@link eu.openanalytics.rsb.config.Configuration#R_SCRIPTS_CATALOG_SUBDIR}: catalog of R scripts</li>
     * <li>{@link eu.openanalytics.rsb.config.Configuration#SWEAVE_FILE_CATALOG_SUBDIR}: catalog of Sweave files</li>
     * <li>{@link eu.openanalytics.rsb.config.Configuration#JOB_CONFIGURATION_CATALOG_SUBDIR}: catalog of ready made job
     * configurations</li>
     * <li>{@link eu.openanalytics.rsb.config.Configuration#EMAIL_REPLIES_CATALOG_SUBDIR}: catalog of Email replies</li>
     * </ul>
     * If any of these sub-directories do not pre-exist, RSB will try to create it.
     */
    public File getCatalogRootDirectory() {
        return catalogRootDirectory;
    }

    public void setCatalogRootDirectory(final File catalogRootDirectory) {
        this.catalogRootDirectory = catalogRootDirectory;
    }

    /**
     * Directory where ActiveMQ stores its persisted data.
     */
    public File getActiveMqWorkDirectory() {
        return activeMqWorkDirectory;
    }

    public void setActiveMqWorkDirectory(final File activeMqWorkDirectory) {
        this.activeMqWorkDirectory = activeMqWorkDirectory;
    }

    /**
     * URI of the RServi RMI pool.
     */
    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public void setDefaultRserviPoolUri(final URI defaultRserviPoolUri) {
        this.defaultRserviPoolUri = defaultRserviPoolUri;
    }

    /**
     * Maximum time a job request can be pending its response (in milliseconds).
     */
    public int getJobTimeOut() {
        return jobTimeOut;
    }

    public void setJobTimeOut(final int jobTimeOut) {
        this.jobTimeOut = jobTimeOut;
    }

    /**
     * Number of concurrent job workers per queue, which must be computed based on the number of nodes in the RServi
     * pool and the number of job queues (one global plus one per "boosted" application).
     */
    public int getNumberOfConcurrentJobWorkersPerQueue() {
        return numberOfConcurrentJobWorkersPerQueue;
    }

    public void setNumberOfConcurrentJobWorkersPerQueue(final int numberOfConcurrentJobWorkersPerQueue) {
        this.numberOfConcurrentJobWorkersPerQueue = numberOfConcurrentJobWorkersPerQueue;
    }

    /**
     * Directory where result files are written.
     */
    public File getResultsDirectory() {
        return resultsDirectory;
    }

    public void setResultsDirectory(final File resultsDirectory) {
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Mapping of application names and RServi RMI pool URIs, or null if no specific mapping is required.
     */
    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return applicationSpecificRserviPoolUris;
    }

    public void setApplicationSpecificRserviPoolUris(final Map<String, URI> applicationSpecificRserviPoolUris) {
        this.applicationSpecificRserviPoolUris = applicationSpecificRserviPoolUris;
    }

    /**
     * Optional job statistics handler.
     */
    public PersistedJobStatisticsHandlerConfiguration getJobStatisticsHandlerConfiguration() {
        return jobStatisticsHandlerConfiguration;
    }

    public void setJobStatisticsHandlerConfiguration(final PersistedJobStatisticsHandlerConfiguration jobStatisticsHandlerConfiguration) {
        this.jobStatisticsHandlerConfiguration = jobStatisticsHandlerConfiguration;
    }

    /**
     * Optional email address where RSB should send permanent error reports and other service related messages.
     */
    public String getAdministratorEmail() {
        return administratorEmail;
    }

    public void setAdministratorEmail(final String administratorEmail) {
        this.administratorEmail = administratorEmail;
    }

    /**
     * The SMTP server that will be used for all outbound email exchanges.
     */
    public PersistedSmtpConfiguration getSmtpConfiguration() {
        return smtpConfiguration;
    }

    public void setSmtpConfiguration(final PersistedSmtpConfiguration smtpConfiguration) {
        this.smtpConfiguration = smtpConfiguration;
    }

    /**
     * The JMX configuration used to manage RSB. If not specified default ports will be used. See
     * {@link JmxConfiguration}.
     */
    public PersistedJmxConfiguration getJmxConfiguration() {
        return jmxConfiguration;
    }

    public void setJmxConfiguration(final PersistedJmxConfiguration jmxRmiConfiguration) {
        this.jmxConfiguration = jmxRmiConfiguration;
    }

    /**
     * Optional configuration of root directories where jobs and results will respectively be dropped and retrieved. The
     * map entry element has the root directory for key and the application name for value. RSB must have full right on
     * the root directory as it will need to create sub-directories (
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_JOBS_SUBDIR} ,
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_ACCEPTED_SUBDIR} and
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_RESULTS_SUBDIR}) and files below it.
     */
    public List<PersistedDepositDirectoryConfiguration> getDepositRootDirectories() {
        return depositRootDirectories;
    }

    public void setDepositRootDirectories(final List<PersistedDepositDirectoryConfiguration> depositRootDirectories) {
        this.depositRootDirectories = depositRootDirectories;
    }

    /**
     * Optional configuration of email accounts that will be polled for jobs.
     */
    public List<PersistedDepositEmailConfiguration> getDepositEmailAccounts() {
        return depositEmailAccounts;
    }

    public void setDepositEmailAccounts(final List<PersistedDepositEmailConfiguration> depositEmailAccounts) {
        this.depositEmailAccounts = depositEmailAccounts;
    }
}

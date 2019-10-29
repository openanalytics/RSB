/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import eu.openanalytics.rsb.config.Configuration.AdminSecurityAuthorization;
import eu.openanalytics.rsb.config.Configuration.ApplicationSecurityAuthorization;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.config.Configuration.JmxConfiguration;
import eu.openanalytics.rsb.config.Configuration.JobStatisticsHandlerConfiguration;
import eu.openanalytics.rsb.config.Configuration.RServiClientPoolValidationStrategy;
import eu.openanalytics.rsb.config.Configuration.SmtpConfiguration;

/**
 * Defines the persisted configuration of RSB, from which the actual {@link Configuration} is
 * derived.
 * 
 * @see Configuration
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class PersistedConfiguration
{
    public static class PersistedSmtpConfiguration implements SmtpConfiguration
    {
        private static final long serialVersionUID = 1L;
        private String host;
        private int port;
        private String username;
        private String password;

        public PersistedSmtpConfiguration(final String host,
                                          final int port,
                                          final String username,
                                          final String password)
        {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public PersistedSmtpConfiguration()
        {
            // NOOP
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public String getHost()
        {
            return host;
        }

        public void setHost(final String host)
        {
            this.host = host;
        }

        @Override
        public int getPort()
        {
            return port;
        }

        public void setPort(final int port)
        {
            this.port = port;
        }

        @Override
        public String getUsername()
        {
            return username;
        }

        public void setUsername(final String username)
        {
            this.username = username;
        }

        @Override
        public String getPassword()
        {
            return password;
        }

        public void setPassword(final String password)
        {
            this.password = password;
        }
    }

    public static class PersistedJmxConfiguration implements JmxConfiguration
    {
        private static final long serialVersionUID = 2L;

        private int stubPort, registryPort, httpPort;
        private String httpAuthenticationUsername, httpAuthenticationPassword;

        public PersistedJmxConfiguration(final int stubPort, final int registryPort, final int httpPort)
        {
            this(stubPort, registryPort, httpPort, null, null);
        }

        public PersistedJmxConfiguration(final int stubPort,
                                         final int registryPort,
                                         final int httpPort,
                                         final String httpAuthenticationUsername,
                                         final String httpAuthenticationPassword)
        {
            this.stubPort = stubPort;
            this.registryPort = registryPort;
            this.httpPort = httpPort;
            this.httpAuthenticationUsername = httpAuthenticationUsername;
            this.httpAuthenticationPassword = httpAuthenticationPassword;
        }

        public PersistedJmxConfiguration()
        {
            // NOOP
        }

        @Override
        public int getStubPort()
        {
            return stubPort;
        }

        public void setStubPort(final int stubPort)
        {
            this.stubPort = stubPort;
        }

        @Override
        public int getRegistryPort()
        {
            return registryPort;
        }

        public void setRegistryPort(final int registryPort)
        {
            this.registryPort = registryPort;
        }

        @Override
        public int getHttpPort()
        {
            return httpPort;
        }

        public void setHttpPort(final int httpPort)
        {
            this.httpPort = httpPort;
        }

        @Override
        public String getHttpAuthenticationUsername()
        {
            return httpAuthenticationUsername;
        }

        public void setHttpAuthenticationUsername(final String httpAuthenticationUsername)
        {
            this.httpAuthenticationUsername = httpAuthenticationUsername;
        }

        @Override
        public String getHttpAuthenticationPassword()
        {
            return httpAuthenticationPassword;
        }

        public void setHttpAuthenticationPassword(final String httpAuthenticationPassword)
        {
            this.httpAuthenticationPassword = httpAuthenticationPassword;
        }
    }

    public static class PersistedJobStatisticsHandlerConfiguration
        implements JobStatisticsHandlerConfiguration
    {
        private static final long serialVersionUID = 1L;
        private String className;
        private Map<String, Object> parameters;

        public PersistedJobStatisticsHandlerConfiguration(final String className,
                                                          final Map<String, Object> parameters)
        {
            this.className = className;
            this.parameters = parameters;
        }

        public PersistedJobStatisticsHandlerConfiguration()
        {
            // NOOP
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public String getClassName()
        {
            return className;
        }

        public void setClassName(final String className)
        {
            this.className = className;
        }

        @Override
        public Map<String, Object> getParameters()
        {
            return parameters;
        }

        public void setParameters(final Map<String, Object> parameters)
        {
            this.parameters = parameters;
        }
    }

    public static class PersistedDepositDirectoryConfiguration implements DepositDirectoryConfiguration
    {
        private static final long serialVersionUID = 1L;
        private File rootDirectory;
        private String applicationName;
        private long pollingPeriod;
        private String jobConfigurationFileName;

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public File getRootDirectory()
        {
            return rootDirectory;
        }

        public void setRootDirectory(final File rootDirectory)
        {
            this.rootDirectory = rootDirectory;
        }

        @Override
        public String getApplicationName()
        {
            return applicationName;
        }

        public void setApplicationName(final String applicationName)
        {
            this.applicationName = applicationName;
        }

        @Override
        public long getPollingPeriod()
        {
            return pollingPeriod;
        }

        public void setPollingPeriod(final long pollingPeriod)
        {
            this.pollingPeriod = pollingPeriod;
        }

        @Override
        public String getJobConfigurationFileName()
        {
            return jobConfigurationFileName;
        }

        public void setJobConfigurationFileName(final String jobConfigurationFileName)
        {
            this.jobConfigurationFileName = jobConfigurationFileName;
        }
    }

    public static class PersistedDepositEmailConfiguration implements DepositEmailConfiguration
    {
        private static final long serialVersionUID = 1L;
        private URI accountURI;
        private String applicationName;
        private long pollingPeriod;
        private String responseFileName;
        private String jobConfigurationFileName;

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public URI getAccountURI()
        {
            return accountURI;
        }

        public void setAccountURI(final URI accountURI)
        {
            this.accountURI = accountURI;
        }

        @Override
        public String getApplicationName()
        {
            return applicationName;
        }

        public void setApplicationName(final String applicationName)
        {
            this.applicationName = applicationName;
        }

        @Override
        public long getPollingPeriod()
        {
            return pollingPeriod;
        }

        public void setPollingPeriod(final long pollingPeriod)
        {
            this.pollingPeriod = pollingPeriod;
        }

        @Override
        public String getResponseFileName()
        {
            return responseFileName;
        }

        public void setResponseFileName(final String responseFileName)
        {
            this.responseFileName = responseFileName;
        }

        @Override
        public String getJobConfigurationFileName()
        {
            return jobConfigurationFileName;
        }

        public void setJobConfigurationFileName(final String jobConfigurationFileName)
        {
            this.jobConfigurationFileName = jobConfigurationFileName;
        }
    }

    public static class PersistedAdminSecurityAuthorization implements AdminSecurityAuthorization
    {
        private static final long serialVersionUID = 2L;
        private Set<String> adminPrincipals;
        private Set<String> adminRoles;

        @Override
        public Set<String> getAdminPrincipals()
        {
            return adminPrincipals;
        }

        public void setAdminPrincipals(final Set<String> adminPrincipals)
        {
            this.adminPrincipals = adminPrincipals;
        }

        @Override
        public Set<String> getAdminRoles()
        {
            return adminRoles;
        }

        public void setAdminRoles(final Set<String> adminRoles)
        {
            this.adminRoles = adminRoles;
        }
    }

    public static class PersistedApplicationSecurityAuthorization extends PersistedAdminSecurityAuthorization
        implements ApplicationSecurityAuthorization
    {
        private static final long serialVersionUID = 3L;
        private Set<String> userPrincipals;
        private Set<String> userRoles;
        private boolean functionCallAllowed;
        private boolean scriptSubmissionAllowed;

        @Override
        public Set<String> getUserPrincipals()
        {
            return userPrincipals;
        }

        public void setUserPrincipals(final Set<String> userPrincipals)
        {
            this.userPrincipals = userPrincipals;
        }

        @Override
        public Set<String> getUserRoles()
        {
            return userRoles;
        }

        public void setUserRoles(final Set<String> userRoles)
        {
            this.userRoles = userRoles;
        }

        @Override
        public boolean isFunctionCallAllowed()
        {
            return functionCallAllowed;
        }

        public void setFunctionCallAllowed(final boolean functionCallAllowed)
        {
            this.functionCallAllowed = functionCallAllowed;
        }

        @Override
        public boolean isScriptSubmissionAllowed()
        {
            return scriptSubmissionAllowed;
        }

        public void setScriptSubmissionAllowed(final boolean scriptSubmissionAllowed)
        {
            this.scriptSubmissionAllowed = scriptSubmissionAllowed;
        }
    }

    private String nodeName;
    private File activeMqWorkDirectory;
    private URI defaultRserviPoolUri;
    private int jobTimeOut;
    private int numberOfConcurrentJobWorkersPerQueue;
    private File catalogRootDirectory;
    private File resultsDirectory;
    private Map<String, ?> applicationSpecificRserviPoolUris;
    private PersistedJobStatisticsHandlerConfiguration jobStatisticsHandlerConfiguration;
    private String administratorEmail;
    private PersistedSmtpConfiguration smtpConfiguration;
    private PersistedJmxConfiguration jmxConfiguration;
    private List<PersistedDepositDirectoryConfiguration> depositRootDirectories;
    private List<PersistedDepositEmailConfiguration> depositEmailAccounts;
    private List<File> dataDirectories;
    private RServiPoolConfig rServiClientPoolConfig;
    private RServiClientPoolValidationStrategy rServiClientPoolValidationStrategy;
    private boolean checkHealthOnStart;
    private Map<String, PersistedApplicationSecurityAuthorization> applicationSecurityConfiguration;
    private PersistedAdminSecurityAuthorization rsbSecurityConfiguration;
    private boolean applicationAwareCatalog;
    private boolean propagateSecurityContext;

    public PersistedConfiguration()
    {
        // NOOP
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PersistedConfiguration(final Configuration configuration)
    {
        setActiveMqWorkDirectory(configuration.getActiveMqWorkDirectory());
        setAdministratorEmail(configuration.getAdministratorEmail());
        setApplicationAwareCatalog(configuration.isApplicationAwareCatalog());
        setApplicationSpecificRserviPoolUris(configuration.getApplicationSpecificRserviPoolUris());
        setApplicationSecurityConfiguration((Map) configuration.getApplicationSecurityConfiguration());
        setCatalogRootDirectory(configuration.getCatalogRootDirectory());
        setCheckHealthOnStart(configuration.isCheckHealthOnStart());
        setDataDirectories(configuration.getDataDirectories());
        setDefaultRserviPoolUri(configuration.getDefaultRserviPoolUri());
        setDepositEmailAccounts((List) configuration.getDepositEmailAccounts());
        setDepositRootDirectories((List) configuration.getDepositRootDirectories());
        setJmxConfiguration((PersistedJmxConfiguration) configuration.getJmxConfiguration());
        setJobStatisticsHandlerConfiguration((PersistedJobStatisticsHandlerConfiguration) configuration.getJobStatisticsHandlerConfiguration());
        setJobTimeOut(configuration.getJobTimeOut());
        setNodeName(configuration.getNodeName());
        setNumberOfConcurrentJobWorkersPerQueue(configuration.getNumberOfConcurrentJobWorkersPerQueue());
        setPropagateSecurityContext(configuration.isPropagateSecurityContext());
        setResultsDirectory(configuration.getResultsDirectory());
        setRsbSecurityConfiguration(getRsbSecurityConfiguration());
        setrServiClientPoolConfig(configuration.getRServiClientPoolConfig());
        setrServiClientPoolValidationStrategy(configuration.getRServiClientPoolValidationStrategy());
        setSmtpConfiguration((PersistedSmtpConfiguration) configuration.getSmtpConfiguration());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * Logical name of the RSB node.
     */
    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(final String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * Directory under which RSB catalog sections are located. The catalog sections are:
     * <ul>
     * <li>{@link eu.openanalytics.rsb.config.Configuration#R_SCRIPTS_CATALOG_SUBDIR} : catalog of R
     * scripts</li>
     * <li>
     * {@link eu.openanalytics.rsb.config.Configuration#SWEAVE_FILES_CATALOG_SUBDIR}: catalog of
     * Sweave files</li>
     * <li>
     * {@link eu.openanalytics.rsb.config.Configuration#JOB_CONFIGURATIONS_CATALOG_SUBDIR} :
     * catalog of ready made job configurations</li>
     * <li>
     * {@link eu.openanalytics.rsb.config.Configuration#EMAIL_REPLIES_CATALOG_SUBDIR} : catalog
     * of Email replies</li>
     * </ul>
     * If any of these sub-directories do not pre-exist, RSB will try to create it.
     */
    public File getCatalogRootDirectory()
    {
        return catalogRootDirectory;
    }

    public void setCatalogRootDirectory(final File catalogRootDirectory)
    {
        this.catalogRootDirectory = catalogRootDirectory;
    }

    /**
     * Directory where ActiveMQ stores its persisted data.
     */
    public File getActiveMqWorkDirectory()
    {
        return activeMqWorkDirectory;
    }

    public void setActiveMqWorkDirectory(final File activeMqWorkDirectory)
    {
        this.activeMqWorkDirectory = activeMqWorkDirectory;
    }

    /**
     * URI of the RServi RMI pool.
     */
    public URI getDefaultRserviPoolUri()
    {
        return defaultRserviPoolUri;
    }

    public void setDefaultRserviPoolUri(final URI defaultRserviPoolUri)
    {
        this.defaultRserviPoolUri = defaultRserviPoolUri;
    }

    /**
     * Maximum time a job request can be pending its response (in milliseconds).
     */
    public int getJobTimeOut()
    {
        return jobTimeOut;
    }

    public void setJobTimeOut(final int jobTimeOut)
    {
        this.jobTimeOut = jobTimeOut;
    }

    /**
     * Number of concurrent job workers per queue, which must be computed based on the number of
     * nodes in the RServi pool and the number of job queues (one global plus one per "boosted"
     * application).
     */
    public int getNumberOfConcurrentJobWorkersPerQueue()
    {
        return numberOfConcurrentJobWorkersPerQueue;
    }

    public void setNumberOfConcurrentJobWorkersPerQueue(final int numberOfConcurrentJobWorkersPerQueue)
    {
        this.numberOfConcurrentJobWorkersPerQueue = numberOfConcurrentJobWorkersPerQueue;
    }

    /**
     * Directory where result files are written.
     */
    public File getResultsDirectory()
    {
        return resultsDirectory;
    }

    public void setResultsDirectory(final File resultsDirectory)
    {
        this.resultsDirectory = resultsDirectory;
    }

    /**
     * Mapping of application names and RServi RMI pool URIs, or null if no specific mapping is
     * required.
     */
    public Map<String, ?> getApplicationSpecificRserviPoolUris()
    {
        return applicationSpecificRserviPoolUris;
    }

    public void setApplicationSpecificRserviPoolUris(final Map<String, ?> applicationSpecificRserviPoolUris)
    {
        this.applicationSpecificRserviPoolUris = applicationSpecificRserviPoolUris;
    }

    /**
     * Optional job statistics handler.
     */
    public PersistedJobStatisticsHandlerConfiguration getJobStatisticsHandlerConfiguration()
    {
        return jobStatisticsHandlerConfiguration;
    }

    public void setJobStatisticsHandlerConfiguration(final PersistedJobStatisticsHandlerConfiguration jobStatisticsHandlerConfiguration)
    {
        this.jobStatisticsHandlerConfiguration = jobStatisticsHandlerConfiguration;
    }

    /**
     * Optional email address where RSB should send permanent error reports and other service
     * related messages.
     */
    public String getAdministratorEmail()
    {
        return administratorEmail;
    }

    public void setAdministratorEmail(final String administratorEmail)
    {
        this.administratorEmail = administratorEmail;
    }

    /**
     * The SMTP server that will be used for all outbound email exchanges.
     */
    public PersistedSmtpConfiguration getSmtpConfiguration()
    {
        return smtpConfiguration;
    }

    public void setSmtpConfiguration(final PersistedSmtpConfiguration smtpConfiguration)
    {
        this.smtpConfiguration = smtpConfiguration;
    }

    /**
     * The JMX configuration used to manage RSB. If not specified default ports will be used. See
     * {@link JmxConfiguration}.
     */
    public PersistedJmxConfiguration getJmxConfiguration()
    {
        return jmxConfiguration;
    }

    public void setJmxConfiguration(final PersistedJmxConfiguration jmxRmiConfiguration)
    {
        this.jmxConfiguration = jmxRmiConfiguration;
    }

    /**
     * Optional configuration of root directories where jobs and results will respectively be
     * dropped and retrieved. The map entry element has the root directory for key and the
     * application name for value. RSB must have full right on the root directory as it will need to
     * create sub-directories (
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_JOBS_SUBDIR} ,
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_ACCEPTED_SUBDIR} and
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_RESULTS_SUBDIR}) and files below it.
     */
    public List<PersistedDepositDirectoryConfiguration> getDepositRootDirectories()
    {
        return depositRootDirectories;
    }

    public void setDepositRootDirectories(final List<PersistedDepositDirectoryConfiguration> depositRootDirectories)
    {
        this.depositRootDirectories = depositRootDirectories;
    }

    /**
     * Optional configuration of email accounts that will be polled for jobs.
     */
    public List<PersistedDepositEmailConfiguration> getDepositEmailAccounts()
    {
        return depositEmailAccounts;
    }

    public void setDepositEmailAccounts(final List<PersistedDepositEmailConfiguration> depositEmailAccounts)
    {
        this.depositEmailAccounts = depositEmailAccounts;
    }

    /**
     * Optional configuration of directories where data is accessible.
     */
    public List<File> getDataDirectories()
    {
        return dataDirectories;
    }

    public void setDataDirectories(final List<File> dataDirectories)
    {
        this.dataDirectories = dataDirectories;
    }

    /**
     * Optional pooling configuration for RServi clients.
     */
    public RServiPoolConfig getrServiClientPoolConfig()
    {
        return rServiClientPoolConfig;
    }

    public void setrServiClientPoolConfig(final RServiPoolConfig rServiClientPoolConfig)
    {
        this.rServiClientPoolConfig = rServiClientPoolConfig;
    }

    public RServiClientPoolValidationStrategy getrServiClientPoolValidationStrategy()
    {
        return rServiClientPoolValidationStrategy;
    }

    /**
     * Optional validation strategy for pooled RServi clients.
     */
    public void setrServiClientPoolValidationStrategy(final RServiClientPoolValidationStrategy rServiClientPoolValidationStrategy)
    {
        this.rServiClientPoolValidationStrategy = rServiClientPoolValidationStrategy;
    }

    /**
     * Should health be checked when RSB starts (recommended for deployments where RServi is not
     * colocated in the same web container).
     */
    public boolean isCheckHealthOnStart()
    {
        return checkHealthOnStart;
    }

    public void setCheckHealthOnStart(final boolean checkHealthOnStart)
    {
        this.checkHealthOnStart = checkHealthOnStart;
    }

    /**
     * Optional application security.
     */
    public Map<String, PersistedApplicationSecurityAuthorization> getApplicationSecurityConfiguration()
    {
        return applicationSecurityConfiguration;
    }

    public void setApplicationSecurityConfiguration(final Map<String, PersistedApplicationSecurityAuthorization> applicationSecurityConfiguration)
    {
        this.applicationSecurityConfiguration = applicationSecurityConfiguration;
    }

    /**
     * Optional RSB security.
     */
    public PersistedAdminSecurityAuthorization getRsbSecurityConfiguration()
    {
        return rsbSecurityConfiguration;
    }

    public void setRsbSecurityConfiguration(final PersistedAdminSecurityAuthorization rsbSecurityConfiguration)
    {
        this.rsbSecurityConfiguration = rsbSecurityConfiguration;
    }

    /**
     * Optionally partition the catalog by application name.
     */
    public boolean isApplicationAwareCatalog()
    {
        return applicationAwareCatalog;
    }

    public void setApplicationAwareCatalog(final boolean applicationAwareCatalog)
    {
        this.applicationAwareCatalog = applicationAwareCatalog;
    }

    /**
     * Optionally propagate the security context to RServi calls.
     */
    public boolean isPropagateSecurityContext()
    {
        return propagateSecurityContext;
    }

    public void setPropagateSecurityContext(final boolean propagateSecurityContext)
    {
        this.propagateSecurityContext = propagateSecurityContext;
    }
}

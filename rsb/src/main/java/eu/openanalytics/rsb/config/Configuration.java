/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
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
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.openanalytics.rsb.stats.JobStatisticsHandler;


/**
 * Defines the configuration of RSB, which is injected in all components in order to support runtime
 * changes.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public interface Configuration
{
    public static final String DEFAULT_JSON_CONFIGURATION_FILE = "rsb-configuration.json";

    public static final String DEPOSIT_JOBS_SUBDIR = "inbox";
    public static final String DEPOSIT_ACCEPTED_SUBDIR = "accepted";
    public static final String DEPOSIT_RESULTS_SUBDIR = "outbox";

    public enum CatalogSection
    {
        R_SCRIPTS("r_scripts"), SWEAVE_FILES("sweave_files"), JOB_CONFIGURATIONS("job_configurations"), EMAIL_REPLIES(
                        "email_replies");

        private final String subDir;

        private CatalogSection(final String subDir)
        {
            this.subDir = subDir;
        }

        public String getSubDir()
        {
            return subDir;
        }
    }

    /**
     * SMTP server configuration used for all RSB outbound email operations.
     */
    public interface SmtpConfiguration extends Serializable
    {
        String getHost();

        int getPort();

        String getUsername();

        String getPassword();
    }

    public interface DepositDirectoryConfiguration extends Serializable
    {
        /**
         * RSB must have full right on the root directory as it will need to create sub-directories
         * ( {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_JOBS_SUBDIR} ,
         * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_ACCEPTED_SUBDIR} and
         * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_RESULTS_SUBDIR}) and files below
         * it.
         */
        File getRootDirectory();

        String getApplicationName();

        /**
         * Interval of time between to email account polling, in milliseconds.
         */
        long getPollingPeriod();

        /**
         * Optional filename of a ready-made job configuration found in the catalog.
         * 
         * @see Configuration#getJobConfigurationCatalogDirectory()
         */
        String getJobConfigurationFileName();
    }

    /**
     * POP/IMAP email account that must be polled for jobs.
     */
    public interface DepositEmailConfiguration extends Serializable
    {
        /**
         * An email account URI is of the form: pop3://usr:pwd@host/INBOX. Supported protocols are
         * pop3 and imap.
         */
        URI getAccountURI();

        String getApplicationName();

        /**
         * Interval of time between two email account polling, in milliseconds.
         */
        long getPollingPeriod();

        /**
         * Optional filename of a ready-made email response found in the catalog.
         * 
         * @see Configuration#getEmailRepliesCatalogDirectory()
         */
        String getResponseFileName();

        /**
         * Optional filename of a ready-made job configuration found in the catalog.
         * 
         * @see Configuration#getJobConfigurationCatalogDirectory()
         */
        String getJobConfigurationFileName();
    }

    /**
     * Job statistics handler configuration.
     */
    public interface JobStatisticsHandlerConfiguration extends Serializable
    {
        /**
         * The class must implement {@link JobStatisticsHandler}.
         */
        String getClassName();

        Map<String, Object> getParameters();
    }

    /**
     * JMX configuration.
     */
    public interface JmxConfiguration extends Serializable
    {
        /**
         * RMI stub port. Defaults to 9098.
         */
        int getStubPort();

        /**
         * RMI registry port. Defaults to 9099.
         */
        int getRegistryPort();

        /**
         * HTTP API port. Defaults to 8889.
         */
        int getHttpPort();

        /**
         * Optional username needed to authenticate on the JMX HTTP web interface.
         */
        String getHttpAuthenticationUsername();

        /**
         * Optional password needed to authenticate on the JMX HTTP web interface.
         */
        String getHttpAuthenticationPassword();
    }

    public static enum RServiClientPoolValidationStrategy
    {
        NONE
        {
            @Override
            public void configurePool(final RServiPoolConfig config)
            {
                config.setTestOnBorrow(false);
                config.setTestOnReturn(false);
            }
        },
        ADAPTIVE, FULL;

        public void configurePool(final RServiPoolConfig config)
        {
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);
        }
    }

    public interface AdminSecurityAuthorization extends Serializable
    {
        Set<String> getAdminPrincipals();

        Set<String> getAdminRoles();
    }

    public interface ApplicationSecurityAuthorization extends AdminSecurityAuthorization
    {
        boolean isFunctionCallAllowed();

        boolean isScriptSubmissionAllowed();

        Set<String> getUserPrincipals();

        Set<String> getUserRoles();
    }

    /**
     * URL from where the configuration has been loaded.
     */
    URL getConfigurationUrl();

    /**
     * Logical name of the RSB node.
     */
    String getNodeName();

    /**
     * Directory under which RSB catalog sections are located.
     */
    File getCatalogRootDirectory();

    /**
     * Directory where result files are written.
     */
    File getResultsDirectory();

    /**
     * Directory where ActiveMQ stores its persisted data.
     */
    File getActiveMqWorkDirectory();

    /**
     * URI of the RServi RMI pool.
     */
    URI getDefaultRserviPoolUri();

    /**
     * Optional mapping of application names and RServi RMI pool URIs.
     */
    Map<String, Set<URI>> getApplicationSpecificRserviPoolUris();

    /**
     * Maximum time a job request can be pending its response (in milliseconds).
     */
    int getJobTimeOut();

    /**
     * Number of concurrent job workers per queue, which must be computed based on the number of
     * nodes in the RServi pool and the number of job queues (one global plus one per "boosted"
     * application).
     */
    int getNumberOfConcurrentJobWorkersPerQueue();

    /**
     * The SMTP server that will be used for all outbound email exchanges.
     */
    SmtpConfiguration getSmtpConfiguration();

    /**
     * The JMX configuration used to manage RSB. If not specified default ports will be used. See
     * {@link JmxConfiguration}.
     */
    JmxConfiguration getJmxConfiguration();

    /**
     * Optional email address where RSB should send permanent error reports and other service
     * related messages.
     */
    String getAdministratorEmail();

    /**
     * Optional job statistics handler.
     */
    JobStatisticsHandlerConfiguration getJobStatisticsHandlerConfiguration();

    /**
     * Optional configuration of root directories where jobs and results will respectively be
     * dropped and retrieved. The map entry element has the root directory for key and the
     * application name for value. RSB must have full right on the root directory as it will need to
     * create sub-directories (
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_JOBS_SUBDIR} ,
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_ACCEPTED_SUBDIR} and
     * {@link eu.openanalytics.rsb.config.Configuration#DEPOSIT_RESULTS_SUBDIR}) and files below it.
     */
    List<DepositDirectoryConfiguration> getDepositRootDirectories();

    /**
     * Optional configuration of email accounts that will be polled for jobs.
     */
    List<DepositEmailConfiguration> getDepositEmailAccounts();

    /**
     * Optional configuration of directories where data is accessible.
     */
    List<File> getDataDirectories();

    /**
     * Optional pooling configuration for RServi clients.
     */
    RServiPoolConfig getRServiClientPoolConfig();

    /**
     * Optional validation strategy for pooled RServi clients.
     */
    RServiClientPoolValidationStrategy getRServiClientPoolValidationStrategy();

    /**
     * Should health be checked when RSB starts (recommended for deployments where RServi is not
     * colocated in the same web container).
     */
    boolean isCheckHealthOnStart();

    /**
     * Optional application security.
     */
    Map<String, ApplicationSecurityAuthorization> getApplicationSecurityConfiguration();

    /**
     * Optional RSB security.
     */
    AdminSecurityAuthorization getRsbSecurityConfiguration();

    /**
     * Optionally partition the catalog by application name.
     */
    boolean isApplicationAwareCatalog();

    /**
     * Optionally propagate the security context to RServi calls.
     */
    boolean isPropagateSecurityContext();
}

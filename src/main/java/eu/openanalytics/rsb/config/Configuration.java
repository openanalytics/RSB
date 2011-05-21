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
import java.util.Map;

import eu.openanalytics.rsb.stats.JobStatisticsHandler;

/**
 * Defines the configuration of RSB, which is injected in all components in order to support runtime
 * changes.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public interface Configuration {
    /**
     * Hopefully self-explicit SMTP server configuration.
     */
    public interface SmtpConfiguration {
        String getHost();

        int getPort();

        String getUsername();

        String getPassword();
    }

    /**
     * Hopefully self-explicit Job statistics handler configuration.
     */
    public interface JobStatisticsHandlerConfiguration {
        /**
         * The class must implement {@link JobStatisticsHandler}.
         */
        String getClassName();

        Map<String, Object> getParameters();
    }

    public static final String DEFAULT_JSON_CONFIGURATION_FILE = "rsb-configuration.json";

    public static final String R_SCRIPTS_CATALOG_SUBDIR = "r_scripts";
    public static final String SWEAVE_FILE_CATALOG_SUBDIR = "sweave_files";
    public static final String EMAIL_REPLIES_CATALOG_SUBDIR = "email_replies";

    public static final String DEPOSIT_JOBS_SUBDIR = "inbox";
    public static final String DEPOSIT_ACCEPTED_SUBDIR = "accepted";
    public static final String DEPOSIT_RESULTS_SUBDIR = "outbox";

    /**
     * Directory where a catalog of R scripts are stored.
     */
    File getRScriptsCatalogDirectory();

    /**
     * Directory where a catalog of Sweave files are stored.
     */
    File getSweaveFilesCatalogDirectory();

    /**
     * Directory where a catalog of Email replies are stored.
     */
    File getEmailRepliesCatalogDirectory();

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
    Map<String, URI> getApplicationSpecificRserviPoolUris();

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
     * create sub-directories ({@value
     * eu.openanalytics.rsb.config.Configuration.DEPOSIT_JOBS_SUBDIR} , {@value
     * eu.openanalytics.rsb.config.Configuration.DEPOSIT_ARCHIVE_SUBDIR} and {@value
     * eu.openanalytics.rsb.config.Configuration.DEPOSIT_RESULTS_SUBDIR}) and files below it.
     */
    // FIXME review to use an dedicated object in order to support polling frequency
    Map<File, String> getDepositRootDirectories();

    /**
     * Optional configuration of email accounts that will be polled for jobs. The map entry element
     * has the email account URI for key and the application name for value. An email account URI is
     * of the form: pop3://usr:pwd@host/INBOX. Supported protocols are pop3 and imap.
     */
    // FIXME review to use an dedicated object in order to support response message and polling
    // frequency
    Map<URI, String> getPolledEmailAccounts();
}

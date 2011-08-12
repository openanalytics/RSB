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

package eu.openanalytics.rsb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.ConfigurationFactory;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(Suite.class)
@SuiteClasses({ EmailDepositITCase.class, DirectoryDepositITCase.class, RestJobsITCase.class, RestMiscITCase.class, SoapMtomITCase.class })
public class SuiteITCase {

    static Configuration configuration;
    static Properties rawMessages;
    static GreenMail greenMail;
    static GreenMailUser userAccount;
    static GreenMailUser rsbAccountWithDefaultSettings;
    static GreenMailUser rsbAccountWithJobConfiguration;
    static GreenMailUser rsbAccountWithResponseFile;
    static final String TEST_USER_EMAIL_DOMAIN = "host.tld";

    private static Set<File> catalogTestFiles = new HashSet<File>();

    @BeforeClass
    public static void setupCatalog() throws IOException {
        configuration = ConfigurationFactory.loadJsonConfiguration();

        putTestFileInCatalog(new File(configuration.getRScriptsCatalogDirectory(), "test.R"));
        putTestFileInCatalog(new File(configuration.getRScriptsCatalogDirectory(), "testSweave.R"));
        putTestFileInCatalog(new File(configuration.getSweaveFilesCatalogDirectory(), "testSweave.Rnw"));
        putTestFileInCatalog(new File(configuration.getJobConfigurationCatalogDirectory(), "test-configuration.txt"));
        putTestFileInCatalog(new File(configuration.getEmailRepliesCatalogDirectory(), "test-email-response.txt"));
    }

    @BeforeClass
    public static void loadRawMessages() throws IOException {
        rawMessages = new Properties();
        rawMessages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("messages.properties"));
    }

    @BeforeClass
    public static void startEmailServer() {
        final ServerSetup pop3ServerSetup = new ServerSetup(9110, "localhost", ServerSetup.PROTOCOL_POP3);
        final ServerSetup imapServerSetup = new ServerSetup(9143, "localhost", ServerSetup.PROTOCOL_IMAP);
        final ServerSetup smtpServerSetup = new ServerSetup(9025, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(new ServerSetup[] { pop3ServerSetup, smtpServerSetup, imapServerSetup });

        userAccount = greenMail.setUser("user@" + TEST_USER_EMAIL_DOMAIN, "user", "test");
        rsbAccountWithDefaultSettings = greenMail.setUser("rsb-default@rsb.openalytics.eu", "rsb-default", "test");
        rsbAccountWithJobConfiguration = greenMail.setUser("rsb-conf@rsb.openalytics.eu", "rsb-conf", "test");
        rsbAccountWithResponseFile = greenMail.setUser("rsb-resp@rsb.openalytics.eu", "rsb-resp", "test");

        greenMail.start();
    }

    @AfterClass
    public static void stopEmailServer() {
        greenMail.stop();
    }

    @AfterClass
    public static void cleanupCatalog() {
        for (final File testFile : catalogTestFiles) {
            FileUtils.deleteQuietly(testFile);
        }
    }

    private static void putTestFileInCatalog(final File testFile) throws FileNotFoundException, IOException {
        catalogTestFiles.add(testFile);

        if (!testFile.isFile()) {
            final FileOutputStream fos = new FileOutputStream(testFile);
            IOUtils.copy(AbstractITCase.getTestData(testFile.getName()), fos);
            IOUtils.closeQuietly(fos);
        }
    }
}

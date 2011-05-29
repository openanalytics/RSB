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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class EmailDepositITCase extends AbstractITCase {
    private static final String TEST_USER_EMAIL_DOMAIN = "host.tld";
    private static GreenMail greenMail;
    private static GreenMailUser userAccount;
    private static GreenMailUser rsbAccountWithDefaultSettings;
    private static GreenMailUser rsbAccountWithJobConfiguration;

    @BeforeClass
    public static void startEmailServer() {
        final ServerSetup pop3ServerSetup = new ServerSetup(9110, "localhost", ServerSetup.PROTOCOL_POP3);
        final ServerSetup imapServerSetup = new ServerSetup(9143, "localhost", ServerSetup.PROTOCOL_IMAP);
        final ServerSetup smtpServerSetup = new ServerSetup(9025, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(new ServerSetup[] { pop3ServerSetup, smtpServerSetup, imapServerSetup });

        userAccount = greenMail.setUser("user@" + TEST_USER_EMAIL_DOMAIN, "user", "test");
        rsbAccountWithDefaultSettings = greenMail.setUser("rsb-default@rsb.openalytics.eu", "rsb-default", "test");
        rsbAccountWithJobConfiguration = greenMail.setUser("rsb-conf@rsb.openalytics.eu", "rsb-conf", "test");

        greenMail.start();
    }

    @After
    public void purgeReceivedEmails() throws FolderException {
        greenMail.getManagers().getImapHostManager().getInbox(userAccount).deleteAllMessages();
    }

    @AfterClass
    public static void stopEmailServer() {
        greenMail.stop();
    }

    @Test
    public void submissionToAccountWithDefaultSettings() throws Exception {
        final String subject = RandomStringUtils.randomAlphanumeric(20);

        GreenMailUtil.sendAttachmentEmail(rsbAccountWithDefaultSettings.getEmail(), userAccount.getEmail(), subject, "some work for you",
                IOUtils.toByteArray(getTestData("r-job-sample.zip")), "application/zip", "r-job-sample.zip", "test job", greenMail
                        .getSmtp().getServerSetup());

        final MimeMessage[] rsbResponseMessages = waitForRsbResponse();

        assertThat(rsbResponseMessages.length, is(1));
        final MimeMessage rsbResponseMessage = rsbResponseMessages[0];
        assertThat(rsbResponseMessage.getSubject(), is("RE: " + subject));

        final Multipart parts = (Multipart) rsbResponseMessage.getContent();
        assertThat(
                StringUtils.normalizeSpace(((MimeMultipart) getMailBodyPart(parts, "multipart/related").getContent()).getBodyPart(0)
                        .getContent().toString()), is(StringUtils.normalizeSpace(getRawMessages().getProperty("email.result.body"))));
        assertThat(getMailBodyPart(parts, "application/pdf").getFileName(), is("rnorm.pdf"));
    }

    @Test
    public void submissionToAccountWithJobConfiguration() throws Exception {
        final String subject = RandomStringUtils.randomAlphanumeric(20);

        GreenMailUtil.sendAttachmentEmail(rsbAccountWithJobConfiguration.getEmail(), userAccount.getEmail(), subject, "some work for you",
                "fake data".getBytes(), "application/octet-stream", "data.bin", "test job", greenMail.getSmtp().getServerSetup());

        final MimeMessage[] rsbResponseMessages = waitForRsbResponse();

        assertThat(rsbResponseMessages.length, is(1));
        final MimeMessage rsbResponseMessage = rsbResponseMessages[0];
        assertThat(rsbResponseMessage.getSubject(), is("RE: " + subject));

        final Multipart parts = (Multipart) rsbResponseMessage.getContent();
        assertThat(
                StringUtils.normalizeSpace(((MimeMultipart) getMailBodyPart(parts, "multipart/related").getContent()).getBodyPart(0)
                        .getContent().toString()), is(StringUtils.normalizeSpace(getRawMessages().getProperty("email.result.body"))));
        assertThat(getMailBodyPart(parts, "application/pdf").getFileName(), is("rnorm.pdf"));
    }

    // TODO test: pre-baked response, invalid zip, no job

    private BodyPart getMailBodyPart(final Multipart parts, final String contentType) throws MessagingException {
        for (int i = 0; i < parts.getCount(); i++) {
            final BodyPart part = parts.getBodyPart(i);
            if (StringUtils.startsWith(part.getContentType(), contentType)) {
                return part;
            }
        }
        throw new IllegalStateException("No part of type " + contentType + " found");
    }

    private MimeMessage[] waitForRsbResponse() throws InterruptedException {
        MimeMessage[] receivedMessages = fetchMessagesFromUserMailBox();

        int attemptCount = 0;
        while ((receivedMessages.length == 0) && (attemptCount++ < 120)) {
            Thread.yield();
            Thread.sleep(250);
            receivedMessages = fetchMessagesFromUserMailBox();
        }
        return receivedMessages;
    }

    private MimeMessage[] fetchMessagesFromUserMailBox() {
        return greenMail.getReceviedMessagesForDomain(TEST_USER_EMAIL_DOMAIN);
    }
}

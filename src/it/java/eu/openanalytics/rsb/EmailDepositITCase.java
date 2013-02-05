/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class EmailDepositITCase extends AbstractITCase
{
    @After
    public void purgeReceivedEmails() throws FolderException
    {
        SuiteITCase.greenMail.getManagers()
            .getImapHostManager()
            .getInbox(SuiteITCase.userAccount)
            .deleteAllMessages();
    }

    @Test
    public void submissionToAccountWithDefaultSettings() throws Exception
    {
        final String subject = sendZipJobEmail(SuiteITCase.rsbAccountWithDefaultSettings, "r-job-sample.zip");
        final MimeMessage rsbResponseMessage = ponderForRsbResponse(subject);
        verifyValidResultWithDefaultResponse(rsbResponseMessage);
    }

    @Test
    public void submissionToAccountWithResponseFile() throws Exception
    {
        final String subject = sendZipJobEmail(SuiteITCase.rsbAccountWithResponseFile, "r-job-sample.zip");
        final MimeMessage rsbResponseMessage = ponderForRsbResponse(subject);
        verifyValidResult(rsbResponseMessage, IOUtils.toString(getTestData("test-email-response.txt")));
    }

    @Test
    public void submissionToAccountWithJobConfiguration() throws Exception
    {
        final String subject = sendJobEmail(SuiteITCase.rsbAccountWithJobConfiguration,
            "fake data".getBytes(), "application/octet-stream", "data.bin");
        final MimeMessage rsbResponseMessage = ponderForRsbResponse(subject);
        verifyValidResultWithDefaultResponse(rsbResponseMessage);
    }

    @Test
    public void submissionOfUnprocessableJob() throws Exception
    {
        final String subject = sendZipJobEmail(SuiteITCase.rsbAccountWithResponseFile,
            "r-job-meta-required.zip");
        final MimeMessage rsbResponseMessage = ponderForRsbResponse(subject);
        verifyErrorResult(rsbResponseMessage);
    }

    @Test
    public void submissionOfInvalidZip() throws Exception
    {
        final String subject = sendZipJobEmail(SuiteITCase.rsbAccountWithResponseFile,
            "invalid-job-subdir.zip");
        final MimeMessage rsbResponseMessage = ponderForRsbResponse(subject);
        verifyErrorResult(rsbResponseMessage);
    }

    private String sendZipJobEmail(final GreenMailUser rsbAccount, final String zipFile)
        throws MessagingException, IOException
    {
        return sendJobEmail(rsbAccount, IOUtils.toByteArray(getTestData(zipFile)), "application/zip", zipFile);
    }

    private String sendJobEmail(final GreenMailUser rsbAccount,
                                final byte[] data,
                                final String contentType,
                                final String filename) throws MessagingException, IOException
    {
        final String subject = RandomStringUtils.randomAlphanumeric(20);

        GreenMailUtil.sendAttachmentEmail(rsbAccount.getEmail(), SuiteITCase.userAccount.getEmail(), subject,
            "some work for you", data, contentType, filename, "test job", SuiteITCase.greenMail.getSmtp()
                .getServerSetup());

        return subject;
    }

    private MimeMessage ponderForRsbResponse(final String subject)
        throws InterruptedException, MessagingException
    {
        final MimeMessage[] rsbResponseMessages = waitForRsbResponse();
        assertThat(rsbResponseMessages.length, is(1));
        final MimeMessage rsbResponseMessage = rsbResponseMessages[0];
        assertThat(rsbResponseMessage.getSubject(), is("RE: " + subject));
        return rsbResponseMessage;
    }

    private void verifyValidResultWithDefaultResponse(final MimeMessage rsbResponseMessage)
        throws IOException, MessagingException
    {
        final String exceptedResponseBody = getRawMessages().getProperty("email.result.body");
        verifyValidResult(rsbResponseMessage, exceptedResponseBody);
    }

    private void verifyValidResult(final MimeMessage rsbResponseMessage, final String exceptedResponseBody)
        throws IOException, MessagingException
    {
        final Multipart parts = (Multipart) rsbResponseMessage.getContent();
        assertThat(
            StringUtils.normalizeSpace(((MimeMultipart) getMailBodyPart(parts, "multipart/related").getContent()).getBodyPart(
                0)
                .getContent()
                .toString()), is(StringUtils.normalizeSpace(exceptedResponseBody)));

        assertThat(getMailBodyPart(parts, "application/pdf").getFileName(), is("rnorm.pdf"));
    }

    private void verifyErrorResult(final MimeMessage rsbResponseMessage)
        throws IOException, MessagingException
    {
        final Multipart parts = (Multipart) rsbResponseMessage.getContent();

        final String responseBody = StringUtils.normalizeSpace(((MimeMultipart) getMailBodyPart(parts,
            "multipart/related").getContent()).getBodyPart(0).getContent().toString());
        assertThat(StringUtils.containsIgnoreCase(responseBody, "error"), is(true));
    }

    private BodyPart getMailBodyPart(final Multipart parts, final String contentType)
        throws MessagingException
    {
        for (int i = 0; i < parts.getCount(); i++)
        {
            final BodyPart part = parts.getBodyPart(i);
            if (StringUtils.startsWith(part.getContentType(), contentType))
            {
                return part;
            }
        }
        throw new IllegalStateException("No part of type " + contentType + " found");
    }

    private MimeMessage[] waitForRsbResponse() throws InterruptedException
    {
        MimeMessage[] receivedMessages = fetchMessagesFromUserMailBox();

        int attemptCount = 0;
        while ((receivedMessages.length == 0) && (attemptCount++ < 120))
        {
            Thread.yield();
            Thread.sleep(250);
            receivedMessages = fetchMessagesFromUserMailBox();
        }
        return receivedMessages;
    }

    private MimeMessage[] fetchMessagesFromUserMailBox()
    {
        return SuiteITCase.greenMail.getReceviedMessagesForDomain(SuiteITCase.TEST_USER_EMAIL_DOMAIN);
    }
}

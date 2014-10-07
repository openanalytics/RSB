/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.config.PersistedConfiguration.PersistedDepositEmailConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailDepositHandlerTestCase {
    private static final String TEST_APPLICATION_NAME = "test_app_name";
    @Mock
    private Configuration configuration;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MessageChannel outboundEmailChannel;

    @Mock
    private BeanFactory beanFactory;

    private EmailDepositHandler emailDepositHandler;

    @Before
    public void prepareTest() {
        emailDepositHandler = new EmailDepositHandler();
        emailDepositHandler.setConfiguration(configuration);
        emailDepositHandler.setMessageDispatcher(messageDispatcher);
        emailDepositHandler.setMessages(messageSource);
        emailDepositHandler.setMailSender(mailSender);
        emailDepositHandler.setOutboundEmailChannel(outboundEmailChannel);
        emailDepositHandler.setBeanFactory(beanFactory);
    }

    @Test
    public void setupChannelAdapters() throws URISyntaxException {
        emailDepositHandler.setupChannelAdapters();
    }

    @Test
    public void closeChannelAdapters() {
        emailDepositHandler.closeChannelAdapters();
    }

    @Test
    public void handleJob() throws Exception {
        final MimeMessage mimeMessage = new MimeMessage((Session) null);
        final MimeMessageHelper mmh = new MimeMessageHelper(mimeMessage, true);
        mmh.setReplyTo("test@test.com");
        mmh.setText("test job");
        mmh.addAttachment("r-job-sample.zip", new ClassPathResource("data/r-job-sample.zip"), Constants.ZIP_CONTENT_TYPE);

        final DepositEmailConfiguration depositEmailConfiguration = mock(PersistedDepositEmailConfiguration.class);
        when(depositEmailConfiguration.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        final Message<MimeMessage> message = MessageBuilder.withPayload(mimeMessage)
                .setHeader(EmailDepositHandler.EMAIL_CONFIG_HEADER_NAME, depositEmailConfiguration).build();

        emailDepositHandler.handleJob(message);

        final ArgumentCaptor<MultiFilesJob> jobCaptor = ArgumentCaptor.forClass(MultiFilesJob.class);
        verify(messageDispatcher).dispatch(jobCaptor.capture());

        final MultiFilesJob job = jobCaptor.getValue();
        assertThat(job.getApplicationName(), is(TEST_APPLICATION_NAME));
        assertThat(job.getMeta().containsKey(EmailDepositHandler.EMAIL_SUBJECT_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(EmailDepositHandler.EMAIL_ADDRESSEE_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(EmailDepositHandler.EMAIL_REPLY_TO_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(EmailDepositHandler.EMAIL_REPLY_CC_META_NAME), is(true));
        assertThat(job.getMeta().containsKey(EmailDepositHandler.EMAIL_BODY_META_NAME), is(true));
        assertThat(job.getSource(), is(Source.EMAIL));
        job.destroy();
    }

    @Test
    public void handleResult() throws Exception {
        final Map<String, Serializable> meta = new HashMap<String, Serializable>();
        meta.put(EmailDepositHandler.EMAIL_ADDRESSEE_META_NAME, "addressee@test.com");
        meta.put(EmailDepositHandler.EMAIL_REPLY_TO_META_NAME, "replyto@test.com");
        meta.put(EmailDepositHandler.EMAIL_REPLY_CC_META_NAME, new String[] { "replyCC@test.com" });
        meta.put(EmailDepositHandler.EMAIL_SUBJECT_META_NAME, "subject");
        meta.put(EmailDepositHandler.EMAIL_BODY_META_NAME, "your result");

        final MultiFilesResult multiFilesResult = mock(MultiFilesResult.class);
        when(multiFilesResult.isSuccess()).thenReturn(true);
        when(multiFilesResult.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
        when(multiFilesResult.getTemporaryDirectory()).thenReturn(FileUtils.getTempDirectory());
        when(multiFilesResult.getMeta()).thenReturn(meta);
        final URL jobFakingAResult = Thread.currentThread().getContextClassLoader().getResource("data/r-job-sample.zip");
        when(multiFilesResult.getPayload()).thenReturn(new File[] { new File(jobFakingAResult.toURI()) });

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailDepositHandler.handleResult(multiFilesResult);

        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(outboundEmailChannel).send(messageCaptor.capture());

        @SuppressWarnings("unchecked")
        final Message<MimeMailMessage> message = messageCaptor.getValue();
        final MimeMessage mimeMessage = message.getPayload().getMimeMessage();
        assertThat(mimeMessage.getSubject(), is("RE: subject"));
    }
}

/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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

package eu.openanalytics.rsb.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static eu.openanalytics.rsb.test.TestUtils.getTestDataFile;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.config.PersistedConfiguration.PersistedDepositEmailConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MessageDispatcher;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.test.TestUtils;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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
        this.emailDepositHandler = new EmailDepositHandler();
        this.emailDepositHandler.setConfiguration(this.configuration);
        this.emailDepositHandler.setMessageDispatcher(this.messageDispatcher);
        this.emailDepositHandler.setMessages(this.messageSource);
        this.emailDepositHandler.setMailSender(this.mailSender);
        this.emailDepositHandler.setOutboundEmailChannel(this.outboundEmailChannel);
        this.emailDepositHandler.setBeanFactory(this.beanFactory);
    }

    @Test
    public void setupChannelAdapters() throws URISyntaxException {
        this.emailDepositHandler.setupChannelAdapters();
    }

    @Test
    public void closeChannelAdapters() {
        this.emailDepositHandler.closeChannelAdapters();
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

        this.emailDepositHandler.handleJob(message);

        final ArgumentCaptor<MultiFilesJob> jobCaptor = ArgumentCaptor.forClass(MultiFilesJob.class);
        verify(this.messageDispatcher).dispatch(jobCaptor.capture());

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
		final var tempDirectory= TestUtils.createTestDirectory();
		
		final Map<String, Serializable> meta= new HashMap<>();
		meta.put(EmailDepositHandler.EMAIL_ADDRESSEE_META_NAME, "addressee@test.com");
		meta.put(EmailDepositHandler.EMAIL_REPLY_TO_META_NAME, "replyto@test.com");
		meta.put(EmailDepositHandler.EMAIL_REPLY_CC_META_NAME, new String[] { "replyCC@test.com" });
		meta.put(EmailDepositHandler.EMAIL_SUBJECT_META_NAME, "subject");
		meta.put(EmailDepositHandler.EMAIL_BODY_META_NAME, "your result");
		
		final MultiFilesResult multiFilesResult= mock(MultiFilesResult.class);
		when(multiFilesResult.isSuccess()).thenReturn(true);
		when(multiFilesResult.getApplicationName()).thenReturn(TEST_APPLICATION_NAME);
		when(multiFilesResult.getTemporaryDirectory()).thenReturn(tempDirectory);
		when(multiFilesResult.getMeta()).thenReturn(meta);
		when(multiFilesResult.getPayload())
				.thenReturn(List.of(getTestDataFile("r-job-sample.zip")));
		
		when(this.mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
		
		this.emailDepositHandler.handleResult(multiFilesResult);
		
		@SuppressWarnings("rawtypes")
		final ArgumentCaptor<Message> messageCaptor= ArgumentCaptor.forClass(Message.class);
		verify(this.outboundEmailChannel).send(messageCaptor.capture());
		
		@SuppressWarnings("unchecked")
		final Message<MimeMailMessage> message= messageCaptor.getValue();
		final MimeMessage mimeMessage= message.getPayload().getMimeMessage();
		assertEquals("RE: subject", mimeMessage.getSubject());
	}
	
}

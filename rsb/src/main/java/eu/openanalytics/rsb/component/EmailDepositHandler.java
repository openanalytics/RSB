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

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;
import static org.eclipse.statet.jcommons.lang.ObjectUtils.nonNullAssert;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.IllegalJobDataException;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.security.ApplicationPermissionEvaluator;
import eu.openanalytics.rsb.si.HeaderSettingMessageSourceWrapper;


/**
 * Handles email based R job and result exchanges.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("emailDepositHandler")
public class EmailDepositHandler extends AbstractComponentWithCatalog implements BeanFactoryAware {
	
	
    public static final String EMAIL_CONFIG_HEADER_NAME = DepositEmailConfiguration.class.getName();

    public static final String EMAIL_REPLY_CC_META_NAME = "emailReplyCC";
    public static final String EMAIL_REPLY_TO_META_NAME = "emailReplyTo";
    public static final String EMAIL_ADDRESSEE_META_NAME = "emailAddressee";
    public static final String EMAIL_SUBJECT_META_NAME = "emailSubject";
    public static final String EMAIL_BODY_META_NAME = "emailBody";

    @Resource
    private JavaMailSender mailSender;

    @Resource(name = "emailDepositChannel")
    private MessageChannel emailDepositChannel;

    @Resource(name = "outboundEmailChannel")
    private MessageChannel outboundEmailChannel;

    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<>();

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setMailSender(final JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    void setOutboundEmailChannel(final MessageChannel outboundEmailChannel)
    {
        this.outboundEmailChannel = outboundEmailChannel;
    }

    @PostConstruct
    public void setupChannelAdapters() throws URISyntaxException
    {
        final List<DepositEmailConfiguration> depositEmailConfigurations = getConfiguration().getDepositEmailAccounts();

        if ((depositEmailConfigurations == null) || (depositEmailConfigurations.isEmpty()))
        {
            return;
        }

        for (final DepositEmailConfiguration depositEmailConfiguration : depositEmailConfigurations)
        {
            final PeriodicTrigger trigger = new PeriodicTrigger(depositEmailConfiguration.getPollingPeriod(),
                TimeUnit.MILLISECONDS);
            trigger.setInitialDelay(5000L);
			
			AbstractMailReceiver mailReceiver = null;
			final URI emailAccountURI= depositEmailConfiguration.getAccountURI();
			if (StringUtils.startsWith(emailAccountURI.getScheme(), "pop3")) {
				mailReceiver= new SafePop3MailReceiver(emailAccountURI.toString());
			}
			else if (StringUtils.startsWith(emailAccountURI.getScheme(), "imap")) {
				mailReceiver= new SafeImapMailReceiver(emailAccountURI.toString());
			}
			else {
				throw new IllegalArgumentException("Invalid email account URI: " + emailAccountURI);
			}
			mailReceiver.setBeanFactory(this.beanFactory);
			mailReceiver.setBeanName("rsb-email-ms-" + emailAccountURI.getHost() + emailAccountURI.hashCode());
			mailReceiver.setShouldDeleteMessages(true);
			mailReceiver.setMaxFetchSize(1);
			mailReceiver.afterPropertiesSet();
            final MailReceivingMessageSource fileMessageSource = new MailReceivingMessageSource(mailReceiver);
            final HeaderSettingMessageSourceWrapper<?> messageSource = new HeaderSettingMessageSourceWrapper<>(
                fileMessageSource, EMAIL_CONFIG_HEADER_NAME, depositEmailConfiguration);

            final SourcePollingChannelAdapter channelAdapter = new SourcePollingChannelAdapter();
            channelAdapter.setBeanFactory(this.beanFactory);
            channelAdapter.setBeanName("rsb-email-ca-" + emailAccountURI.getHost()
                                       + emailAccountURI.hashCode());
            channelAdapter.setOutputChannel(this.emailDepositChannel);
            channelAdapter.setSource(messageSource);
            channelAdapter.setTrigger(trigger);
            channelAdapter.afterPropertiesSet();
            channelAdapter.start();

            getLogger().info("Started channel adapter: " + channelAdapter);

            this.channelAdapters.add(channelAdapter);
        }
    }

    @PreDestroy
    public void closeChannelAdapters()
    {
        for (final SourcePollingChannelAdapter channelAdapter : this.channelAdapters)
        {
            channelAdapter.stop();
            getLogger().info("Stopped channel adapter: " + channelAdapter);
        }
    }
	
	
	/** via Spring Integration */
	public void handleJob(final Message<MimeMessage> message) throws MessagingException, IOException {
		final DepositEmailConfiguration depositEmailConfiguration= message.getHeaders().get(
				EMAIL_CONFIG_HEADER_NAME, DepositEmailConfiguration.class);
		final String applicationName= depositEmailConfiguration.getApplicationName();
		final MimeMessage mimeMessage= message.getPayload();
		
		final Address[] replyTo= mimeMessage.getReplyTo();
		Validate.notEmpty(replyTo,
				"no reply address found for job emailed with headers:"
						+ Collections.list(mimeMessage.getAllHeaders()));
		
		final Map<String, Serializable> meta= new HashMap<>();
		meta.put(EMAIL_SUBJECT_META_NAME, mimeMessage.getSubject());
		meta.put(EMAIL_ADDRESSEE_META_NAME, getPrimaryAddressee(mimeMessage));
		meta.put(EMAIL_REPLY_TO_META_NAME, replyTo[0].toString());
		meta.put(EMAIL_REPLY_CC_META_NAME, getCCAddressees(mimeMessage));
		meta.put(EMAIL_BODY_META_NAME, getResponseBody(depositEmailConfiguration));
		
		final MultiFilesJob job= new MultiFilesJob(Source.EMAIL, applicationName,
				ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME, UUID.randomUUID(),
				(GregorianCalendar)GregorianCalendar.getInstance(), meta );
		try {
			addEmailAttachmentsToJob(depositEmailConfiguration, mimeMessage, job);
			getMessageDispatcher().dispatch(job);
		}
		catch (final Exception e) {
			try {
				final Exception errorInfo= HandlerUtils.handleJobError(this, e);
				final MultiFilesResult errorResult= job.buildErrorResult(errorInfo, getMessages());
				handleResult(errorResult);
			}
			finally {
				job.destroy();
			}
		}
	}
	
	public void handleResult(final MultiFilesResult result) throws MessagingException, IOException {
		final Serializable responseBody= nonNullAssert(result.getMeta().get(EMAIL_BODY_META_NAME));
		final String responseText= (responseBody instanceof URI) ?
				Files.readString(Path.of((URI)responseBody)) :
				responseBody.toString();
		
		final MimeMessage mimeMessage= this.mailSender.createMimeMessage();
		final MimeMessageHelper mmh= new MimeMessageHelper(mimeMessage, true);
		
		mmh.setFrom((String)result.getMeta().get(EMAIL_ADDRESSEE_META_NAME));
		mmh.setTo((String)result.getMeta().get(EMAIL_REPLY_TO_META_NAME));
		mmh.setCc((String[])result.getMeta().get(EMAIL_REPLY_CC_META_NAME));
		mmh.setSubject("RE: " + result.getMeta().get(EMAIL_SUBJECT_META_NAME));
		
		if (result.isSuccess()) {
			mmh.setText(responseText);
			for (final var resultFile : result.getPayload()) {
				mmh.addAttachment(requireFileName(resultFile).toString(), resultFile.toFile());
			}
		}
		else {
			mmh.setText(Files.readString(result.getPayload().get(0)));
		}
		
		final Message<MimeMailMessage> message= new GenericMessage<>(new MimeMailMessage(mmh));
		this.outboundEmailChannel.send(message);
	}
	
	private void addEmailAttachmentsToJob(final DepositEmailConfiguration depositEmailConfiguration,
			final MimeMessage mimeMessage,
			final MultiFilesJob job)
			throws IllegalJobDataException, MessagingException, IOException {
		final String jobConfigurationFileName= depositEmailConfiguration.getJobConfigurationFileName();
		if (jobConfigurationFileName != null && !jobConfigurationFileName.isBlank()) {
			final Path jobConfigurationFile= getJobConfigurationFile(
					depositEmailConfiguration.getApplicationName(), jobConfigurationFileName );
			try (final var in= Files.newInputStream(jobConfigurationFile)) {
				job.addFile(Constants.MULTIPLE_FILES_JOB_CONFIGURATION, in);
			}
		}
		
		final Object content= mimeMessage.getContent();
		Validate.isTrue(content instanceof Multipart, "only multipart emails can be processed");
		final Multipart multipart= (Multipart)content;
		for (int i= 0, n= multipart.getCount(); i < n; i++) {
			final Part part= multipart.getBodyPart(i);
			
			final String disposition= part.getDisposition();
			
			if (disposition != null
					&& (disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE)) ) {
				final String name= part.getFileName();
				final String contentType= StringUtils.substringBefore(part.getContentType(), ";");
				try (final var data= part.getInputStream()) {
					MultiFilesJob.addDataToJob(contentType, name, data, job);
				}
			}
		}
	}
	
	private Serializable getResponseBody(final DepositEmailConfiguration configuration) {
		if (StringUtils.isBlank(configuration.getResponseFileName())) {
			return getMessages().getMessage("email.result.body", null, null);
		}
		
		return getCatalogManager().internalGetCatalogFile(CatalogSection.EMAIL_REPLIES,
						configuration.getApplicationName(), configuration.getResponseFileName() )
				.toUri();
	}
	
    private String getPrimaryAddressee(final MimeMessage mimeMessage) throws MessagingException
    {
        final Address[] recipients = mimeMessage.getRecipients(RecipientType.TO);
        return recipients != null && recipients.length > 0 ? recipients[0].toString() : "?";
    }

    private String[] getCCAddressees(final MimeMessage mimeMessage) throws MessagingException
    {
        final Address[] recipients = mimeMessage.getRecipients(RecipientType.CC);
        if (recipients == null)
        {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final String[] result = new String[recipients.length];
        for (int i = 0; i < recipients.length; i++)
        {
            result[i] = recipients[i].toString();
        }
        return result;
    }
	
	
	/* Improved MailReceivers
		simpleContent == true -> full copy
		autoClose == true -> purge
		receiveLock -> really thread-safe loading of messages (don't close while another read)
	 */
	
	private static class SafePop3MailReceiver extends Pop3MailReceiver {
		
		
		private final Object receiveLock= new Object();
		
		
		private SafePop3MailReceiver(final String url) {
			super(url);
			setSimpleContent(true);
			setAutoCloseFolder(true);
		}
		
		@Override
		public Object[] receive() throws MessagingException {
			synchronized (this.receiveLock) {
				return super.receive();
			}
		}
		
	}
	
	
	private static class SafeImapMailReceiver extends ImapMailReceiver {
		
		
		final ReentrantReadWriteLock receiveLock= new ReentrantReadWriteLock();
		
		
		private SafeImapMailReceiver(final String url) {
			super(url);
			setSimpleContent(true);
			setAutoCloseFolder(true);
			setShouldMarkMessagesAsRead(true);
		}
		
		@Override
		public Object[] receive() throws MessagingException {
			this.receiveLock.readLock().lock();
			try {
				return super.receive();
			}
			finally {
				if (this.receiveLock.getReadHoldCount() > 0) {
					this.receiveLock.readLock().unlock();
				}
			}
		}
		
		@Override
		protected void closeFolder() {
			if (this.receiveLock.getReadHoldCount() > 0) {
				this.receiveLock.readLock().unlock();
				if (this.receiveLock.writeLock().tryLock()) {
					try {
						super.closeFolder();
					}
					finally {
						this.receiveLock.writeLock().unlock();
					}
				}
			}
			else {
				this.receiveLock.writeLock().lock();
				try {
					super.closeFolder();
				}
				finally {
					this.receiveLock.writeLock().unlock();
				}
			}
		}
		
	}
	
}

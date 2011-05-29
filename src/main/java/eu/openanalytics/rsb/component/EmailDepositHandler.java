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

package eu.openanalytics.rsb.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.integration.message.GenericMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;
import eu.openanalytics.rsb.message.MultiFilesJob;
import eu.openanalytics.rsb.message.MultiFilesResult;
import eu.openanalytics.rsb.si.HeaderSettingMessageSourceWrapper;

/**
 * Handles email based R job and result exchanges.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@Component("emailDepositHandler")
public class EmailDepositHandler extends AbstractComponent implements BeanFactoryAware {
    private static final String EMAIL_CONFIG_HEADER_NAME = DepositEmailConfiguration.class.getName();

    private static final String EMAIL_REPLY_CC_META_NAME = "emailReplyCC";

    private static final String EMAIL_REPLY_TO_META_NAME = "emailReplyTo";

    private static final String EMAIL_ADDRESSEE_META_NAME = "emailAddressee";

    private static final String EMAIL_SUBJECT_META_NAME = "emailSubject";

    @Resource
    private JavaMailSender mailSender;

    // TODO unit test, integration test
    @Resource(name = "emailDepositChannel")
    private MessageChannel emailDepositChannel;

    @Resource(name = "outboundEmailChannel")
    private MessageChannel outboundEmailChannel;

    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<SourcePollingChannelAdapter>();

    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setMailSender(final JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    void setJobDirectoryDepositChannel(final MessageChannel jobDirectoryDepositChannel) {
        this.emailDepositChannel = jobDirectoryDepositChannel;
    }

    void setOutboundEmailChannel(final MessageChannel outboundEmailChannel) {
        this.outboundEmailChannel = outboundEmailChannel;
    }

    @PostConstruct
    public void setupChannelAdapters() throws URISyntaxException {
        final List<DepositEmailConfiguration> depositEmailAccounts = getConfiguration().getDepositEmailAccounts();

        if ((depositEmailAccounts == null) || (depositEmailAccounts.isEmpty())) {
            return;
        }

        for (final DepositEmailConfiguration depositEmailAccount : depositEmailAccounts) {
            final PeriodicTrigger trigger = new PeriodicTrigger(depositEmailAccount.getPollingPeriod(), TimeUnit.MILLISECONDS);
            trigger.setInitialDelay(5000L);

            AbstractMailReceiver mailReceiver = null;

            final URI emailAccountURI = depositEmailAccount.getAccountURI();
            if (StringUtils.equals(emailAccountURI.getScheme(), "pop3")) {
                mailReceiver = new Pop3MailReceiver(emailAccountURI.toString());
            } else if (StringUtils.equals(emailAccountURI.getScheme(), "imap")) {
                mailReceiver = new ImapMailReceiver(emailAccountURI.toString());
                ((ImapMailReceiver) mailReceiver).setShouldMarkMessagesAsRead(true);
            }

            Validate.notNull(mailReceiver, "Invalid email account URI: " + emailAccountURI);
            mailReceiver.setBeanFactory(beanFactory);
            mailReceiver.setBeanName("rsb-email-ms-" + emailAccountURI.getHost() + emailAccountURI.hashCode());
            mailReceiver.setShouldDeleteMessages(true);
            mailReceiver.setMaxFetchSize(1);
            mailReceiver.afterPropertiesSet();
            final MailReceivingMessageSource fileMessageSource = new MailReceivingMessageSource(mailReceiver);
            final HeaderSettingMessageSourceWrapper<javax.mail.Message> messageSource = new HeaderSettingMessageSourceWrapper<javax.mail.Message>(
                    fileMessageSource, EMAIL_CONFIG_HEADER_NAME, depositEmailAccount);

            final SourcePollingChannelAdapter channelAdapter = new SourcePollingChannelAdapter();
            channelAdapter.setBeanFactory(beanFactory);
            channelAdapter.setBeanName("rsb-email-ca-" + emailAccountURI.getHost() + emailAccountURI.hashCode());
            channelAdapter.setOutputChannel(emailDepositChannel);
            channelAdapter.setSource(messageSource);
            channelAdapter.setTrigger(trigger);
            channelAdapter.afterPropertiesSet();
            channelAdapter.start();

            getLogger().info("Started channel adapter: " + channelAdapter);

            channelAdapters.add(channelAdapter);
        }
    }

    @PreDestroy
    public void closeChannelAdapters() {
        for (final SourcePollingChannelAdapter channelAdapter : channelAdapters) {
            channelAdapter.stop();
            getLogger().info("Stopped channel adapter: " + channelAdapter);
        }
    }

    @SuppressWarnings("unchecked")
    public void handleJob(final Message<MimeMessage> message) throws IOException, MessagingException {
        final DepositEmailConfiguration depositEmailConfiguration = message.getHeaders().get(EMAIL_CONFIG_HEADER_NAME,
                DepositEmailConfiguration.class);
        final String applicationName = depositEmailConfiguration.getApplicationName();
        final MimeMessage mimeMessage = message.getPayload();

        // TODO immediate return of jobs that fail initial construction (like in DirectoryDepositHandler)
        final Address[] replyTo = mimeMessage.getReplyTo();
        Validate.notEmpty(replyTo, "no reply address found for job emailed with headers:" + Collections.list(mimeMessage.getAllHeaders()));

        final Map<String, Serializable> meta = new HashMap<String, Serializable>();
        meta.put(EMAIL_SUBJECT_META_NAME, mimeMessage.getSubject());
        meta.put(EMAIL_ADDRESSEE_META_NAME, getPrimaryAddressee(mimeMessage));
        meta.put(EMAIL_REPLY_TO_META_NAME, replyTo[0].toString());
        meta.put(EMAIL_REPLY_CC_META_NAME, getCCAddressees(mimeMessage));

        final MultiFilesJob job = new MultiFilesJob(Source.EMAIL, applicationName, UUID.randomUUID(),
                (GregorianCalendar) GregorianCalendar.getInstance(), meta);

        if (StringUtils.isNotBlank(depositEmailConfiguration.getJobConfigurationFileName())) {
            final File jobConfigurationFile = new File(getConfiguration().getJobConfigurationCatalogDirectory(),
                    depositEmailConfiguration.getJobConfigurationFileName());
            job.addFile(Constants.MULTIPLE_FILES_JOB_CONFIGURATION, new FileInputStream(jobConfigurationFile));
        }

        final Object content = mimeMessage.getContent();
        Validate.isTrue(content instanceof Multipart, "only multipart emails can be processed");

        final Multipart multipart = (Multipart) content;
        for (int i = 0, n = multipart.getCount(); i < n; i++) {
            final Part part = multipart.getBodyPart(i);

            final String disposition = part.getDisposition();

            if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))))) {
                final String name = part.getFileName();
                final String contentType = StringUtils.substringBefore(part.getContentType(), ";");
                MultiFilesJob.addDataToJob(contentType, name, part.getInputStream(), job);
            }
        }

        getMessageDispatcher().dispatch(job);
    }

    public void handleResult(final MultiFilesResult result) throws MessagingException, IOException {
        // TODO use pre-baked response as body if configured with
        final String responseText = getMessages().getMessage("email.result.body", null, null);

        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        final MimeMessageHelper mmh = new MimeMessageHelper(mimeMessage, true);

        mmh.setFrom((String) result.getMeta().get(EMAIL_ADDRESSEE_META_NAME));
        mmh.setTo((String) result.getMeta().get(EMAIL_REPLY_TO_META_NAME));
        mmh.setCc((String[]) result.getMeta().get(EMAIL_REPLY_CC_META_NAME));
        mmh.setSubject("RE: " + result.getMeta().get(EMAIL_SUBJECT_META_NAME));
        mmh.setText(responseText);

        // TODO put error text as email body - else, attach all success files
        for (final File resultFile : result.getPayload()) {
            mmh.addAttachment(resultFile.getName(), resultFile);
        }

        final Message<MimeMailMessage> message = new GenericMessage<MimeMailMessage>(new MimeMailMessage(mmh));
        outboundEmailChannel.send(message);
    }

    private String getPrimaryAddressee(final MimeMessage mimeMessage) throws MessagingException {
        final Address[] recipients = mimeMessage.getRecipients(RecipientType.TO);
        return recipients != null && recipients.length > 0 ? recipients[0].toString() : "?";
    }

    private String[] getCCAddressees(final MimeMessage mimeMessage) throws MessagingException {
        final Address[] recipients = mimeMessage.getRecipients(RecipientType.CC);
        if (recipients == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final String[] result = new String[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            result[i] = recipients[i].toString();
        }
        return result;
    }
}

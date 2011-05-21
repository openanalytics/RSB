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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

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
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;
import eu.openanalytics.rsb.si.ApplicationNameAwareMessageSourceWrapper;

/**
 * Handles email based R job and result exchanges.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@Component("emailDepositHandler")
public class EmailDepositHandler extends AbstractComponent implements BeanFactoryAware {
    // TODO unit test, integration test
    public static final String INBOX_DIRECTORY_META_NAME = "inboxDirectory";
    public static final String ORIGINAL_FILENAME_META_NAME = "originalFilename";
    public static final String DEPOSIT_ROOT_DIRECTORY_META_NAME = "depositRootDirectory";

    @Resource(name = "emailDepositChannel")
    private MessageChannel emailDepositChannel;

    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<SourcePollingChannelAdapter>();

    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setJobDirectoryDepositChannel(final MessageChannel jobDirectoryDepositChannel) {
        this.emailDepositChannel = jobDirectoryDepositChannel;
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
            final ApplicationNameAwareMessageSourceWrapper<javax.mail.Message> messageSource = new ApplicationNameAwareMessageSourceWrapper<javax.mail.Message>(
                    fileMessageSource, depositEmailAccount.getApplicationName());

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

    public void handleJob(final Message<MimeMessage> message) {
        getLogger().warn("Email>>> " + message);

        // FIXME implement email job handling
        // explode zip attachments
        // required meta: emailAddressee, emailSubject, emailReplyTo, emailReplyCC
        // copy optional job config from catalog to config.txt
        // [Payload=javax.mail.internet.MimeMessage@117341c][Headers={timestamp=1306001658248,
        // id=50f9dbd8-7875-4290-af20-483909f45122, applicationName=scientist}]
    }

    // FIXME implement email result handling
    // if error, put error text as email body - if success, attach all success
    // files and use pre-baked response as body
    // message.setProperty('fromAddress', emailAddressee)
    // message.setProperty('toAddresses', replyTo)
    // message.setProperty('ccAddresses', replyCc)
    // message.setProperty('subject', 'RE: ' + subject)
}

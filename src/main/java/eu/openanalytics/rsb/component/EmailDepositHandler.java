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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

/**
 * Handles email based R job and result exchanges.
 * 
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@Component("emailDepositHandler")
public class EmailDepositHandler extends AbstractComponent implements BeanFactoryAware {
    public static final String INBOX_DIRECTORY_META_NAME = "inboxDirectory";
    public static final String ORIGINAL_FILENAME_META_NAME = "originalFilename";
    public static final String DEPOSIT_ROOT_DIRECTORY_META_NAME = "depositRootDirectory";

    // FIXME support configured response message, polling frequency
    @Resource(name = "jobEmailAccountChannel")
    private MessageChannel jobEmailAccountChannel;

    private BeanFactory beanFactory;

    private final List<SourcePollingChannelAdapter> channelAdapters = new ArrayList<SourcePollingChannelAdapter>();

    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // exposed for testing
    void setJobDirectoryDepositChannel(final MessageChannel jobDirectoryDepositChannel) {
        this.jobEmailAccountChannel = jobDirectoryDepositChannel;
    }

    @PostConstruct
    public void setupChannelAdapters() throws URISyntaxException {
        final Map<URI, String> polledEmailAccounts = getConfiguration().getPolledEmailAccounts();

        if (polledEmailAccounts == null) {
            return;
        }

        final PeriodicTrigger fileTrigger = new PeriodicTrigger(30L, TimeUnit.SECONDS);
        fileTrigger.setInitialDelay(10L);

        for (final URI emailAccountURI : polledEmailAccounts.keySet()) {
            AbstractMailReceiver mailReceiver = null;

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

            final MailReceivingMessageSource messageSource = new MailReceivingMessageSource(mailReceiver);

            final SourcePollingChannelAdapter channelAdapter = new SourcePollingChannelAdapter();
            channelAdapter.setBeanFactory(beanFactory);
            channelAdapter.setBeanName("rsb-email-ca-" + emailAccountURI.getHost() + emailAccountURI.hashCode());
            channelAdapter.setOutputChannel(jobEmailAccountChannel);
            channelAdapter.setSource(messageSource);
            channelAdapter.setTrigger(fileTrigger);
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

    public void handleJob(final Object o) {
        // FIXME implement javax.mail.internet.MimeMessage@1e9fb74
        getLogger().warn("Email>>> " + o);
    }
}

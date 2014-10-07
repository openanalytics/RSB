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

package eu.openanalytics.rsb.si;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class HeaderSettingMessageSourceWrapperTestCase {

    private static final String TEST_HEADER_NAME = "test-header-name";
    private static final Object TEST_HEADER_VALUE = new Object();

    @Mock
    private MessageSource<String> wrappedMessageSource;

    private HeaderSettingMessageSourceWrapper<String> headerSettingMessageSourceWrapper;

    @Before
    public void prepareWrapper() {
        headerSettingMessageSourceWrapper = new HeaderSettingMessageSourceWrapper<String>(wrappedMessageSource, TEST_HEADER_NAME,
                TEST_HEADER_VALUE);
    }

    @Test
    public void receiveNotNull() {
        final String payload = "test-payload";
        when(wrappedMessageSource.receive()).thenReturn(MessageBuilder.withPayload(payload).build());

        final Message<String> message = headerSettingMessageSourceWrapper.receive();

        assertThat(message.getPayload(), is(payload));
        assertThat(message.getHeaders().get(TEST_HEADER_NAME), is(TEST_HEADER_VALUE));
    }

    @Test
    public void receiveNull() {
        when(wrappedMessageSource.receive()).thenReturn(null);

        assertThat(headerSettingMessageSourceWrapper.receive(), is(nullValue()));
    }
}

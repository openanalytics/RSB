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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.walware.rj.servi.RServi;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemHealthResourceTestCase {

    private SystemHealthResource systemHealthResource;

    @Mock
    private Configuration configuration;
    @Mock
    private RServiInstanceProvider rServiInstanceProvider;

    @Before
    public void prepareTest() throws URISyntaxException {
        systemHealthResource = new SystemHealthResource();
        systemHealthResource.setConfiguration(configuration);
        systemHealthResource.setRServiInstanceProvider(rServiInstanceProvider);

        final URI defaultPoolUri = new URI("fake://default");
        when(configuration.getDefaultRserviPoolUri()).thenReturn(defaultPoolUri);
    }

    @Test
    public void happyCheck() throws Exception {
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenReturn(rServi);

        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(200));
        assertThat(checkResult.getEntity().toString(), is("OK"));
    }

    @Test
    public void unhappyCheck() throws Exception {
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString())).thenThrow(
                new RuntimeException("simulated RServi provider issue"));

        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(500));
        assertThat(checkResult.getEntity().toString(), is("ERROR"));
    }
}

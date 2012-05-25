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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.walware.rj.servi.RServi;
import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.rest.types.NodeInformation;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;

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
    public void getInfo() throws Exception {
        final NodeInformation info = systemHealthResource.getInfo(mock(ServletContext.class));

        assertThat(info, is(notNullValue()));
    }

    @Test
    public void defaultCheck() throws Exception {
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenReturn(rServi);

        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(200));
        assertThat(checkResult.getEntity().toString(), is("OK"));
    }

    @Test
    public void happyCheck() throws Exception {
        final RServi rServi = mock(RServi.class);
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenReturn(rServi);

        systemHealthResource.verifyNodeHealth();
        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(200));
        assertThat(checkResult.getEntity().toString(), is("OK"));
    }

    @Test
    public void unhappyCheck() throws Exception {
        when(rServiInstanceProvider.getRServiInstance(anyString(), anyString(), eq(PoolingStrategy.NEVER))).thenThrow(
                new RuntimeException("simulated RServi provider issue"));

        systemHealthResource.verifyNodeHealth();
        final Response checkResult = systemHealthResource.check();

        assertThat(checkResult.getStatus(), is(500));
        assertThat(checkResult.getEntity().toString(), is("ERROR"));
    }
}

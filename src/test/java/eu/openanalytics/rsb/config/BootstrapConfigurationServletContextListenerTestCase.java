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

package eu.openanalytics.rsb.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import javax.servlet.ServletContextEvent;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class BootstrapConfigurationServletContextListenerTestCase {

    private BootstrapConfigurationServletContextListener bcscl;

    private MockServletContext mockServletContext;
    private ServletContextEvent servletContextEvent;

    @Before
    public void prepareTest() {
        bcscl = new BootstrapConfigurationServletContextListener();
        mockServletContext = new MockServletContext() {
            @Override
            public String getRealPath(final String path) {
                return FileUtils.getTempDirectoryPath();
            };
        };
        servletContextEvent = new ServletContextEvent(mockServletContext);
    }

    @Test
    public void contextInitializedConfigurationPresent() {
        mockServletContext.addInitParameter(BootstrapConfigurationServletContextListener.RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM,
                Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
        bcscl.contextInitialized(servletContextEvent);
    }

    @Test
    public void contextInitializedConfigurationAbsent() {
        final File expectedConfigurationFileCreated = new File(FileUtils.getTempDirectory(), Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
        FileUtils.deleteQuietly(expectedConfigurationFileCreated);

        mockServletContext.addInitParameter(BootstrapConfigurationServletContextListener.RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM,
                "non-existant-configuration");
        bcscl.contextInitialized(servletContextEvent);

        assertThat(expectedConfigurationFileCreated.isFile(), is(true));
    }

    @Test
    public void contextDestroyed() {
        bcscl.contextDestroyed(servletContextEvent);
    }

    @Test
    public void defaultConfigurationIsValid() throws Exception {
        final PersistedConfiguration configuration = BootstrapConfigurationServletContextListener.createDefaultConfiguration(FileUtils
                .getTempDirectory());
        ConfigurationFactory.validate(new PersistedConfigurationAdapter(null, configuration));
    }
}

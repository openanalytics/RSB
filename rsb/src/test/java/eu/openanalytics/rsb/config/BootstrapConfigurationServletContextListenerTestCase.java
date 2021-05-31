/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
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

package eu.openanalytics.rsb.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import javax.servlet.ServletContextEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.stringtemplate.v4.ST;

import eu.openanalytics.rsb.Util;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class BootstrapConfigurationServletContextListenerTestCase
{

    private BootstrapConfigurationServletContextListener bcscl;

    private MockServletContext mockServletContext;
    private ServletContextEvent servletContextEvent;

    @Before
    public void prepareTest()
    {
        bcscl = new BootstrapConfigurationServletContextListener();
        mockServletContext = new MockServletContext()
        {
            @Override
            public String getRealPath(final String path)
            {
                return FileUtils.getTempDirectoryPath();
            }
        };
        servletContextEvent = new ServletContextEvent(mockServletContext);
    }

    @Test
    public void contextInitializedConfigurationPresent()
    {
        mockServletContext.addInitParameter(
            BootstrapConfigurationServletContextListener.RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM,
            Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
        bcscl.contextInitialized(servletContextEvent);
    }

    @Test
    public void contextInitializedConfigurationAbsent()
    {
        final File writableConfigurationDirectory = bcscl.getWritableConfigurationDirectory(servletContextEvent);
        final File expectedConfigurationFileCreated = new File(writableConfigurationDirectory,
            Configuration.DEFAULT_JSON_CONFIGURATION_FILE);
        FileUtils.deleteQuietly(expectedConfigurationFileCreated);

        mockServletContext.addInitParameter(
            BootstrapConfigurationServletContextListener.RSB_CONFIGURATION_SERVLET_CONTEXT_PARAM,
            "non-existant-configuration");
        bcscl.contextInitialized(servletContextEvent);

        assertThat(expectedConfigurationFileCreated.isFile(), is(true));
    }

    @Test
    public void contextDestroyed()
    {
        bcscl.contextDestroyed(servletContextEvent);
    }

    @Test
    public void defaultConfigurationIsValid() throws Exception
    {
        final File tempDirectory = FileUtils.getTempDirectory();
        final PersistedConfiguration configuration = BootstrapConfigurationServletContextListener.createDefaultConfiguration(tempDirectory);
        ConfigurationFactory.validate(new PersistedConfigurationAdapter(null, configuration));

        final ST defaultConfigurationTestTemplate = Util.newStringTemplate(IOUtils.toString(Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("rsb-configuration-default.json"), Charset.defaultCharset()));
        final File defaultRsbHomeDirectory = BootstrapConfigurationServletContextListener.getDefaultRsbHomeDirectory(tempDirectory);
        defaultConfigurationTestTemplate.add("RSB_HOME", defaultRsbHomeDirectory.toString().replace("\\", "/"));
        assertThat(defaultConfigurationTestTemplate.render(),
        		is(Util.toJson(configuration).replace("\\\\", "/")));
    }
}

/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.runtime.BasicAppEnvironment;
import org.eclipse.statet.jcommons.runtime.CommonsRuntime;
import org.eclipse.statet.jcommons.runtime.bundle.Bundles;
import org.eclipse.statet.jcommons.status.StatusException;
import org.eclipse.statet.jcommons.status.util.ACommonsLoggingStatusLogger;

import org.eclipse.statet.rj.server.RjsComConfig;
import org.eclipse.statet.rj.server.client.RClientGraphicDummyFactory;

import eu.openanalytics.rsb.Constants;


/**
 * Handles the RServi runtime environment.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class RServiEnvironmentServletContextListener extends BasicAppEnvironment
        implements ServletContextListener
{

    public RServiEnvironmentServletContextListener() throws StatusException
    {
        super(Constants.BUNDLE_ID, new ACommonsLoggingStatusLogger(), Bundles.createResolver());
    }


    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        CommonsRuntime.init(this);

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicDummyFactory());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        onAppStopping();
    }

}

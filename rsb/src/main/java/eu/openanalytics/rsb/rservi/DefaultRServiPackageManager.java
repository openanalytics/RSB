/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2020
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

package eu.openanalytics.rsb.rservi;

import java.io.File;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.statet.rj.servi.RServi;
import org.eclipse.statet.rj.services.util.RPkgInstallation;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component
public class DefaultRServiPackageManager implements RServiPackageManager
{
    private static final Log LOGGER = LogFactory.getLog(DefaultRServiPackageManager.class);

    @Resource
    private RServiInstanceProvider rServiInstanceProvider;

    public void install(final File packageSourceFile, final String rServiPoolUri) throws Exception
    {
        RServi rServiInstance = null;

        try
        {
            rServiInstance = rServiInstanceProvider.getRServiInstance(rServiPoolUri,
                Constants.RSERVI_CLIENT_ID, PoolingStrategy.NEVER);
            new RPkgInstallation(packageSourceFile).install(rServiInstance, null);
        }
        finally
        {
            if (rServiInstance != null)
            {
                try
                {
                    rServiInstance.close();
                }
                catch (final Exception e)
                {
                    LOGGER.warn("Failed to close RServi instance: " + rServiInstance, e);
                }
            }
        }
    }
}

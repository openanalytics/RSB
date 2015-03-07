/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.ECommons.IAppEnvironment;
import de.walware.ecommons.IDisposable;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.client.RClientGraphic;
import de.walware.rj.server.client.RClientGraphic.InitConfig;
import de.walware.rj.server.client.RClientGraphicActions;
import de.walware.rj.server.client.RClientGraphicDummy;
import de.walware.rj.server.client.RClientGraphicFactory;

/**
 * Handles the RServi runtime environment.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RServiEnvironmentServletContextListener implements ServletContextListener, IAppEnvironment
{

    private final Set<IDisposable> stopListeners = new CopyOnWriteArraySet<IDisposable>();

    private Log logger;

    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        ECommons.init("de.walware.rj.services.eruntime", this);
        logger = LogFactory.getLog("de.walware.rj.servi.pool");

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicFactory()
        {
            @Override
            public Map<String, ? extends Object> getInitServerProperties()
            {
                return null;
            }

            @Override
            public RClientGraphic newGraphic(final int devId,
                                             final double w,
                                             final double h,
                                             final InitConfig config,
                                             final boolean active,
                                             final RClientGraphicActions actions,
                                             final int options)
            {
                return new RClientGraphicDummy(devId, w, h);
            }

            @Override
            public void closeGraphic(final RClientGraphic graphic)
            {
                // NOOP
            }
        });
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        try
        {
            for (final IDisposable listener : this.stopListeners)
            {
                listener.dispose();
            }
        }
        finally
        {
            stopListeners.clear();
        }
    }

    @Override
    public void addStoppingListener(final IDisposable listener)
    {
        stopListeners.add(listener);
    }

    @Override
    public void removeStoppingListener(final IDisposable listener)
    {
        stopListeners.remove(listener);
    }

    @Override
    public void log(final IStatus status)
    {
        switch (status.getSeverity())
        {
            case IStatus.INFO :
                logger.info(status.getMessage(), status.getException());
                break;
            case IStatus.WARNING :
                logger.warn(status.getMessage(), status.getException());
                break;
            case IStatus.ERROR :
                logger.error(status.getMessage(), status.getException());
                break;
            default :
                logger.debug(status.getMessage(), status.getException());
        }
    }
}

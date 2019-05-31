/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.statet.jcommons.collections.ImList;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.ObjectUtils.ToStringBuilder;
import org.eclipse.statet.jcommons.runtime.BasicAppEnvironment;
import org.eclipse.statet.jcommons.runtime.CommonsRuntime;
import org.eclipse.statet.jcommons.status.Status;
import org.eclipse.statet.jcommons.status.StatusPrinter;
import org.eclipse.statet.jcommons.status.Statuses;
import org.eclipse.statet.rj.server.RjsComConfig;
import org.eclipse.statet.rj.server.client.RClientGraphicDummyFactory;


/**
 * Handles the RServi runtime environment.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class RServiEnvironmentServletContextListener extends BasicAppEnvironment
        implements ServletContextListener
{

    private final ConcurrentHashMap<String, Log> logs= new ConcurrentHashMap<>();
    private final StatusPrinter logStatusPrinter= new StatusPrinter();


    @Override
    public String getBundleId() {
        return "eu.openanalytics.rsb";
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

    @Override
    public void log(final Status status)
    {
        final Log log= this.logs.computeIfAbsent(status.getBundleId(),
                (final String s) -> LogFactory.getLog(s) );

        final ToStringBuilder sb= new ToStringBuilder();
        sb.append(Statuses.getSeverityString(status.getSeverity()));
        sb.append(" ["); //$NON-NLS-1$
        sb.append(status.getCode());
        sb.append(']');
        if (status.getMessage().length() <= 80 && status.getMessage().indexOf('\n') == -1) {
            sb.append(' ');
            sb.append(status.getMessage());
        }
        else {
            sb.addProp("message", status.getMessage()); //$NON-NLS-1$
        }
        if (status.isMultiStatus()) {
            final ImList<Status> children= status.getChildren();
            if (children != null && !children.isEmpty()) {
                final StringBuilder sb0= new StringBuilder();
                sb0.append("Status:\n");
                this.logStatusPrinter.print(children, sb0);
                sb.addProp("children", sb0.toString()); //$NON-NLS-1$
            }
            else {
                sb.addProp("children", "<none>"); //$NON-NLS-1$
            }
        }

        switch (status.getSeverity()) {
        case Status.ERROR:
            log.error(sb.toString(), status.getException());
            break;
        case Status.WARNING:
            log.warn(sb.toString(), status.getException());
            break;
        default:
            log.info(sb.toString(), status.getException());
            break;
        }
    }

}

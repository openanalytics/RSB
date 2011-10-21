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

package eu.openanalytics.rsb.cxf;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.transport.servlet.CXFServlet;

/**
 * Supports reloading CXF servlets in case the Spring context has been refreshed.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ReloadableCXFServlet extends CXFServlet {
    private static final long serialVersionUID = -4790705810726987505L;
    private static final Log LOGGER = LogFactory.getLog(ReloadableCXFServlet.class);

    private static final AtomicReference<ServletConfig> servletConfigRef = new AtomicReference<ServletConfig>();
    private static final Set<ReloadableCXFServlet> SERVLETS = new CopyOnWriteArraySet<ReloadableCXFServlet>();

    public static void reloadAll() {
        final HashSet<ReloadableCXFServlet> servletsToReload = new HashSet<ReloadableCXFServlet>(SERVLETS);
        final ServletConfig servletConfig = servletConfigRef.get();
        for (final ReloadableCXFServlet servlet : servletsToReload) {
            try {
                servlet.destroy();
                servlet.setBus(null);
                servlet.init(servletConfig);
            } catch (final ServletException se) {
                LOGGER.error("Failed to reload servlet: " + ToStringBuilder.reflectionToString(servlet, ToStringStyle.SHORT_PREFIX_STYLE),
                        se);
            }
        }
    }

    @Override
    public void init(final ServletConfig sc) throws ServletException {
        servletConfigRef.compareAndSet(null, sc);
        super.init(sc);
        SERVLETS.add(this);
    }

    @Override
    public void destroy() {
        SERVLETS.remove(this);
        super.destroy();
    }
}

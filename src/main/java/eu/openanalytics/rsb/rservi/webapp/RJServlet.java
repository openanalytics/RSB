/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2014
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

package eu.openanalytics.rsb.rservi.webapp;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.walware.rj.servi.pool.JMPoolServer;

/**
 * Variant of <code>de.walware.rj.servi.webapp.RJServlet</code> that uses {@link ServletRJContext}
 * instead of <code>de.walware.rj.servi.webapp.ServletRJContext</code>.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RJServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final String POOLID_KEY = "pool.id";
    private static final String RJCONTEXT_KEY = "rj.context";
    private static final String RJ_POOLSERVER_KEY = "rj.pool.server";

    private JMPoolServer server = null;

    @Override
    public void init(final ServletConfig config) throws ServletException
    {
        final ServletContext servletContext = config.getServletContext();

        servletContext.log("Initializing: " + getClass().getSimpleName());

        super.init(config);

        try
        {
            String id = servletContext.getContextPath();
            if (id.startsWith("/"))
            {
                id = id.substring(1);
            }
            servletContext.setAttribute(POOLID_KEY, id);

            final ServletRJContext rjContext = new ServletRJContext(servletContext);
            servletContext.setAttribute(RJCONTEXT_KEY, rjContext);

            servletContext.log("RServi properties file location: " + rjContext.getPropertiesDirPath());

            servletContext.log("Initializing: " + JMPoolServer.class.getSimpleName());
            server = new JMPoolServer(id, rjContext);
            servletContext.log("Starting: " + server);
            server.start();

            servletContext.setAttribute(RJ_POOLSERVER_KEY, server);

            servletContext.log("RServi intial context attributes: " + POOLID_KEY + " = "
                               + servletContext.getAttribute(POOLID_KEY) + " ; " + RJCONTEXT_KEY + " = "
                               + servletContext.getAttribute(RJCONTEXT_KEY) + " ; " + RJ_POOLSERVER_KEY
                               + " = " + servletContext.getAttribute(RJ_POOLSERVER_KEY));
        }
        catch (final Exception e)
        {
            destroy();

            throw new ServletException("Failed to initialize RServi Server.", e);
        }
    }

    @Override
    public void destroy()
    {
        getServletContext().log("Destroying: " + getClass().getSimpleName());

        try
        {
            if (server != null)
            {
                getServletContext().removeAttribute(RJ_POOLSERVER_KEY);
                server.shutdown();
                server = null;
            }
        }
        catch (final Exception e)
        {
            getServletContext().log("Failed to destroy RServi servlet", e);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setStatus(server != null ? SC_OK : SC_SERVICE_UNAVAILABLE);
    }
}

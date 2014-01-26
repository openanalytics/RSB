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

import java.io.IOException;

import javax.servlet.ServletConfig;
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

    private JMPoolServer server;

    @Override
    public void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            String id = getServletContext().getContextPath();
            if (id.startsWith("/"))
            {
                id = id.substring(1);
            }
            getServletContext().setAttribute("pool.id", id);

            final ServletRJContext rjContext = new ServletRJContext(getServletContext());
            getServletContext().setAttribute("rj.context", rjContext);

            server = new JMPoolServer(id, rjContext);
            server.start();

            getServletContext().setAttribute("rj.pool.server", server);
        }
        catch (final Exception e)
        {
            if (server != null)
            {
                server.shutdown();
                server = null;
            }
            throw new ServletException("Failed to initialized RServi Server.", e);
        }
    }

    @Override
    public void destroy()
    {
        if (server != null)
        {
            getServletContext().removeAttribute("rj.pool.server");

            server.shutdown();
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {

        response.setStatus(server != null
                                         ? HttpServletResponse.SC_OK
                                         : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
}

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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.walware.rj.servi.RServi;
import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;

/**
 * Handles health check requests.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("systemHealthResource")
@Path("/" + Constants.HEALTH_PATH)
public class SystemHealthResource extends AbstractComponent {
    @Resource
    private RServiInstanceProvider rServiInstanceProvider;

    private final AtomicBoolean nodeHealthy = new AtomicBoolean(true);

    // exposed for unit testing
    void setRServiInstanceProvider(final RServiInstanceProvider rServiInstanceProvider) {
        this.rServiInstanceProvider = rServiInstanceProvider;
    }

    @GET
    @Path("/check")
    @Produces({ Constants.TEXT_CONTENT_TYPE })
    public Response check() {
        return nodeHealthy.get() ? Response.ok("OK").build() : Response.status(Status.INTERNAL_SERVER_ERROR).entity("ERROR").build();
    }

    @Scheduled(fixedDelay = 60000)
    public void verifyNodeHealth() {
        try {
            verifyRServiConnectivity();
            // LATER consider other tests
            nodeHealthy.set(true);
        } catch (final Exception e) {
            getLogger().error("RSB is in bad health!", e);
            nodeHealthy.set(false);
        }
    }

    private void verifyRServiConnectivity() throws Exception {
        final RServi rServi = rServiInstanceProvider.getRServiInstance(getConfiguration().getDefaultRserviPoolUri().toString(),
                Constants.RSERVI_CLIENT_ID);
        rServi.close();
    }
}

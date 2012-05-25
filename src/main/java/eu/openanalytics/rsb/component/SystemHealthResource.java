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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.walware.rj.servi.RServi;
import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.NodeInformation;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider;
import eu.openanalytics.rsb.rservi.RServiInstanceProvider.PoolingStrategy;

/**
 * Handles health check requests.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("systemResource")
@Path("/" + Constants.SYSTEM_PATH)
public class SystemHealthResource extends AbstractComponent {
    @Resource
    private RServiInstanceProvider rServiInstanceProvider;

    private final AtomicBoolean nodeHealthy = new AtomicBoolean(true);

    // exposed for unit testing
    void setRServiInstanceProvider(final RServiInstanceProvider rServiInstanceProvider) {
        this.rServiInstanceProvider = rServiInstanceProvider;
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

    @GET
    @Path("/health/check")
    @Produces({ Constants.TEXT_CONTENT_TYPE })
    public Response check() {
        return nodeHealthy.get() ? Response.ok("OK").build() : Response.status(Status.INTERNAL_SERVER_ERROR).entity("ERROR").build();
    }

    @GET
    @Path("/info")
    @Produces({ Constants.RSB_XML_CONTENT_TYPE, Constants.RSB_JSON_CONTENT_TYPE })
    public NodeInformation getInfo(@Context final ServletContext sc) {
        final NodeInformation info = Util.REST_OBJECT_FACTORY.createNodeInformation();

        info.setName(getConfiguration().getNodeName());
        info.setHealthy(nodeHealthy.get());
        info.setRsbVersion(getClass().getPackage().getImplementationVersion());
        info.setServletContainerInfo(sc.getServerInfo());

        final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        info.setOsLoadAverage(operatingSystemMXBean.getSystemLoadAverage());

        final Runtime runtime = Runtime.getRuntime();
        info.setJvmMaxMemory(runtime.maxMemory());
        info.setJvmFreeMemory(runtime.freeMemory());

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        final long uptimeMilliseconds = runtimeMXBean.getUptime();
        info.setUptime(uptimeMilliseconds);
        info.setUptimeText(DurationFormatUtils.formatDurationWords(uptimeMilliseconds, true, true));

        return info;
    }

    private void verifyRServiConnectivity() throws Exception {
        final RServi rServi = rServiInstanceProvider.getRServiInstance(getConfiguration().getDefaultRserviPoolUri().toString(),
                Constants.RSERVI_CLIENT_ID, PoolingStrategy.NEVER);
        rServi.close();
    }
}

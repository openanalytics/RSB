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
import de.walware.rj.server.client.RClientGraphicActions;
import de.walware.rj.server.client.RClientGraphicDummy;
import de.walware.rj.server.client.RClientGraphicFactory;

/**
 * Handles the RServi runtime environment.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RServiEnvironmentServletContextListener implements ServletContextListener, IAppEnvironment {

    private final Set<IDisposable> stopListeners = new CopyOnWriteArraySet<IDisposable>();

    private Log logger;

    public void contextInitialized(final ServletContextEvent sce) {
        ECommons.init("de.walware.rj.services.eruntime", this);
        logger = LogFactory.getLog("de.walware.rj.servi.pool");

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicFactory() {
            public Map<String, ? extends Object> getInitServerProperties() {
                return null;
            }

            public RClientGraphic newGraphic(final int devId, final double w, final double h, final boolean active,
                    final RClientGraphicActions actions, final int options) {
                return new RClientGraphicDummy(devId, w, h);
            }

            public void closeGraphic(final RClientGraphic graphic) {
                // NOOP
            }
        });
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        try {
            for (final IDisposable listener : this.stopListeners) {
                listener.dispose();
            }
        } finally {
            stopListeners.clear();
        }
    }

    public void addStoppingListener(final IDisposable listener) {
        stopListeners.add(listener);
    }

    public void removeStoppingListener(final IDisposable listener) {
        stopListeners.remove(listener);
    }

    public void log(final IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.INFO:
            logger.info(status.getMessage(), status.getException());
            break;
        case IStatus.WARNING:
            logger.warn(status.getMessage(), status.getException());
            break;
        case IStatus.ERROR:
            logger.error(status.getMessage(), status.getException());
            break;
        default:
            logger.debug(status.getMessage(), status.getException());
        }
    }
}

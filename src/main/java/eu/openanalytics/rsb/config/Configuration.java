package eu.openanalytics.rsb.config;

import java.io.File;
import java.net.URI;
import java.util.Map;

public interface Configuration {

    File getRsbResultsDirectory();

    File getActiveMqWorkDirectory();

    URI getDefaultRserviPoolUri();

    Map<String, URI> getApplicationSpecificRserviPoolUris();

    /**
     * @return time-out in milliseconds.
     */
    int getJobTimeOut();
}

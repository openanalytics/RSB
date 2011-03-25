package eu.openanalytics.rsb.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

public abstract class DefaultConfiguration implements Configuration {
    protected final File userHomeDirectory;
    private final File rsbHomeDirectory;
    private final File rsbResultsDirectory;
    private final File activeMqWorkDirectory;
    private final URI defaultRserviPoolUri;
    private final Map<String, URI> applicationSpecificRserviPoolUris;
    private final int jobTimeOut;

    public DefaultConfiguration() throws URISyntaxException {
        userHomeDirectory = new File(System.getProperty("user.home"));
        rsbHomeDirectory = new File(userHomeDirectory, "rsb");
        rsbResultsDirectory = new File(rsbHomeDirectory, "results");
        activeMqWorkDirectory = new File(rsbHomeDirectory, "activemq");
        defaultRserviPoolUri = new URI("rmi://127.0.0.1/rservi-pool");
        applicationSpecificRserviPoolUris = Collections.emptyMap();
        jobTimeOut = 600000; // 10 minutes
    }

    protected File getUserHomeDirectory() {
        return userHomeDirectory;
    }

    public File getRsbResultsDirectory() {
        return rsbResultsDirectory;
    }

    public File getActiveMqWorkDirectory() {
        return activeMqWorkDirectory;
    }

    public URI getDefaultRserviPoolUri() {
        return defaultRserviPoolUri;
    }

    public Map<String, URI> getApplicationSpecificRserviPoolUris() {
        return applicationSpecificRserviPoolUris;
    }

    public int getJobTimeOut() {
        return jobTimeOut;
    }
}

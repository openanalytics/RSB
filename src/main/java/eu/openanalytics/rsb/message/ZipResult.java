package eu.openanalytics.rsb.message;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Represents a RSB result that consists of an Zip archive of multiple files.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class ZipResult extends MultiFilesResult {
    private static final long serialVersionUID = 1L;

    public ZipResult(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime, final boolean success)
            throws IOException {
        super(applicationName, jobId, submissionTime, success);
    }
}

package eu.openanalytics.rsb.message;

import java.io.Serializable;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import eu.openanalytics.rsb.Constants;

/**
 * Represents a RSB job that consists in calling a 1-arity function on R.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class FunctionCallJob implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationName;
    private String jobId;
    private Map<String, String> meta;
    private final String argumentType;
    private String argument;

    public FunctionCallJob(final String applicationName, final String jobId, final MediaType mediaType, final String argument,
            final Map<String, String> meta) {
        this.applicationName = applicationName;
        this.jobId = jobId;
        this.argumentType = mediaType.toString();
        this.argument = argument;
        this.meta = meta;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getFunctionName() {
        if (StringUtils.equalsIgnoreCase(argumentType, Constants.XML_JOB_CONTENT_TYPE)) {
            return "RSBXmlService";
        }
        if (StringUtils.equalsIgnoreCase(argumentType, Constants.JSON_JOB_CONTENT_TYPE)) {
            return "RSBJsonService";
        }
        throw new IllegalArgumentException("Impossible to determine function name for: " + this);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(final Map<String, String> meta) {
        this.meta = meta;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(final String argument) {
        this.argument = argument;
    }

    public String getArgumentType() {
        return argumentType;
    }
}

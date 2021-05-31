/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.activation.MimeType;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;


/**
 * Useful constants.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class Constants {
	
	public static final String BUNDLE_ID= "eu.openanalytics.rsb"; //$NON-NLS-1$
	
	
    /**
     * This header allows support of SSL termination to happen upstream of RSB.
     */
    public static final String FORWARDED_PROTOCOL_HTTP_HEADER = "X-Forwarded-Protocol";

    public static final String CONTENT_TYPE_HTTP_HEADER = "Content-Type";
	
	
	/**
	 * The name of the field for the application name.
	 */
	public static final String APPLICATION_NAME_FIELD_NAME= "x-rsb-application-name";
	
	/**
	 * The name prefix of fields for job meta information parameter (specification type A).
	 * 
	 * The parameter name is defined by the substring of the field name following this prefix.
	 * The field value is the parameter value.
	 * 
	 * If the parameter is a custom parameter specified by a HTTP header field, the parameter name
	 * is always converted to lower case.
	 */
	public static final String RSB_META_A_FIELD_NAME_PREFIX= "x-rsb-meta-";
	
	/**
	 * The name prefix of fields for job meta information parameter (specification type B).
	 * 
	 * The substring of the field name following this prefix is supposed to make the field name
	 * unqiue, it has no role in RSB.
	 * The parameter is specified by a name-value-pair <code>ParameterName= ParameterValue</code>
	 * in the field value.
	 */
	public static final String RSB_META_B_FIELD_NAME_PREFIX= "x-rsb-meta-#";
	
	/**
	 * The name of the field for the jobfile names.
	 */
	public static final String JOB_FILES_FIELD_NAME= "x-rsb-jobfile[]";
	
	/** @deprecated use {@link #APPLICATION_NAME_FIELD_NAME} instead */
	@Deprecated
	public static final String APPLICATION_NAME_HTTP_HEADER= "X-RSB-Application-Name";
	/** @deprecated use {@link #RSB_META_A_FIELD_NAME_PREFIX} instead */
	@Deprecated
	public static final String RSB_META_HEADER_HTTP_PREFIX= "X-RSB-Meta-";
	/** @deprecated use {@link #JOB_FILES_FIELD_NAME} instead */
	@Deprecated
	public static final String JOB_FILES_MULTIPART_NAME= "X-RSB-JobFile[]";
	
    public final static String JOBS_PATH = "jobs";
    public final static String PROCESS_PATH = "process";
    public final static String RESULTS_PATH = "results";
    public final static String RESULT_PATH = "result";
    public final static String DATA_DIR_PATH = "data";
    public final static String SYSTEM_PATH = "system";
    public final static String ADMIN_PATH = "admin";

    public static final String SOURCE_MESSAGE_HEADER = "source";
    public static final String APPLICATION_NAME_MESSAGE_HEADER = "applicationName";
    public static final String JOB_ID_MESSAGE_HEADER = "jobId";

    public static final String MULTIPLE_FILES_JOB_CONFIGURATION = "configuration.txt";
    public static final String R_SCRIPT_FILE_EXTENSION = "R";
    public static final String R_SCRIPT_CONFIGURATION_KEY = "rScript";
    public static final String SWEAVE_FILE_CONFIGURATION_KEY = "sweaveFile";

    public static final Map<String, String> WELL_KNOWN_CONFIGURATION_KEYS;
    static
    {
        WELL_KNOWN_CONFIGURATION_KEYS = new HashMap<>();
        WELL_KNOWN_CONFIGURATION_KEYS.put(R_SCRIPT_CONFIGURATION_KEY.toLowerCase(Locale.ROOT),
            R_SCRIPT_CONFIGURATION_KEY);
        WELL_KNOWN_CONFIGURATION_KEYS.put(SWEAVE_FILE_CONFIGURATION_KEY.toLowerCase(Locale.ROOT),
            SWEAVE_FILE_CONFIGURATION_KEY);
    }

    public static final String DEFAULT_R_LOG_FILE = "out.log";

    public static final String RSB_JSON_CONTENT_TYPE = "application/vnd.rsb+json";
    public static final String RSB_XML_CONTENT_TYPE = "application/vnd.rsb+xml";

    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_CONTENT_TYPE = "text/plain";
    public static final String ZIP_CONTENT_TYPE = "application/zip";
    public static final String ZIP_CONTENT_TYPE2 = "application/x-zip";
    public static final String ZIP_CONTENT_TYPE3 = "application/x-zip-compressed";
    public static final Set<String> ZIP_CONTENT_TYPES = new HashSet<>(Arrays.asList(ZIP_CONTENT_TYPE,
        ZIP_CONTENT_TYPE2, ZIP_CONTENT_TYPE3));
    public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    public static final String GZIP_CONTENT_TYPE = "application/gzip";

    public static final MimeType JSON_MIME_TYPE;
    public static final MimeType XML_MIME_TYPE;
    public static final MimeType TEXT_MIME_TYPE;
    public static final MimeType PDF_MIME_TYPE;
    public static final MimeType ZIP_MIME_TYPE;
    public static final MimeType DEFAULT_MIME_TYPE;

    public static final String HOST_NAME;
    public static final String RSERVI_CLIENT_ID;
    public static final String RSERVI_CLIENT_POOL_OBJECT_NAME = "rsb:domain=rservi,name=RServiClientPool";

    static
    {
        try
        {
            JSON_MIME_TYPE = new MimeType(JSON_CONTENT_TYPE);
            XML_MIME_TYPE = new MimeType(XML_CONTENT_TYPE);
            TEXT_MIME_TYPE = new MimeType(TEXT_CONTENT_TYPE);
            PDF_MIME_TYPE = new MimeType(PDF_CONTENT_TYPE);
            ZIP_MIME_TYPE = new MimeType(ZIP_CONTENT_TYPE);
            DEFAULT_MIME_TYPE = new MimeType("application/octet-stream");

            HOST_NAME = InetAddress.getLocalHost().getHostName();
            RSERVI_CLIENT_ID = "rsb@" + HOST_NAME;
        }
        catch (final Exception mtpe)
        {
            throw new IllegalStateException(mtpe);
        }
    }


    private Constants()
    {
        throw new UnsupportedOperationException("do not instantiate");
    }
}

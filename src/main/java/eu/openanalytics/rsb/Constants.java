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
package eu.openanalytics.rsb;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * Useful constants.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class Constants {
    public static final String URI_OVERRIDE_HTTP_HEADER = "X-Base-Uri";
    public static final String APPLICATION_NAME_HTTP_HEADER = "X-RSB-Application-Name";
    public static final String RSB_META_HEADER_PREFIX = "X-RSB-Meta-";

    public static final String APPLICATION_NAME_JMS_HEADER = "applicationName";
    public static final String JOB_ID_JMS_HEADER = "jobId";

    public static final String RSB_JSON_CONTENT_TYPE = "application/vnd.rsb+json";
    public static final String RSB_XML_CONTENT_TYPE = "application/vnd.rsb+xml";

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_CONTENT_TYPE = "text/plain";
    public static final String ZIP_CONTENT_TYPE = "application/zip";
    public static final String[] ALL_ZIP_CONTENT_TYPES = { ZIP_CONTENT_TYPE, "application/x-zip", "application/x-zip-compressed" };

    public static final MimeType JSON_MIME_TYPE;
    public static final MimeType XML_MIME_TYPE;
    public static final MimeType TEXT_MIME_TYPE;
    public static final MimeType ZIP_MIME_TYPE;

    static {
        try {
            JSON_MIME_TYPE = new MimeType(JSON_CONTENT_TYPE);
            XML_MIME_TYPE = new MimeType(XML_CONTENT_TYPE);
            TEXT_MIME_TYPE = new MimeType(TEXT_CONTENT_TYPE);
            ZIP_MIME_TYPE = new MimeType(ZIP_CONTENT_TYPE);
        } catch (final MimeTypeParseException mtpe) {
            throw new IllegalStateException(mtpe);
        }
    }

    private Constants() {
        throw new UnsupportedOperationException("do not instantiate");
    }
}

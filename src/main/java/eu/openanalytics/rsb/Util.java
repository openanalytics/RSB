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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.openanalytics.rsb.rest.types.ErrorResult;
import eu.openanalytics.rsb.rest.types.ObjectFactory;

/**
 * Shared utilities.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class Util {
    private final static Pattern APPLICATION_NAME_VALIDATOR = Pattern.compile("\\w+");

    private final static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static ObjectMapper PRETTY_JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static JAXBContext ERROR_RESULT_JAXB_CONTEXT;
    private final static DatatypeFactory XML_DATATYPE_FACTORY;

    private static final MimetypesFileTypeMap MIMETYPES_FILETYPE_MAP = new MimetypesFileTypeMap();
    private static final String DEFAULT_FILE_EXTENSION = "dat";
    private static final Map<String, String> DEFAULT_FILE_EXTENSIONS = new HashMap<String, String>();

    static {
        PRETTY_JSON_OBJECT_MAPPER.configure(Feature.INDENT_OUTPUT, true);
        PRETTY_JSON_OBJECT_MAPPER.configure(Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
        PRETTY_JSON_OBJECT_MAPPER.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

        try {
            ERROR_RESULT_JAXB_CONTEXT = JAXBContext.newInstance(ErrorResult.class);
            XML_DATATYPE_FACTORY = DatatypeFactory.newInstance();
            MIMETYPES_FILETYPE_MAP.addMimeTypes(Constants.JSON_CONTENT_TYPE + " json\n" + Constants.XML_CONTENT_TYPE + " xml\n"
                    + Constants.TEXT_CONTENT_TYPE + " txt\n" + Constants.TEXT_CONTENT_TYPE + " R\n" + Constants.TEXT_CONTENT_TYPE
                    + " Rnw\n" + Constants.PDF_CONTENT_TYPE + " pdf\n" + Constants.ZIP_CONTENT_TYPE + " zip");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        DEFAULT_FILE_EXTENSIONS.put(Constants.JSON_CONTENT_TYPE, "json");
        DEFAULT_FILE_EXTENSIONS.put(Constants.XML_CONTENT_TYPE, "xml");
        DEFAULT_FILE_EXTENSIONS.put(Constants.TEXT_CONTENT_TYPE, "txt");
        DEFAULT_FILE_EXTENSIONS.put(Constants.PDF_CONTENT_TYPE, "pdf");
        DEFAULT_FILE_EXTENSIONS.put(Constants.ZIP_CONTENT_TYPE, "zip");
    }

    public final static ObjectFactory REST_OBJECT_FACTORY = new ObjectFactory();
    public final static eu.openanalytics.rsb.soap.types.ObjectFactory SOAP_OBJECT_FACTORY = new eu.openanalytics.rsb.soap.types.ObjectFactory();

    private Util() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    /**
     * Returns the must probable resource type for a MimeType.
     * 
     * @param mimeType
     * @return
     */
    public static String getResourceType(final MimeType mimeType) {
        final String result = DEFAULT_FILE_EXTENSIONS.get(mimeType.toString());
        return result != null ? result : DEFAULT_FILE_EXTENSION;
    }

    /**
     * Returns the must probable content type for a file.
     * 
     * @param file
     * @return "application/octet-stream" if unknown.
     */
    public static String getContentType(final File file) {
        return MIMETYPES_FILETYPE_MAP.getContentType(file);
    }

    /**
     * Returns the must probable mime type for a file.
     * 
     * @param file
     * @return {@link eu.openanalytics.rsb.Constants#DEFAULT_MIME_TYPE} if unknown.
     */
    public static MimeType getMimeType(final File file) {
        try {
            return new MimeType(getContentType(file));
        } catch (final MimeTypeParseException mtpe) {
            return Constants.DEFAULT_MIME_TYPE;
        }
    }

    /**
     * Builds a result URI.
     * 
     * @param applicationName
     * @param jobId
     * @param httpHeaders
     * @param uriInfo
     * @return
     * @throws URISyntaxException
     */
    public static URI buildResultUri(final String applicationName, final String jobId, final HttpHeaders httpHeaders, final UriInfo uriInfo)
            throws URISyntaxException {
        return getUriBuilder(uriInfo, httpHeaders).path(Constants.RESULTS_PATH).path(applicationName).path(jobId).build();
    }

    /**
     * Builds a data directory URI.
     * 
     * @param applicationName
     * @param jobId
     * @param httpHeaders
     * @param uriInfo
     * @return
     * @throws URISyntaxException
     */
    public static URI buildDataDirectoryUri(final HttpHeaders httpHeaders, final UriInfo uriInfo, final String... directoryPathElements)
            throws URISyntaxException {
        UriBuilder uriBuilder = getUriBuilder(uriInfo, httpHeaders).path(Constants.DATA_DIR_PATH);
        for (final String directoryPathElement : directoryPathElements) {
            if (StringUtils.isNotEmpty(directoryPathElement)) {
                uriBuilder = uriBuilder.path(directoryPathElement);
            }
        }
        return uriBuilder.build();
    }

    /**
     * Validates that the passed string is a valid application name.
     * 
     * @param name
     * @return
     */
    public static boolean isValidApplicationName(final String name) {
        return StringUtils.isNotBlank(name) && APPLICATION_NAME_VALIDATOR.matcher(name).matches();
    }

    /**
     * Extracts an UriBuilder for the current request, taking into account the possibility of header-based URI override.
     * 
     * @param uriInfo
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     */
    public static UriBuilder getUriBuilder(final UriInfo uriInfo, final HttpHeaders httpHeaders) throws URISyntaxException {
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        final List<String> hosts = httpHeaders.getRequestHeader(HttpHeaders.HOST);
        if ((hosts != null) && (!hosts.isEmpty())) {
            final String host = hosts.get(0);
            uriBuilder.host(StringUtils.substringBefore(host, ":"));

            final String port = StringUtils.substringAfter(host, ":");
            if (StringUtils.isNotBlank(port)) {
                uriBuilder.port(Integer.valueOf(port));
            }
        }

        final String protocol = getSingleHeader(httpHeaders, Constants.FORWARDED_PROTOCOL_HTTP_HEADER);
        if (StringUtils.isNotBlank(protocol)) {
            uriBuilder.scheme(protocol);
        }

        return uriBuilder;
    }

    /**
     * Converts a {@link GregorianCalendar} into a {@link XMLGregorianCalendar}
     * 
     * @param calendar
     * @return
     */
    public static XMLGregorianCalendar convertToXmlDate(final GregorianCalendar calendar) {
        final GregorianCalendar zuluDate = new GregorianCalendar();
        zuluDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        zuluDate.setTimeInMillis(calendar.getTimeInMillis());

        final XMLGregorianCalendar xmlDate = XML_DATATYPE_FACTORY.newXMLGregorianCalendar(zuluDate);
        return xmlDate;
    }

    /**
     * Gets the first header of multiple HTTP headers, returning null if no header is found for the name.
     * 
     * @param httpHeaders
     * @param headerName
     * @return
     */
    public static String getSingleHeader(final HttpHeaders httpHeaders, final String headerName) {
        final List<String> headers = httpHeaders.getRequestHeader(headerName);

        if ((headers == null) || (headers.isEmpty())) {
            return null;
        }

        return headers.get(0);
    }

    /**
     * Creates a temporary directory. Lifted from:
     * http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java/617438#617438
     * 
     * @return
     * @throws IOException
     */
    public static File createTemporaryDirectory(final String type) throws IOException {
        final File temp;

        temp = File.createTempFile("rsb_", type);

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    /**
     * Marshals an {@link ErrorResult} to XML.
     * 
     * @param errorResult
     * @return
     */
    public static String toXml(final ErrorResult errorResult) {
        try {
            final Marshaller marshaller = ERROR_RESULT_JAXB_CONTEXT.createMarshaller();
            final StringWriter sw = new StringWriter();
            marshaller.marshal(errorResult, sw);
            return sw.toString();
        } catch (final JAXBException je) {
            final String objectAsString = ToStringBuilder.reflectionToString(errorResult, ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to XML marshall: " + objectAsString, je);
        }
    }

    /**
     * Marshals an {@link Object} to a JSON string.
     * 
     * @param o
     * @return
     */
    public static String toJson(final Object o) {
        try {
            return PRETTY_JSON_OBJECT_MAPPER.writeValueAsString(o);
        } catch (final IOException ioe) {
            final String objectAsString = ToStringBuilder.reflectionToString(o, ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to JSON marshall: " + objectAsString, ioe);
        }
    }

    /**
     * Marshals an {@link Object} to a pretty-printed JSON file.
     * 
     * @param o
     * @throws IOException
     */
    public static void toPrettyJsonFile(final Object o, final File f) throws IOException {
        try {
            PRETTY_JSON_OBJECT_MAPPER.writeValue(f, o);
        } catch (final JsonProcessingException jpe) {
            final String objectAsString = ToStringBuilder.reflectionToString(o, ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to JSON marshall: " + objectAsString, jpe);
        }
    }

    /**
     * Unmarshals a JSON string to a desired type.
     * 
     * @param s
     * @return
     */
    public static <T> T fromJson(final String s, final Class<T> clazz) {
        try {
            return JSON_OBJECT_MAPPER.readValue(s, clazz);
        } catch (final IOException ioe) {
            throw new RuntimeException("Failed to JSON unmarshall: " + s, ioe);
        }
    }
}

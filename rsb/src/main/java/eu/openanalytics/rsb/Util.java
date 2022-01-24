/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
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

import static org.eclipse.statet.jcommons.io.FileUtils.requireFileName;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
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

import org.eclipse.statet.jcommons.status.StatusException;

import org.eclipse.statet.rj.data.RObject;
import org.eclipse.statet.rj.servi.RServi;
import org.eclipse.statet.rj.services.FunctionCall;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import eu.openanalytics.rsb.rest.types.ErrorResult;
import eu.openanalytics.rsb.rest.types.ObjectFactory;


/**
 * Shared utilities.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class Util
{
    private final static Pattern APPLICATION_NAME_VALIDATOR = Pattern.compile("\\w+");

    private final static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static ObjectMapper PRETTY_JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static JAXBContext ERROR_RESULT_JAXB_CONTEXT;
    private final static DatatypeFactory XML_DATATYPE_FACTORY;

    private static final MimetypesFileTypeMap MIMETYPES_FILETYPE_MAP = new MimetypesFileTypeMap();
    private static final String DEFAULT_FILE_EXTENSION = "dat";
    private static final Map<String, String> DEFAULT_FILE_EXTENSIONS = new HashMap<>();
    private static final STGroup $_STRING_TEMPLATE_GROUP = new STGroup('$', '$');

    static
    {
        PRETTY_JSON_OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        PRETTY_JSON_OBJECT_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        PRETTY_JSON_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try
        {
            ERROR_RESULT_JAXB_CONTEXT = JAXBContext.newInstance(ErrorResult.class);
            XML_DATATYPE_FACTORY = DatatypeFactory.newInstance();
            MIMETYPES_FILETYPE_MAP.addMimeTypes(Constants.JSON_CONTENT_TYPE + " json\n"
                                                + Constants.XML_CONTENT_TYPE + " xml\n"
                                                + Constants.TEXT_CONTENT_TYPE + " txt\n"
                                                + Constants.TEXT_CONTENT_TYPE + " R\n"
                                                + Constants.TEXT_CONTENT_TYPE + " Rnw\n"
                                                + Constants.PDF_CONTENT_TYPE + " pdf\n"
                                                + Constants.ZIP_CONTENT_TYPE + " zip");
        }
        catch (final Exception e)
        {
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

    private Util()
    {
        throw new UnsupportedOperationException("do not instantiate");
    }

    /**
     * Creates a new {@link ST} configured with a $..$ group.
     * 
     * @param template
     * @return a new {@link ST}
     */
    public static ST newStringTemplate(final String template)
    {
        return new ST($_STRING_TEMPLATE_GROUP, template);
    }

    /**
     * Returns the must probable resource type for a MimeType.
     * 
     * @param mimeType
     * @return
     */
    public static String getResourceType(final MimeType mimeType)
    {
        final String result = DEFAULT_FILE_EXTENSIONS.get(mimeType.toString());
        return result != null ? result : DEFAULT_FILE_EXTENSION;
    }
	
	/**
	 * Returns the must probable content type for a file.
	 * 
	 * @param file
	 * @return "application/octet-stream" if unknown.
	 */
	public static String getContentType(final Path file) {
		return MIMETYPES_FILETYPE_MAP.getContentType(requireFileName(file).toString());
	}
	
    /**
     * Returns the must probable content type for a file.
     * 
     * @param file
     * @return "application/octet-stream" if unknown.
     */
    public static String getContentType(final File file)
    {
    	return MIMETYPES_FILETYPE_MAP.getContentType(file);
    }
    
	/**
	 * Returns the must probable mime type for a file.
	 * 
	 * @param file
	 * @return {@link eu.openanalytics.rsb.Constants#DEFAULT_MIME_TYPE} if unknown.
	 */
	public static MimeType getMimeType(final Path file) {
		try {
			return new MimeType(getContentType(file));
		}
		catch (final MimeTypeParseException e) {
			return Constants.DEFAULT_MIME_TYPE;
		}
	}
	
    /**
     * Returns the must probable mime type for a file.
     * 
     * @param file
     * @return {@link eu.openanalytics.rsb.Constants#DEFAULT_MIME_TYPE} if unknown.
     */
    public static MimeType getMimeType(final File file)
    {
        try
        {
            return new MimeType(getContentType(file));
        }
        catch (final MimeTypeParseException mtpe)
        {
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
    public static URI buildResultUri(final String applicationName,
                                     final String jobId,
                                     final HttpHeaders httpHeaders,
                                     final UriInfo uriInfo) throws URISyntaxException
    {
        return getUriBuilder(uriInfo, httpHeaders).path(Constants.RESULTS_PATH)
            .path(applicationName)
            .path(jobId)
            .build();
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
    public static URI buildDataDirectoryUri(final HttpHeaders httpHeaders,
                                            final UriInfo uriInfo,
                                            final String... directoryPathElements) throws URISyntaxException
    {
        UriBuilder uriBuilder = getUriBuilder(uriInfo, httpHeaders).path(Constants.DATA_DIR_PATH);
        for (final String directoryPathElement : directoryPathElements)
        {
            if (StringUtils.isNotEmpty(directoryPathElement))
            {
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
    public static boolean isValidApplicationName(final String name)
    {
        return StringUtils.isNotBlank(name) && APPLICATION_NAME_VALIDATOR.matcher(name).matches();
    }

    /**
     * Extracts an UriBuilder for the current request, taking into account the possibility of
     * header-based URI override.
     * 
     * @param uriInfo
     * @param httpHeaders
     * @return
     * @throws URISyntaxException
     */
    public static UriBuilder getUriBuilder(final UriInfo uriInfo, final HttpHeaders httpHeaders)
        throws URISyntaxException
    {
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        final List<String> hosts = httpHeaders.getRequestHeader(HttpHeaders.HOST);
        if ((hosts != null) && (!hosts.isEmpty()))
        {
            final String host = hosts.get(0);
            uriBuilder.host(StringUtils.substringBefore(host, ":"));

            final String port = StringUtils.substringAfter(host, ":");
            if (StringUtils.isNotBlank(port))
            {
                uriBuilder.port(Integer.parseInt(port));
            }
        }

        final String protocol = getSingleHeader(httpHeaders, Constants.FORWARDED_PROTOCOL_HTTP_HEADER);
        if (StringUtils.isNotBlank(protocol))
        {
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
    public static XMLGregorianCalendar convertToXmlDate(final GregorianCalendar calendar)
    {
        final GregorianCalendar zuluDate = new GregorianCalendar();
        zuluDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        zuluDate.setTimeInMillis(calendar.getTimeInMillis());

        final XMLGregorianCalendar xmlDate = XML_DATATYPE_FACTORY.newXMLGregorianCalendar(zuluDate);
        return xmlDate;
    }

    /**
     * Gets the first header of multiple HTTP headers, returning null if no header is found for the
     * name.
     * 
     * @param httpHeaders
     * @param headerName
     * @return
     */
    public static String getSingleHeader(final HttpHeaders httpHeaders, final String headerName)
    {
        final List<String> headers = httpHeaders.getRequestHeader(headerName);

        if ((headers == null) || (headers.isEmpty()))
        {
            return null;
        }

        return headers.get(0);
    }
	
	
    /**
     * Marshals an {@link ErrorResult} to XML.
     * 
     * @param errorResult
     * @return
     */
    public static String toXml(final ErrorResult errorResult)
    {
        try
        {
            final Marshaller marshaller = ERROR_RESULT_JAXB_CONTEXT.createMarshaller();
            final StringWriter sw = new StringWriter();
            marshaller.marshal(errorResult, sw);
            return sw.toString();
        }
        catch (final JAXBException je)
        {
            final String objectAsString = ToStringBuilder.reflectionToString(errorResult,
                ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to XML marshall: " + objectAsString, je);
        }
    }

    /**
     * Marshals an {@link Object} to a JSON string.
     * 
     * @param o
     * @return
     */
    public static String toJson(final Object o)
    {
        try
        {
            return PRETTY_JSON_OBJECT_MAPPER.writeValueAsString(o);
        }
        catch (final IOException ioe)
        {
            final String objectAsString = ToStringBuilder.reflectionToString(o,
                ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to JSON marshall: " + objectAsString, ioe);
        }
    }

    /**
     * Marshals an {@link Object} to a pretty-printed JSON file.
     * 
     * @param o
     * @throws IOException
     */
    public static void toPrettyJsonFile(final Object o, final File f) throws IOException
    {
        try
        {
            PRETTY_JSON_OBJECT_MAPPER.writeValue(f, o);
        }
        catch (final JsonProcessingException jpe)
        {
            final String objectAsString = ToStringBuilder.reflectionToString(o,
                ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to JSON marshall: " + objectAsString, jpe);
        }
    }

    /**
     * Unmarshalls a JSON string to a desired type.
     * 
     * @param s
     * @return
     */
    public static <T> T fromJson(final String s, final Class<T> clazz)
    {
        try
        {
            return JSON_OBJECT_MAPPER.readValue(s, clazz);
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException("Failed to JSON unmarshall: " + s, ioe);
        }
    }

    /**
     * Perform a simple arithmetic operation on R to ensure it responds correctly.
     * 
     * @param rServi
     * @return
     */
    public static boolean isRResponding(final RServi rServi)
    {
        try
        {
            final FunctionCall functionCall = rServi.createFunctionCall("sum");
            functionCall.addInt(1);
            functionCall.addInt(2);
            final RObject result = functionCall.evalData(null);
            return result.getData().getInt(0) == 3;
        }
        catch (final StatusException ce)
        {
            return false;
        }
    }

    /**
     * Creates a new {@link URI} from String, throwing an {@link IllegalArgumentException} in case
     * of issue.
     * 
     * @param uri
     * @return
     */
    public static URI newURI(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (final URISyntaxException urise)
        {
            throw new IllegalArgumentException(uri + " is not a valid URI", urise);
        }
    }
	
	public static String normalizeJobMetaKey(final String key) {
		final String knownKey= Constants.WELL_KNOWN_CONFIGURATION_KEYS.get(key.toLowerCase(Locale.ROOT));
		return (knownKey != null) ? knownKey : key;
	}
	
	/**
	 * Rename known meta properties to their canonical keys.
	 * 
	 * @param meta
	 * @return
	 */
	public static Map<String, Serializable> normalizeJobMeta(final Map<String, Serializable> meta) {
		final Map<String, Serializable> normalized= new HashMap<>(meta.size());
		for (final Entry<String, Serializable> entry : meta.entrySet()) {
			normalized.put(normalizeJobMetaKey(entry.getKey()), entry.getValue());
		}
		return normalized;
	}
	
	
    /**
     * Safely decodes a {@link String} into an {@link UUID} instance.
     * 
     * @param uuid a {@link String} the potentially contains a UUID. Can be null.
     * @return the decoded UUID or null if the format is invalid.
     */
    public static UUID safeUuidFromString(final String uuid)
    {
        if (StringUtils.isBlank(uuid))
        {
            return null;
        }

        try
        {
            return UUID.fromString(uuid);
        }
        catch (final RuntimeException re)
        {
            return null;
        }
    }

    /**
     * Null-safe replacement of non-word characters (ie matching <code>\W</code>).
     * 
     * @param source
     * @param replacement
     * @return
     */
    public static String replaceNonWordChars(final String source, final String replacement)
    {
        if (StringUtils.isEmpty(source))
        {
            return source;
        }
        return source.replaceAll("\\W", replacement);
    }
}

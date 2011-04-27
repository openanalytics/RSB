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
import java.util.List;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;
import eu.openanalytics.rsb.message.AbstractWorkItem;
import eu.openanalytics.rsb.rest.types.ErrorResult;
import eu.openanalytics.rsb.rest.types.ObjectFactory;

/**
 * Shared utilities.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class Util {
    private static final class WorkItemMessagePostProcessor implements MessagePostProcessor {
        private final AbstractWorkItem workItem;

        private WorkItemMessagePostProcessor(final AbstractWorkItem workItem) {
            this.workItem = workItem;
        }

        public Message postProcessMessage(final Message message) throws JMSException {
            message.setStringProperty(Constants.SOURCE_JMS_HEADER, workItem.getSource().toString());
            message.setStringProperty(Constants.APPLICATION_NAME_JMS_HEADER, workItem.getApplicationName());
            message.setStringProperty(Constants.JOB_ID_JMS_HEADER, workItem.getJobId().toString());
            return message;
        }
    }

    private final static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static Pattern APPLICATION_NAME_VALIDATOR = Pattern.compile("\\w+");
    private final static JAXBContext ERROR_RESULT_JAXB_CONTEXT;
    private final static DatatypeFactory XML_DATATYPE_FACTORY;

    static {
        try {
            ERROR_RESULT_JAXB_CONTEXT = JAXBContext.newInstance(ErrorResult.class);
            XML_DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final static ObjectFactory REST_OBJECT_FACTORY = new ObjectFactory();
    public final static eu.openanalytics.rsb.soap.types.ObjectFactory SOAP_OBJECT_FACTORY = new eu.openanalytics.rsb.soap.types.ObjectFactory();

    private Util() {
        throw new UnsupportedOperationException("do not instantiate");
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
     * Validates that the passed string is a valid application name.
     * 
     * @param name
     * @return
     */
    public static boolean isValidApplicationName(final String name) {
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
    public static UriBuilder getUriBuilder(final UriInfo uriInfo, final HttpHeaders httpHeaders) throws URISyntaxException {
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        final String protocol = Util.getSingleHeader(httpHeaders, Constants.FORWARDED_PROTOCOL_HEADER);
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
    public static XMLGregorianCalendar convert(final GregorianCalendar calendar) {
        return XML_DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
    }

    /**
     * Gets the first header of multiple HTTP headers, returning null if no header is found for the
     * name.
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
     * Builds an {@link ErrorResult} for a job whose processing has failed.
     * 
     * @param job
     * @param error
     * @return
     */
    public static ErrorResult buildJobProcessingErrorResult(final AbstractJob job, final Throwable error) {
        final ErrorResult errorResult = REST_OBJECT_FACTORY.createErrorResult();
        errorResult.setApplicationName(job.getApplicationName());
        errorResult.setJobId(job.getJobId().toString());
        errorResult.setSubmissionTime(Util.convert(job.getSubmissionTime()));
        errorResult.setErrorMessage(error.getMessage());
        return errorResult;
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
            return JSON_OBJECT_MAPPER.writeValueAsString(o);
        } catch (final IOException ioe) {
            final String objectAsString = ToStringBuilder.reflectionToString(o, ToStringStyle.SHORT_PREFIX_STYLE);
            throw new RuntimeException("Failed to JSON marshall: " + objectAsString, ioe);
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

    /**
     * Dispatches an {@link AbstractJob} over JMS.
     * 
     * @param job
     * @param jmsTemplate
     */
    public static void dispatch(final AbstractJob job, final JmsTemplate jmsTemplate) {
        jmsTemplate.convertAndSend(getQueueName(job), job, new WorkItemMessagePostProcessor(job));
    }

    /**
     * Dispatches an {@link AbstractResult} over JMS.
     * 
     * @param result
     * @param jmsTemplate
     */
    public static void dispatch(final AbstractResult<?> result, final JmsTemplate jmsTemplate) {
        jmsTemplate.convertAndSend(getQueueName(result), result, new WorkItemMessagePostProcessor(result));
    }

    private static String getQueueName(final AbstractJob job) {
        return "r.jobs." + job.getApplicationName();
    }

    private static String getQueueName(final AbstractResult<?> result) {
        return "r.results." + result.getApplicationName();
    }
}

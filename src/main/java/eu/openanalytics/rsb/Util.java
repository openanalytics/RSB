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

import java.io.IOException;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
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
            message.setStringProperty(Constants.APPLICATION_NAME_JMS_HEADER, workItem.getApplicationName());
            message.setStringProperty(Constants.JOB_ID_JMS_HEADER, workItem.getJobId().toString());
            return message;
        }
    }

    private final static class BadRequestStatus implements StatusType {
        private final String reasonPhrase;

        private BadRequestStatus(final String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public int getStatusCode() {
            return Status.BAD_REQUEST.getStatusCode();
        }

        public Family getFamily() {
            return Status.BAD_REQUEST.getFamily();
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }
    };

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private final static Pattern APPLICATION_NAME_VALIDATOR = Pattern.compile("\\w+");

    private final static JAXBContext ERROR_RESULT_JAXB_CONTEXT;

    public final static ObjectFactory REST_OBJECT_FACTORY = new ObjectFactory();

    static {
        try {
            ERROR_RESULT_JAXB_CONTEXT = JAXBContext.newInstance(ErrorResult.class);
        } catch (final JAXBException je) {
            throw new IllegalStateException(je);
        }
    }

    private Util() {
        throw new UnsupportedOperationException("do not instantiate");
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
     * Throws an exception that will yield a 400 HTTP error.
     * 
     * @param badRequestMessage
     * @return
     * @throws WebApplicationException
     */
    public static void throwCustomBadRequestException(final String badRequestMessage) throws WebApplicationException {
        throw new WebApplicationException(Response.status(new BadRequestStatus("Bad request - " + badRequestMessage)).build());
    }

    /**
     * Converts a {@link GregorianCalendar} into a {@link XMLGregorianCalendar}
     * 
     * @param calendar
     * @return
     */
    public static XMLGregorianCalendar convert(final GregorianCalendar calendar) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (final DatatypeConfigurationException dce) {
            throw new IllegalStateException(dce);
        }
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
     * Marshals an {@link Object} to JSON.
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
    public static void dispatch(final AbstractResult result, final JmsTemplate jmsTemplate) {
        jmsTemplate.convertAndSend(getQueueName(result), result, new WorkItemMessagePostProcessor(result));
    }

    private static String getQueueName(final AbstractJob job) {
        return "r.jobs." + job.getApplicationName();
    }

    private static String getQueueName(final AbstractResult result) {
        return "r.results." + result.getApplicationName();
    }
}

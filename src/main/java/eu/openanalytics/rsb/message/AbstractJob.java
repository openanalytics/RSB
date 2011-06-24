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
package eu.openanalytics.rsb.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.MessageSource;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.ErrorResult;

/**
 * Represents a generic RSB job.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class AbstractJob extends AbstractWorkItem {
    private static final long serialVersionUID = 1L;

    public AbstractJob(final Source source, final String applicationName, final UUID jobId, final GregorianCalendar submissionTime,
            final Map<String, Serializable> meta) {
        super(source, applicationName, jobId, submissionTime, meta);
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    public abstract AbstractResult<?> buildErrorResult(Throwable t, MessageSource messageSource) throws IOException;

    /**
     * Builds an {@link ErrorResult} for a job whose processing has failed.
     * 
     * @param job
     * @param error
     * @return
     */
    public static ErrorResult buildJobProcessingErrorResult(final AbstractJob job, final Throwable error) {
        final ErrorResult errorResult = Util.REST_OBJECT_FACTORY.createErrorResult();
        errorResult.setApplicationName(job.getApplicationName());
        errorResult.setJobId(job.getJobId().toString());
        errorResult.setSubmissionTime(Util.convertToXmlDate(job.getSubmissionTime()));
        errorResult.setErrorMessage(error.getMessage());
        return errorResult;
    }
}

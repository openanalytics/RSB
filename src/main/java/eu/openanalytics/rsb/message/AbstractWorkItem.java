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

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Parent of all the work item messages.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractWorkItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean destroyed;
    private final String applicationName;
    private final UUID jobId;
    private final GregorianCalendar submissionTime;

    public AbstractWorkItem(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime) {
        this.destroyed = false;
        this.applicationName = applicationName;
        this.jobId = jobId;
        this.submissionTime = submissionTime;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        releaseResources();
        destroyed = true;
    }

    protected abstract void releaseResources();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public GregorianCalendar getSubmissionTime() {
        return submissionTime;
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

package eu.openanalytics.rsb.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.MimeType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Defines what a result store should persist for job result.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class PersistedResult
{
    private final String applicationName;
    private final String userName;
    private final UUID jobId;
    private final GregorianCalendar resultTime;
    private final boolean success;
    private final MimeType mimeType;

    public PersistedResult(final String applicationName,
                           final String userName,
                           final UUID jobId,
                           final GregorianCalendar resultTime,
                           final boolean success,
                           final MimeType mimeType)
    {
        this.applicationName = applicationName;
        this.userName = userName;
        this.jobId = jobId;
        this.resultTime = resultTime;
        this.success = success;
        this.mimeType = mimeType;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public String getUserName()
    {
        return userName;
    }

    public UUID getJobId()
    {
        return jobId;
    }

    public GregorianCalendar getResultTime()
    {
        return resultTime;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public MimeType getMimeType()
    {
        return mimeType;
    }

    public abstract InputStream getData() throws IOException;

    public abstract long getDataLength() throws IOException;
}

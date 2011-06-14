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

/**
 * Represents the result of a {@link AbstractJob}.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class AbstractResult<T> extends AbstractWorkItem {
    private static final long serialVersionUID = 1L;

    private final boolean success;

    public AbstractResult(final Source source, final String applicationName, final UUID jobId, final GregorianCalendar submissionTime,
            final Map<String, Serializable> meta, final boolean success) {
        super(source, applicationName, jobId, submissionTime, meta);
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public abstract T getPayload() throws IOException;
}

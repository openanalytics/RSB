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

import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.MimeType;

/**
 * Represents the result of a {@link AbstractFunctionCallJob}.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractFunctionCallResult extends AbstractResult<String> {
    private static final long serialVersionUID = 1L;

    private final String result;

    public AbstractFunctionCallResult(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime,
            final boolean success, final String result) {
        super(applicationName, jobId, submissionTime, success);
        this.result = result;
    }

    public abstract MimeType getMimeType();

    @Override
    protected void releaseResources() {
        // NOOP
    }

    @Override
    public String getPayload() {
        return result;
    }
}

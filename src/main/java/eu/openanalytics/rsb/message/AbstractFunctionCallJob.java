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

import java.util.Calendar;
import java.util.Map;

/**
 * Represents a RSB job that consists in calling a 1-arity function on R.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractFunctionCallJob<R extends AbstractFunctionCallResult> extends AbstractJob {
    private static final long serialVersionUID = 1L;

    private String argument;

    public AbstractFunctionCallJob(final String applicationName, final String jobId, final Calendar submissionTime, final String argument,
            final Map<String, String> meta) {
        super(applicationName, jobId, submissionTime, meta);
        this.argument = argument;
    }

    @Override
    protected void releaseResources() {
        // NOOP
    }

    public abstract String getFunctionName();

    public abstract R buildResult(boolean success, String result);

    public String getArgument() {
        return argument;
    }

    public void setArgument(final String argument) {
        this.argument = argument;
    }
}

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
 * Represents a RSB job that consists in calling the RSBJsonService function on R.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class JsonFunctionCallJob extends AbstractFunctionCallJob<JsonFunctionCallResult> {
    private static final long serialVersionUID = 1L;

    public JsonFunctionCallJob(final String applicationName, final String jobId, final Calendar submissionTime, final String argument,
            final Map<String, String> meta) {
        super(applicationName, jobId, submissionTime, argument, meta);
    }

    @Override
    public Type getType() {
        return Type.JSON_FUNCTION_CALL;
    }

    @Override
    public String getFunctionName() {
        return "RSBJsonService";
    }

    @Override
    public JsonFunctionCallResult buildResult(final boolean success, final String result) {
        return new JsonFunctionCallResult(getApplicationName(), getJobId(), getSubmissionTime(), success, result);
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
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
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Represents a RSB job that consists in calling a unary function on R.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class AbstractFunctionCallJob extends AbstractJob
{
    private static final long serialVersionUID = 1L;

    private final String argument;

    @SuppressWarnings("unchecked")
    public AbstractFunctionCallJob(final Source source,
                                   final String applicationName,
                                   final String userName,
                                   final UUID jobId,
                                   final GregorianCalendar submissionTime,
                                   final String argument)
    {
        // function call jobs and results have no meta
        super(source, applicationName, userName, jobId, submissionTime, Collections.EMPTY_MAP);
        this.argument = argument;
    }

    public abstract AbstractResult<String> buildSuccessResult(String result) throws IOException;

    @Override
    protected void releaseResources()
    {
        // NOOP
    }

    public abstract String getFunctionName();

    public String getArgument()
    {
        return argument;
    }
}

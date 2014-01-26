/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2014
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

package eu.openanalytics.rsb.stats;

import java.util.Calendar;
import java.util.Map;

import eu.openanalytics.rsb.message.Job;

/**
 * Defines a handler for RSB job statistics.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public interface JobStatisticsHandler
{
    void setConfiguration(Map<String, Object> configuration);

    void initialize();

    void destroy();

    void storeJobStatistics(Job job,
                            Calendar jobCompletionTime,
                            long millisecondsSpentProcessing,
                            String rServiAddress);
}

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

package eu.openanalytics.rsb.stats;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class NoopJobStatisticsHandler implements JobStatisticsHandler {
    public final static NoopJobStatisticsHandler INSTANCE = new NoopJobStatisticsHandler();

    private NoopJobStatisticsHandler() {
        // use singleton
    }

    public void storeJobStatistics(final String applicationName, final UUID jobId, final Calendar jobCompletionTime,
            final long millisecondsSpentProcessing, final String rServiAddress) {
        // NOOP
    }

    public void setConfiguration(final Map<String, Object> configuration) {
        // NOOP
    }

    public void initialize() {
        // NOOP
    }

    public void destroy() {
        // NOOP
    }
}

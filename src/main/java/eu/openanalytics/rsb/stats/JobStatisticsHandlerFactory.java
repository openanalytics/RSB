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

import java.util.Map;

import org.springframework.beans.BeanUtils;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public abstract class JobStatisticsHandlerFactory {

    private JobStatisticsHandlerFactory() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    public static JobStatisticsHandler create(final Class<? extends JobStatisticsHandler> clazz, final Map<String, Object> configuration) {
        final JobStatisticsHandler jobStatisticsHandler = BeanUtils.instantiate(clazz);
        jobStatisticsHandler.setConfiguration(configuration);
        return jobStatisticsHandler;
    }
}

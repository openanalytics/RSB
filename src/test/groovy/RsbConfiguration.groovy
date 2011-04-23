import eu.openanalytics.rsb.stats.JobStatisticsHandler;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

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
 
/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
class RsbConfiguration extends eu.openanalytics.rsb.config.DefaultConfiguration {
    
    // Demonstrates how to send statistics to Redis
//    eu.openanalytics.rsb.stats.JobStatisticsHandler getJobStatisticsHandler() {
//        new eu.openanalytics.rsb.stats.RedisJobStatisticsHandler("localhost", 6379)
//    }
}

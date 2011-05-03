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
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
class RsbConfiguration extends eu.openanalytics.rsb.config.DefaultConfiguration {

    // Custom directories configuration example
    //File resultsDirectory = new File('<path to results dir>')
    //File activeMqWorkDirectory = new File('<path to ActiveMQ work dir>')
    //File rScriptsCatalogDirectory = new File('<path to RSB catalog dir>')
    //File sweaveFilesCatalogDirectory = new File('<path to Sweave File catalog dir>')
    //File emailRepliesCatalogDirectory = new File('<path to email replies catalog dir>')

    // Demonstrates how to send statistics to Redis
    //eu.openanalytics.rsb.stats.JobStatisticsHandler jobStatisticsHandler = new eu.openanalytics.rsb.stats.RedisJobStatisticsHandler('localhost', 6379)
}

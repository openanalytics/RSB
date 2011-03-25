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
package eu.openanalytics.rsb.config;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * @author rsb.development@openanalytics.eu
 */
public interface Configuration {

    File getRsbResultsDirectory();

    File getActiveMqWorkDirectory();

    URI getDefaultRserviPoolUri();

    Map<String, URI> getApplicationSpecificRserviPoolUris();

    /**
     * @return time-out in milliseconds.
     */
    int getJobTimeOut();
}

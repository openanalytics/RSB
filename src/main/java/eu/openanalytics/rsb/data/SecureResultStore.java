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

package eu.openanalytics.rsb.data;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * Defines a username-aware result store.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public interface SecureResultStore extends ResultStore
{
    boolean deleteByApplicationNameAndJobId(String applicationName, String userName, UUID jobId)
        throws IOException;

    /**
     * @return an empty collection if no result was found.
     */
    Collection<PersistedResult> findByApplicationName(String applicationName, String userName);

    /**
     * @return null if no result was found.
     */
    PersistedResult findByApplicationNameAndJobId(String applicationName, String userName, UUID jobId);
}

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

package eu.openanalytics.rsb.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import eu.openanalytics.rsb.config.Configuration.CatalogSection;

/**
 * The RSB catalog. The <code>applicationName</code> argument are mandatory if the catalog runs in
 * application-aware mode.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public interface CatalogManager
{
    Map<Pair<CatalogSection, File>, List<File>> getCatalog(String applicationName);

    File getCatalogFile(CatalogSection catalogSection, String applicationName, String fileName);

    /**
     * This must only be called when it's impossible to have a security context (ie after going
     * through JMS)
     */
    File internalGetCatalogFile(CatalogSection catalogSection, String applicationName, String fileName);

    enum PutCatalogFileResult
    {
        CREATED, UPDATED
    };

    Pair<PutCatalogFileResult, File> putCatalogFile(CatalogSection catalogSection,
                                                    String applicationName,
                                                    String fileName,
                                                    InputStream in) throws IOException;
}

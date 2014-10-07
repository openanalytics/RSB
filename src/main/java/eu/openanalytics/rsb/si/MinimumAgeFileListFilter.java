/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2014
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
package eu.openanalytics.rsb.si;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.integration.file.filters.AbstractFileListFilter;

/**
 * Spring Integration file filter that selects only files of a certain age, preventing picking files
 * that are being written.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class MinimumAgeFileListFilter extends AbstractFileListFilter<File> {
    private int minimumAge; // in milliseconds

    public void setMinimumAge(final int minimumAge) {
        this.minimumAge = minimumAge;
    }

    @Override
    protected boolean accept(final File file) {
        return FileUtils.isFileOlder(file, System.currentTimeMillis() - minimumAge);
    }
}

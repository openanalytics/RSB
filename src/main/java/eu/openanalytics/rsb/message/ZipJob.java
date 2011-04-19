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
package eu.openanalytics.rsb.message;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a RSB job that consists of an Zip archive of multiple files.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class ZipJob extends MultiFilesJob {
    private static final long serialVersionUID = 1L;

    public ZipJob(final String applicationName, final UUID jobId, final GregorianCalendar submissionTime, final Map<String, String> meta,
            final File filesDirectory) throws IOException {
        super(applicationName, jobId, submissionTime, meta, filesDirectory);
    }
}

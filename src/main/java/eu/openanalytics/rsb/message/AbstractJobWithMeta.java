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

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a RSB job that has meta information attached to it.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractJobWithMeta extends AbstractJob {
    private static final long serialVersionUID = 1L;

    private Map<String, String> meta;

    public AbstractJobWithMeta(final String applicationName, final UUID jobId, final Calendar submissionTime, final Map<String, String> meta) {
        super(applicationName, jobId, submissionTime);
        this.meta = meta;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(final Map<String, String> meta) {
        this.meta = meta;
    }
}

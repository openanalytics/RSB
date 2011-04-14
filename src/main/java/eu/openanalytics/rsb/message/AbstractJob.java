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
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Represents a generic RSB job.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class AbstractJob extends AbstractJobContext {
    public enum Type {
        JSON_FUNCTION_CALL {
            @Override
            public JsonFunctionCallJob build(final String applicationName, final String jobId, final String argument,
                    final Map<String, String> meta) {
                return new JsonFunctionCallJob(applicationName, jobId, GregorianCalendar.getInstance(), argument, meta);
            }
        },
        XML_FUNCTION_CALL {
            @Override
            public XmlFunctionCallJob build(final String applicationName, final String jobId, final String argument,
                    final Map<String, String> meta) {
                return new XmlFunctionCallJob(applicationName, jobId, GregorianCalendar.getInstance(), argument, meta);
            }
        };

        public abstract AbstractJob build(final String applicationName, final String jobId, final String argument,
                final Map<String, String> meta);
    }

    private static final long serialVersionUID = 1L;

    private Map<String, String> meta;

    public AbstractJob(final String applicationName, final String jobId, final Calendar submissionTime, final Map<String, String> meta) {
        super(applicationName, jobId, submissionTime);
        this.meta = meta;
    }

    public abstract Type getType();

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(final Map<String, String> meta) {
        this.meta = meta;
    }
}

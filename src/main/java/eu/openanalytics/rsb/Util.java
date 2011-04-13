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
package eu.openanalytics.rsb;

import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.StringUtils;

/**
 * Shared utilities.
 * 
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public abstract class Util {
    private final static Pattern APPLICATION_NAME_VALIDATOR = Pattern.compile("\\w+");

    private Util() {
        throw new UnsupportedOperationException("do not instantiate");
    }

    public static boolean isValidApplicationName(final String name) {
        return StringUtils.isNotBlank(name) && APPLICATION_NAME_VALIDATOR.matcher(name).matches();
    }

    public static String getSingleHeader(final HttpHeaders httpHeaders, final String headerName) {
        final List<String> headers = httpHeaders.getRequestHeader(headerName);

        if ((headers == null) || (headers.isEmpty())) {
            return null;
        }

        return headers.get(0);
    }
}

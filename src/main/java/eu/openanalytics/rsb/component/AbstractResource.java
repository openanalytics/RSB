/*
 *   R Service Bus
 *
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

package eu.openanalytics.rsb.component;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import eu.openanalytics.rsb.security.ApplicationPermissionEvaluator;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public abstract class AbstractResource extends AbstractComponent
{
    @Context
    private SecurityContext securityContext;

    protected String getUserName()
    {
        return securityContext != null && securityContext.getUserPrincipal() != null
                                                                                    ? securityContext.getUserPrincipal()
                                                                                        .getName()
                                                                                    : ApplicationPermissionEvaluator.NO_AUTHENTICATED_USERNAME;
    }

    // exposed for unit testing
    void setSecurityContext(final SecurityContext securityContext)
    {
        this.securityContext = securityContext;
    }
}

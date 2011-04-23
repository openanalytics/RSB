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
package eu.openanalytics.rsb.component;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import eu.openanalytics.rsb.message.AbstractJob;
import eu.openanalytics.rsb.message.AbstractResult;

/**
 * @author "Open Analytics <rsb.development@openanalytics.eu>"
 */
public class DlqHandlerTestCase {

    private DlqHandler dlqHandler;

    @Before
    public void prepareTest() {
        dlqHandler = new DlqHandler();
    }

    @Test
    public void handleAbstractJob() {
        final AbstractJob job = mock(AbstractJob.class);
        dlqHandler.handle(job);
    }

    @Test
    public void handleAbstractResult() {
        final AbstractResult<?> result = mock(AbstractResult.class);
        dlqHandler.handle(result);
    }
}

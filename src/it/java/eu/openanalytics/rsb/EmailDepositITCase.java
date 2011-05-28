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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
public class EmailDepositITCase {
    private static GreenMail greenMail;

    @BeforeClass
    public static void startEmailServer() {
        final ServerSetup pop3 = new ServerSetup(9110, "localhost", ServerSetup.PROTOCOL_POP3);
        final ServerSetup smtp = new ServerSetup(9025, "localhost", ServerSetup.PROTOCOL_SMTP);
        greenMail = new GreenMail(new ServerSetup[] { pop3, smtp });
        greenMail.setUser("rsb-default@rsb.openalytics.eu", "rsb-default", "test");
        greenMail.start();
    }

    @Test
    public void submissionToDefaultConfiguredEmail() {
        // TODO code me :)
    }

    // TODO test invalid job

    @AfterClass
    public static void stopEmailServer() {
        greenMail.stop();
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2015
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.eviware.soapui.tools.SoapUITestCaseRunner;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class SoapMtomITCase extends AbstractITCase {
    @Test
    public void soapMtomJobsApi() throws Exception {
        final SoapUITestCaseRunner runner = new SoapUITestCaseRunner("RSB SOAP MTOM Integration Tests");
        runner.setSettingsFile("src/it/config/rsb-it-soapui-settings.xml");
        runner.setProjectFile("src/it/config/rsb-it-soapui-project.xml");
        runner.setGlobalProperties(new String[] { "rsb.soap.uri=" + RSB_BASE_URI + "/api/soap/mtom-jobs" });
        assertThat(runner.run(), is(true));
    }
}

/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2022
 * 
 * ===========================================================================
 * 
 * This file is part of R Service Bus.
 * 
 * R Service Bus is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 * 
 * You should have received a copy of the Apache License
 * along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 */

package eu.openanalytics.rsb;

import static org.junit.Assert.assertTrue;

import com.eviware.soapui.tools.SoapUITestCaseRunner;
import org.junit.Test;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class SoapMtomITCase extends AbstractITCase {
	
	
	@Test
	public void soapMtomJobsApi() throws Exception {
		final SoapUITestCaseRunner runner= new SoapUITestCaseRunner("RSB SOAP MTOM Integration Tests");
		runner.setSettingsFile("src/it/config/rsb-it-soapui-settings.xml");
		runner.setProjectFile("src/it/config/rsb-it-soapui-project.xml");
		runner.setGlobalProperties(new String[] { "rsb.soap.uri=" + RSB_BASE_URI + "/api/soap/mtom-jobs" });
		assertTrue(runner.run());
	}
	
}

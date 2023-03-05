/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2023
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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;

import eu.openanalytics.rsb.config.Configuration;
import eu.openanalytics.rsb.test.TestUtils;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public abstract class AbstractITCase {
	
	protected static final String TEST_APPLICATION_NAME_PREFIX= "rsb_it_";
	
	protected final static String RSB_BASE_URI= "http://localhost:8888/rsb";
	
	
    @Before
    public void setupXmlNameSpaces() {
        final Map<String, String> m = new HashMap<>();
        m.put("rsb", "http://rest.rsb.openanalytics.eu/types");

        final NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    protected Configuration getConfiguration() {
        return SuiteITCase.configuration;
    }

    protected Properties getRawMessages() {
        return SuiteITCase.rawMessages;
    }
	
	
	public static Path getTestDataFile(final String payloadResourceFile) {
		return TestUtils.getTestDataFile(payloadResourceFile);
	}
	
	public static InputStream getTestDataStream(final String payloadResourceFile) {
		return TestUtils.getTestDataStream(payloadResourceFile);
	}
	
	
	public static void validateZipResult(final InputStream responseStream) throws IOException {
		try (final ZipInputStream result= new ZipInputStream(responseStream)) {
			ZipEntry ze= null;
			
			while ((ze= result.getNextEntry()) != null) {
				if (ze.getName().endsWith(".pdf")) {
					return;
				}
			}
		}
		
		fail("No PDF file found in Zip result");
	}
	
	public static void validateErrorResult(final InputStream responseStream) throws IOException {
		final String response= new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
		
		assertTrue(response + " should contain 'error'",
				StringUtils.containsIgnoreCase(response, "error") );
	}
	
	
    protected String newTestApplicationName() {
        return TEST_APPLICATION_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(20);
    }
}

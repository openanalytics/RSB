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

package eu.openanalytics.rsb.si;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class MinimumAgeFileListFilterTestCase {
	
	
	private File testFile;
	
	private MinimumAgeFileListFilter filter;
	
	
	public MinimumAgeFileListFilterTestCase() {
	}
	
	
	@Before
	public void prepareTest() throws IOException {
		this.filter= new MinimumAgeFileListFilter();
		this.filter.setMinimumAge(10000);
		
		this.testFile= File.createTempFile("MinimumAgeFileListFilterTestCase", null);
	}
	
	@Test
	public void acceptFile() {
		this.testFile.setLastModified(0);
		assertThat(this.filter.accept(this.testFile), is(true));
	}
	
	@Test
	public void rejectFile() {
		this.testFile.setLastModified(System.currentTimeMillis());
		assertThat(this.filter.accept(this.testFile), is(false));
	}
	
}

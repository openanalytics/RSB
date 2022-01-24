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

package eu.openanalytics.rsb.test;

import static org.eclipse.statet.jcommons.lang.ObjectUtils.nonNullAssert;

import java.io.IOException;
import java.io.InputStream;
import java.lang.StackWalker.StackFrame;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;


@NonNullByDefault
public class TestUtils {
	
	
	private static final Path TEMP_DIRECTORY;
	private static final int RSB_PREFIX_LENGTH= "eu.openanalytics.rsb.".length();
	static {
		try {
			TEMP_DIRECTORY= Files.createTempDirectory("rsb-test-");
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Path getTempDirectory() {
		return TEMP_DIRECTORY;
	}
	
	public static Path createTestDirectory() throws IOException {
		final StackFrame stackFrame= StackWalker.getInstance().walk((stream) -> stream.skip(1)
				.findFirst().get());
		final String name= stackFrame.getClassName().substring(RSB_PREFIX_LENGTH)
				+ '.' + stackFrame.getMethodName();
		final Path directory= TEMP_DIRECTORY.resolve(name);
		
		Files.createDirectories(directory);
		
		return directory;
	}
	
	
	public static Path getTestDataFile(final String fileName) {
		try {
			final var loader= nonNullAssert(Thread.currentThread().getContextClassLoader());
			return Path.of(loader.getResource("data/" + fileName).toURI());
		}
		catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static InputStream getTestDataStream(final String fileName) {
		final var loader= nonNullAssert(Thread.currentThread().getContextClassLoader());
		return nonNullAssert(loader.getResourceAsStream("data/" + fileName));
	}
	
	
	private TestUtils() {
	}
	
}

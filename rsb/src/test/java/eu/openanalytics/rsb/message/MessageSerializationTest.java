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

package eu.openanalytics.rsb.message;

import static org.eclipse.statet.jcommons.lang.ObjectUtils.nonNullAssert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.statet.jcommons.io.FileUtils;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.junit.Test;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.AbstractWorkItem.Source;


@NonNullByDefault
public class MessageSerializationTest {
	
	
	private final Path testDirectory;
	private final Path testDataDirectory;
	
	private final Path tempDirectory;
	
	
	public MessageSerializationTest() throws IOException, URISyntaxException {
		this.testDataDirectory= nonNullAssert(Path.of(
						nonNullAssert(Thread.currentThread().getContextClassLoader())
								.getResource("data/test.R").toURI() )
				.getParent() );
		this.testDirectory= this.testDataDirectory.resolveSibling("sertest");
		Files.createDirectories(this.testDirectory);
		this.tempDirectory= Path.of("/tmp/rsb.sertest");
		Files.createDirectories(this.tempDirectory);
	}
	
	
	@Test
	public void readMultiFilesJob_Current() throws Exception {
		writeObj(createMultiFilesJob());
		
		final MultiFilesJob obj= readObj(MultiFilesJob.class, "Current", 1);
		assertNotNull(obj);
	}
	
	@Test
	public void readMultiFilesJob_6_4() throws Exception {
		final MultiFilesJob obj= readObj(MultiFilesJob.class, "6.4", 1);
		assertNotNull(obj);
	}
	
	@Test
	public void readMultiFilesResult_Current() throws Exception {
		writeObj(createMultiFilesResult(createMultiFilesJob()));
		
		final MultiFilesResult obj= readObj(MultiFilesResult.class, "Current", 1);
		assertNotNull(obj);
	}
	
	@Test
	public void readMultiFilesResult_6_4() throws Exception {
		final MultiFilesResult obj= readObj(MultiFilesResult.class, "6.4", 1);
		assertNotNull(obj);
	}
	
	
	public static void main(final String[] args) throws Exception {
		final MessageSerializationTest test= new MessageSerializationTest();
		System.setProperty("java.io.tmpdir", test.tempDirectory.toString());
		
		final MultiFilesJob multiFilesJob= test.createMultiFilesJob();
		test.writeObj(multiFilesJob);
		test.writeObj(test.createMultiFilesResult(multiFilesJob));
	}
	
	
	private void writeObj(final Object obj, final String name) throws IOException {
		final Path file= this.testDirectory.resolve(name);
		try (	final OutputStream fileOut= Files.newOutputStream(file);
				final ObjectOutput out= new ObjectOutputStream(fileOut) ) {
			out.writeObject(obj);
		}
		System.out.println("-> " + file.toString());
	}
	
	private void writeObj(final Object obj) throws Exception {
		final Class<?> objClass= obj.getClass();
		final Field serVersionField= objClass.getDeclaredField("serialVersionUID");
		serVersionField.setAccessible(true);
		writeObj(obj, createFileName(objClass, "Current", serVersionField.getLong(null)));
	}
	
	private @Nullable Object readObj(final String name)
			throws IOException, ClassNotFoundException {
		final Path file= this.testDirectory.resolve(name);
		try (	final InputStream fileIn= Files.newInputStream(file);
				final ObjectInput out= new ObjectInputStream(fileIn) ) {
			final Object obj= out.readObject();
			
			assertEquals(-1, out.read());
			
			return obj;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> @Nullable T readObj(final Class<T> objClass, final String rsbVersion, final long serVersion)
			throws IOException, ClassNotFoundException {
		return (@Nullable T)readObj(createFileName(objClass, rsbVersion, serVersion));
	}
	
	private String createFileName(final Class<?> objClass, final String rsbVersion, final long serVersion) {
		return objClass.getSimpleName()
				+ '-' + rsbVersion
				+ '-' + serVersion
				+ ".ser";
	}
	
	
	private MultiFilesJob createMultiFilesJob() throws IOException, IllegalJobDataException {
		final GregorianCalendar submisstionTime= (GregorianCalendar)GregorianCalendar.getInstance();
		final Map<String, Serializable> meta= new HashMap<>();
		final MultiFilesJob job= new MultiFilesJob(Source.DIRECTORY, "SerTest",
				"test.user", UUID.randomUUID(), submisstionTime,
				meta );
		
		final Path rScriptFile= this.testDataDirectory.resolve("test.R");
		try (final InputStream data= Files.newInputStream(rScriptFile)) {
			MultiFilesJob.addDataToJob(Util.getContentType(rScriptFile),
					FileUtils.requireFileName(rScriptFile).toString(), data, job);
		}
		
		return job;
	}
	
	private MultiFilesResult createMultiFilesResult(final MultiFilesJob job) throws IOException {
		final MultiFilesResult result= job.buildSuccessResult();
		result.createNewResultFile("result.dat");
		
		return result;
	}
	
}

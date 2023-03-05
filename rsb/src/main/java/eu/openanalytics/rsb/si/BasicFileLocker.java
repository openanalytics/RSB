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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.springframework.integration.file.FileLocker;
import org.springframework.messaging.MessagingException;


@NonNullByDefault
public class BasicFileLocker implements FileLocker {
	
	
	private final ConcurrentMap<Path, FileLock> locks= new ConcurrentHashMap<>();
	
	
	public BasicFileLocker() {
	}
	
	
	@Override
	public boolean isLockable(final File file) {
		return true;
	}
	
	@Override
	public boolean lock(final File file) {
		final var path= file.toPath();
		
		if (this.locks.containsKey(path)) {
			return true;
		}
		final FileLock newLock= tryLock(path);
		if (newLock != null) {
			if (this.locks.putIfAbsent(path, newLock) != null) {
				close(newLock.channel());
			}
			return true;
		}
		return false;
	}
	
	private @Nullable FileLock tryLock(final Path path) {
		FileChannel channel= null;
		try {
			if (!Files.exists(path)) {
				return null;
			}
			channel= FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
			return channel.tryLock();
		}
		catch (final OverlappingFileLockException e) {
			close(channel);
			return null;
		}
		catch (final IOException e) {
			close(channel);
			throw new MessagingException("Failed to lock file: " + path, e);
		}
	}
	
	@Override
	public void unlock(final File file) {
		final var path= file.toPath();
		
		final FileLock fileLock= this.locks.remove(path);
		if (fileLock != null) {
			try {
				fileLock.channel().close();
			}
			catch (final IOException e) {
				throw new MessagingException("Failed to unlock file: " + file, e);
			}
		}
	}
	
	private void close(final @Nullable FileChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			}
			catch (final IOException e) {}
		}
	}
	
}

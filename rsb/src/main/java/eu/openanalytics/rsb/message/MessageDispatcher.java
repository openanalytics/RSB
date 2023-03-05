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

package eu.openanalytics.rsb.message;

import org.eclipse.statet.jcommons.lang.NonNullByDefault;


/**
 * Defines a Job and Result message dispatcher.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public interface MessageDispatcher {
	
	
	/**
	 * Dispatches an {@link AbstractJob}.
	 * 
	 * @param job
	 */
	void dispatch(AbstractJob job);
	
	/**
	 * Dispatches an {@link AbstractResult}.
	 * 
	 * @param result
	 */
	void dispatch(AbstractResult<?> result);
	
	/**
	 * Dispatches an {@link AbstractJob} and waits for an {@link AbstractResult}.
	 * 
	 * @param job
	 * @return
	 */
	<T extends AbstractResult<?>> T process(AbstractJob job);
	
}

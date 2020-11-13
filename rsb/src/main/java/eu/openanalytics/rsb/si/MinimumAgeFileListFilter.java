/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2020
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

import org.apache.commons.io.FileUtils;
import org.springframework.integration.file.filters.AbstractFileListFilter;

/**
 * Spring Integration file filter that selects only files of a certain age, preventing picking files
 * that are being written.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class MinimumAgeFileListFilter extends AbstractFileListFilter<File> {
    private int minimumAge; // in milliseconds

    public void setMinimumAge(final int minimumAge) {
        this.minimumAge = minimumAge;
    }

    @Override
    public boolean accept(final File file) {
        return FileUtils.isFileOlder(file, System.currentTimeMillis() - minimumAge);
    }
}

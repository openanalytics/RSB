/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2019
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 */

package eu.openanalytics.rsb.data;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class exists for backwards compatibility reason: it allows existing
 * implementations of {@link ResultStore} to be used when a {@link SecureResultStore}
 * is now expected.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class SecureResultStoreAdapter implements SecureResultStore
{
    private static final Log LOGGER = LogFactory.getLog(SecureResultStoreAdapter.class);

    private ResultStore resultStore;

    public boolean deleteByApplicationNameAndJobId(final String applicationName, final UUID jobId)
        throws IOException
    {
        return resultStore.deleteByApplicationNameAndJobId(applicationName, jobId);
    }

    public Collection<PersistedResult> findByApplicationName(final String applicationName)
    {
        return resultStore.findByApplicationName(applicationName);
    }

    public PersistedResult findByApplicationNameAndJobId(final String applicationName, final UUID jobId)
    {
        return resultStore.findByApplicationNameAndJobId(applicationName, jobId);
    }

    public void store(final PersistedResult result) throws IOException
    {
        if ((!isAdaptedResultStoreSecure()) && (StringUtils.isNotBlank(result.getUserName())))
        {
            LOGGER.warn("Caution: storing a secure result with an unsecure store");
        }
        resultStore.store(result);
    }

    public boolean deleteByApplicationNameAndJobId(final String applicationName,
                                                   final String userName,
                                                   final UUID jobId) throws IOException
    {
        if ((!isAdaptedResultStoreSecure()) && (StringUtils.isNotBlank(userName)))
        {
            LOGGER.warn("Caution: deleting a secure result from an unsecure store");
        }
        return deleteByApplicationNameAndJobId(applicationName, jobId);
    }

    public Collection<PersistedResult> findByApplicationName(final String applicationName,
                                                             final String userName)
    {
        if ((!isAdaptedResultStoreSecure()) && (StringUtils.isNotBlank(userName)))
        {
            LOGGER.warn("Caution: finding secure results from an unsecure store");
        }
        return findByApplicationName(applicationName);
    }

    public PersistedResult findByApplicationNameAndJobId(final String applicationName,
                                                         final String userName,
                                                         final UUID jobId)
    {
        if ((!isAdaptedResultStoreSecure()) && (StringUtils.isNotBlank(userName)))
        {
            LOGGER.warn("Caution: finding secure result from an unsecure store");
        }
        return findByApplicationNameAndJobId(applicationName, jobId);
    }

    private boolean isAdaptedResultStoreSecure()
    {
        return resultStore instanceof SecureResultStore;
    }

    public ResultStore getResultStore()
    {
        return resultStore;
    }

    public void setResultStore(final ResultStore resultStore)
    {
        this.resultStore = resultStore;
    }
}

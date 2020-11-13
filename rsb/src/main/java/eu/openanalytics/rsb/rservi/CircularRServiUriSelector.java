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

package eu.openanalytics.rsb.rservi;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.config.Configuration;

/**
 * Provides RServi URIs, using a circular buffer when several URIs are configured for
 * one application.
 * 
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component
public class CircularRServiUriSelector implements RServiUriSelector
{
    @Resource
    private Configuration configuration;

    private Map<String, Deque<URI>> circularApplicationUris;

    // exposed for unit testing
    void setConfiguration(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    @PostConstruct
    public void initialize()
    {
        final Map<String, Deque<URI>> newCircularApplicationUris = new HashMap<String, Deque<URI>>();

        final Map<String, Set<URI>> applicationSpecificRserviPoolUris = configuration.getApplicationSpecificRserviPoolUris();
        if ((applicationSpecificRserviPoolUris != null) && (!applicationSpecificRserviPoolUris.isEmpty()))
        {
            for (final Entry<String, Set<URI>> applicationSpecificRserviPoolUri : applicationSpecificRserviPoolUris.entrySet())
            {
                newCircularApplicationUris.put(applicationSpecificRserviPoolUri.getKey(),
                    new ArrayDeque<URI>(applicationSpecificRserviPoolUri.getValue()));
            }
        }

        circularApplicationUris = Collections.unmodifiableMap(newCircularApplicationUris);
    }

    public URI getUriForApplication(final String applicationName)
    {
        if ((circularApplicationUris == null) || (circularApplicationUris.isEmpty()))
        {
            return configuration.getDefaultRserviPoolUri();
        }

        final Deque<URI> applicationRserviPoolUris = circularApplicationUris.get(applicationName);

        final boolean applicationHasNoSpecificUris = applicationRserviPoolUris == null
                                                     || applicationRserviPoolUris.isEmpty();

        return applicationHasNoSpecificUris
                                           ? configuration.getDefaultRserviPoolUri()
                                           : getCircular(applicationRserviPoolUris);
    }

    private URI getCircular(final Deque<URI> applicationRserviPoolUris)
    {
        if (applicationRserviPoolUris.size() == 1)
        {
            return applicationRserviPoolUris.peek();
        }

        synchronized (applicationRserviPoolUris)
        {
            final URI uri = applicationRserviPoolUris.poll();
            applicationRserviPoolUris.add(uri);
            return uri;
        }
    }
}

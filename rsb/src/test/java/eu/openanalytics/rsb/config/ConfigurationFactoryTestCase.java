/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2020
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


package eu.openanalytics.rsb.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ConfigurationFactoryTestCase
{

    private static final String[] TEST_JSON_CONFIGURATIONS = {"rsb-configuration.json",
        "rsb-configuration-minimal.json", "rsb-configuration-redis.json", "rsb-configuration-pooling.json",
        "rsb-configuration-extended.json", "rsb-configuration-full.json"};

    // full config is expected to be invalid because of missing job configuration
    // file, response file and data
    // directories
    private static final boolean[] VALIDATION_RESULTS = {true, true, true, true, true, false};

    @Test
    public void validateTestJsonConfigurations() throws IOException
    {
        int i = 0;
        for (final String configurationFile : TEST_JSON_CONFIGURATIONS)
        {
            final Set<String> validationResult = ConfigurationFactory.validate(ConfigurationFactory.load(Thread.currentThread()
                .getContextClassLoader()
                .getResource(configurationFile)));

            assertThat(configurationFile + " yielded: " + validationResult, validationResult.isEmpty(),
                is(VALIDATION_RESULTS[i++]));
        }
    }
}

/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.openanalytics.rsb.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ConfigurationFactoryTestCase {

    private static final String[] TEST_JSON_CONFIGURATIONS = { "rsb-configuration.json", "rsb-configuration-minimal.json",
            "rsb-configuration-redis.json", "rsb-configuration-pooling.json", "rsb-configuration-full.json" };

    // full config is expected to be invalid because of missing job configuration file, response file and data
    // directories
    private static final boolean[] VALIDATION_RESULTS = { true, true, true, true, false };

    @Test
    public void validateTestJsonConfigurations() throws IOException {
        int i = 0;
        for (final String configurationFile : TEST_JSON_CONFIGURATIONS) {
            final Set<String> validationResult = ConfigurationFactory.validate(ConfigurationFactory.load(configurationFile));
            assertThat(configurationFile, validationResult.isEmpty(), is(VALIDATION_RESULTS[i++]));
        }
    }
}

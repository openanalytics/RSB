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

package eu.openanalytics.rsb.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import eu.openanalytics.rsb.Util;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RedisJobStatisticsHandler implements JobStatisticsHandler {
    private static final Log LOGGER = LogFactory.getLog(RedisJobStatisticsHandler.class);

    private static final SimpleDateFormat MONTH_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String RSB_STATS_KEY_PREFIX = "rsb:stats:";
    private static final String RSB_STATS_APPLICATIONS_SET_KEY = RSB_STATS_KEY_PREFIX + "applications";

    private JedisPool pool;
    private String redisHost;
    private int redisPort;

    private interface RedisAction {
        void run(Jedis jedis);
    }

    public void setConfiguration(final Map<String, Object> configuration) {
        redisHost = (String) configuration.get("host");
        redisPort = (Integer) configuration.get("port");
    }

    public void initialize() {
        final GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;

        pool = new JedisPool(poolConfig, redisHost, redisPort);

        final boolean redisConfigurationOk = runWithJedis(new RedisAction() {
            public void run(final Jedis jedis) {
                final String pingResponse = jedis.ping();

                Validate.isTrue("PONG".equals(pingResponse), "Unexpected response received from pinging Redis: " + pingResponse);

                LOGGER.info(String.format("%s successfully connected to Redis: %s:%d", getClass().getSimpleName(), redisHost, redisPort));
            }
        });

        Validate.isTrue(redisConfigurationOk,
                "Redis can't be contacted: please check parameter 'rsb.jobs.stats.handler' (set it to 'none' to disable statistics altogether)");
    }

    public void destroy() {
        pool.destroy();
    }

    public void storeJobStatistics(final String applicationName, final UUID jobId, final Calendar jobCompletionTime,
            final long millisecondsSpentProcessing, final String rServiAddress) {

        runWithJedis(new RedisAction() {
            public void run(final Jedis jedis) {
                // ensure application is registered as a statistics producer
                jedis.sadd(RSB_STATS_APPLICATIONS_SET_KEY, applicationName);

                // add monthstamp to application's set of monthstamps
                jobCompletionTime.setTimeZone(UTC);
                final String monthStamp = MONTH_STAMP_FORMAT.format(jobCompletionTime.getTime());
                jedis.sadd(RSB_STATS_KEY_PREFIX + applicationName + ":monthstamps", monthStamp);

                // create persisted statistics JSON structure and store it in monthstamp list
                final Map<String, Object> statsMap = new HashMap<String, Object>(5);
                statsMap.put("application_name", applicationName);
                statsMap.put("job_id", jobId);
                statsMap.put("utc_timestamp", jobCompletionTime.getTimeInMillis());
                statsMap.put("time_spent", millisecondsSpentProcessing);
                statsMap.put("r_servi_address", rServiAddress);
                final String statsJson = Util.toJson(statsMap);
                jedis.lpush(RSB_STATS_KEY_PREFIX + applicationName + ":" + monthStamp, statsJson);
            }
        });
    }

    private boolean runWithJedis(final RedisAction action) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();
            action.run(jedis);
            return true;
        } catch (final Throwable t) {
            LOGGER.warn("Failed to run action on Redis", t);
            return false;
        } finally {
            if (jedis != null) {
                try {
                    pool.returnResource(jedis);
                } catch (final Throwable t) {
                    LOGGER.warn("Failed to return Redis client to the pool", t);
                }
            }
        }
    }
}

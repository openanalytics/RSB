/*
 * R Service Bus
 * 
 * Copyright (c) Copyright of Open Analytics NV, 2010-2021
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

package eu.openanalytics.rsb.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.message.Job;


/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RedisJobStatisticsHandler implements JobStatisticsHandler 
{
    private static final Log LOGGER = LogFactory.getLog(RedisJobStatisticsHandler.class);

    private static final SimpleDateFormat MONTH_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String RSB_STATS_KEY_PREFIX = "rsb:stats:";
    private static final String RSB_STATS_APPLICATIONS_SET_KEY = RSB_STATS_KEY_PREFIX + "applications";

    private JedisPool pool;
    private String redisHost;
    private int redisPort;

    private interface RedisAction
    {
        void run(Jedis jedis);
    }

	@Override
	public void setConfiguration(final Map<String, Object> configuration)
    {
        redisHost = (String) configuration.get("host");
        redisPort = (Integer) configuration.get("port");
    }

	@Override
	public void initialize()
    {
        final GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setBlockWhenExhausted(false);
        
        pool = new JedisPool(poolConfig, redisHost, redisPort);

        final boolean redisConfigurationOk = runWithJedis(new RedisAction()
        {
			@Override
			public void run(final Jedis jedis)
            {
                final String pingResponse = jedis.ping();

                Validate.isTrue("PONG".equals(pingResponse),
                    "Unexpected response received from pinging Redis: " + pingResponse);

                LOGGER.info(String.format("%s successfully connected to Redis: %s:%d",
                    getClass().getSimpleName(), redisHost, redisPort));
            }
        });

        Validate.isTrue(
            redisConfigurationOk,
            "Redis can't be contacted: please check parameter 'rsb.jobs.stats.handler' (set it to 'none' to disable statistics altogether)");
    }

	@Override
	public void destroy()
    {
        pool.destroy();
    }

	@Override
	public void storeJobStatistics(final Job job,
                                   final Calendar jobCompletionTime,
                                   final long millisecondsSpentProcessing,
                                   final String rServiAddress)
    {

        runWithJedis(new RedisAction()
        {
			@Override
			public void run(final Jedis jedis)
            {
                // ensure application is registered as a statistics producer
                jedis.sadd(RSB_STATS_APPLICATIONS_SET_KEY, job.getApplicationName());

                // add monthstamp to application's set of monthstamps
                jobCompletionTime.setTimeZone(UTC);
                final String monthStamp = MONTH_STAMP_FORMAT.format(jobCompletionTime.getTime());
                jedis.sadd(RSB_STATS_KEY_PREFIX + job.getApplicationName() + ":monthstamps", monthStamp);

                // create persisted statistics JSON structure and store it in monthstamp list
                final Map<String, Object> statsMap = new HashMap<>(5);
                statsMap.put("application_name", job.getApplicationName());
                statsMap.put("job_id", job.getJobId());
                statsMap.put("utc_timestamp", jobCompletionTime.getTimeInMillis());
                statsMap.put("time_spent", millisecondsSpentProcessing);
                statsMap.put("r_servi_address", rServiAddress);

                if (StringUtils.isNotBlank(job.getUserName()))
                {
                    statsMap.put("user_name", job.getUserName());
                }

                final String statsJson = Util.toJson(statsMap);
                jedis.lpush(RSB_STATS_KEY_PREFIX + job.getApplicationName() + ":" + monthStamp, statsJson);
            }
        });
    }

    private boolean runWithJedis(final RedisAction action)
    {
        Jedis jedis = null;

        try
        {
            jedis = pool.getResource();
            action.run(jedis);
            return true;
        }
        catch (final Throwable t)
        {
            LOGGER.warn("Failed to run action on Redis", t);
            return false;
        }
        finally
        {
            if (jedis != null)
            {
                try
                {
                    jedis.close();
                }
                catch (final Throwable t)
                {
                    LOGGER.warn("Failed to return Redis client to the pool", t);
                }
            }
        }
    }
}

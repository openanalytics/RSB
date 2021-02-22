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

package eu.openanalytics.rsb.config;

import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import eu.openanalytics.rsb.rservi.RmiRServiInstanceProvider.PooledRServiWrapper;

public class RServiPoolConfig extends GenericKeyedObjectPoolConfig<PooledRServiWrapper> {
  
  private int maxActive = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL_PER_KEY;
  private int maxIdle = GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY;
  private int minIdle = GenericKeyedObjectPoolConfig.DEFAULT_MIN_IDLE_PER_KEY;
  private long maxWait = BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
  private int whenExhaustedAction = 1;
  
  public RServiPoolConfig() {
    super();
  }
  
  /**
   * @return the maxActive
   */
  public int getMaxActive() {
    return maxActive;
  }
  /**
   * @param maxActive the maxActive to set
   */
  public void setMaxActive(int maxActive) {
    this.maxActive = maxActive;
    setMaxTotalPerKey(maxActive);
  }
  /**
   * @return the maxIdle
   */
  public int getMaxIdle() {
    return maxIdle;
  }
  /**
   * @param maxIdle the maxIdle to set
   */
  public void setMaxIdle(int maxIdle) {
    this.maxIdle = maxIdle;
    setMaxIdlePerKey(maxIdle);
  }
  /**
   * @return the minIdle
   */
  public int getMinIdle() {
    return minIdle;
  }
  /**
   * @param minIdle the minIdle to set
   */
  public void setMinIdle(int minIdle) {
    this.minIdle = minIdle;
    setMinIdlePerKey(minIdle);
  }

  public long getMaxWait() {
    return maxWait;
  }

  public void setMaxWait(long maxWait) {
    this.maxWait = maxWait;
    setMaxWaitMillis(maxWait);
  }

  /**
   * @return the whenExhaustedAction
   */
  public int getWhenExhaustedAction() {
    return whenExhaustedAction;
  }

  /**
   * @param whenExhaustedAction the whenExhaustedAction to set
   */
  public void setWhenExhaustedAction(int whenExhaustedAction) {
    this.whenExhaustedAction = whenExhaustedAction;
    if(whenExhaustedAction == 1) {
      setBlockWhenExhausted(true);
    } else {
      setBlockWhenExhausted(false);
    }
  }

}

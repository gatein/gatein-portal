/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.application;

import org.gatein.common.concurrent.AtomicPositiveLong;
import org.gatein.common.concurrent.LongSampler;

/**
 * Created by The eXo Platform SAS Author : tam.nguyen
 * tam.nguyen@exoplatform.com Mar 17, 2009
 */

public class ApplicationStatistic
{

   /** . */
   private static final int ONE_SECOND = 20000;

   private final String appId;

   private final LongSampler times = new LongSampler(1000);

   private final LongSampler throughput = new LongSampler(1000);

   private final AtomicPositiveLong maxTime = new AtomicPositiveLong();

   private final AtomicPositiveLong minTime = new AtomicPositiveLong();

   // count variable, store number of request
   private volatile long countRequest = 0;

   public ApplicationStatistic(String appId)
   {
      this.appId = appId;
   }

   public String getAppId()
   {
      return appId;
   }

   /**
    * Log the time.
    * 
    * @param timeMillis the time to log in milliseconds
    */
   public void logTime(long timeMillis)
   {

      //
      times.add(timeMillis);

      //  add current time to throughput array
      throughput.add(System.currentTimeMillis());

      // if time > max time then put a new max time value
      maxTime.setIfGreater(timeMillis);

      // generate first value for min time
      minTime.setIfLower(timeMillis);

      //
      countRequest++;
   }

   public double getMaxTime()
   {
      long maxTime = this.maxTime.get();
      if (maxTime == -1)
      {
         return -1;
      }
      return maxTime;
   }

   public double getMinTime()
   {
      long minTime = this.minTime.get();
      if (minTime == -1)
      {
         return -1;
      }
      return minTime;
   }

   public double getAverageTime()
   {
      return times.average();
   }

   /**
    * Compute the throughput.
    * 
    * @return the throughput
    */
   public double getThroughput()
   {
      return throughput.countAboveThreshold(System.currentTimeMillis() - ONE_SECOND);
   }

   public long executionCount()
   {
      return countRequest;
   }
}

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

import org.exoplatform.application.registry.Application;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
@Managed
@NameTemplate({@Property(key = "view", value = "portal"), @Property(key = "service", value = "statistic"),
   @Property(key = "type", value = "application")})
@ManagedDescription("Application statistic service")
public class ApplicationStatisticService implements Startable
{

   /** . */
   private final ConcurrentMap<String, ApplicationStatistic> apps = new ConcurrentHashMap<String, ApplicationStatistic>();

   public ApplicationStatisticService()
   {
   }

   /*
    * Returns the list of applicationId sorted alphabetically.
    */
   @Managed
   @ManagedDescription("The list of application identifiers sorted alphabetically")
   public String[] getApplicationList()
   {
      List<String> list = new ArrayList<String>(apps.keySet());
      Collections.sort(list);
      return list.toArray(new String[list.size()]);
   }

   /*
    * get ApplicationStatistic by application id, if it isn't exits, create a new one
    */
   public ApplicationStatistic getApplicationStatistic(
      @ManagedDescription("The application id") @ManagedName("applicationId") String appId)
   {
      ApplicationStatistic app = apps.get(appId);
      if (app == null)
      {
         app = new ApplicationStatistic(appId);
         ApplicationStatistic existing = apps.putIfAbsent(appId, app);
         if (existing != null)
         {
            app = existing;
         }
      }
      return app;
   }

   /*
    * return max time of an specify application
    */
   @Managed
   @ManagedDescription("The maximum execution time of a specified application in seconds")
   public double getMaxTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId)
   {
      ApplicationStatistic app = getApplicationStatistic(appId);
      return toSeconds(app.getMaxTime());
   }

   /*
    * return min time of an specify application
    */
   @Managed
   @ManagedDescription("The minimum execution time of a specified application in seconds")
   public double getMinTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId)
   {
      ApplicationStatistic app = getApplicationStatistic(appId);
      return toSeconds(app.getMinTime());
   }

   /*
    * return average time of an specify application
    */
   @Managed
   @ManagedDescription("Return the average execution time of a specified application in seconds")
   public double getAverageTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId)
   {
      ApplicationStatistic app = getApplicationStatistic(appId);
      return toSeconds(app.getAverageTime());
   }

   /*
    * return count of an specify application
    */
   @Managed
   @ManagedDescription("The execution count of a specified application")
   public long getExecutionCount(@ManagedDescription("The application id") @ManagedName("applicationId") String appId)
   {
      ApplicationStatistic app = getApplicationStatistic(appId);
      return app.executionCount();
   }

   /*
    * returns  10 slowest applications
    */
   @Managed
   @ManagedDescription("The list of the 10 slowest applications")
   public String[] getSlowestApplications()
   {
      return getApplicationsSortedByAverageTime(true);
   }

   /*
    * returns  10 fastest applications
    */
   @Managed
   @ManagedDescription("The list of the 10 fastest applications")
   public String[] getFastestApplications()
   {
      return getApplicationsSortedByAverageTime(false);
   }

   private String[] getApplicationsSortedByAverageTime(boolean desc)
   {
      TreeMap<Double, String> map = new TreeMap<Double, String>();
      for (ApplicationStatistic app : apps.values())
      {
         if (app.getAverageTime() > 0)
         {
            map.put(app.getAverageTime(), app.getAppId());
         }
      }
      List<String> list = new ArrayList<String>(map.values());
      if (desc)
      {
         Collections.reverse(list);
      }
      List<String> sub = list.subList(0, Math.min(map.size(), 10));
      return sub.toArray(new String[sub.size()]);
   }

   /*
    * returns  10 most executed applications
    */
   @Managed
   @ManagedDescription("The list of the 10 most executed applications")
   public String[] getMostExecutedApplications()
   {
      TreeMap<Long, String> map = new TreeMap<Long, String>();
      for (ApplicationStatistic app : apps.values())
      {
         if (app.executionCount() > 0)
         {
            map.put(app.executionCount(), app.getAppId());
         }
      }
      List<String> list = new ArrayList<String>(map.values());
      Collections.reverse(list);
      List<String> sub = list.subList(0, Math.min(map.size(), 10));
      return sub.toArray(new String[sub.size()]);
   }

   private double toSeconds(double value)
   {
      return value == -1 ? -1 : value / 1000D;
   }

   public void start()
   {
   }

   public void stop()
   {
   }
}

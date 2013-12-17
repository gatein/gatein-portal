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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
@Managed
@ManagedDescription("Application statistic service")
@NameTemplate({ @Property(key = "view", value = "portal"), @Property(key = "service", value = "statistic"),
        @Property(key = "type", value = "application") })
@RESTEndpoint(path = "applicationstatistic")
public class ApplicationStatisticService implements Startable {

    /** . */
    private final ConcurrentMap<String, ApplicationStatistic> apps = new ConcurrentHashMap<String, ApplicationStatistic>();

    public ApplicationStatisticService() {
    }

    public ApplicationStatistic findApplicationStatistic(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Parameter 'applicationId' is required.");
        } else {
            ApplicationStatistic result = apps.get(name);
            if (result == null) {
                /* Try to prevent a potential XSS */
                String safeName = name.replaceAll("[^a-zA-Z0-9_\\-\\./]+", "");
                throw new IllegalArgumentException("There is no such application with applicationId '"+ safeName +"'.");
            } else {
                return result;
            }
        }
    }

    /*
     * Returns the list of applicationId sorted alphabetically.
     */
    @Managed
    @ManagedDescription("The list of application identifiers sorted alphabetically")
    public String[] getApplicationList() {
        List<String> list = new ArrayList<String>(apps.keySet());
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    /*
     * get ApplicationStatistic by application id, if it isn't exits, create a new one
     */
    public ApplicationStatistic getApplicationStatistic(String appId) {
        ApplicationStatistic app = apps.get(appId);
        if (app == null) {
            app = new ApplicationStatistic(appId);
            ApplicationStatistic existing = apps.putIfAbsent(appId, app);
            if (existing != null) {
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
    @Impact(ImpactType.READ)
    public double getMaxTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId) {
        ApplicationStatistic app = findApplicationStatistic(appId);
        return toSeconds(app.getMaxTime());
    }

    /*
     * return min time of an specify application
     */
    @Managed
    @ManagedDescription("The minimum execution time of a specified application in seconds")
    @Impact(ImpactType.READ)
    public double getMinTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId) {
        ApplicationStatistic app = findApplicationStatistic(appId);
        return toSeconds(app.getMinTime());
    }

    /*
     * return average time of an specify application
     */
    @Managed
    @ManagedDescription("Return the average execution time of a specified application in seconds")
    @Impact(ImpactType.READ)
    public double getAverageTime(@ManagedDescription("The application id") @ManagedName("applicationId") String appId) {
        ApplicationStatistic app = findApplicationStatistic(appId);
        return toSeconds(app.getAverageTime());
    }

    /*
     * return count of an specify application
     */
    @Managed
    @ManagedDescription("The execution count of a specified application")
    @Impact(ImpactType.READ)
    public long getExecutionCount(@ManagedDescription("The application id") @ManagedName("applicationId") String appId) {
        ApplicationStatistic app = findApplicationStatistic(appId);
        return app.executionCount();
    }

    /*
     * returns 10 slowest applications
     */
    @Managed
    @ManagedDescription("The list of the 10 slowest applications")
    public String[] getSlowestApplications() {
        return getApplicationsSortedByAverageTime(true);
    }

    /*
     * returns 10 fastest applications
     */
    @Managed
    @ManagedDescription("The list of the 10 fastest applications")
    public String[] getFastestApplications() {
        return getApplicationsSortedByAverageTime(false);
    }

    private String[] getApplicationsSortedByAverageTime(boolean desc) {
        List<ApplicationStatistic> list = new ArrayList<ApplicationStatistic>();
        for (ApplicationStatistic app : apps.values()) {
            if (app.getAverageTime() > 0) {
                list.add(app);
            }
        }
        Collections.sort(list, new Comparator<ApplicationStatistic>() {
            public int compare(ApplicationStatistic o1, ApplicationStatistic o2) {
                return (int) Math.signum(o1.getAverageTime() - o2.getAverageTime());
            }
        });
        if (desc) {
            Collections.reverse(list);
        }
        List<ApplicationStatistic> sub = list.subList(0, Math.min(list.size(), 10));
        return asIds(sub);
    }

    /*
     * returns 10 most executed applications
     */
    @Managed
    @ManagedDescription("The list of the 10 most executed applications")
    public String[] getMostExecutedApplications() {
        ArrayList<ApplicationStatistic> list = new ArrayList<ApplicationStatistic>();
        for (ApplicationStatistic app : apps.values()) {
            if (app.executionCount() > 0) {
                list.add(app);
            }
        }
        Collections.sort(list, new Comparator<ApplicationStatistic>() {
            public int compare(ApplicationStatistic o1, ApplicationStatistic o2) {
                long diff = o1.executionCount() - o2.executionCount();
                return diff == 0 ? 0 : diff > 0 ? -1 : 1;
            }
        });
        List<ApplicationStatistic> sub = list.subList(0, Math.min(list.size(), 10));
        return asIds(sub);
    }

    private String[] asIds(List<ApplicationStatistic> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).getAppId();
        }
        return array;
    }

    private double toSeconds(double value) {
        return value == -1 ? -1 : value / 1000D;
    }

    public void start() {
    }

    public void stop() {
    }
}

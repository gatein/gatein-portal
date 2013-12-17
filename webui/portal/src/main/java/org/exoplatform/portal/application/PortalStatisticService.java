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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
@Managed
@ManagedDescription("The portal statistic service")
@NameTemplate({ @Property(key = "view", value = "portal"), @Property(key = "service", value = "statistic"),
        @Property(key = "type", value = "portal") })
@RESTEndpoint(path = "portalstatistic")
public class PortalStatisticService implements Startable {

    private ConcurrentMap<String, PortalStatistic> apps = new ConcurrentHashMap<String, PortalStatistic>();

    private UserPortalConfigService configService;

    /** . */
    private final Logger log = LoggerFactory.getLogger(PortalStatisticService.class);

    public PortalStatisticService(UserPortalConfigService res) {
        configService = res;

    }

    protected PortalStatistic findPortalStatistic(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Parameter 'portalId' is required.");
        } else {
            PortalStatistic result = apps.get(name);
            if (result == null) {
                /* Try to prevent a potential XSS */
                String safeName = name.replaceAll("[^a-zA-Z0-9_\\-\\./]+", "");
                throw new IllegalArgumentException("There is no such portal with portalId '"+ safeName +"'.");
            } else {
                return result;
            }
        }
    }

    /*
     * Returns the list of the known portal names.
     */
    @Managed
    @ManagedDescription("The list of identifier of the known portals")
    public String[] getPortalList() {
        try {
            List<String> names = configService.getAllPortalNames();
            return names.toArray(new String[names.size()]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new String[0];
        }
    }

    /*
     * Get PortalStatistic, if it doesn't exits, create a new one.
     */
    public PortalStatistic getPortalStatistic(String appId) {
        PortalStatistic app = apps.get(appId);
        if (app == null) {
            app = new PortalStatistic(appId);
            PortalStatistic existing = apps.putIfAbsent(appId, app);
            if (existing != null) {
                app = existing;
            }
        }
        return app;
    }

    /*
     * Returns the max time of a specified portal
     */
    @Managed
    @ManagedDescription("The maximum execution time of a specified portal in seconds")
    @Impact(ImpactType.READ)
    public double getMaxTime(@ManagedDescription("The portal id") @ManagedName("portalId") String id) {
        return toSeconds(findPortalStatistic(id).getMaxTime());
    }

    /*
     * Return the min time of a specified portal
     */
    @Managed
    @ManagedDescription("The mininum execution time of a specified portal in seconds")
    @Impact(ImpactType.READ)
    public double getMinTime(@ManagedDescription("The portal id") @ManagedName("portalId") String id) {
        return toSeconds(findPortalStatistic(id).getMinTime());
    }

    /*
     * Return the average time of a specified portal
     */
    @Managed
    @ManagedDescription("The average execution time of a specified portal in seconds")
    @Impact(ImpactType.READ)
    public double getAverageTime(@ManagedDescription("The portal id") @ManagedName("portalId") String id) {
        return toSeconds(findPortalStatistic(id).getAverageTime());
    }

    /*
     * Return the throughput of a specified portal
     */
    @Managed
    @ManagedDescription("The number of request per second of a specified portal")
    @Impact(ImpactType.READ)
    public double getThroughput(@ManagedDescription("The portal id") @ManagedName("portalId") String id) {
        return findPortalStatistic(id).getThroughput();
    }

    /*
     * Return the count of a specified portal
     */
    @Managed
    @ManagedDescription("The execution count of a specified portal")
    @Impact(ImpactType.READ)
    public long getExecutionCount(@ManagedDescription("The portal id") @ManagedName("portalId") String id) {
        return findPortalStatistic(id).viewCount();
    }

    private double toSeconds(double value) {
        return value == -1 ? -1 : value / 1000D;
    }

    public void start() {
    }

    public void stop() {
    }
}

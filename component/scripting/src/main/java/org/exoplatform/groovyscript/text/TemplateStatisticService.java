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

package org.exoplatform.groovyscript.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;

/**
 * Created by The eXo Platform SAS Author : tam.nguyen tamndrok@gmail.com Mar 17, 2009
 */

@Managed
@ManagedDescription("Template statistic service")
@NameTemplate({ @Property(key = "view", value = "portal"), @Property(key = "service", value = "statistic"),
        @Property(key = "type", value = "template") })
@RESTEndpoint(path = "templatestatistics")
public class TemplateStatisticService {

    final Map<String, TemplateStatistic> apps = new ConcurrentHashMap<String, TemplateStatistic>();

    private final String ASC = "ASC";

    private final String DESC = "DESC";

    public TemplateStatisticService() {
    }

    public TemplateStatistic findTemplateStatistic(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Parameter 'templateid' is required.");
        } else {
            TemplateStatistic result = apps.get(name);
            if (result == null) {
                /* Try to prevent a potential XSS */
                String safeName = name.replaceAll("[^a-zA-Z0-9_\\-\\./]+", "");
                throw new IllegalArgumentException("There is no such template with templateid '"+ safeName +"'.");
            } else {
                return result;
            }
        }
    }

    /*
     * get TemplateStatistic by name, if TemplateStatistic isn't exits, create a new one.
     */
    public TemplateStatistic getTemplateStatistic(String name) {
        TemplateStatistic app = apps.get(name);
        if (app == null) {
            app = new TemplateStatistic(name);
            apps.put(name, app);
        }
        return app;
    }

    /*
     * returns a list of templateId sorted alphabetically
     */
    @Managed
    @ManagedDescription("The list of template identifiers sorted alphabetically")
    public String[] getTemplateList() {
        List<Map.Entry<String, TemplateStatistic>> list = new LinkedList<Map.Entry<String, TemplateStatistic>>(apps.entrySet());
        String[] app = new String[list.size()];
        int index = 0;
        for (Iterator<Entry<String, TemplateStatistic>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, TemplateStatistic> entry = it.next();
            app[index] = entry.getKey();
            index++;
        }
        return app;
    }

    /*
     * return max time of an specify template
     */
    @Managed
    @ManagedDescription("The maximum rendering time of a specified template in seconds")
    @Impact(ImpactType.READ)
    public double getMaxTime(@ManagedDescription("The template id") @ManagedName("templateId") String name) {
        TemplateStatistic app = findTemplateStatistic(name);
        return toSeconds(app.getMaxTime());
    }

    /*
     * return min time of an specify template
     */
    @Managed
    @ManagedDescription("The minimum rendering time of a specified template in seconds")
    @Impact(ImpactType.READ)
    public double getMinTime(@ManagedDescription("The template id") @ManagedName("templateId") String name) {
        TemplateStatistic app = findTemplateStatistic(name);
        return toSeconds(app.getMinTime());
    }

    /*
     * return count of an specify template
     */
    @Managed
    @ManagedDescription("The rendering count of a specified template")
    @Impact(ImpactType.READ)
    public long getExecutionCount(@ManagedDescription("The template id") @ManagedName("templateId") String name) {
        TemplateStatistic app = findTemplateStatistic(name);
        return app.executionCount();
    }

    /*
     * return average time of an specify template
     */
    @Managed
    @ManagedDescription("The average rendering time of a specified template in seconds")
    @Impact(ImpactType.READ)
    public double getAverageTime(@ManagedDescription("The template id") @ManagedName("templateId") String name) {
        TemplateStatistic app = findTemplateStatistic(name);
        return toSeconds(app.getAverageTime());
    }

    /*
     * returns 10 slowest template
     */
    @Managed
    @ManagedDescription("The list of the 10 slowest templates")
    public String[] getSlowestTemplates() {

        Map<String, Double> application = new HashMap<String, Double>();
        for (Map.Entry<String, TemplateStatistic> entry : apps.entrySet()) {
            String url = entry.getKey();
            application.put(url, getAverageTime(url));
        }

        return sort(application, DESC);
    }

    /*
     * returns 10 slowest template
     */
    @Managed
    @ManagedDescription("The list of the 10 most executed templates")
    public String[] getMostExecutedTemplates() {

        Map<String, Long> application = new HashMap<String, Long>();
        for (Map.Entry<String, TemplateStatistic> entry : apps.entrySet()) {
            String url = entry.getKey();
            application.put(url, getExecutionCount(url));
        }

        return sort(application, DESC);
    }

    /*
     * returns 10 fastest template
     */
    @Managed
    @ManagedDescription("The list of the 10 fastest templates")
    public String[] getFastestTemplates() {

        Map<String, Double> application = new HashMap<String, Double>();
        for (Map.Entry<String, TemplateStatistic> entry : apps.entrySet()) {
            String url = entry.getKey();
            application.put(url, getAverageTime(url));
        }

        return sort(application, ASC);
    }

    private <T extends Comparable<T>> String[] sort(Map<String, T> source, final String order) {
        String[] app = new String[10];
        List<Map.Entry<String, T>> list = new ArrayList<Map.Entry<String, T>>(source.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, T>>() {
            public int compare(Map.Entry<String, T> o1, Map.Entry<String, T> o2) {
                T value1 = o1.getValue();
                T value2 = o2.getValue();
                if (DESC.equals(order)) {
                    T tmp = value1;
                    value1 = value2;
                    value2 = tmp;
                }
                return value1.compareTo(value2);
            }
        });
        int index = 0;
        for (Map.Entry<String, T> entry : list) {
            app[index] = entry.getKey();
            index++;
            if (index >= app.length) {
                break;
            }
        }
        return app;
    }

    private double toSeconds(double value) {
        return value == -1 ? -1 : value / 1000D;
    }
}

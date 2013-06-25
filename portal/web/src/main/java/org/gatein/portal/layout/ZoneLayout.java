/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import juzu.template.Template;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.page.PageContext;
import org.gatein.portal.page.Result;

/**
 * A layout implementing the spec <a href="https://community.jboss.org/wiki/InPlaceEditing">In Place Editing</a>.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ZoneLayout extends Layout {

    /** . */
    private final ZoneLayoutFactory factory;

    /** . */
    private final String id;

    /** . */
    private final Map<String, ArrayList<WindowLayout>> windows;

    public ZoneLayout(ZoneLayoutFactory factory, String id, Map<String, ArrayList<WindowLayout>> windows) {
        this.id = id;
        this.factory = factory;
        this.windows = windows;
    }

    private Map<String, Result.Fragment> getFragments(String zone, Map<String, Result.Fragment> fragments) {
        Map<String, Result.Fragment> list = null;
        ArrayList<WindowLayout> column = windows.get(zone);
        if (column != null) {
            list = new LinkedHashMap<String, Result.Fragment>();
            for (WindowLayout window : column) {
                Result.Fragment fragment = fragments.get(window.name);
                if (fragment != null) {
                    list.put(window.name, fragment);
                }
            }
        }
        return list;
    }

    @Override
    public void render(Map<String, Result.Fragment> fragments, String body, PageContext state, Appendable to) {
        Template template = null;
        Map<String, Object> parameters = null;
        if ("1".equals(id)) {
            template = factory.zone_1_column;
            parameters = Collections.<String, Object>singletonMap("l1", getFragments("1", fragments));
        } else if ("site".equals(id)) {
            template = factory.site;
            juzu.impl.common.Builder.Map<String, Object> builder = juzu.impl.common.Builder.
                    <String, Object>map("header", getFragments("header", fragments)).
                    map("footer", getFragments("footer", fragments));
            if (body != null) {
                builder = builder.map("body", body);
            }
            parameters = builder.build();
        }
        if (template != null) {
            template.renderTo(to, parameters);
        } else {
            throw new UnsupportedOperationException("Layout not found");
        }
    }

    private Template.Builder render1_column(ArrayList<Result.Fragment> l1) {
        return factory.zone_1_column.with(Collections.<String, Object>singletonMap("l1", l1));
    }

    private Template.Builder render2_columns_30_70(ArrayList<Result.Fragment> l1, ArrayList<Result.Fragment> l2) {
        return factory.zone_2_columns_70_30.with(juzu.impl.common.Builder.map("l1", l1).map("l2", l2).build());
    }

    private static class WindowLayout {

        /** . */
        final String name;

        /** . */
        final ElementState.Window state;

        WindowLayout(String name, ElementState.Window state) {
            this.name = name;
            this.state = state;
        }
    }

    public static class Builder implements LayoutBuilder {

        /** . */
        private final String id;

        /** . */
        private final ZoneLayoutFactory factory;

        /** . */
        private LinkedList<ArrayList<WindowLayout>> currentWindows;

        /** . */
        private Map<String, ArrayList<WindowLayout>> windows = Collections.emptyMap();

        public Builder(ZoneLayoutFactory factory, String id) {
            this.id = id;
            this.factory = factory;
            this.currentWindows = new LinkedList<ArrayList<WindowLayout>>();
        }

        @Override
        public void beginContainer(String name, ElementState.Container state) {
            String zone = parseZone(name);
            if (zone != null) {
                currentWindows.push(new ArrayList<WindowLayout>());
            }
        }

        @Override
        public void window(String name, ElementState.Window state) {
            if (currentWindows.size() > 0) {
                currentWindows.peek().add(new WindowLayout(name, state));
            }
        }

        @Override
        public void endContainer(String name, ElementState.Container state) {
            String zone = parseZone(name);
            if (zone != null) {
                ArrayList<WindowLayout> zoneWindows = currentWindows.pop();
                if (zoneWindows.size() > 0) {
                    if (windows.isEmpty()) {
                        windows = new HashMap<String, ArrayList<WindowLayout>>();
                    }
                    windows.put(zone, zoneWindows);
                }
            }
        }

        @Override
        public Layout build() {
            return new ZoneLayout(factory, id, windows);
        }

        private String parseZone(String zone) {
            if (zone != null) {
                zone = zone.trim();
                if ("header".equals(zone) || "footer".equals(zone)) {
                    return zone;
                } else {
                    try {
                        Integer.parseInt(zone);
                        return zone;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return null;
        }
    }

}

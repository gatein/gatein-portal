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
package org.gatein.portal.web.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import juzu.template.Template;

import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.web.page.PageContext;
import org.gatein.portal.content.Result;
import org.gatein.portal.content.Result.Fragment;

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
    public void render(RenderingContext renderingContext, Map<String, Fragment> fragments, String body, PageContext state, Appendable to) {
        Template template = null;
        final Map<String, Object> parameters = new HashMap<String, Object>();
        if ("1".equals(id)) {
            template = factory.zone_1_column;
            parameters.put("l1", getFragments("1", fragments));
        } else if ("2".equals(id)) {
            template = factory.zone_2_columns_70_30;
            Map<String, Fragment> l1 = getFragments("1", fragments);
            Map<String, Fragment> l2 = getFragments("2", fragments);
            parameters.put("l1", l1);
            parameters.put("l2", l2);
        } else if ("site".equals(id)) {
            template = factory.site;
            parameters.put("header", getFragments("header", fragments));
            parameters.put("footer", getFragments("footer", fragments));
            if (body != null) {
                parameters.put("body", body);
            }
        } else {
            if ("3".equals(id)) {
                template = factory.zone_2_columns_1_row;
            } else if ("4".equals(id)) {
                template = factory.zone_1_row_2_columns;
            } else if ("5".equals(id)) {
                template = factory.zone_3_columns;
            }
            Map<String, Fragment> l1 = getFragments("1", fragments);
            Map<String, Fragment> l2 = getFragments("2", fragments);
            Map<String, Fragment> l3 = getFragments("3", fragments);
            parameters.put("l1", l1);
            parameters.put("l2", l2);
            parameters.put("l3", l3);
        }
        if (template != null) {
            parameters.put("layoutId", renderingContext.layoutId != null ? renderingContext.layoutId : "");
            parameters.put("pageKey", renderingContext.pageKey != null ? renderingContext.pageKey : "");

            template.renderTo(to, parameters);
        } else {
            throw new UnsupportedOperationException("Layout not found");
        }
    }

    public Template.Builder render1_column(ArrayList<Result.Fragment> l1) {
        return factory.zone_1_column.with(Collections.<String, Object>singletonMap("l1", l1));
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

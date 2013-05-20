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
import java.util.LinkedList;
import java.util.Map;

import juzu.PropertyMap;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.page.PageContext;

/**
 * A layout implementing the spec <a href="https://community.jboss.org/wiki/InPlaceEditing">In Place Editing</a>.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ZoneLayout extends Layout {

    /** . */
    private final ZoneLayoutFactory factory;

    /** . */
    private final Map<Integer, ArrayList<WindowLayout>> windows;

    public ZoneLayout(ZoneLayoutFactory factory, Map<Integer, ArrayList<WindowLayout>> windows) {
        this.factory = factory;
        this.windows = windows;
    }

    private ArrayList<String> getFragments(int zone, Map<String, String> fragments) {
        ArrayList<String> list = null;
        ArrayList<WindowLayout> column = windows.get(zone);
        if (column != null) {
            list = new ArrayList<String>();
            for (WindowLayout window : column) {
                String fragment = fragments.get(window.name);
                if (fragment != null) {
                    list.add(fragment);
                }
            }
        }
        return list;
    }

    @Override
    public void render(Map<String, String> fragments, PageContext state, PropertyMap properties, Appendable to) {
        // For now we implements "1 column" and "2 columns 70/30" according to the page structure
        ArrayList<String> l1 = getFragments(1, fragments);
        if (l1 != null) {
            ArrayList<String> l2 = getFragments(2, fragments);
            if (l2 == null) {
                render1_column(l1, state, to);
            } else {
                render2_columns_30_70(l1, l2, state, to);
            }
        }
    }

    private void render1_column(ArrayList<String> l1, PageContext state, Appendable to) {
        Map<String, Object> a = Collections.<String, Object>singletonMap("l1", l1);
        factory.zone_1_column.renderTo(to, a);
    }

    private void render2_columns_30_70(ArrayList<String> l1, ArrayList<String> l2, PageContext state, Appendable to) {
        factory.zone_1_column.renderTo(to);
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
        private final ZoneLayoutFactory factory;

        /** . */
        private LinkedList<ArrayList<WindowLayout>> currentWindows;

        /** . */
        private Map<Integer, ArrayList<WindowLayout>> windows = Collections.emptyMap();

        public Builder(ZoneLayoutFactory factory) {
            this.factory = factory;
            this.currentWindows = new LinkedList<ArrayList<WindowLayout>>();
        }

        @Override
        public void beginContainer(String name, ElementState.Container state) {
            Integer zone = parseZone(state);
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
            Integer zone = parseZone(state);
            if (zone != null) {
                ArrayList<WindowLayout> zoneWindows = currentWindows.pop();
                if (zoneWindows.size() > 0) {
                    if (windows.isEmpty()) {
                        windows = new HashMap<Integer, ArrayList<WindowLayout>>();
                    }
                    windows.put(zone, zoneWindows);
                }
            }
        }

        @Override
        public Layout build() {
            return new ZoneLayout(factory, windows);
        }

        private Integer parseZone(ElementState.Container state) {
            String name = state.properties.get(ElementState.Container.NAME);
            if (name != null) {
                try {
                    int zone = Integer.parseInt(name);
                    return zone >= 0 ? zone : null;
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            return null;
        }
    }

}

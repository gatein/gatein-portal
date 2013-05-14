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
import java.util.Map;

import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.page.PageState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ZoneLayout extends Layout {

    /** . */
    private final Map<Integer, ArrayList<WindowLayout>> windows;

    public ZoneLayout(Map<Integer, ArrayList<WindowLayout>> windows) {
        this.windows = windows;
    }

    @Override
    public void render(PageState state, Appendable to) {



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
        private Integer currentZone;

        /** . */
        private ArrayList<WindowLayout> currentWindows;

        /** . */
        private Map<Integer, ArrayList<WindowLayout>> windows = Collections.emptyMap();

        @Override
        public void beginContainer(String name, ElementState.Container state) {
            currentZone = parseZone(name);
        }

        @Override
        public void window(String name, ElementState.Window state) {
            if (currentZone != null) {
                if (currentWindows == null) {
                    currentWindows = new ArrayList<WindowLayout>();
                }
                currentWindows.add(new WindowLayout(name, state));
            }
        }

        @Override
        public void endContainer(String name, ElementState.Container state) {
            currentZone = null;
            if (currentWindows != null) {
                if (windows.isEmpty()) {
                    windows = new HashMap<Integer, ArrayList<WindowLayout>>();
                }
                windows.put(currentZone, currentWindows);
                currentWindows = null;
            }
        }

        @Override
        public Layout build() {
            return new ZoneLayout(windows);
        }

        private Integer parseZone(String name) {
            try {
                int zone = Integer.parseInt(name);
                return zone >= 0 ? zone : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

}

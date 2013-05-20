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
package org.gatein.portal.page;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class WindowState {

    /** . */
    private static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();

    /** The window id. */
    public final String id;

    /** The window name. */
    public final String name;

    /** The portlet window parameters. */
    Map<String, String[]> parameters;

    /** The portlet window state. */
    org.gatein.pc.api.WindowState windowState;

    /** The portlet window state. */
    org.gatein.pc.api.Mode mode;

    WindowState(NodeState node) {
        this.name = node.context.getName();
        this.id = node.context.getId();
        this.parameters = null;
        this.windowState = null;
        this.mode = null;
    }

    public WindowState(WindowState that) {

        Map<String, String[]> parameters;
        if (that.parameters == null) {
            parameters = null;
        } else if (that.parameters.isEmpty()) {
            parameters = NO_PARAMETERS;
        } else {
            parameters = new HashMap<String, String[]>(that.parameters);
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                parameter.setValue(parameter.getValue().clone());
            }
        }

        //
        this.name = that.name;
        this.id = that.id;
        this.parameters = parameters;
        this.windowState = that.windowState;
        this.mode = that.mode;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public String[] getParameter(String name) {
        return parameters.get(name);
    }

    public void setParameter(String name, String[] value) {
        if (value.length == 0) {
            if (parameters != NO_PARAMETERS) {
                parameters.remove(name);
            }
        } else {
            if (parameters == NO_PARAMETERS) {
                parameters = new HashMap<String, String[]>();
            }
            parameters.put(name, value);
        }
    }
}

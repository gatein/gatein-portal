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

package org.gatein.portal.mop.layout;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ElementState implements Serializable {

    public static NodeModel<?, ElementState> model() {
        return new NodeModel<Object, ElementState>() {
            @Override
            public NodeContext<Object, ElementState> getContext(Object node) {
                return (NodeContext<Object, ElementState>)node;
            }

            @Override
            public Object create(NodeContext<Object, ElementState> context) {
                return context.getNode();
            }
        };
    }

    private ElementState() {
    }

    public abstract boolean equals(Object o);

    public static class Window<S> extends ElementState {

        /** . */
        public final ApplicationType<S> type;

        /** . */
        public final ApplicationState<S> state;

        /** . */
        public final String title;

        /** . */
        public final String icon;

        /** . */
        public final String description;

        /** . */
        public final boolean showInfoBar;

        /** . */
        public final boolean showApplicationState;

        /** . */
        public final boolean showApplicationMode;

        /** . */
        public final String theme;

        /** . */
        public final String width;

        /** . */
        public final String height;

        /** . */
        public final Map<String, String> properties;

        /** . */
        public final List<String> accessPermissions;

        public Window(
                ApplicationType<S> type,
                ApplicationState<S> state,
                String title,
                String icon,
                String description,
                boolean showInfoBar,
                boolean showApplicationState,
                boolean showApplicationMode,
                String theme,
                String width,
                String height,
                Map<String, String> properties,
                List<String> accessPermissions) {
            this.type = type;
            this.state = state;
            this.title = title;
            this.icon = icon;
            this.description = description;
            this.showInfoBar = showInfoBar;
            this.showApplicationState = showApplicationState;
            this.showApplicationMode = showApplicationMode;
            this.theme = theme;
            this.width = width;
            this.height = height;
            this.properties = properties;
            this.accessPermissions = accessPermissions;
        }

        @Override
        public boolean equals(Object o) {
            ElementState.Window that = (Window)o;
            return Safe.equals(type, that.type) &&
                    Safe.equals(state, that.state) &&
                    Safe.equals(title, that.title) &&
                    Safe.equals(icon, that.icon) &&
                    Safe.equals(description, that.description) &&
                    Safe.equals(showInfoBar, that.showInfoBar) &&
                    Safe.equals(showApplicationState, that.showApplicationState) &&
                    Safe.equals(showApplicationMode, that.showApplicationMode) &&
                    Safe.equals(theme, that.theme) &&
                    Safe.equals(width, that.width) &&
                    Safe.equals(height, that.height) &&
                    Safe.equals(properties, that.properties) &&
                    Safe.equals(accessPermissions, that.accessPermissions);
        }
    }

    public static class Body extends ElementState {

        @Override
        public boolean equals(Object o) {
            Body that = (Body) o;
            return true;
        }
    }

    public static class Container extends ElementState {

        /** . */
        public final String id;

        /** . */
        public final String name;

        /** . */
        public final String icon;

        /** . */
        public final String template;

        /** . */
        public final String factoryId;

        /** . */
        public final String title;

        /** . */
        public final String description;

        /** . */
        public final String width;

        /** . */
        public final String height;

        /** . */
        public final List<String> accessPermissions;

        /** . */
        public final boolean dashboard;

        public Container(
                String id,
                String name,
                String icon,
                String template,
                String factoryId,
                String title,
                String description,
                String width,
                String height,
                List<String> accessPermissions,
                boolean dashboard) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.template = template;
            this.factoryId = factoryId;
            this.title = title;
            this.description = description;
            this.width = width;
            this.height = height;
            this.accessPermissions = accessPermissions;
            this.dashboard = dashboard;
        }

        @Override
        public boolean equals(Object o) {
            Container that = (Container) o;
            return Safe.equals(id, that.id) &&
                    Safe.equals(name, that.name) &&
                    Safe.equals(icon, that.icon) &&
                    Safe.equals(template, that.template) &&
                    Safe.equals(factoryId, that.factoryId) &&
                    Safe.equals(title, that.title) &&
                    Safe.equals(description, that.description) &&
                    Safe.equals(width, that.width) &&
                    Safe.equals(height, that.height) &&
                    Safe.equals(accessPermissions, that.accessPermissions) &&
                    Safe.equals(dashboard, that.dashboard);
        }
    }

}

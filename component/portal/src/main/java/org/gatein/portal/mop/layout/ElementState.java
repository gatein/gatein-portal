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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.config.Utils;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ElementState implements Serializable {

    /** . */
    public static final String[] EMPTY_STRINGS = new String[0];

    /** . */
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

    public abstract static class Builder<E extends ElementState> {

        public abstract E build();

    }

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

        public WindowBuilder builder() {
            HashMap<String, String> properties = this.properties != null ? new HashMap<String, String>(this.properties) : new HashMap<String, String>();
            String[] accessPermissions;
            if (this.accessPermissions == null) {
                accessPermissions = EMPTY_STRINGS;
            } else {
                accessPermissions = this.accessPermissions.toArray(new String[this.accessPermissions.size()]);
            }
            return new WindowBuilder(
                    type,
                    state,
                    title,
                    icon,
                    description,
                    showInfoBar,
                    showApplicationState,
                    showApplicationMode,
                    theme,
                    width,
                    height,
                    properties,
                    accessPermissions
            );
        }
    }

    public static class WindowBuilder extends Builder<Window> {

        /** . */
        private ApplicationType type;

        /** . */
        private ApplicationState state;

        /** . */
        private String title;

        /** . */
        private String icon;

        /** . */
        private String description;

        /** . */
        private boolean showInfoBar;

        /** . */
        private boolean showApplicationState;

        /** . */
        private boolean showApplicationMode;

        /** . */
        private String theme;

        /** . */
        private String width;

        /** . */
        private String height;

        /** . */
        private Map<String, String> properties;

        /** . */
        private String[] accessPermissions;

        public WindowBuilder(
                ApplicationType type,
                ApplicationState state,
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
                String[] accessPermissions) {
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

        public WindowBuilder type(ApplicationType<?> type) {
            this.type = type;
            return this;
        }

        public WindowBuilder state(ApplicationState<?> state) {
            this.state = state;
            return this;
        }

        public WindowBuilder title(String title) {
            this.title = title;
            return this;
        }

        public WindowBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public WindowBuilder description(String description) {
            this.description = description;
            return this;
        }

        public WindowBuilder showInfoBar(boolean showInfoBar) {
            this.showInfoBar = showInfoBar;
            return this;
        }

        public WindowBuilder showApplicationState(boolean showApplicationState) {
            this.showApplicationState = showApplicationState;
            return this;
        }

        public WindowBuilder showApplicationMode(boolean showApplicationMode) {
            this.showApplicationMode = showApplicationMode;
            return this;
        }

        public WindowBuilder theme(String theme) {
            this.theme = theme;
            return this;
        }

        public WindowBuilder width(String width) {
            this.width = width;
            return this;
        }

        public WindowBuilder height(String height) {
            this.height = height;
            return this;
        }

        public WindowBuilder properties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public WindowBuilder property(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public WindowBuilder accessPermissions(String... accessPermissions) {
            this.accessPermissions = accessPermissions;
            return this;
        }

        // Autocast (remove me please later)
        public Window build() {
            return new Window(
                    type,
                    state,
                    title,
                    icon,
                    description,
                    showInfoBar,
                    showApplicationState,
                    showApplicationMode,
                    theme,
                    width,
                    height,
                    Collections.unmodifiableMap(new HashMap<String, String>(properties)),
                    Utils.safeImmutableList(accessPermissions)
            );
        }
    }

    public static class Body extends ElementState {

        @Override
        public boolean equals(Object o) {
            Body that = (Body) o;
            return true;
        }

        public BodyBuilder builder() {
            return new BodyBuilder();
        }
    }

    public static class BodyBuilder extends Builder<Body> {

        public BodyBuilder() {
        }

        public Body build() {
            return new Body();
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

        public ContainerBuilder builder() {
            return new ContainerBuilder(
                    id,
                    name,
                    icon,
                    template,
                    factoryId,
                    title,
                    description,
                    width,
                    height,
                    accessPermissions.toArray(new String[accessPermissions.size()]),
                    dashboard
            );
        }

    }

    public static class ContainerBuilder extends Builder<Container> {

        /** . */
        private String id;

        /** . */
        private String name;

        /** . */
        private String icon;

        /** . */
        private String template;

        /** . */
        private String factoryId;

        /** . */
        private String title;

        /** . */
        private String description;

        /** . */
        private String width;

        /** . */
        private String height;

        /** . */
        private String[] accessPermissions;

        /** . */
        private boolean dashboard;

        public ContainerBuilder(
                String id,
                String name,
                String icon,
                String template,
                String factoryId,
                String title,
                String description,
                String width,
                String height,
                String[] accessPermissions,
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

        public ContainerBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ContainerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ContainerBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ContainerBuilder template(String template) {
            this.template = template;
            return this;
        }

        public ContainerBuilder factoryId(String factoryId) {
            this.factoryId = factoryId;
            return this;
        }

        public ContainerBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ContainerBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ContainerBuilder width(String width) {
            this.width = width;
            return this;
        }

        public ContainerBuilder height(String height) {
            this.height = height;
            return this;
        }

        public ContainerBuilder accessPermissions(String... accessPermissions) {
            this.accessPermissions = accessPermissions;
            return this;
        }

        public Container build() {
            return new Container(
                    id,
                    name,
                    icon,
                    template,
                    factoryId,
                    title,
                    description,
                    width,
                    height,
                    Utils.safeImmutableList(accessPermissions),
                    dashboard
            );
        }
    }
}

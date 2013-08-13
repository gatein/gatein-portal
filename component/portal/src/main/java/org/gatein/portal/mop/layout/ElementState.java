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
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.config.Utils;
import org.gatein.portal.mop.Properties;
import org.gatein.portal.mop.PropertyType;
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

    public abstract Builder<? extends ElementState> builder();

    public abstract Properties getProperties();

    public abstract List<String> getAccessPermissions();

    public abstract static class Builder<E extends ElementState> {

        public abstract E build();

    }

    public static class Window<S extends Serializable> extends ElementState {

        /** . */
        public static final PropertyType<String> TITLE = new PropertyType<String>("title,", String.class){};

        /** . */
        public static final PropertyType<String> ICON = new PropertyType<String>("icon", String.class){};

        /** . */
        public static final PropertyType<String> DESCRIPTION = new PropertyType<String>("description", String.class){};

        /** . */
        public static final PropertyType<Boolean> SHOW_INFO_BAR = new PropertyType<Boolean>("show-info-bar", Boolean.class){};

        /** . */
        public static final PropertyType<Boolean> SHOW_APPLICATION_STATE = new PropertyType<Boolean>("show-application-state", Boolean.class){};

        /** . */
        public static final PropertyType<Boolean> SHOW_APPLICATION_MODE = new PropertyType<Boolean>("show-application-mode", Boolean.class){};

        /** . */
        public static final PropertyType<String> THEME = new PropertyType<String>("theme", String.class){};

        /** . */
        public static final PropertyType<String> WIDTH = new PropertyType<String>("width", String.class){};

        /** . */
        public static final PropertyType<String> HEIGHT = new PropertyType<String>("height", String.class){};

        /** . */
        public final ApplicationType<S> type;

        /** . */
        public final ApplicationState<S> state;

        /** . */
        public final Properties properties;

        /** . */
        public final List<String> accessPermissions;

        public Window(
                ApplicationType<S> type,
                ApplicationState<S> state,
                Properties properties,
                List<String> accessPermissions) {

            //
            this.type = type;
            this.state = state;
            this.properties = properties;
            this.accessPermissions = accessPermissions;
        }

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

            //
            Properties.Builder builder = Properties.EMPTY.builder();
            builder.set(TITLE, title);
            builder.set(ICON, icon);
            builder.set(DESCRIPTION, description);
            builder.set(SHOW_INFO_BAR, showInfoBar);
            builder.set(SHOW_APPLICATION_STATE, showApplicationState);
            builder.set(SHOW_APPLICATION_MODE, showApplicationMode);
            builder.set(THEME, theme);
            builder.set(WIDTH, width);
            builder.set(HEIGHT, height);
            builder.set(properties);

            //
            this.type = type;
            this.state = state;
            this.properties = builder.build();
            this.accessPermissions = accessPermissions;
        }

        @Override
        public boolean equals(Object o) {
            ElementState.Window that = (Window)o;
            return Safe.equals(type, that.type) &&
                    Safe.equals(state, that.state) &&
                    Safe.equals(properties, that.properties) &&
                    Safe.equals(accessPermissions, that.accessPermissions);
        }

        @Override
        public List<String> getAccessPermissions() {
            return accessPermissions;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        public WindowBuilder builder() {
            String[] accessPermissions;
            if (this.accessPermissions == null) {
                accessPermissions = EMPTY_STRINGS;
            } else {
                accessPermissions = this.accessPermissions.toArray(new String[this.accessPermissions.size()]);
            }
            return new WindowBuilder(
                    type,
                    state,
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
        private Properties.Builder properties;

        /** . */
        private String[] accessPermissions;

        public WindowBuilder(
                ApplicationType type,
                ApplicationState state,
                Properties properties,
                String[] accessPermissions) {
            this.type = type;
            this.state = state;
            this.properties = properties.builder();
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
            properties.set(Window.TITLE, title);
            return this;
        }

        public WindowBuilder icon(String icon) {
            properties.set(Window.ICON, icon);
            return this;
        }

        public WindowBuilder description(String description) {
            properties.set(Window.DESCRIPTION, description);
            return this;
        }

        public WindowBuilder showInfoBar(boolean showInfoBar) {
            properties.set(Window.SHOW_INFO_BAR, showInfoBar);
            return this;
        }

        public WindowBuilder showApplicationState(boolean showApplicationState) {
            properties.set(Window.SHOW_APPLICATION_STATE, showApplicationState);
            return this;
        }

        public WindowBuilder showApplicationMode(boolean showApplicationMode) {
            properties.set(Window.SHOW_APPLICATION_MODE, showApplicationMode);
            return this;
        }

        public WindowBuilder theme(String theme) {
            properties.set(Window.THEME, theme);
            return this;
        }

        public WindowBuilder width(String width) {
            properties.set(Window.WIDTH, width);
            return this;
        }

        public WindowBuilder height(String height) {
            properties.set(Window.HEIGHT, height);
            return this;
        }

        public WindowBuilder properties(Map<String, String> properties) {
            this.properties.set(properties);
            return this;
        }

        public WindowBuilder property(String name, String value) {
            properties.set(name, value);
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
                    properties.build(),
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

        @Override
        public Properties getProperties() {
            return Properties.EMPTY;
        }

        @Override
        public List<String> getAccessPermissions() {
            return Collections.emptyList();
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
        public static final PropertyType<String> NAME = new PropertyType<String>("name", String.class){};

        /** . */
        public static final PropertyType<String> ICON = new PropertyType<String>("icon", String.class){};

        /** . */
        public static final PropertyType<String> TEMPLATE = new PropertyType<String>("template", String.class){};

        /** . */
        public static final PropertyType<String> FACTORY_ID = new PropertyType<String>("factory_id", String.class){};

        /** . */
        public static final PropertyType<String> TITLE = new PropertyType<String>("title", String.class){};

        /** . */
        public static final PropertyType<String> DESCRIPTION = new PropertyType<String>("description", String.class){};

        /** . */
        public static final PropertyType<String> WIDTH = new PropertyType<String>("width", String.class){};

        /** . */
        public static final PropertyType<String> HEIGHT = new PropertyType<String>("height", String.class){};

        /** . */
        public final String id;

        /** . */
        public final Properties properties;

        /** . */
        public final List<String> accessPermissions;

        /** . */
        public final boolean dashboard;

        public Container(
                String id,
                Properties properties,
                List<String> accessPermissions,
                boolean dashboard) {
            this.id = id;
            this.properties = properties;
            this.accessPermissions = accessPermissions;
            this.dashboard = dashboard;
        }

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

            //
            Properties.Builder builder = Properties.EMPTY.builder();
            builder.set(NAME, name);
            builder.set(ICON, icon);
            builder.set(TEMPLATE, template);
            builder.set(FACTORY_ID, factoryId);
            builder.set(TITLE, title);
            builder.set(DESCRIPTION, description);
            builder.set(WIDTH, width);
            builder.set(HEIGHT, height);

            //
            this.id = id;
            this.properties = builder.build();
            this.accessPermissions = accessPermissions;
            this.dashboard = dashboard;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public List<String> getAccessPermissions() {
            return accessPermissions;
        }

        @Override
        public boolean equals(Object o) {
            Container that = (Container) o;
            return Safe.equals(id, that.id) &&
                    Safe.equals(properties, that.properties) &&
                    Safe.equals(accessPermissions, that.accessPermissions) &&
                    Safe.equals(dashboard, that.dashboard);
        }

        public ContainerBuilder builder() {
            return new ContainerBuilder(
                    id,
                    properties,
                    accessPermissions.toArray(new String[accessPermissions.size()]),
                    dashboard
            );
        }

    }

    public static class ContainerBuilder extends Builder<Container> {

        /** . */
        private String id;

        /** . */
        private Properties.Builder properties;

        /** . */
        private String[] accessPermissions;

        /** . */
        private boolean dashboard;

        public ContainerBuilder(
                String id,
                Properties properties,
                String[] accessPermissions,
                boolean dashboard) {
            this.id = id;
            this.properties = properties.builder();
            this.accessPermissions = accessPermissions;
            this.dashboard = dashboard;
        }

        public ContainerBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ContainerBuilder name(String name) {
            this.properties.set(Container.NAME, name);
            return this;
        }

        public ContainerBuilder icon(String icon) {
            this.properties.set(Container.ICON, icon);
            return this;
        }

        public ContainerBuilder template(String template) {
            this.properties.set(Container.TEMPLATE, template);
            return this;
        }

        public ContainerBuilder factoryId(String factoryId) {
            this.properties.set(Container.FACTORY_ID, factoryId);
            return this;
        }

        public ContainerBuilder title(String title) {
            this.properties.set(Container.TITLE, title);
            return this;
        }

        public ContainerBuilder description(String description) {
            this.properties.set(Container.DESCRIPTION, description);
            return this;
        }

        public ContainerBuilder width(String width) {
            this.properties.set(Container.WIDTH, width);
            return this;
        }

        public ContainerBuilder height(String height) {
            this.properties.set(Container.HEIGHT, height);
            return this;
        }

        public ContainerBuilder accessPermissions(String... accessPermissions) {
            this.accessPermissions = accessPermissions;
            return this;
        }

        public Container build() {
            return new Container(
                    id,
                    properties.build(),
                    Utils.safeImmutableList(accessPermissions),
                    dashboard
            );
        }
    }
}

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

package org.exoplatform.webui.application;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.web.application.Application;
import org.exoplatform.webui.Util;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS May 7, 2006
 *
 * This abstract class defines several methods to abstract the differnt type of web application the eXo web framework can
 * provide such as portal or portlet.
 */
public abstract class WebuiApplication extends Application {

    private ConfigurationManager configManager_;

    private StateManager stateManager_;

    /**
     * This initialisation goals is to first extract and parse the webui configuration XML file defined inside the web.xml of
     * the web application.
     *
     * The ConfigurationManager class is responsible of the parsing and then wrap all the information about the UI
     * configuration.
     *
     * One of the information is the real implementation of the StateManager object. That object is extracted from the
     * configuration and stored as a field in that class.
     *
     * Lifecycle phases are also extracted from the XML file, referenced in this WebuiApplication class and initialized at the
     * same time.
     *
     */
    public void onInit() throws Exception {
        String configPath = getApplicationInitParam("webui.configuration");
        InputStream is = getResourceResolver().getInputStream(configPath);
        configManager_ = new ConfigurationManager(is);
        String stateManagerClass = configManager_.getApplication().getStateManager();
        StateManager stManager = (StateManager) Util.createObject(stateManagerClass, null);
        setStateManager(stManager);
        setApplicationLifecycle(configManager_.getApplication().getApplicationLifecycleListeners());
        super.onInit();
    }

    public ConfigurationManager getConfigurationManager() {
        return configManager_;
    }

    public StateManager getStateManager() {
        return stateManager_;
    }

    public void setStateManager(StateManager sm) {
        stateManager_ = sm;
    }

    public abstract String getApplicationInitParam(String name);

    public <T> void broadcast(Event<T> event) throws Exception {
        List<EventListener> listeners = configManager_.getApplication().getApplicationEventListeners(event.getName());
        if (listeners == null)
            return;
        for (EventListener<T> listener : listeners)
            listener.execute(event);
    }

    public <T extends UIComponent> T createUIComponent(Class<T> type, String configId, String id, WebuiRequestContext context)
            throws Exception {
        Component config = configManager_.getComponentConfig(type, configId);
        if (config == null) {
            throw new Exception("Cannot find the configuration for the component " + type.getName() + ", configId " + configId);
        }
        T uicomponent = Util.createObject(type, config.getInitParams());
        uicomponent.setComponentConfig(id, config);
        return uicomponent;
    }

    public Set<UIComponent> getDefaultUIComponentToUpdateByAjax(WebuiRequestContext context) {
        Set<UIComponent> list = new LinkedHashSet<UIComponent>(3);
        list.add(context.getUIApplication());
        return list;
    }
}

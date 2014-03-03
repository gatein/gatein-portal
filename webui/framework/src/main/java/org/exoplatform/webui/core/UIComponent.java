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

package org.exoplatform.webui.core;

import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.MonitorEvent;

/** Created by The eXo Platform SAS May 7, 2006 */
@Serialized
public abstract class UIComponent {
    private static final Log log = ExoLogger.getLogger("webui:UIComponent");

    public static final String OBJECTID = "objectId";

    public static final String UICOMPONENT = "uicomponent";

    public static final String AJAX_ASYNC = "ajax_async";

    private static final String GTN_PREFIX = "gtn";

    private String id;

    private boolean rendered = true;

    protected UIComponent uiparent;

    protected Component config;

    private static final Lifecycle<UIComponent> DEFAULT_LIFECYCLE = new Lifecycle<UIComponent>();

    public String getId() {
        return this.id;
    }

    public UIComponent setId(String id) {
        if (id == null) {
            this.id = new StringBuilder().append(GTN_PREFIX).append(Math.abs(hashCode())).toString();
        } else {
            this.id = id;
        }
        return this;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public <T extends UIComponent> T getParent() {
        return (T) this.uiparent;
    }

    public void setParent(UIComponent uicomponent) {
        this.uiparent = uicomponent;
    }

    public boolean isRendered() {
        return this.rendered;
    }

    @SuppressWarnings("unchecked")
    public <T extends UIComponent> T setRendered(boolean b) {
        this.rendered = b;
        return (T) this;
    }

    //
    // public void processInit(WebuiRequestContext context) throws Exception {
    // MonitorEvent<UIComponent> mevent = createMonitorEvent(Event.Phase.INIT, context);
    // config.getUIComponentLifecycle().init(this, context) ;
    // if(mevent != null) {
    // mevent.setEndExecutionTime(System.currentTimeMillis()) ;
    // mevent.broadcast() ;
    // }
    // }
    //

    public void processDecode(WebuiRequestContext context) throws Exception {
        MonitorEvent<UIComponent> mevent = createMonitorEvent(Event.Phase.DECODE, context);
        getLifecycle().processDecode(this, context);
        if (mevent != null) {
            mevent.setEndExecutionTime(System.currentTimeMillis());
            mevent.broadcast();
        }
    }

    public void processAction(WebuiRequestContext context) throws Exception {
        MonitorEvent<UIComponent> mevent = createMonitorEvent(Event.Phase.PROCESS, context);
        getLifecycle().processAction(this, context);
        if (mevent != null) {
            mevent.setEndExecutionTime(System.currentTimeMillis());
            mevent.broadcast();
        }
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        MonitorEvent<UIComponent> mevent = createMonitorEvent(Event.Phase.RENDER, context);
        getLifecycle().processRender(this, context);
        if (mevent != null) {
            mevent.setEndExecutionTime(System.currentTimeMillis());
            mevent.broadcast();
        }
    }

    private Lifecycle<UIComponent> getLifecycle() throws Exception {
        if (config == null) {
            log.debug("No config was found for " + getClass().getSimpleName() + " with id '" + id + "'. Using a default one.");
            return DEFAULT_LIFECYCLE;
        }
        return config.getUIComponentLifecycle();
    }

    //
    // public void processDestroy(WebuiRequestContext context) throws Exception {
    // MonitorEvent<UIComponent> mevent = createMonitorEvent(Event.Phase.DESTROY, context);
    // config.getUIComponentLifecycle().init(this, context) ;
    // if(mevent != null) {
    // mevent.setEndExecutionTime(System.currentTimeMillis()) ;
    // mevent.broadcast() ;
    // }
    // }

    public Component getComponentConfig() {
        return this.config;
    }

    public void setComponentConfig(String componentId, Component config) {
        this.config = config;
        if (componentId == null || componentId.length() == 0) {
            componentId = config.getId();
        }
        if (componentId == null) {
            String type = config.getType();
            componentId = type.substring(type.lastIndexOf('.') + 1);
        }
        setId(componentId);
    }

    public void setComponentConfig(Class<?> clazz, String id) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        WebuiApplication app = (WebuiApplication) context.getApplication();
        this.config = app.getConfigurationManager().getComponentConfig(clazz, id);
    }

    public String getTemplate() {
        return config != null ? config.getTemplate() : null;
    }

    public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
        return context.getResourceResolver(template);
    }

    public <T extends UIComponent> T getAncestorOfType(Class<T> classType) {
        UIComponent parent = getParent();
        while (parent != null) {
            if (classType.isInstance(parent)) {
                return classType.cast(parent);
            }
            parent = parent.getParent();
        }
        return null;
    }

    protected String loadConfirmMesssage(org.exoplatform.webui.config.Event event, WebuiRequestContext context, String beanId) {
        String confirmKey = event.getConfirm();
        if (confirmKey.length() < 1) {
            return confirmKey;
        }
        try {
            String confirm = context.getApplicationResourceBundle().getString(confirmKey);
            return confirm.replaceAll("\\{0\\}", beanId);
        } catch (Exception e) {
        }
        return confirmKey;
    }

    public String event(String name) throws Exception {
        return event(name, null);
    }

    public String event(String name, String beanId) throws Exception {
        return event(name, beanId, null);
    }

    /**
     * Render an event ajax URL for a given bean.
     *
     * @param name the event name
     * @param beanId the optional bean id
     * @param params the optional event parameters
     * @return the rendered URL
     * @throws Exception any exception
     */
    public String event(String name, String beanId, Parameter[] params) throws Exception {
        return renderEventURL(true, name, beanId, params);
    }

    public String url(String name) throws Exception {
        return url(name, null);
    }

    public String url(String name, String beanId) throws Exception {
        return url(name, beanId, null);
    }

    /**
     * Render an event URL for a given bean.
     *
     * @param name the event name
     * @param beanId the optional bean id
     * @param params the optional event parameters
     * @return the rendered URL
     * @throws Exception any exception
     */
    public String url(String name, String beanId, Parameter[] params) throws Exception {
        return renderEventURL(false, name, beanId, params);
    }

    /**
     * Render an event URL of a given bean.
     *
     * @param ajax the url type, true for ajax, false otherwise
     * @param name the event name
     * @param beanId the optional bean id
     * @param params the optional event parameters
     * @return the rendered URL
     * @throws Exception any exception
     */
    public String renderEventURL(boolean ajax, String name, String beanId, Parameter[] params) throws Exception {
        org.exoplatform.webui.config.Event event = config.getUIComponentEventConfig(name);
        if (event == null) {
            return "??config??";
        }

        //
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        URLBuilder<UIComponent> urlBuilder = context.getURLBuilder();
        if (urlBuilder == null) {
            return "??builder??";
        }

        //
        String confirm = loadConfirmMesssage(event, context, beanId);

        //
        if (ajax) {
            return urlBuilder.createAjaxURL(this, event.getName(), confirm, beanId, params, event.isCsrfCheck());
        } else {
            try {
                return urlBuilder.createURL(this, event.getName(), confirm, beanId, params, event.isCsrfCheck());
            } catch (Exception e) {
                log.error("Could not render component even URL for id=" + beanId + ", name=" + name, e);
                return "";
            }
        }
    }

    // julien : check if this is used effectively or not
    public String doAsync(String name, String beanId, Parameter[] params) throws Exception {
        return event(name, beanId, new Parameter[] { new Parameter(AJAX_ASYNC, "true") });
    }

    public <T> void broadcast(Event<T> event, Phase phase) throws Exception {
        if (config == null) {
            return;
        }
        org.exoplatform.webui.config.Event econfig = config.getUIComponentEventConfig(event.getName());
        if (econfig == null) {
            return;
        }
        event.setCsrfCheck(econfig.isCsrfCheck());

        Phase executionPhase = econfig.getExecutionPhase();
        if (executionPhase == phase || executionPhase == Event.Phase.ANY) {
            for (EventListener<T> listener : econfig.getCachedEventListeners()) {
                listener.execute(event);
            }
        }
    }

    public <T extends UIComponent> T createUIComponent(Class<T> type, String configId, String componentId, UIComponent parent)
            throws Exception {
        T uicomp = createUIComponent(type, configId, componentId);
        uicomp.setParent(parent);
        return uicomp;
    }

    public <T extends UIComponent> T createUIComponent(Class<T> type, String configId, String componentId) throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        return createUIComponent(context, type, configId, componentId);
    }

    public <T extends UIComponent> T createUIComponent(WebuiRequestContext context, Class<T> type, String configId,
            String componentId) throws Exception {
        WebuiApplication app = (WebuiApplication) context.getApplication();
        T comp = app.createUIComponent(type, configId, componentId, context);
        return comp;
    }

    @SuppressWarnings("unchecked")
    public <T extends UIComponent> T findComponentById(String lookupId) {
        if (getId().equals(lookupId)) {
            return (T) this;
        }
        return null;
    }

    public <T extends UIComponent> T findFirstComponentOfType(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return null;
    }

    public <T> void findComponentOfType(List<T> list, Class<T> type) {
        if (type.isInstance(this)) {
            list.add(type.cast(this));
        }
    }

    public <T extends UIComponent> void setRenderSibling(Class<T> type) {
        if (uiparent instanceof UIContainer) {
            UIContainer uicontainer = (UIContainer) uiparent;
            List<UIComponent> children = uicontainer.getChildren();
            for (UIComponent child : children) {
                if (type.isInstance(child)) {
                    child.setRendered(true);
                } else {
                    child.setRendered(false);
                }
            }
        }
    }

    public String getUIComponentName() {
        return UICOMPONENT;
    }

    public <T> T getApplicationComponent(Class<T> type) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        return type.cast(container.getComponentInstanceOfType(type));
    }

    public Event<UIComponent> createEvent(String name, Phase phase, WebuiRequestContext context) throws Exception {
        if (config == null) {
            return null;
        }
        org.exoplatform.webui.config.Event econfig = config.getUIComponentEventConfig(name);
        if (econfig == null) {
            return null;
        }
        Phase executionPhase = econfig.getExecutionPhase();
        if (executionPhase == phase || executionPhase == Event.Phase.ANY) {
            Event<UIComponent> event = new Event<UIComponent>(this, name, context);
            event.setExecutionPhase(phase);
            event.setEventListeners(econfig.getCachedEventListeners());
            event.setCsrfCheck(econfig.isCsrfCheck());
            return event;
        }
        return null;
    }

    private MonitorEvent<UIComponent> createMonitorEvent(Phase phase, WebuiRequestContext context) throws Exception {
        if (config == null) {
            return null;
        }
        org.exoplatform.webui.config.Event econfig = config
                .getUIComponentEventConfig(MonitorEvent.UICOMPONENT_LIFECYCLE_MONITOR_EVENT);
        if (econfig == null) {
            return null;
        }
        Phase executionPhase = econfig.getExecutionPhase();
        if (executionPhase == phase || executionPhase == Event.Phase.ANY) {
            MonitorEvent<UIComponent> mevent = new MonitorEvent<UIComponent>(this,
                    MonitorEvent.UICOMPONENT_LIFECYCLE_MONITOR_EVENT, context);
            mevent.setEventListeners(econfig.getCachedEventListeners());
            mevent.setStartExecutionTime(System.currentTimeMillis());
            mevent.setExecutionPhase(phase);
            return mevent;
        }
        return null;
    }
}

/*
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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.portlet.PortletConfig;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.pc.ExoKernelIntegration;
import org.exoplatform.resolver.ApplicationResourceResolver;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.resources.PortletConfigRegistry;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SAS May 7, 2006
 *
 * The main class to manage the request context in a webui environment
 *
 * It adds: - some access to the root UI component (UIApplication) - access to the request and response objects - information
 * about the current state of the request - the list of object to be updated in an AJAX way - an access to the ResourceResolver
 * bound to an uri scheme - the reference on the StateManager object
 */
public abstract class WebuiRequestContext extends RequestContext {

    public static final char NAME_DELIMITER = '-';

    protected UIApplication uiApplication_;

    protected String sessionId_;

    protected ResourceBundle appRes_;

    private StateManager stateManager_;

    private boolean responseComplete_ = false;

    private boolean processRender_ = false;

    private Set<UIComponent> uicomponentToUpdateByAjax;

    public WebuiRequestContext(Application app) {
        super(app);
    }

    protected WebuiRequestContext(RequestContext parentAppRequestContext, Application app_) {
        super(parentAppRequestContext, app_);
    }

    public String getSessionId() {
        return sessionId_;
    }

    protected void setSessionId(String id) {
        sessionId_ = id;
    }

    public UIApplication getUIApplication() {
        return uiApplication_;
    }

    public void setUIApplication(UIApplication uiApplication) throws Exception {
        uiApplication_ = uiApplication;
        appRes_ = null;
    }

    public ResourceBundle getApplicationResourceBundle() {
        if (appRes_ == null) {
            try {
                appRes_ = findApplicationResourceBundle();
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        }
        return appRes_;
    }

    /**
     * Dirty fix for GTNPORTAL-2700. When GTNPORTAL-2700 is solved in {@link ExoKernelIntegration}. This method can be changed
     * to always return {@code getApplication().getResourceBundle(locale)}
     *
     * @return
     * @throws Exception
     */
    protected ResourceBundle findApplicationResourceBundle() throws Exception {
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        PortletConfigRegistry registry = (PortletConfigRegistry) container
                .getComponentInstanceOfType(PortletConfigRegistry.class);
        String portlet = getApplication().getApplicationName();
        PortletConfig config = registry.getPortletConfig(portlet);
        Locale locale = getLocale();
        if (config != null) {
            ResourceBundle result = config.getResourceBundle(locale);
            if (result != null) {
                return result;
            }
        }
        return getApplication().getResourceBundle(locale);
    }

    public String getActionParameterName() {
        return WebuiRequestContext.ACTION;
    }

    public String getUIComponentIdParameterName() {
        return UIComponent.UICOMPONENT;
    }

    @Override
    public abstract URLBuilder<UIComponent> getURLBuilder();

    public abstract String getRequestContextPath();

    /**
     * Returns the context path of the portal or null if it does not execute in the context of an aggregated portal request.
     *
     * @return the portal context path
     */
    public abstract String getPortalContextPath();

    public abstract <T> T getRequest();

    public abstract <T> T getResponse();

    public boolean isResponseComplete() {
        return responseComplete_;
    }

    public void setResponseComplete(boolean b) {
        responseComplete_ = b;
    }

    public abstract void sendRedirect(String url) throws Exception;

    public boolean getProcessRender() {
        return processRender_;
    }

    public void setProcessRender(boolean b) {
        processRender_ = b;
    }

    public Set<UIComponent> getUIComponentToUpdateByAjax() {
        return uicomponentToUpdateByAjax;
    }

    public void addUIComponentToUpdateByAjax(UIComponent uicomponent) {
        if (uicomponentToUpdateByAjax == null) {
            uicomponentToUpdateByAjax = new LinkedHashSet<UIComponent>();
        }
        uicomponentToUpdateByAjax.add(uicomponent);
    }

    public ResourceResolver getResourceResolver(String uri) {
        Application app = getApplication();
        RequestContext pcontext = this;
        while (app != null) {
            ApplicationResourceResolver appResolver = app.getResourceResolver();
            ResourceResolver resolver = appResolver.getResourceResolver(uri);
            if (resolver != null) {
                return resolver;
            }
            pcontext = pcontext.getParentAppRequestContext();
            if (pcontext != null) {
                app = pcontext.getApplication();
            } else {
                app = null;
            }
        }
        return null;
    }

    public StateManager getStateManager() {
        return stateManager_;
    }

    public void setStateManager(StateManager manager) {
        stateManager_ = manager;
    }

    public JavascriptManager getJavascriptManager() {
        // Yes nasty cast
        return ((WebuiRequestContext) getParentAppRequestContext()).getJavascriptManager();
    }

    public static String generateUUID(String prefix) {
        String uuid = UUID.randomUUID().toString();
        /* The following is equivalent to prefix.length() + 1 + uuid.length() - 4
         * where
         *  + 1 is for the additional minus and
         *  -4 is for the number of minus signs removed from uuid
         *    you may want to look into the source of UUID.toString() to see that there are 4
         *    minus signs in a default UUID */
        int uuidLen = uuid.length();
        StringBuilder result = new StringBuilder(prefix.length() + uuidLen  - 3);
        result.append(prefix).append(NAME_DELIMITER);
        for (int i = 0; i < uuidLen; i++) {
            char ch = uuid.charAt(i);
            if (ch != NAME_DELIMITER) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static String stripUUIDSuffix(String name) {
        int lastMinus = name.lastIndexOf(NAME_DELIMITER);
        if (lastMinus >= 0) {
            return name.substring(0, lastMinus);
        } else {
            return name;
        }
    }
}

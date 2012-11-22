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

package org.exoplatform.web.application;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.resolver.ApplicationResourceResolver;

/**
 * Created by The eXo Platform SAS May 7, 2006
 */
public abstract class Application extends BaseComponentPlugin {

    public static final String JSR168_APPLICATION_TYPE = "jsr168Application";

    public static final String EXO_PORTLET_TYPE = "portlet";

    public static final String EXO_PORTAL_TYPE = "eXoPortal";

    public static final String EXO_GADGET_TYPE = "eXoGadget";

    public static final String WSRP_TYPE = "wsrp";

    private List<ApplicationLifecycle> lifecycleListeners_;

    private ApplicationResourceResolver resourceResolver_;

    private Hashtable<String, Object> attributes_ = new Hashtable<String, Object>();

    public abstract String getApplicationId();

    public abstract String getApplicationType();

    public abstract String getApplicationGroup();

    public abstract String getApplicationName();

    public final ApplicationResourceResolver getResourceResolver() {
        return resourceResolver_;
    }

    public final void setResourceResolver(ApplicationResourceResolver resolver) {
        resourceResolver_ = resolver;
    }

    public final Object getAttribute(String name) {
        return attributes_.get(name);
    }

    public final void setAttribute(String name, Object value) {
        attributes_.put(name, value);
    }

    public abstract ResourceBundle getResourceBundle(Locale locale);

    public abstract ResourceBundle getOwnerResourceBundle(String username, Locale locale);

    public ExoContainer getApplicationServiceContainer() {
        return ExoContainerContext.getCurrentContainer();
    }

    public final List<ApplicationLifecycle> getApplicationLifecycle() {
        return lifecycleListeners_;
    }

    public final void setApplicationLifecycle(List<ApplicationLifecycle> list) {
        lifecycleListeners_ = list;
    }

    public void onInit() throws Exception {
        for (ApplicationLifecycle lifecycle : lifecycleListeners_) {
            lifecycle.onInit(this);
        }
    }

    public void onDestroy() throws Exception {
        for (ApplicationLifecycle lifecycle : lifecycleListeners_)
            lifecycle.onDestroy(this);
    }
}

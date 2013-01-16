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

package org.exoplatform.portal.application;

import java.util.Locale;

import org.gatein.portal.mop.site.SiteType;
import org.exoplatform.portal.url.StandaloneAppURLContext;
import org.exoplatform.portal.webui.application.UIStandaloneAppContainer;
import org.exoplatform.portal.webui.workspace.UIStandaloneApplication;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLFactory;
import org.exoplatform.webui.core.UIApplication;

public class StandaloneAppRequestContext extends PortalRequestContext {
    public StandaloneAppRequestContext(StandaloneApplication app, ControllerContext controllerContext, String siteName,
            String requestPath) throws Exception {
        super(app, controllerContext, SiteType.USER.name(), siteName, requestPath, null);
    }

    @Override
    public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
        StandaloneAppURLContext context = new StandaloneAppURLContext(getControllerContext());
        U url = urlFactory.newURL(resourceType, context);
        if (url != null) {
            url.setAjax(false);
        }
        return url;
    }

    public String getStorageId() {
        return getNodePath();
    }

    public String getTitle() throws Exception {
        String title = null;
        UIApplication uiApp = getUIApplication();
        UIStandaloneAppContainer container = uiApp.getChild(UIStandaloneAppContainer.class);
        if (container != null) {
            title = container.getCurrAppName();
        }

        if (title == null) {
            title = "";
        }
        return title;
    }

    public Orientation getOrientation() {
        return ((UIStandaloneApplication) uiApplication_).getOrientation();
    }

    public Locale getLocale() {
        return ((UIStandaloneApplication) uiApplication_).getLocale();
    }

    public String getPortalOwner() {
        return null;
    }
}

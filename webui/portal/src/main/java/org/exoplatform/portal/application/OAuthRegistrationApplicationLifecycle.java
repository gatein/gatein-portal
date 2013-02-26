/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

import org.exoplatform.portal.webui.register.UIRegisterOAuth;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthRegistrationApplicationLifecycle implements ApplicationLifecycle<PortalRequestContext> {

    /** . */
    private final Logger log = LoggerFactory.getLogger(OAuthRegistrationApplicationLifecycle.class);

    public void onInit(Application app) throws Exception {
    }

    public void onStartRequest(Application app, PortalRequestContext context) throws Exception {
        if (context.getRequestURI().contains("/facebookRegister")) {
            System.out.println("Facebook register is here!!!! Starting to process");
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            UIComponent uiLogin = uiMaskWS.createUIComponent(UIRegisterOAuth.class, null, null);
            uiMaskWS.setUIComponent(uiLogin);
            Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
        }
    }

    public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) {
    }

    public void onEndRequest(Application app, PortalRequestContext context) throws Exception {
    }

    public void onDestroy(Application app) throws Exception {
    }
}

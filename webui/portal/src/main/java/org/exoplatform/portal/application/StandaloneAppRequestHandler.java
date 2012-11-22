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

import java.util.ServiceLoader;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;

public class StandaloneAppRequestHandler extends PortalRequestHandler {
    private static StandaloneApplicationFactory appProvider;

    private String webuiConfigPath;

    static {
        ServiceLoader<StandaloneApplicationFactory> loader = ServiceLoader.load(StandaloneApplicationFactory.class);
        for (StandaloneApplicationFactory factory : loader) {
            // We are expecting only one
            appProvider = factory;
            break;
        }
    }

    public StandaloneAppRequestHandler(InitParams params) {
        ValueParam valueParam = params.getValueParam("webui.configuration");
        if (valueParam != null) {
            webuiConfigPath = valueParam.getValue();
        }
    }

    @Override
    public String getHandlerName() {
        return "standalone";
    }

    @Override
    public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
        StandaloneApplication application;
        if (appProvider != null) {
            application = appProvider.createApplication(sConfig);
        } else {
            application = new StandaloneApplication(sConfig);
        }
        application.setWebUIConfigPath(webuiConfigPath);
        application.onInit();
        controller.addApplication(application);
    }

    @Override
    public boolean execute(ControllerContext controllerContext) throws Exception {
        HttpServletRequest req = controllerContext.getRequest();
        HttpServletResponse res = controllerContext.getResponse();

        log.debug("Session ID = " + req.getSession().getId());
        res.setHeader("Cache-Control", "no-cache");

        //
        String siteName = req.getRemoteUser();
        String requestPath = controllerContext.getParameter(REQUEST_PATH);

        StandaloneApplication app = controllerContext.getController().getApplication(
                StandaloneApplication.STANDALONE_APPLICATION_ID);
        StandaloneAppRequestContext context = new StandaloneAppRequestContext(app, controllerContext, siteName == null ? ""
                : siteName, requestPath);

        if (req.getRemoteUser() == null) {
            context.requestAuthenticationLogin();
        } else {
            processRequest(context, app);
        }
        return true;
    }
}

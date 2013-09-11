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

import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.StaleModelException;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.ApplicationRequestPhaseLifecycle;
import org.exoplatform.web.application.Phase;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;

/**
 * Created by The eXo Platform SAS Dec 9, 2006
 *
 * This class handle the request that target the portal paths /public and /private
 *
 */
public class PortalRequestHandler extends WebRequestHandler {

    protected static Log log = ExoLogger.getLogger("portal:PortalRequestHandler");

    private static PortalApplicationFactory appProvider;

    /** . */
    public static final QualifiedName REQUEST_PATH = QualifiedName.create("gtn", "path");

    /** . */
    public static final QualifiedName REQUEST_SITE_TYPE = QualifiedName.create("gtn", "sitetype");

    /** . */
    public static final QualifiedName REQUEST_SITE_NAME = QualifiedName.create("gtn", "sitename");

    /** . */
    public static final QualifiedName LANG = QualifiedName.create("gtn", "lang");

    static {
        ServiceLoader<PortalApplicationFactory> loader = ServiceLoader.load(PortalApplicationFactory.class);
        for (PortalApplicationFactory factory : loader) {
            // We are expecting only one
            appProvider = factory;
            break;
        }
    }

    public String getHandlerName() {
        return "portal";
    }

    @Override
    public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
        PortalApplication application;
        if (appProvider != null) {
            application = appProvider.createApplication(sConfig);
        } else {
            application = new PortalApplication(sConfig);
        }
        application.onInit();
        controller.addApplication(application);
    }

    /**
     * This method will handle incoming portal request. It gets a reference to the WebAppController
     *
     * Here are the steps done in the method:
     *
     * 1) set the header Cache-Control to no-cache 2) Get the PortalApplication reference from the controller 3) Create a
     * PortalRequestContext object that is a convenient wrapper on all the request information 4) Set that context in a
     * ThreadLocal to easily access it 5) Get the collection of ApplicationLifecycle referenced in the PortalApplication and
     * defined in the webui-configuration.xml of the portal application 6) Call onStartRequest() on each ApplicationLifecycle
     * object 7) Get the StateManager object from the PortalApplication (also referenced in the XML file) 8) Use the
     * StateManager to get a reference on the root UI component: UIApplication; the method used is
     * restoreUIRootComponent(context) 9) If the UI component is not the current one in used in the PortalContextRequest, then
     * replace it 10) Process decode on the PortalApplication 11) Process Action on the PortalApplication 12) Process Render on
     * the UIApplication UI component 11) call onEndRequest on all the ApplicationLifecycle 12) Release the context from the
     * thread
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(ControllerContext controllerContext) throws Exception {
        HttpServletRequest req = controllerContext.getRequest();
        HttpServletResponse res = controllerContext.getResponse();

        log.debug("Session ID = " + req.getSession().getId());
        res.setHeader("Cache-Control", "no-cache");

        //
        String requestPath = controllerContext.getParameter(REQUEST_PATH);
        String requestSiteType = controllerContext.getParameter(REQUEST_SITE_TYPE);
        String requestSiteName = controllerContext.getParameter(REQUEST_SITE_NAME);

        //
        Locale requestLocale;
        String lang = controllerContext.getParameter(LANG);
        if (lang == null || lang.length() == 0) {
            requestLocale = null;
        } else {
            requestLocale = I18N.parseTagIdentifier(lang);
        }

        if (requestSiteName == null) {
            res.sendRedirect(req.getContextPath());
            return true;
        }

        PortalApplication app = controllerContext.getController().getApplication(PortalApplication.PORTAL_APPLICATION_ID);
        PortalRequestContext context = new PortalRequestContext(app, controllerContext, requestSiteType, requestSiteName,
                requestPath, requestLocale);
        if (context.getUserPortalConfig() == null) {
            DataStorage storage = (DataStorage) PortalContainer.getComponent(DataStorage.class);
            PortalConfig persistentPortalConfig = storage.getPortalConfig(requestSiteType, requestSiteName);
            if (persistentPortalConfig == null) {
                return false;
            } else if (req.getRemoteUser() == null) {
                context.requestAuthenticationLogin();
            } else {
                context.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            processRequest(context, app);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected void processRequest(PortalRequestContext context, PortalApplication app) throws Exception {
        WebuiRequestContext.setCurrentInstance(context);
        UIApplication uiApp = app.getStateManager().restoreUIRootComponent(context);

        List<ApplicationLifecycle> lifecycles = app.getApplicationLifecycle();
        try {
            if (context.getUIApplication() != uiApp)
                context.setUIApplication(uiApp);
            for (ApplicationLifecycle lifecycle : lifecycles)
                lifecycle.onStartRequest(app, context);

            if (uiApp != null) {
                uiApp.processDecode(context);
            }

            if (!context.isResponseComplete() && !context.getProcessRender()) {
                startRequestPhaseLifecycle(app, context, lifecycles, Phase.ACTION);
                uiApp.processAction(context);
                endRequestPhaseLifecycle(app, context, lifecycles, Phase.ACTION);
            }

            if (!context.isResponseComplete()) {
                startRequestPhaseLifecycle(app, context, lifecycles, Phase.RENDER);
                uiApp.processRender(context);
                endRequestPhaseLifecycle(app, context, lifecycles, Phase.RENDER);
            }

            if (uiApp != null)
                uiApp.setLastAccessApplication(System.currentTimeMillis());

            // Store ui root
            app.getStateManager().storeUIRootComponent(context);
        } catch (StaleModelException staleModelEx) {
            // Minh Hoang TO:
            // At the moment, this catch block is never reached, as the StaleModelException is intercepted temporarily
            // in UI-related code
            for (ApplicationLifecycle lifecycle : lifecycles) {
                lifecycle.onFailRequest(app, context, RequestFailure.CONCURRENCY_FAILURE);
            }
        } catch (Exception NonStaleModelEx) {
            log.error("Error while handling request", NonStaleModelEx);
        } finally {

            // We close the writer here once and for all
            Safe.close(context.getWriter());

            //
            try {
                for (ApplicationLifecycle lifecycle : lifecycles)
                    lifecycle.onEndRequest(app, context);
            } catch (Exception exception) {
                log.error("Error while ending request on all ApplicationLifecycle", exception);
            }
            WebuiRequestContext.setCurrentInstance(null);
        }
    }

    private void startRequestPhaseLifecycle(PortalApplication app, PortalRequestContext context,
            List<ApplicationLifecycle> lifecycles, Phase phase) {
        for (ApplicationLifecycle lifecycle : lifecycles) {
            if (lifecycle instanceof ApplicationRequestPhaseLifecycle)
                ((ApplicationRequestPhaseLifecycle) lifecycle).onStartRequestPhase(app, context, phase);
        }
    }

    private void endRequestPhaseLifecycle(PortalApplication app, PortalRequestContext context,
            List<ApplicationLifecycle> lifecycles, Phase phase) {
        for (ApplicationLifecycle lifecycle : lifecycles) {
            if (lifecycle instanceof ApplicationRequestPhaseLifecycle)
                ((ApplicationRequestPhaseLifecycle) lifecycle).onEndRequestPhase(app, context, phase);
        }
    }

    @Override
    protected boolean getRequiresLifeCycle() {
        return true;
    }
}

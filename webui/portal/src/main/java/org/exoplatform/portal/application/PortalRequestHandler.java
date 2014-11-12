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
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.StaleModelException;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
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
 * This is 'portal' handler, it handle the request of URLs that are routed by navigation controller (using urls parameter).
 * This handler is registered to WebAppController by xml configuration.
 * @see WebAppController
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

    /** Used to sanitize the CacheControl HTTP header */
    private static final Pattern CACHE_CONTROL_SANITIZE_PATTERN = Pattern.compile("[\\r\\n]");

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

    /**
     * Dispatched from WebAppController, after the portal servlet init function called, this method create and register
     * PortalApplication to WebAppController
     *
     * PortalApplication creation can be customized by registering PortalApplicationFactory implementation using ServiceLoader
     *
     * @see PortalApplication
     */
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
     * 1) Get the PortalApplication reference from the controller <br/>
     * 2) Create a PortalRequestContext object that is a convenient wrapper on all the request information <br/>
     * 3) Get the collection of ApplicationLifecycle referenced in the PortalApplication and defined in
     * the webui-configuration.xml of the portal application <br/>
     * 4) Set that context in a ThreadLocal to easily access it <br/>
     * 5) Check if user have permission to access portal, if not, send 403 status code,
     * if user has not login, redirect to login page <br/>
     * 6) dispatch to processRequest method, this is protected method, we can extend and override this method to
     * write a new requestHandler base on PortalRequestHandler (@see {@link StandaloneAppRequestHandler})
     * 7) set the header Cache-Control to no-cache or to the value specified in the portal configuration <br/>
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(ControllerContext controllerContext) throws Exception {
        HttpServletRequest req = controllerContext.getRequest();
        HttpServletResponse res = controllerContext.getResponse();

        log.debug("Session ID = " + req.getSession().getId());

        // watch out: this might get overriden later, if the portal itself has a configuration for this value
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

        DataStorage storage = (DataStorage) PortalContainer.getComponent(DataStorage.class);
        PortalConfig persistentPortalConfig = storage.getPortalConfig(requestSiteType, requestSiteName);

        if (context.getUserPortalConfig() == null) {
            if (persistentPortalConfig == null) {
                return false;
            } else if (req.getRemoteUser() == null) {
                context.requestAuthenticationLogin();
            } else {
                context.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            if (persistentPortalConfig != null) {
                String cacheControl = persistentPortalConfig.getProperty(PortalProperties.CACHE_CONTROL);
                if (cacheControl != null) {
                    // GTNPORTAL-3361
                    // Previously, the Cache-Control was set to no-cache at all times, the reason for that being unclear.
                    // A feature request to allow portals to set their own policy caused this change, but we might
                    // revert if there are bad side-effects. If so, please replace this comment with the background information,
                    // so that it gets documented why the no-cache setting is forced.
                    res.setHeader("Cache-Control", getSanitizedCacheControl(cacheControl));
                }
            }
            processRequest(context, app);
        }

        return true;
    }

    /**
     * This method do the main job on processing a portal request:
     *
     * 1) Call onStartRequest() on each ApplicationLifecycle object <br/>
     * 2) Get the StateManager object from the PortalApplication (also referenced in the XML file) <br/>
     * 3) Use the StateManager to get a reference on the root UI component: UIApplication;
     * the method used is restoreUIRootComponent(context) <br/>
     * 4) If the UI component is not the current one in used in the PortalContextRequest, then replace it <br/>
     * 5) Process decode on the PortalApplication <br/>
     * 6) Process Action on the PortalApplication <br/>
     * 7) Process Render on the UIApplication UI component <br/>
     * 8) call onEndRequest on all the ApplicationLifecycle <br/>
     * 9) Release the context from the thread
     *
     * @param context
     * @param app
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void processRequest(PortalRequestContext context, PortalApplication app) throws Exception {
        WebuiRequestContext.setCurrentInstance(context);
        PortalRequestImpl.createInstance(context);

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
            PortalRequestImpl.clearInstance();
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

    public String getSanitizedCacheControl(String cacheControl) {
        if (null == cacheControl) {
            return null;
        }
        return CACHE_CONTROL_SANITIZE_PATTERN.matcher(cacheControl).replaceAll("");
    }
}

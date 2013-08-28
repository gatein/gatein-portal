/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.web.application.javascript;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;

/**
 * An listener for listening the ADDED and REMOVED events of the webapp to deploy/undeploy Javascript configured in
 * <code>/WEB-INF/gatein-resources.xml</code> file.
 *
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class JavascriptConfigDeployer implements WebAppListener {

    public static final String GATEIN_CONFIG_RESOURCE = "/WEB-INF/gatein-resources.xml";

    /**
     * Logger
     */
    private static final Log LOG = ExoLogger.getLogger(JavascriptConfigDeployer.class);

    /** . */
    private final JavascriptConfigService javascriptService;

    /**
     * The name of the portal container
     */
    private final String portalContainerName;

    public JavascriptConfigDeployer(String portalContainerName, JavascriptConfigService javascriptService) {
        this.javascriptService = javascriptService;
        this.portalContainerName = portalContainerName;
    }

    public void onEvent(WebAppEvent event) {
        if (event instanceof WebAppLifeCycleEvent) {
            WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent) event;
            ServletContext servletContext = lifeCycleEvent.getWebApp().getServletContext();
            switch (lifeCycleEvent.getType()) {
                case WebAppLifeCycleEvent.ADDED:
                    add(event.getWebApp());
                    break;
                case WebAppLifeCycleEvent.REMOVED:
                    remove(event.getWebApp());
                    break;
            }
        }
    }

    private void add(final WebApp webApp) {
        try {
            InputStream is = webApp.getServletContext().getResourceAsStream(GATEIN_CONFIG_RESOURCE);
            if (is == null) {
                return;
            }

            Safe.close(is);

            final PortalContainerPostInitTask task = new PortalContainerPostInitTask() {

                public void execute(ServletContext scontext, PortalContainer portalContainer) {
                    register(scontext, portalContainer);
                    javascriptService.registerContext(webApp);
                }
            };
            PortalContainer.addInitTask(webApp.getServletContext(), task, portalContainerName);
        } catch (Exception ex) {
            LOG.error(
                    "An error occurs while registering 'Javascript in gatein-resources.xml' from the context '"
                            + (webApp.getServletContext() == null ? "unknown" : webApp.getServletContext()
                                    .getServletContextName()) + "'", ex);
        }
    }

    private void remove(WebApp webApp) {
        javascriptService.unregisterServletContext(webApp);
        try {
            JavascriptConfigParser.unregisterResources(javascriptService, webApp.getServletContext());
        } catch (Exception ex) {
            LOG.error(
                "An error occured while removing script resources for the context '"
                    + webApp.getServletContext().getServletContextName() + "'", ex);
        }
    }

    private void register(ServletContext scontext, PortalContainer container) {
        InputStream is = null;
        try {
            is = scontext.getResourceAsStream(GATEIN_CONFIG_RESOURCE);
            JavascriptConfigParser.processConfigResource(is, javascriptService, scontext);
        } catch (Exception ex) {
            LOG.error(
                    "An error occurs while processing 'Javascript in gatein-resources.xml' from the context '"
                            + scontext.getServletContextName() + "'", ex);
        } finally {
            Safe.close(is);
        }
    }
}

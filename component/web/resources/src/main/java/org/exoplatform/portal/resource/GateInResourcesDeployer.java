/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.portal.resource.config.tasks.SkinConfigTask;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.application.javascript.ScriptResources;
import org.exoplatform.web.application.javascript.ScriptResources.ImmutableScriptResources;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class GateInResourcesDeployer implements WebAppListener {
    private static final Logger log = LoggerFactory.getLogger(GateInResourcesDeployer.class);

    /** Path to {@code gatein-resources.xml} */
    public static final String GATEIN_CONFIG_RESOURCE = "/WEB-INF/gatein-resources.xml";

    /** A key used to store a collection of JavaScript resources loaded from a given servlet context. */
    public static final String SCRIPT_RESOURCES_ATTR = "gatein.script.resources";

    /** The {@link SkinService} */
    private final SkinService skinService;

    /** The {@link JavascriptConfigService} */
    private final JavascriptConfigService javascriptConfigService;

    /** The name of the portal container */
    private final String portalContainerName;

    /**
     * @param portalContainerName
     * @param skinService
     * @param javascriptConfigService
     */
    public GateInResourcesDeployer(String portalContainerName, SkinService skinService, JavascriptConfigService javascriptConfigService) {
        super();
        this.portalContainerName = portalContainerName;
        this.skinService = skinService;
        this.javascriptConfigService = javascriptConfigService;
    }

    /**
     * @see org.gatein.wci.WebAppListener#onEvent(org.gatein.wci.WebAppEvent)
     */
    public void onEvent(WebAppEvent event) {
        if (event instanceof WebAppLifeCycleEvent) {
            WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent) event;
            WebApp webApp = null;
            URL url = null;
            switch (lifeCycleEvent.getType()) {
                case WebAppLifeCycleEvent.ADDED:
                    webApp = event.getWebApp();
                    url = getGateinResourcesXml(webApp);
                    if (url != null) {
                        add(webApp, url);
                    }
                    break;
                case WebAppLifeCycleEvent.REMOVED:
                    webApp = event.getWebApp();
                    url = getGateinResourcesXml(webApp);
                    if (url != null) {
                        remove(event.getWebApp());
                    }
                    break;
            }
        }
    }

    /**
     * Called on web application add event if the application contains {@value #GATEIN_CONFIG_RESOURCE} file.
     *
     * @param webApp
     * @param url
     */
    protected void add(final WebApp webApp, final URL url) {

        ServletContext scontext = webApp.getServletContext();
        try {
            /* Validate straight away here before creating the PortalContainerPostInitTask */
            final Document document = GateInResourcesSchemaValidator.validate(url);
            /* Also parse both js and skin resources before creating the PortalContainerPostInitTask */
            final ScriptResources scriptResources = new JavascriptConfigParser(scontext, document).parse();
            final List<SkinConfigTask> skinTasks = SkinConfigParser.parse(document);

            /* No exceptions at this point */
            final PortalContainerPostInitTask task = new PortalContainerPostInitTask() {
                public void execute(ServletContext scontext, PortalContainer portalContainer) {

                    try {

                        if (!scriptResources.isEmpty()) {
                            javascriptConfigService.add(scriptResources);
                            scontext.setAttribute(SCRIPT_RESOURCES_ATTR, scriptResources.toImmutable());
                        }
                        javascriptConfigService.registerContext(webApp);

                        if (skinTasks != null && !skinTasks.isEmpty()) {
                            skinService.addSkins(skinTasks, scontext);
                        }
                        skinService.registerContext(webApp);

                    } catch (Exception e) {
                        log.error("Could not register script and skin resources from the context '"
                                        + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", e);

                        /* try to cleanup if anything went wrong */
                        try {
                            remove(webApp);
                        } catch (Exception e1) {
                            log.error("Could not cleanup script and skin resources from the context '"
                                    + (scontext == null ? "unknown" : scontext.getServletContextName()) + "' after a registration failure", e);
                        }
                    }

                }
            };
            PortalContainer.addInitTask(scontext, task, portalContainerName);
        } catch (Exception ex) {
            log.error(
                    "Could not parse or validate gatein-resources.xml in context '"
                            + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
        }

    }

    /**
     * Called on web application remove event if the application contains {@value #GATEIN_CONFIG_RESOURCE} file.
     *
     * @param webApp
     */
    protected void remove(WebApp webApp) {
        javascriptConfigService.unregisterServletContext(webApp);
        try {
            ServletContext scontext = webApp.getServletContext();
            ImmutableScriptResources scriptResources = (ImmutableScriptResources) scontext.getAttribute(SCRIPT_RESOURCES_ATTR);
            if (scriptResources != null) {
                javascriptConfigService.remove(scriptResources);
                scontext.removeAttribute(SCRIPT_RESOURCES_ATTR);
            }
        } catch (Exception ex) {
            log.error(
                "An error occurred while removing script resources for the context '"
                    + webApp.getServletContext().getServletContextName() + "'", ex);
        }

        String contextPath = webApp.getServletContext().getContextPath();
        try {
            skinService.removeSkins(SkinDependentManager.getPortalSkins(contextPath));
            skinService.removeSkins(SkinDependentManager.getPortletSkins(contextPath));

            /*
             * Remove skinName defined by the webApp, if no other webApps supports the skinName
             */
            Set<String> supportedSkins = SkinDependentManager.getSkinNames(contextPath);
            if (supportedSkins != null) {
                for (String skin : supportedSkins) {
                    if (SkinDependentManager.skinNameIsRemovable(skin, contextPath)) {
                        skinService.removeSupportedSkin(skin);
                    }
                }
            }

            // Update the 'skinDependentManager'
            SkinDependentManager.clearAssociatedSkins(contextPath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        skinService.unregisterServletContext(webApp);
    }

    protected URL getGateinResourcesXml(final WebApp webApp) {
        try {
            return webApp.getServletContext().getResource(GATEIN_CONFIG_RESOURCE);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}

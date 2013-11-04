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

package org.exoplatform.portal.resource;

import java.net.URL;
import java.util.Set;

import javax.servlet.ServletContext;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebApp;

/**
 *
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 * Sep 16, 2009
 */
public class GateInSkinConfigDeployer extends AbstractResourceDeployer {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(GateInSkinConfigDeployer.class);

    /** . */
    private final SkinService skinService;

    /**
     * The name of the portal container
     */
    private final String portalContainerName;

    public GateInSkinConfigDeployer(String portalContainerName, SkinService _skinService) {
        this.skinService = _skinService;
        this.portalContainerName = portalContainerName;
    }

    /**
     * @see org.exoplatform.portal.resource.AbstractResourceDeployer#add(org.gatein.wci.WebApp)
     */
    @Override
    protected void add(final WebApp webApp, final URL url) {
        ServletContext scontext = null;
        try {
            scontext = webApp.getServletContext();
            final PortalContainerPostInitTask task = new PortalContainerPostInitTask() {
                public void execute(ServletContext scontext, PortalContainer portalContainer) {
                    try {
                        SkinConfigParser.processConfigResource(DocumentSource.create(url), skinService, scontext);
                    } catch (Exception ex) {
                        log.error("An error occurs while registering '" + GATEIN_CONFIG_RESOURCE + "' from the context '"
                                + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
                    }
                    skinService.registerContext(webApp);
                }
            };
            PortalContainer.addInitTask(scontext, task, portalContainerName);
        } catch (Exception ex) {
            log.error("An error occurs while registering '" + GATEIN_CONFIG_RESOURCE + "' from the context '"
                    + (scontext == null ? "unknown" : scontext.getServletContextName()) + "'", ex);
        }
    }

    /**
     * @see org.exoplatform.portal.resource.AbstractResourceDeployer#remove(org.gatein.wci.WebApp)
     */
    @Override
    protected void remove(WebApp webApp) {
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

}

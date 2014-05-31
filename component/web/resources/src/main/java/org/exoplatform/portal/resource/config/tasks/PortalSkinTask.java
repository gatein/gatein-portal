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

package org.exoplatform.portal.resource.config.tasks;

import javax.servlet.ServletContext;

import org.exoplatform.portal.resource.SkinConfigParser;
import org.exoplatform.portal.resource.SkinDependentManager;
import org.exoplatform.portal.resource.SkinService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 * Sep 16, 2009
 */
public class PortalSkinTask extends AbstractSkinModule implements SkinConfigTask {

    private static final String DEFAULT_MODULE_NAME = "CoreSkin";

    private String moduleName;

    public PortalSkinTask() {
        super(SkinService.DEFAULT_SKIN);
        this.overwrite = true;
        this.moduleName = DEFAULT_MODULE_NAME;
    }

    private void bindingModuleName(Element element) {
        NodeList nodes = element.getElementsByTagName(SkinConfigParser.SKIN_MODULE_TAG);
        if (nodes == null || nodes.getLength() < 1) {
            return;
        }
        moduleName = nodes.item(0).getFirstChild().getNodeValue();
    }

    public void binding(Element elemt) {
        bindingCSSPath(elemt);
        bindingSkinName(elemt);
        bindingModuleName(elemt);
        bindingOverwrite(elemt);
        bindingCSSPriority(elemt);
    }

    public void execute(SkinService skinService, ServletContext scontext) {
        if (moduleName == null || skinName == null || cssPath == null) {
            return;
        }
        String contextPath = scontext.getContextPath();
        String fullCSSPath = contextPath + cssPath;
        int priority;
        try {
            priority = Integer.valueOf(cssPriority);
        } catch (Exception e) {
            priority = Integer.MAX_VALUE;
        }
        skinService.addPortalSkin(moduleName, skinName, fullCSSPath, priority, overwrite);
        updateSkinDependentManager(contextPath, moduleName, skinName);
    }

    /** Update skinDependentManager as it is needed to undeploy skin at runtime */
    private void updateSkinDependentManager(String webApp, String moduleName, String skinName) {
        SkinDependentManager.addPortalSkin(webApp, moduleName, skinName);
        SkinDependentManager.addSkinDeployedInApp(webApp, skinName);

        // Remark: Invoked only in PortalSkinTask
        SkinDependentManager.addDependentAppToSkinName(skinName, webApp);
    }
}

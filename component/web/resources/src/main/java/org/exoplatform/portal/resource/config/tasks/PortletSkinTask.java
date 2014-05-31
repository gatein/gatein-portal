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
public class PortletSkinTask extends AbstractSkinModule implements SkinConfigTask {

    private String applicationName;

    private String portletName;

    public PortletSkinTask() {
        super(SkinService.DEFAULT_SKIN);
        this.overwrite = true;
    }

    private void bindingApplicationName(Element element) {
        NodeList nodes = element.getElementsByTagName(SkinConfigParser.APPLICATION_NAME_TAG);
        if (nodes == null || nodes.getLength() < 1) {
            return;
        }
        String applicationName = nodes.item(0).getFirstChild().getNodeValue();
        setApplicationName(applicationName);
    }

    private void bindingPortletName(Element element) {
        NodeList nodes = element.getElementsByTagName(SkinConfigParser.PORTLET_NAME_TAG);
        if (nodes == null || nodes.getLength() < 1) {
            return;
        }
        String portletName = nodes.item(0).getFirstChild().getNodeValue();
        setPortletName(portletName);
    }

    public void setApplicationName(String _applicationName) {
        this.applicationName = _applicationName;
    }

    public void setPortletName(String _portletName) {
        this.portletName = _portletName;
    }

    public void execute(SkinService skinService, ServletContext scontext) {
        if (portletName == null || skinName == null || cssPath == null) {
            return;
        }
        if (applicationName == null) {
            applicationName = scontext.getContextPath();
        }
        String moduleName = applicationName + "/" + portletName;
        String contextPath = scontext.getContextPath();
        String fullCSSPath = contextPath + cssPath;
        int priority;
        try {
            priority = Integer.valueOf(cssPriority);
        } catch (Exception e) {
            priority = Integer.MAX_VALUE;
        }
        skinService.addSkin(moduleName, skinName, fullCSSPath, priority, overwrite);
        updateSkinDependentManager(contextPath, moduleName, skinName);
    }

    private void updateSkinDependentManager(String webApp, String moduleName, String skinName) {
        SkinDependentManager.addPortletSkin(webApp, moduleName, skinName);
        SkinDependentManager.addSkinDeployedInApp(webApp, skinName);
    }

    public void binding(Element elemt) {
        bindingApplicationName(elemt);
        bindingPortletName(elemt);
        bindingCSSPath(elemt);
        bindingSkinName(elemt);
        bindingOverwrite(elemt);
        bindingCSSPriority(elemt);
    }

}

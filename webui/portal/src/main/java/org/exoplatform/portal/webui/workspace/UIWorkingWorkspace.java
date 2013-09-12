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

package org.exoplatform.portal.webui.workspace;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 12, 2006
 */

@ComponentConfig(id = "UIWorkingWorkspace", template = "system:/groovy/portal/webui/workspace/UIWorkingWorkspace.gtmpl", events = {
        @EventConfig(listeners = UIMainActionListener.CreatePortalActionListener.class),
        @EventConfig(listeners = UIMainActionListener.PageCreationWizardActionListener.class),
        @EventConfig(listeners = UIMainActionListener.EditBackgroundActionListener.class),
        @EventConfig(listeners = UIMainActionListener.EditInlineActionListener.class),
        @EventConfig(listeners = UIMainActionListener.EditPageInFullPreviewActionListener.class)})
public class UIWorkingWorkspace extends UIContainer {

    private UIPortal backupUIPortal = null;

    public UIPortal getBackupUIPortal() {
        return backupUIPortal;
    }

    public void setBackupUIPortal(UIPortal uiPortal) {
        backupUIPortal = uiPortal;
    }

    public UIPortal restoreUIPortal() {
        UIPortal result = backupUIPortal;
        if (result == null) {
            throw new IllegalStateException("backupUIPortal not available");
        } else {
            UISiteBody siteBody = findFirstComponentOfType(UISiteBody.class);
            siteBody.setUIComponent(result);
            return result;
        }
    }

    public void updatePortletByWindowId(String windowId) {
        List<UIPortlet> portletInstancesInPage = new ArrayList<UIPortlet>();
        findComponentOfType(portletInstancesInPage, UIPortlet.class);

        for (UIPortlet portlet : portletInstancesInPage) {
            if (portlet.getWindowId().equals(windowId)) {
                Util.getPortalRequestContext().addUIComponentToUpdateByAjax(portlet);
            }
        }
    }

    public void updatePortletsByName(String portletName) {
        List<UIPortlet> portletInstancesInPage = new ArrayList<UIPortlet>();
        findComponentOfType(portletInstancesInPage, UIPortlet.class);

        for (UIPortlet portlet : portletInstancesInPage) {
            String applicationId = portlet.getApplicationId();
            ApplicationType<?> type = portlet.getState().getApplicationType();
            if (type == ApplicationType.PORTLET) {
                String[] chunks = Utils.split("/", applicationId);
                if (chunks[1].equals(portletName)) {
                    Util.getPortalRequestContext().addUIComponentToUpdateByAjax(portlet);
                }
            } else if (type == ApplicationType.GADGET) {
                if (applicationId.equals(portletName)) {
                    Util.getPortalRequestContext().addUIComponentToUpdateByAjax(portlet);
                }
            } else {
                throw new AssertionError("Need to handle wsrp case later");
            }
        }
    }
}

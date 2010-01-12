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

package org.exoplatform.toolbar.webui.component;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIAdminToolbarPortlet.gtmpl")
public class UIAdminToolbarPortlet extends UIPortletApplication
{
   // Minh Hoang TO
   // TODO: Add a ThreadLocal cache to avoid double invocation of editPermission
   // check ( one in processRender method, and one in Groovy template )

   public UIAdminToolbarPortlet() throws Exception
   {
   }

   public PageNavigation getSelectedNavigation() throws Exception
   {
      PageNavigation nav = Util.getUIPortal().getSelectedNavigation();
      if (nav != null)
         return nav;
      if (Util.getUIPortal().getNavigations().size() < 1)
         return null;
      return Util.getUIPortal().getNavigations().get(0);
   }

   @Override
   public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception
   {
      // A user could view the toolbar portlet iff he/she has edit permission
      // either on
      // 'active' page or 'active' portal
      if (hasEditPermissionOnNavigation() || hasEditPermissionOnPage())
      {
         super.processRender(app, context);
      }
   }

   private boolean hasEditPermissionOnNavigation() throws Exception
   {
      PageNavigation selectedNavigation = getSelectedNavigation();
      UIPortalApplication portalApp = Util.getUIPortalApplication();
      UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
      if (selectedNavigation == null || userACL == null)
      {
         return false;
      }
      else
      {
         return userACL.hasEditPermission(selectedNavigation);
      }
   }

   private boolean hasEditPermissionOnPage() throws Exception
   {
      UIPortalApplication portalApp = Util.getUIPortalApplication();
      UIWorkingWorkspace uiWorkingWS = portalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      UIPageBody pageBody = uiWorkingWS.findFirstComponentOfType(UIPageBody.class);
      UIPage uiPage = (UIPage)pageBody.getUIComponent();

      if (uiPage == null)
      {
         return false;
      }
      else
      {
         UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
         return userACL.hasEditPermissionOnPage(uiPage.getOwnerType(), uiPage.getOwnerId(), uiPage.getEditPermission());
      }
   }

}

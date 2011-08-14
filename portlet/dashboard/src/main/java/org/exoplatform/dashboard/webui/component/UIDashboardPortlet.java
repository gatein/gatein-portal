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

package org.exoplatform.dashboard.webui.component;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

import javax.portlet.PortletPreferences;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/dashboard/webui/component/UIDashboardPortlet.gtmpl", events = {})
public class UIDashboardPortlet extends UIPortletApplication implements DashboardParent
{
   public UIDashboardPortlet() throws Exception
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();

      UIDashboard dashboard = addChild(UIDashboard.class, null, null);
      addChild(UIDashboardEditForm.class, null, null);

      PortletPreferences pref = context.getRequest().getPreferences();
      String containerTemplate = pref.getValue("template", "three-columns");
      dashboard.setContainerTemplate(containerTemplate);

      String aggregatorId = pref.getValue("aggregatorId", "rssAggregator");
      dashboard.setAggregatorId(aggregatorId);
   }

   public int getNumberOfCols()
   {
      UIDashboardContainer dbCont = getChild(UIDashboard.class).getChild(UIDashboardContainer.class);
      return dbCont.getChild(UIContainer.class).getChildren().size();
   }

   /**
    * The implementation returns true if the current user has edit permission on the page owning the dashboard
    * portlet. Later it will be implemented with a finer granilarity.
    */
   public boolean canEdit()
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortalRequestContext prc = (PortalRequestContext)context.getParentAppRequestContext();
      UIPortalApplication portalApp = (UIPortalApplication)prc.getUIApplication();
      UIPortal portal = portalApp.getCurrentSite();

      //
      try
      {
         UserNode node = portal.getSelectedUserNode();
         if (node != null)
         {
            String pageRef = node.getPageRef();
            DataStorage storage = portal.getApplicationComponent(DataStorage.class);
            Page page = storage.getPage(pageRef);
            if (page != null)
            {
               UserACL userACL = portal.getApplicationComponent(UserACL.class);
               return userACL.hasPermission(page);
            }
         }
      }
      catch (Exception e)
      {
         log.error("Could not check dashboard edition" ,e);
      }


      //
      return false;
   }

}

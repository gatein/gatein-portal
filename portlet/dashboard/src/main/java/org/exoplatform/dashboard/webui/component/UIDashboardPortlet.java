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

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

import javax.portlet.PortletPreferences;

/**
 * set the event listeners.
 */
/**
 * @author exo
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/dashboard/webui/component/UIDashboardPortlet.gtmpl", events = {})
/**
 * Dashboard portlet that display google gadgets
 */
public class UIDashboardPortlet extends UIPortletApplication implements DashboardParent
{
   private boolean isPrivate;

   private String owner;

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

      isPrivate = pref.getValue(ISPRIVATE, "0").equals(1);
      owner = pref.getValue(OWNER, null);
   }

   public int getNumberOfCols()
   {
      UIDashboardContainer dbCont = getChild(UIDashboard.class).getChild(UIDashboardContainer.class);
      return dbCont.getChild(UIContainer.class).getChildren().size();
   }

   public boolean canEdit()
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      String accessUser = context.getRemoteUser();
      if (accessUser == null || accessUser.equals(""))
         return false;
      if ("__CURRENT_USER__".equals(owner))
      {
         return true;
      }
      if (isPrivate)
      {
         if (accessUser.equals(owner))
            return true;
      }
      return false;
   }

   public String getDashboardOwner()
   {
      if ("__CURRENT_USER__".equals(owner))
      {
         PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
         return context.getRemoteUser();
      }
      return owner;
   }

}

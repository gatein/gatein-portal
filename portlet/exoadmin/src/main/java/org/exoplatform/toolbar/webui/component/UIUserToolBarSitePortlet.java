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

import java.util.List;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarSitePortlet.gtmpl")
public class UIUserToolBarSitePortlet extends BasePartialUpdateToolbar
{

   public UIUserToolBarSitePortlet() throws Exception
   {
      UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
      builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      builder.withTemporalCheck();
      toolbarFilterConfig = builder.build();
   }

   public List<String> getAllPortalNames() throws Exception
   {
      UserPortalConfigService dataStorage = getApplicationComponent(UserPortalConfigService.class);
      return dataStorage.getAllPortalNames();
   }
   
   public String getPortalLabel(String portalName) throws Exception
   {
      DataStorage storage_ = getApplicationComponent(DataStorage.class);
      PortalConfig portalConfig = storage_.getPortalConfig(portalName);
      String label = portalConfig.getLabel();
      if (label != null && label.trim().length() > 0)
      {
         return label;
      }
      
      return portalName;
   }

   @Override
   protected UserNode getNodeFromResourceID(String resourceId) throws Exception
   {      
      UserNavigation currNav = getNavigation(SiteKey.portal(getCurrentPortal()));
      if (currNav == null) return null;
    
      UserPortal userPortal = getUserPortal(); 
      UserNode node = userPortal.resolvePath(currNav, toolbarFilterConfig, resourceId);
      if (node != null && node.getURI().equals(resourceId))
      {
         return node;
      }
      return null;
   }

   private String getCurrentPortal()
   {
      return Util.getPortalRequestContext().getPortalOwner();
   }

   @Override
   protected String getResourceIdFromNode(UserNode node, String navId) throws Exception
   {
      if (node == null) 
      {
         throw new IllegalArgumentException("node can't be null");
      }
      return node.getURI();      
   }
}

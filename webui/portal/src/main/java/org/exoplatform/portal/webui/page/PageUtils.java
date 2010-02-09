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

package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Feb 19, 2008  
 */
public class PageUtils
{

   /**
    * This method create new Page and PageNode from an existing page and add created PageNode to children of parentNode
    *
    */
   public static void createNodeFromPageTemplate(String nodeName, String nodeLabel, String pageId, PageNode parentNode)
      throws Exception
   {

      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      UserPortalConfigService configService = uiPortalApp.getApplicationComponent(UserPortalConfigService.class);
      String accessUser = Util.getPortalRequestContext().getRemoteUser();
      PageNode node =
         configService.createNodeFromPageTemplate(nodeName, nodeLabel, pageId, PortalConfig.USER_TYPE, accessUser);
      node.setUri(parentNode.getUri() + "/" + node.getName());
      if (parentNode.getChildren() == null)
         parentNode.setChildren(new ArrayList<PageNode>());
      parentNode.getChildren().add(node);
   }

   /**
    * This method create new Page and PageNode from an existing page and add created PageNode to user PageNavigation.
    * It also saves changes to database and UIPortal
    *
    */
   public static void createNodeFromPageTemplate(String nodeName, String nodeLabel, String pageId, PageNavigation navi)
      throws Exception
   {

      UIPortal uiPortal = Util.getUIPortal();
      UserPortalConfigService configService = uiPortal.getApplicationComponent(UserPortalConfigService.class);
      String accessUser = Util.getPortalRequestContext().getRemoteUser();
      PageNode node =
         configService.createNodeFromPageTemplate(nodeName, nodeLabel, pageId, PortalConfig.USER_TYPE, accessUser);

      node.setUri(node.getName());
      navi.addNode(node);
      
      DataStorage dataService = uiPortal.getApplicationComponent(DataStorage.class);
      dataService.save(navi);
      setNavigation(uiPortal.getNavigations(), navi);
   }

   private static void setNavigation(List<PageNavigation> navs, PageNavigation nav)
   {
      for (int i = 0; i < navs.size(); i++)
      {
         if (navs.get(i).getId() == nav.getId())
         {
            navs.set(i, nav);
            return;
         }
      }
   }

}

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

import java.util.Collection;

import javax.portlet.EventRequest;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl",
   events = {@EventConfig(name = "AddDefaultDashboard", listeners = UIUserToolBarDashboardPortlet.AddDashboardActionListener.class),
      @EventConfig(listeners = UIUserToolBarDashboardPortlet.NavigationChangeActionListener.class)})
public class UIUserToolBarDashboardPortlet extends BasePartialUpdateToolbar
{

   public static String DEFAULT_TAB_NAME = "Tab_Default";

   public UIUserToolBarDashboardPortlet() throws Exception
   {
      UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
      builder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      builder.withTemporalCheck();
      toolbarFilterConfig = builder.build();      
   }

   public UserNavigation getCurrentUserNavigation() throws Exception
   {
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      return getNavigation(SiteKey.user(rcontext.getRemoteUser()));
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

   @Override
   protected UserNode getNodeFromResourceID(String resourceId) throws Exception
   {
      UserNavigation currNav = getCurrentUserNavigation();
      if (currNav == null) return null;
    
      UserPortal userPortal = getUserPortal(); 
      UserNode node = userPortal.resolvePath(currNav, toolbarFilterConfig, resourceId);
      if (node != null && node.getURI().equals(resourceId))
      {
         return node;
      }
      return null;
   }
   
   static public class NavigationChangeActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {
      private Log log = ExoLogger.getExoLogger(NavigationChangeActionListener.class);

      @Override
      public void execute(Event<UIUserToolBarDashboardPortlet> event) throws Exception
      {
         log.debug("PageNode : " + ((EventRequest)event.getRequestContext().getRequest()).getEvent().getValue() + " is deleted");
      }
   }
   
   static public class AddDashboardActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {

      private final static String PAGE_TEMPLATE = "dashboard";

      private static Log logger = ExoLogger.getExoLogger(AddDashboardActionListener.class);

      public void execute(Event<UIUserToolBarDashboardPortlet> event) throws Exception
      {
         UIUserToolBarDashboardPortlet toolBarPortlet = event.getSource();
         String nodeName = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         Collection<UserNode> nodes = toolBarPortlet.getNavigationNodes(toolBarPortlet.getCurrentUserNavigation());
         if (nodes.size() < 1)
         {
            createDashboard(nodeName, toolBarPortlet);
         }
         else
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(
               prContext.getPortalURI() + nodes.iterator().next().getURI());
         }
      }

      private static void createDashboard(String _nodeName, UIUserToolBarDashboardPortlet toolBarPortlet)
      {
         try
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            if (_nodeName == null)
            {
               logger.debug("Parsed nodeName is null, hence use Tab_0 as default name");
               _nodeName = DEFAULT_TAB_NAME;
            }

            UserPortal userPortal = toolBarPortlet.getUserPortal();
            UserNavigation userNav = toolBarPortlet.getCurrentUserNavigation();
            if (userNav == null)
            {
               return;
            }
            SiteKey siteKey = userNav.getKey();

            UserPortalConfigService _configService = toolBarPortlet.getApplicationComponent(UserPortalConfigService.class);
            Page page =
               _configService.createPageTemplate(PAGE_TEMPLATE, siteKey.getTypeName(), siteKey.getName());
            page.setTitle(_nodeName);
            page.setName(_nodeName);
            toolBarPortlet.getApplicationComponent(DataStorage.class).create(page);

            UserNode rootNode = userPortal.getNode(userNav, Scope.CHILDREN, toolBarPortlet.toolbarFilterConfig, null);
            UserNode tabNode = rootNode.addChild(_nodeName);
            tabNode.setLabel(_nodeName);            
            tabNode.setPageRef(page.getPageId());

            userPortal.saveNode(rootNode, null);
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + tabNode.getURI());
         }
         catch (Exception ex)
         {
            logger.info("Could not create default dashboard page", ex);
         }
      }
   }
}

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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.List;
import javax.portlet.EventRequest;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * May 26, 2009  
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserToolBarDashboardPortlet.gtmpl",
   events = {@EventConfig(name = "AddDefaultDashboard", listeners = UIUserToolBarDashboardPortlet.AddDashboardActionListener.class),
      @EventConfig(listeners = UIUserToolBarDashboardPortlet.UserPageNodeDeletedActionListener.class)})
public class UIUserToolBarDashboardPortlet extends UIPortletApplication
{

   public static String DEFAULT_TAB_NAME = "Tab_Default";

   public UIUserToolBarDashboardPortlet() throws Exception
   {
   }

   public PageNavigation getCurrentUserNavigation() throws Exception
   {
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      return getPageNavigation(PortalConfig.USER_TYPE + "::" + remoteUser);
   }

   private PageNavigation getPageNavigation(String owner) throws Exception
   {
      //List<PageNavigation> allNavigations = Util.getUIPortal().getNavigations();
      List<PageNavigation> allNavigations = Util.getUIPortalApplication().getNavigations();
      for (PageNavigation nav : allNavigations)
      {
         if (nav.getOwner().equals(owner))
            return nav;
      }
      return null;
   }

   public PageNode getSelectedPageNode() throws Exception
   {
      return Util.getUIPortal().getSelectedNode();
   }

   static public class UserPageNodeDeletedActionListener extends EventListener<UIUserToolBarDashboardPortlet>
   {
      private Log log = ExoLogger.getExoLogger(UserPageNodeDeletedActionListener.class);

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

         PageNavigation cachedNavigation = toolBarPortlet.getCurrentUserNavigation();
         
         // Update navigation for prevent create first node which already existed
         DataStorage dataStorage = toolBarPortlet.getApplicationComponent(DataStorage.class);         
         PageNavigation userNavigation =
            dataStorage.getPageNavigation(cachedNavigation.getOwnerType(), cachedNavigation.getOwnerId());
         cachedNavigation.merge(userNavigation);

         UserPortalConfigService configService = toolBarPortlet.getApplicationComponent(UserPortalConfigService.class);
         if (cachedNavigation != null && configService != null && cachedNavigation.getNodes().size() < 1)
         {
            createDashboard(nodeName, cachedNavigation, configService);
         }
         else
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(
               prContext.getPortalURI() + cachedNavigation.getNodes().get(0).getName());
         }
      }

      private static void createDashboard(String _nodeName, PageNavigation _pageNavigation,
         UserPortalConfigService _configService)
      {
         try
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            if (_nodeName == null)
            {
               logger.debug("Parsed nodeName is null, hence use Tab_0 as default name");
               _nodeName = DEFAULT_TAB_NAME;
            }
            Page page =
               _configService.createPageTemplate(PAGE_TEMPLATE, _pageNavigation.getOwnerType(), _pageNavigation
                  .getOwnerId());
            page.setTitle(_nodeName);
            page.setName(_nodeName);

            PageNode pageNode = new PageNode();
            pageNode.setName(_nodeName);
            pageNode.setLabel(_nodeName);
            pageNode.setUri(_nodeName);
            pageNode.setPageReference(page.getPageId());

            _pageNavigation.addNode(pageNode);
            _configService.create(page);
            _configService.update(_pageNavigation);

            prContext.getResponse().sendRedirect(prContext.getPortalURI() + _nodeName);
         }
         catch (Exception ex)
         {
            logger.info("Could not create default dashboard page", ex);
         }
      }
   }
}
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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 *      Aug 10, 2009
 */
@ComponentConfig(template = "app:/groovy/dashboard/webui/component/UITabPaneDashboard.gtmpl", events = {
   @EventConfig(confirm = "UITabPaneDashboard.msg.deleteTab", name = "DeleteTab", listeners = UITabPaneDashboard.DeleteTabActionListener.class),
   @EventConfig(name = "AddDashboard", listeners = UITabPaneDashboard.AddDashboardActionListener.class),
   @EventConfig(name = "SwitchShowedTabRange", listeners = UITabPaneDashboard.SwitchShowedTabRangeActionListener.class),
   @EventConfig(name = "RenameTabLabel", listeners = UITabPaneDashboard.RenameTabLabelActionListener.class),
   @EventConfig(name = "PermuteTab", listeners = UITabPaneDashboard.PermuteTabActionListener.class)})
public class UITabPaneDashboard extends UIContainer
{

   private static Log logger = ExoLogger.getExoLogger(UITabPaneDashboard.class);

   private int startShowIndex;

   private int endShowIndex;

   private int tabNbs;

   private UserPortalConfigService configService;

   private PageNavigation pageNavigation;

   private UIPortal uiPortal;

   final private static int MAX_SHOWED_TAB_NUMBER = 6;

   final public static String PAGE_TEMPLATE = "dashboard";

   public UITabPaneDashboard() throws Exception
   {
      configService = getApplicationComponent(UserPortalConfigService.class);
      uiPortal = Util.getUIPortal();
      initPageNavigation();
   }

   private void initPageNavigation()
   {
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      pageNavigation = getPageNavigation(PortalConfig.USER_TYPE + "::" + remoteUser);
   }

   private PageNavigation getPageNavigation(String owner)
   {
      List<PageNavigation> allNavigations = uiPortal.getNavigations();
      for (PageNavigation nav : allNavigations)
      {
         if (nav.getOwner().equals(owner))
            return nav;
      }
      return null;
   }

   public int getCurrentNumberOfTabs()
   {
      return pageNavigation.getNodes().size();
   }

   public int getStartShowIndex()
   {
      return this.startShowIndex;
   }

   public int getEndShowIndex()
   {
      if (this.endShowIndex > 0)
      {
         return this.endShowIndex;
      }
      else
      {
         return Math.min(this.tabNbs, this.startShowIndex + MAX_SHOWED_TAB_NUMBER);
      }
   }

   public PageNavigation getPageNavigation()
   {
      if (pageNavigation == null)
      {
         initPageNavigation();
      }
      return pageNavigation;
   }

   /**
    * Remove node specified by nodeIndex and returns the node to switch to
    * @param nodeIndex
    * @return
    */
   public PageNode removePageNode(int nodeIndex)
   {
      try
      {
         ArrayList<PageNode> nodes = pageNavigation.getNodes();
         if (nodes.size() < 2)
         {
            return null; // At the moment, we prevent user from deleting all the dashboard
         }
         PageNode tobeRemoved = nodes.get(nodeIndex);
         PageNode selectedNode = uiPortal.getSelectedNode();

         nodes.remove(nodeIndex);

         if (tobeRemoved.getUri().equals(selectedNode.getUri()))
         {
            selectedNode = nodes.get(Math.max(0, nodeIndex - 1));
            uiPortal.setSelectedNode(selectedNode);
            configService.update(pageNavigation);
            return selectedNode;
         }

         configService.update(pageNavigation);
         return null; //Return null as there is no need to switch to new node
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   public String createNewPageNode(String nodeLabel)
   {
      try
      {
         if (nodeLabel == null || nodeLabel.length() == 0)
         {
            nodeLabel = "Tab_" + getCurrentNumberOfTabs();
         }
         Page page =
            configService.createPageTemplate(UITabPaneDashboard.PAGE_TEMPLATE, pageNavigation.getOwnerType(),
               pageNavigation.getOwnerId());
         page.setTitle(nodeLabel);

         PageNode pageNode = new PageNode();
         pageNode.setLabel(nodeLabel);
         String uniqueNodeName = nodeLabel.toLowerCase().replace(' ', '_');
         if (nameExisted(uniqueNodeName))
         {
            uniqueNodeName = uniqueNodeName + "_" + System.currentTimeMillis();
         }

         page.setName(uniqueNodeName);
         pageNode.setName(uniqueNodeName);
         pageNode.setUri(uniqueNodeName);
         pageNode.setPageReference(page.getPageId());

         pageNavigation.addNode(pageNode);
         uiPortal.setSelectedNode(pageNode);

         configService.create(page);
         configService.update(pageNavigation);

         return uniqueNodeName;
      }
      catch (Exception ex)
      {
         logger.info("Could not create page template", ex);
         return null;
      }
   }

   private boolean validateName(String label)
   {
      label = label.trim();
      if (Character.isDigit(label.charAt(0)) || label.charAt(0) == '-')
      {
         return false;
      }
      for (int i = 0; i < label.length(); i++)
      {
         char c = label.charAt(i);
         if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || Character.isSpaceChar(c))
         {
            continue;
         }
         return false;
      }
      return true;
   }

   private boolean nameExisted(String nodeName)
   {
      for (PageNode node : pageNavigation.getNodes())
      {
         if (node.getName().equals(nodeName))
         {
            return true;
         }
      }
      return false;
   }

   public String renamePageNode(int nodeIndex, String newNodeLabel)
   {
      try
      {
         ArrayList<PageNode> nodes = pageNavigation.getNodes();
         PageNode renamedNode = nodes.get(nodeIndex);
         if (renamedNode == null || newNodeLabel.length() == 0)
         {
            return null;
         }

         renamedNode.setLabel(newNodeLabel);

         String newNodeName = newNodeLabel.toLowerCase().replace(' ', '_');
         if (nameExisted(newNodeName))
         {
            newNodeName = newNodeName + "_" + System.currentTimeMillis();
         }
         renamedNode.setName(newNodeName);
         renamedNode.setUri(newNodeName);

         configService.update(pageNavigation);
         return newNodeName;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   /**
    * Exchange two nodes under user navigation
    * 
    * @param firstIndex
    * @param secondIndex
    * @return
    */
   public boolean permutePageNode(int firstIndex, int secondIndex)
   {
      if (firstIndex == secondIndex)
      {
         return false;
      }

      try
      {
         ArrayList<PageNode> nodes = pageNavigation.getNodes();
         PageNode firstNode = nodes.get(firstIndex);
         PageNode secondNode = nodes.get(secondIndex);
         nodes.set(firstIndex, secondNode);
         nodes.set(secondIndex, firstNode);

         configService.update(pageNavigation);
         return true;
      }
      catch (Exception ex)
      {
         return false;
      }
   }

   static public class DeleteTabActionListener extends EventListener<UITabPaneDashboard>
   {
      public void execute(Event<UITabPaneDashboard> event) throws Exception
      {
         UITabPaneDashboard source = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         if (source.getCurrentNumberOfTabs() == 1)
         {
            source.getAncestorOfType(UIApplication.class).addMessage(
               new ApplicationMessage("UITabPaneDashboard.msg.cannotDeleteLastTab", null));
            return;
         }
         int removedNodeIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         PageNode selectedNode = source.removePageNode(removedNodeIndex);

         //If the node is removed successfully, then redirect to the node specified by tab on the left
         if (selectedNode != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + selectedNode.getName());
         }
      }
   }

   static public class AddDashboardActionListener extends EventListener<UITabPaneDashboard>
   {
      public void execute(Event<UITabPaneDashboard> event) throws Exception
      {
         UITabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         String newTabLabel = context.getRequestParameter(UIComponent.OBJECTID);
         if (!tabPane.validateName(newTabLabel))
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", null));
            return;
         }
         String newNodeName = tabPane.createNewPageNode(newTabLabel);

         //If new node is created with success, then redirect to it
         if (newNodeName != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + newNodeName);
         }
      }
   }

   static public class SwitchShowedTabRangeActionListener extends EventListener<UITabPaneDashboard>
   {
      public void execute(Event<UITabPaneDashboard> event) throws Exception
      {
      }
   }

   static public class RenameTabLabelActionListener extends EventListener<UITabPaneDashboard>
   {

      final public static String RENAMED_TAB_LABEL_PARAMETER = "newTabLabel";

      public void execute(Event<UITabPaneDashboard> event) throws Exception
      {
         UITabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         int nodeIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         String newTabLabel = context.getRequestParameter(RENAMED_TAB_LABEL_PARAMETER);
         if (!tabPane.validateName(newTabLabel))
         {
            context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", null));
            return;
         }
         String newNodeName = tabPane.renamePageNode(nodeIndex, newTabLabel);

         //If page node is renamed with success, then redirect to new URL
         if (newNodeName != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + newNodeName);
         }
      }
   }

   static public class PermuteTabActionListener extends EventListener<UITabPaneDashboard>
   {

      final public static String TARGETED_TAB_PARAMETER = "targetedTab";

      public void execute(Event<UITabPaneDashboard> event) throws Exception
      {
         UITabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         int dragingTabIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         int targetedTabIndex = Integer.parseInt(context.getRequestParameter(TARGETED_TAB_PARAMETER));

         //If two nodes are permuted, then update the tab pane
         if (tabPane.permutePageNode(dragingTabIndex, targetedTabIndex))
         {
            context.addUIComponentToUpdateByAjax(tabPane);
         }
      }
   }
}

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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.page.UIPageBody;
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

import java.net.URLEncoder;
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

   private DataStorage dataService;

   private UIPortal uiPortal;

   final private static int MAX_SHOWED_TAB_NUMBER = 6;

   final public static String PAGE_TEMPLATE = "dashboard";

   public UITabPaneDashboard() throws Exception
   {
      configService = getApplicationComponent(UserPortalConfigService.class);
      dataService = getApplicationComponent(DataStorage.class);
      uiPortal = Util.getUIPortal();
   }

   /*
   private PageNavigation getPageNavigation(String owner) throws Exception
   {
      List<PageNavigation> allNavigations = uiPortal.getNavigations();
      for (PageNavigation nav : allNavigations)
      {
         if (nav.getOwner().equals(owner))
            return nav;
      }
      return null;
   }

   */

   public int getCurrentNumberOfTabs() throws Exception
   {

      return getSameSiblingsNode().size();
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

   public List<PageNode> getSameSiblingsNode() throws Exception
   {
      List<PageNode> siblings = getPageNavigation().getNodes();
      List<PageNode> selectedPath = Util.getUIPortal().getSelectedPath();
      if (selectedPath != null && selectedPath.size() > 1)
      {
         PageNode currentParent = selectedPath.get(selectedPath.size() - 2);
         siblings = currentParent.getChildren();
      }
      return siblings;
   }

   public PageNavigation getPageNavigation() throws Exception
   {
      return uiPortal.getSelectedNavigation();
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
         PageNavigation pageNavigation = getPageNavigation();
         List<PageNode> nodes = pageNavigation.getNodes();
         PageNode tobeRemoved = nodes.get(nodeIndex);
         PageNode selectedNode = uiPortal.getSelectedNode();

         boolean isRemoved = true; // To check 
         PageNavigation updateNav =
            dataService.getPageNavigation(pageNavigation.getOwnerType(), pageNavigation.getOwnerId());
         for (PageNode pageNode : updateNav.getNodes())
         {
            if (pageNode.getUri().equals(tobeRemoved.getUri()))
            {
               isRemoved = false;
               break;
            }
         }

         if (nodes.size() >= 2)
         {
            // Remove node
            nodes.remove(tobeRemoved);

            // Choose selected Node
            if (tobeRemoved.getUri().equals(selectedNode.getUri()))
            {
               selectedNode = nodes.get(Math.max(0, nodeIndex - 1));

            }
            else if (!nodes.contains(selectedNode))
            {
               selectedNode = nodes.get(0);
            }

            // Update
            if (!isRemoved)
            {
               String pageRef = tobeRemoved.getPageReference();
               if (pageRef != null && pageRef.length() > 0)
               {
                  Page page = configService.getPage(pageRef);
                  if (page != null)
                     dataService.remove(page);
                  UIPortal uiPortal = Util.getUIPortal();
                  // Remove from cache
                  uiPortal.setUIPage(pageRef, null);
               }
               //uiPortal.setSelectedNode(selectedNode);
               dataService.save(pageNavigation);
            }
         }
         else
         {
            getAncestorOfType(UIApplication.class).addMessage(
               new ApplicationMessage("UITabPaneDashboard.msg.cannotDeleteLastTab", null));
            return null;
         }

         return selectedNode;
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
         PageNavigation pageNavigation = getPageNavigation();
         Page page =
            configService.createPageTemplate(UITabPaneDashboard.PAGE_TEMPLATE, pageNavigation.getOwnerType(),
               pageNavigation.getOwnerId());
         page.setTitle(nodeLabel);

         List<PageNode> selectedPath = uiPortal.getSelectedPath();
         PageNode parentNode = null;
         if (selectedPath != null && selectedPath.size() > 1)
         {
            parentNode = selectedPath.get(selectedPath.size() - 2);
         }

         PageNode pageNode = new PageNode();
         pageNode.setLabel(nodeLabel);
         String uniqueNodeName = nodeLabel.toLowerCase().replace(' ', '_');
         if (nameExisted(uniqueNodeName))
         {
            uniqueNodeName = uniqueNodeName + "_" + System.currentTimeMillis();
         }

         String fullName = (parentNode != null) ? parentNode.getUri() + "/" + uniqueNodeName : uniqueNodeName;

         page.setName(uniqueNodeName);
         pageNode.setName(uniqueNodeName);
         pageNode.setUri(fullName);
         pageNode.setPageReference(page.getPageId());

         if (parentNode == null)
         {
            pageNavigation.addNode(pageNode);
         }
         else if (parentNode.getChildren() != null)
         {
            parentNode.getChildren().add(pageNode);
         }

         //uiPortal.setSelectedNode(pageNode);

         dataService.create(page);
         dataService.save(pageNavigation);

         return fullName;
      }
      catch (Exception ex)
      {
         logger.info("Could not create page template", ex);
         return null;
      }
   }

   private boolean validateName(String label)
   {
      if (label == null || label.length() < 1)
      {
         return false;
      }
      label = label.trim();
      if (label.length() < 1 || Character.isDigit(label.charAt(0)) || label.charAt(0) == '-')
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

   private boolean nameExisted(String nodeName) throws Exception
   {
      for (PageNode node : getPageNavigation().getNodes())
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
         PageNavigation pageNavigation = getPageNavigation();
         List<PageNode> nodes = pageNavigation.getNodes();
         List<PageNode> selectedPath = uiPortal.getSelectedPath();
         PageNode parentNode = null;
         if (selectedPath != null && selectedPath.size() > 1)
         {
            parentNode = selectedPath.get(selectedPath.size() - 2);
            nodes = parentNode.getChildren();
         }

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

         String newUri = (parentNode != null) ? parentNode.getUri() + "/" + newNodeName : newNodeName;

         renamedNode.setUri(newUri);

         Page page = configService.getPage(renamedNode.getPageReference());
         page.setTitle(newNodeLabel);
         if (page != null)
            dataService.save(page);
         
         dataService.save(pageNavigation);
         return newUri;
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
         PageNavigation pageNavigation = getPageNavigation();
         ArrayList<PageNode> nodes = pageNavigation.getNodes();
         PageNode firstNode = nodes.get(firstIndex);
         PageNode secondNode = nodes.get(secondIndex);
         nodes.set(firstIndex, secondNode);
         nodes.set(secondIndex, firstNode);

         dataService.save(pageNavigation);
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
         int removedNodeIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         PageNode selectedNode = source.removePageNode(removedNodeIndex);

         //If the node is removed successfully, then redirect to the node specified by tab on the left
         if (selectedNode != null)
         {
            // set maximizedUIComponent of UIPageBody is null if it is maximized portlet of removed page
            UIPortal uiPortal = Util.getUIPortal();
            UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
            if (uiPageBody != null && uiPageBody.getMaximizedUIComponent() != null)
            {
               uiPageBody.setMaximizedUIComponent(null);
            }

            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.setResponseComplete(true);
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(selectedNode.getUri(), "UTF-8"));
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
            //TODO nguyenanhkien2a@gmail.com
            //We should redirect to current node while adding new tab fails
            PageNode currentNode = tabPane.uiPortal.getSelectedNode();
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(currentNode.getUri(), "UTF-8"));
            
            Object[] args = {newTabLabel};
            context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
            return;
         }
         String uri = tabPane.createNewPageNode(newTabLabel);

         //If new node is created with success, then redirect to it
         if (uri != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.setResponseComplete(true);
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(uri, "UTF-8"));
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
            //We should redirect to current node while renaming fails
            PageNode currentNode = tabPane.uiPortal.getSelectedNode();
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(currentNode.getUri(), "UTF-8"));
            
            Object[] args = {newTabLabel};
            context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
            return;
         }
         String newUri = tabPane.renamePageNode(nodeIndex, newTabLabel);

         //If page node is renamed with success, then redirect to new URL
         if (newUri != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(newUri, "UTF-8"));
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

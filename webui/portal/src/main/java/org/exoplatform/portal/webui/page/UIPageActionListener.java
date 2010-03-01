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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tran The Trong trongtt@gmail.com Jun
 * 14, 2006
 */
public class UIPageActionListener
{

   static public class ChangePageNodeActionListener extends EventListener<UIPortal>
   {
      @Override
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal showedUIPortal = event.getSource();
         UIPortalApplication uiPortalApp = showedUIPortal.getAncestorOfType(UIPortalApplication.class);
         
         //This code snippet is to make sure that Javascript/Skin is fully loaded at the first request
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         PortalRequestContext pcontext = Util.getPortalRequestContext();
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         
         PageNavigation currentNav = showedUIPortal.getSelectedNavigation();
         String currentUri = showedUIPortal.getSelectedNode().getUri();
         if(currentUri.startsWith("/"))
         {
            currentUri = currentUri.substring(1);
         }
         
         //This if branche is to make sure that the first time user logs in, showedUIPortal has selectedPaths
         //Otherwise, there will be NPE on BreadcumbsPortlet
         if(showedUIPortal.getSelectedPath() == null)
         {
            List<PageNode> currentSelectedPath = findPath(currentNav, currentUri.split("/"));
            showedUIPortal.setSelectedPath(currentSelectedPath);
         }
         
         String targetedUri = ((PageNodeEvent<UIPortal>)event).getTargetNodeUri();
         if(targetedUri.startsWith("/"))
         {
            targetedUri = targetedUri.substring(1);
         }
         
         PageNavigation targetedNav = getTargetedNav(uiPortalApp, targetedUri);
         
         if(targetedNav == null)
         {
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            pcontext.setFullRender(true);
            return;
         }
       
         String formerNavType = currentNav.getOwnerType();
         String formerNavId = currentNav.getOwnerId();
         String newNavType = targetedNav.getOwnerType();
         String newNavId = targetedNav.getOwnerId();
         
         String[] targetPath = targetedUri.split("/");
         PageNode targetPageNode = getTargetedNode(targetedNav, targetPath);
         List<PageNode> targetedPathNodes = findPath(targetedNav, targetPath);
         
         if(formerNavType.equals(newNavType) && formerNavId.equals(newNavId))
         {
            //Case 1: Both navigation type and id are not changed, but current page node is changed
            if(!currentUri.equals(targetedUri))
            {
               showedUIPortal.setSelectedNode(targetPageNode);
               showedUIPortal.setSelectedPath(targetedPathNodes);
               showedUIPortal.refreshUIPage();
               return;
            }
         }
         else
         {
            // Case 2: Either navigation type or id has been changed
            // First, we try to find a cached UIPortal
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            pcontext.setFullRender(true);
            UIPortal cachedUIPortal = uiPortalApp.getCachedUIPortal(newNavType, newNavId);
            if (cachedUIPortal != null)
            {
//               System.out.println("Found UIPortal with OWNERTYPE: " + newNavType + " OWNERID " + newNavId);
               cachedUIPortal.setSelectedNode(targetPageNode);
               cachedUIPortal.setSelectedPath(targetedPathNodes);
               uiPortalApp.setShowedUIPortal(cachedUIPortal);
               
               //Temporary solution to fix edit inline error while switching between navigations
               DataStorage storageService = uiPortalApp.getApplicationComponent(DataStorage.class);
               PortalConfig associatedPortalConfig = storageService.getPortalConfig(newNavType, newNavId);
               uiPortalApp.getUserPortalConfig().setPortal(associatedPortalConfig);

               cachedUIPortal.refreshUIPage();
               return;
            }
            else
            {
               UIPortal newUIPortal = buildUIPortal(targetedNav, uiPortalApp, uiPortalApp.getUserPortalConfig());
               if(newUIPortal == null)
               {
                  return;
               }
               newUIPortal.setSelectedNode(targetPageNode);
               newUIPortal.setSelectedPath(targetedPathNodes);
               uiPortalApp.setShowedUIPortal(newUIPortal);
               uiPortalApp.addUIPortal(newUIPortal);
               newUIPortal.refreshUIPage();
               return;
            }
         }
      }
      
      /**
       * Get the targeted <code>PageNavigation</code>
       * 
       * @param uiPortalApp
       * @param targetedUri
       * @return
       */
      private PageNavigation getTargetedNav(UIPortalApplication uiPortalApp, String targetedUri)
      {
         List<PageNavigation> allNavs = uiPortalApp.getUserPortalConfig().getNavigations();
         
         //That happens when user browses to an empty-nodeUri URL like ../portal/public/classic/
         //In this case, we returns default navigation
         if(targetedUri.length() == 0)
         {
            return uiPortalApp.getNavigations().get(0);
         }
         
         String[] pathNodes = targetedUri.split("/");
         
         //We check the first navigation in the list containing all descendants corresponding to pathNodes
         for(PageNavigation nav : allNavs)
         {
            if(containingDescendantNodes(nav, pathNodes))
            {
               return nav;
            }
         }
         return null;
      }
      
      /**
       *  Check if a given <code>PageNavigation</code> contains all the descendants corresponding to the pathNodes
       * 
       * @param navigation
       * @param pathNodes
       * @return
       */
      private static boolean containingDescendantNodes(PageNavigation navigation, String[] pathNodes)
      {
        PageNode firstLevelNode = navigation.getNode(pathNodes[0]);
        if(firstLevelNode == null)
        {
           return false;
        }
        
        //Recursive code snippet with two variables
        PageNode tempNode = firstLevelNode;
        PageNode currentNode;
        
        for(int i = 1; i < pathNodes.length; i++)
        {
           currentNode = tempNode.getChild(pathNodes[i]);
           
           //If the navigation does not support an intermediate pathNode, then returns false
           if (currentNode == null)
            {
               return false;
            }
            else
            {
               tempNode = currentNode;
            }
        }
         return true;
      }
      
      /**
       * Fetch the currently selected pageNode under a PageNavigation. It is the last node encountered
       * while descending the pathNodes
       * 
       * @param targetedNav
       * @param pathNodes
       * @return
       */
      private static PageNode getTargetedNode(PageNavigation targetedNav, String[] pathNodes)
      {
         //Case users browses to a URL of the form  */portal/public/classic
         if(pathNodes.length == 0)
         {
            return targetedNav.getNodes().get(0);
         }
         
         PageNode currentNode = targetedNav.getNode(pathNodes[0]);
         PageNode tempNode = null;
         
         for(int i = 1; i < pathNodes.length; i++)
         {
            tempNode = currentNode.getChild(pathNodes[i]);
            if (tempNode == null)
            {
               return null;
            }
            else
            {
               currentNode = tempNode;
            }
         }
         
         return currentNode;
      }
      
      private static List<PageNode> findPath(PageNavigation nav, String[] pathNodes)
      {
         List<PageNode> nodes = new ArrayList<PageNode>(4);
         
         //That happens when user browses to a URL like */portal/public/classic
         if(pathNodes.length == 0)
         {
            nodes.add(nav.getNodes().get(0));
            return nodes;
         }
         PageNode startNode = nav.getNode(pathNodes[0]);
         if (startNode == null)
         {
            return nodes;
         }
         nodes.add(startNode);

         for (int i = 1; i < pathNodes.length; i++)
         {
            startNode = startNode.getChild(pathNodes[i]);
            if (startNode == null)
            {
               return nodes;
            }
            else
            {
               nodes.add(startNode);
            }
         }
         return nodes;
      }

      private static UIPortal buildUIPortal(PageNavigation newPageNav, UIPortalApplication uiPortalApp, UserPortalConfig userPortalConfig) throws Exception
      {
         DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
         if(storage == null){
            return null;
         }
         PortalConfig portalConfig = storage.getPortalConfig(newPageNav.getOwnerType(), newPageNav.getOwnerId());
         Container layout = portalConfig.getPortalLayout();
         if(layout != null)
         {
            userPortalConfig.setPortal(portalConfig);
         }
         UIPortal uiPortal = uiPortalApp.createUIComponent(UIPortal.class, null, null);
         //Reset selected navigation on userPortalConfig
         userPortalConfig.setSelectedNavigation(newPageNav);
//         System.out.println("Build new UIPortal with OWNERTYPE: " + newPageNav.getOwnerType() + " OWNERID: " + newPageNav.getOwnerId());
         PortalDataMapper.toUIPortal(uiPortal, userPortalConfig);
         return uiPortal;
      }
   }

  
   static public class DeleteGadgetActionListener extends EventListener<UIPage>
   {
      public void execute(Event<UIPage> event) throws Exception
      {
         WebuiRequestContext pContext = event.getRequestContext();
         String id = pContext.getRequestParameter(UIComponent.OBJECTID);
         UIPage uiPage = event.getSource();
         List<UIGadget> uiWidgets = new ArrayList<UIGadget>();
         uiPage.findComponentOfType(uiWidgets, UIGadget.class);
         for (UIGadget uiWidget : uiWidgets)
         {
            if (uiWidget.getId().equals(id))
            {
               uiPage.getChildren().remove(uiWidget);
               String userName = pContext.getRemoteUser();
               if (userName != null && userName.trim().length() > 0)
               {
                  // Julien : commented as normally removing the gadget should
                  // remove the state associated with it
                  // in the MOP
                  // UserGadgetStorage widgetDataService =
                  // uiPage.getApplicationComponent(UserGadgetStorage.class) ;
                  // widgetDataService.delete(userName,
                  // uiWidget.getApplicationName(), uiWidget.getId()) ;
               }
               if (uiPage.isModifiable())
               {
                  Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
                  if (page.getChildren() == null)
                  {
                     page.setChildren(new ArrayList<ModelObject>());
                  }
                  DataStorage dataService = uiPage.getApplicationComponent(DataStorage.class);
                  dataService.save(page);
               }
               break;
            }
         }
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         pcontext.setFullRender(false);
         pcontext.setResponseComplete(true);
         pcontext.getWriter().write(EventListener.RESULT_OK);
      }
   }

   static public class RemoveChildActionListener extends EventListener<UIPage>
   {
      public void execute(Event<UIPage> event) throws Exception
      {
         UIPage uiPage = event.getSource();
         String id = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         if (uiPage.isModifiable())
         {
            uiPage.removeChildById(id);
            Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
            if (page.getChildren() == null)
            {
               page.setChildren(new ArrayList<ModelObject>());
            }
            DataStorage dataService = uiPage.getApplicationComponent(DataStorage.class);
            dataService.save(page);
            pcontext.setFullRender(false);
            pcontext.setResponseComplete(true);
            pcontext.getWriter().write(EventListener.RESULT_OK);
         }
         else
         {
            org.exoplatform.webui.core.UIApplication uiApp = pcontext.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIPage.msg.EditPermission.null", null));
         }
      }
   }
}

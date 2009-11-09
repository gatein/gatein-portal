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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
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

import javax.portlet.WindowState;
import java.util.ArrayList;
import java.util.List;

/** Created by The eXo Platform SAS Author : Tran The Trong trongtt@gmail.com Jun 14, 2006 */
public class UIPageActionListener
{

   static public class ChangePageNodeActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         PageNodeEvent<UIPortal> pnevent = (PageNodeEvent<UIPortal>)event;
         UIPortal uiPortal = pnevent.getSource();
         UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
         UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         if (uiPortalApp.getModeState() != UIPortalApplication.NORMAL_MODE)
         {
            UserPortalConfigService configService = uiPortalApp.getApplicationComponent(UserPortalConfigService.class);
            String remoteUser = Util.getPortalRequestContext().getRemoteUser();
            UserPortalConfig portalConfig = configService.getUserPortalConfig(Util.getPortalRequestContext().getPortalOwner(), remoteUser);
            uiPortal.getChildren().clear();
            PortalDataMapper.toUIPortal(uiPortal, portalConfig);
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiPortal.broadcast(event, event.getExecutionPhase());
            return;
         }
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         PortalRequestContext pcontext = Util.getPortalRequestContext();
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         pcontext.setFullRender(true);

         String currentUri = (uiPortal.getSelectedNode() == null) ? null : uiPortal.getSelectedNode().getUri();
         PageNavigation currentNav = uiPortal.getSelectedNavigation();

         uiPortal.setSelectedNavigation(null);
         uiPortal.setSelectedNode(null);
         List<PageNode> selectedPaths_ = new ArrayList<PageNode>(5);

         List<PageNavigation> navigations = uiPortal.getNavigations();
         String uri = pnevent.getTargetNodeUri();
         if (uri == null || (uri = uri.trim()).length() < 1)
         {
            return;
         }
         if (uri.length() == 1 && uri.charAt(0) == '/')
         {
            for (PageNavigation nav : navigations)
            {
               for (PageNode child : nav.getNodes())
               {
                  if (PageNavigationUtils.filter(child, pcontext.getRemoteUser()) != null)
                  {
                     selectedPaths_.add(child);
                     uiPortal.setSelectedNode(child);
                     uiPortal.setSelectedPaths(selectedPaths_);
                     String selectedUri =
                        (uiPortal.getSelectedNode() == null) ? null : uiPortal.getSelectedNode().getUri();

                     if (!currentUri.equals(selectedUri))
                     {
                        updateLayout(uiPortal, currentNav, uiPortal.getSelectedNavigation(), uiPortalApp);
                        uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
                        if (uiPageBody.getMaximizedUIComponent() != null)
                        {
                           UIPortlet currentPortlet = (UIPortlet)uiPageBody.getMaximizedUIComponent();
                           currentPortlet.setCurrentWindowState(WindowState.NORMAL);
                           uiPageBody.setMaximizedUIComponent(null);
                        }
                     }
                     uiPageBody.setPageBody(uiPortal.getSelectedNode(), uiPortal);
                     return;
                  }
               }
            }
         }
         if (uri.charAt(0) == '/')
         {
            uri = uri.substring(1);
         }

         int idx = uri.lastIndexOf("::");
         if (idx < 0)
         {
            PageNode selectedNode = null;
            for (PageNavigation nav : navigations)
            {
               String[] nodeNames = uri.split("/");
               int i = 0;
               PageNode tempNode = nav.getNode(nodeNames[i]);
               selectedNode = tempNode;
               while (tempNode != null && ++i < nodeNames.length)
               {
                  selectedPaths_.add(selectedNode = tempNode);
                  tempNode = tempNode.getChild(nodeNames[i]);
               }
               if (tempNode != null)
               {
                  selectedPaths_.add(selectedNode = tempNode);
               }

               if (selectedNode != null)
               {
                  uiPortal.setSelectedNavigation(nav);
                  break;
               }
            }
            // TODO tam.nguyen: filter navigation, select navigation up to user
            if (selectedNode == null)
            {
               filter:
               for (PageNavigation nav : navigations)
               {
                  for (PageNode child : nav.getNodes())
                  {
                     if (PageNavigationUtils.filter(child, pcontext.getRemoteUser()) != null)
                     {
                        selectedNode = child;
                        break filter;
                     }
                  }
               }
            }
            uiPortal.setSelectedNode(selectedNode);
            if (selectedNode == null)
            {
               selectedPaths_.add(uiPortal.getSelectedNode());
            }
            uiPortal.setSelectedPaths(selectedPaths_);
            String selectedUri = (uiPortal.getSelectedNode() == null) ? null : uiPortal.getSelectedNode().getUri();
            if (currentUri != null && !currentUri.equals(selectedUri))
            {
               updateLayout(uiPortal, currentNav, uiPortal.getSelectedNavigation(), uiPortalApp);
               uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
               if (uiPageBody.getMaximizedUIComponent() != null)
               {
                  UIPortlet currentPortlet = (UIPortlet)uiPageBody.getMaximizedUIComponent();
                  currentPortlet.setCurrentWindowState(WindowState.NORMAL);
                  uiPageBody.setMaximizedUIComponent(null);
               }
            }

            uiPageBody.setPageBody(uiPortal.getSelectedNode(), uiPortal);
            return;
         }
         String navId = uri.substring(0, idx);
         uri = uri.substring(idx + 2, uri.length());
         PageNavigation nav = null;
         for (PageNavigation ele : navigations)
         {
            if (ele.getId() == Integer.parseInt(navId))
            {
               nav = ele;
               break;
            }
         }
         if (nav != null)
         {
            String[] nodeNames = uri.split("/");
            int i = 0;
            PageNode tempNode = nav.getNode(nodeNames[i]);
            PageNode selecttedNode = tempNode;
            while (tempNode != null && ++i < nodeNames.length)
            {
               selectedPaths_.add(selecttedNode = tempNode);
               tempNode = tempNode.getChild(nodeNames[i]);
            }
            if (tempNode != null)
            {
               selectedPaths_.add(selecttedNode = tempNode);
            }

            uiPortal.setSelectedNode(selecttedNode);
            uiPortal.setSelectedNavigation(nav);
         }

         pcontext.getJavascriptManager().addCustomizedOnLoadScript(
            "document.title='" + uiPortal.getSelectedNode().getResolvedLabel().replaceAll("'", "\\\\'") + "';");
         uiPortal.setSelectedPaths(selectedPaths_);
         String selectedUri = (uiPortal.getSelectedNode() == null) ? null : uiPortal.getSelectedNode().getUri();

         if (!currentUri.equals(selectedUri))
         {
            if (uiPageBody.getMaximizedUIComponent() != null)
            {
               UIPortlet currentPortlet = (UIPortlet)uiPageBody.getMaximizedUIComponent();
               currentPortlet.setCurrentWindowState(WindowState.NORMAL);
               uiPageBody.setMaximizedUIComponent(null);
            }
         }
         uiPageBody.setPageBody(uiPortal.getSelectedNode(), uiPortal);
      }

      /**
       * Update the layout of UIPortal if both ownerType and ownerId of navigation are changed
       *
       * @param uiPortal
       * @param formerNav
       * @param newNav
       * @param uiPortalApp
       * @throws Exception
       */
      private void updateLayout(UIPortal uiPortal, PageNavigation formerNav, PageNavigation newNav,
                                UIPortalApplication uiPortalApp) throws Exception
      {
         if (formerNav == null || newNav == null)
         {
            return;
         }
         String newOwnerType = newNav.getOwnerType();
         String newOwnerId = newNav.getOwnerId();
         String formerOwnerType = formerNav.getOwnerType();
         String formerOwnerId = formerNav.getOwnerId();

         if (newOwnerId.equals(formerOwnerId) && formerOwnerType.equals(newOwnerType))
         {
            return;
         }

         DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
         PortalConfig pConfig = storage.getPortalConfig(newOwnerType, newOwnerId);
         Container container = pConfig.getPortalLayout();
         if (container != null)
         {
            UserPortalConfig portalConfig = uiPortalApp.getUserPortalConfig();
            portalConfig.setPortal(pConfig);
            rebuildUIPortal(uiPortal, portalConfig);
         }
      }

      /**
       * Rebuild UIPortal with updated UserPortalConfig
       *
       * @param uiPortal
       * @param portalConfig
       * @throws Exception
       */
      private void rebuildUIPortal(UIPortal uiPortal, UserPortalConfig portalConfig) throws Exception
      {
         PageNode backupSelectedNode = uiPortal.getSelectedNode();
         PageNavigation backupSelectedNavigation = uiPortal.getSelectedNavigation();
         List<PageNode> backupSelectedPaths = uiPortal.getSelectedPaths();
         uiPortal.getChildren().clear();
         PortalDataMapper.toUIPortal(uiPortal, portalConfig);
         uiPortal.setSelectedNode(backupSelectedNode);
         uiPortal.setSelectedNavigation(backupSelectedNavigation);
         uiPortal.setSelectedPaths(backupSelectedPaths);
      }
   }

   //  
   //  static public class DeleteWidgetActionListener extends EventListener<UIPage> {
   //    public void execute(Event<UIPage> event) throws Exception {
   //      WebuiRequestContext pContext = event.getRequestContext();
   //      String id  = pContext.getRequestParameter(UIComponent.OBJECTID);
   //      UIPage uiPage = event.getSource();
   //      List<UIWidget> uiWidgets = new ArrayList<UIWidget>();
   //      uiPage.findComponentOfType(uiWidgets, UIWidget.class);
   //      for(UIWidget uiWidget : uiWidgets) {
   //        if(uiWidget.getApplicationInstanceUniqueId().equals(id)) {
   //          uiPage.getChildren().remove(uiWidget);
   //          String userName = pContext.getRemoteUser() ;
   //          if(userName != null && userName.trim().length() > 0) {
   //            UserWidgetStorage widgetDataService = uiPage.getApplicationComponent(UserWidgetStorage.class) ;
   //            widgetDataService.delete(userName, uiWidget.getApplicationName(), uiWidget.getApplicationInstanceUniqueId()) ;            
   //          }
   //          if(uiPage.isModifiable()) {
   //            Page page = PortalDataMapper.toPageModel(uiPage);    
   //            UserPortalConfigService configService = uiPage.getApplicationComponent(UserPortalConfigService.class);     
   //            if(page.getChildren() == null) page.setChildren(new ArrayList<Object>());
   //            configService.update(page);
   //          }
   //          break;
   //        }
   //      }
   //      PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
   //      pcontext.setFullRender(false);
   //      pcontext.setResponseComplete(true) ;
   //      pcontext.getWriter().write(EventListener.RESULT_OK) ;
   //    }
   //  }
   //  
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
                  // Julien : commented as normally removing the gadget should remove the state associated with it
                  // in the MOP
                  // UserGadgetStorage widgetDataService = uiPage.getApplicationComponent(UserGadgetStorage.class) ;
                  // widgetDataService.delete(userName, uiWidget.getApplicationName(), uiWidget.getId()) ;
               }
               if (uiPage.isModifiable())
               {
                  Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
                  UserPortalConfigService configService = uiPage.getApplicationComponent(UserPortalConfigService.class);
                  if (page.getChildren() == null)
                  {
                     page.setChildren(new ArrayList<ModelObject>());
                  }
                  configService.update(page);
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
            UserPortalConfigService configService = uiPage.getApplicationComponent(UserPortalConfigService.class);
            if (page.getChildren() == null)
            {
               page.setChildren(new ArrayList<ModelObject>());
            }
            configService.update(page);
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

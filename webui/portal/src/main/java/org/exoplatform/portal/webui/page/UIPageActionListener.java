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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
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
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal showedUIPortal = event.getSource();
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         
         UserPortal userPortal = uiPortalApp.getUserPortalConfig().getUserPortal();
         
         String uri = ((PageNodeEvent<UIPortal>)event).getTargetNodeUri();
         UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
         builder.withAuthorizationCheck();
         UserNode naviPath = userPortal.resolvePath(builder.build(), uri);

         if (naviPath == null)
         {
            UIPageBody uiPageBody = showedUIPortal.findFirstComponentOfType(UIPageBody.class);
            uiPageBody.setUIComponent(null);
            return;
         }
         
         UserNavigation targetNav = naviPath.getNavigation();
         
         UserNode currentNavPath = showedUIPortal.getNavPath();
         
         if(currentNavPath != null && currentNavPath.getNavigation().getKey().equals(targetNav.getKey()))
         {
            //Case 1: Both navigation type and id are not changed, but current page node is changed
            if(!currentNavPath.getURI().equals(naviPath.getURI()))
            {
               showedUIPortal.setNavPath(naviPath);
            }
         }
         else
         {
            // Case 2: Either navigation type or id has been changed
            // First, we try to find a cached UIPortal
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            showedUIPortal = uiPortalApp.getCachedUIPortal(targetNav.getKey());
            if (showedUIPortal != null)
            {
               showedUIPortal.setNavPath(naviPath);
               uiPortalApp.setShowedUIPortal(showedUIPortal);
               
               //Temporary solution to fix edit inline error while switching between navigations
               DataStorage storageService = uiPortalApp.getApplicationComponent(DataStorage.class);
               PortalConfig associatedPortalConfig = storageService.getPortalConfig(targetNav.getKey().getTypeName(), targetNav.getKey().getName());
               UserPortalConfig userPortalConfig = uiPortalApp.getUserPortalConfig();
               
               //Update layout-related data on UserPortalConfig
               userPortalConfig.setPortal(associatedPortalConfig);
            }
            else
            {
               showedUIPortal = buildUIPortal(targetNav.getKey(), uiPortalApp, uiPortalApp.getUserPortalConfig());
               if(showedUIPortal == null)
               {
                  return;
               }
               showedUIPortal.setNavPath(naviPath);
               uiPortalApp.setShowedUIPortal(showedUIPortal);
               uiPortalApp.putCachedUIPortal(showedUIPortal);
            }
         }
         
         showedUIPortal.refreshUIPage();
      }

      private UIPortal buildUIPortal(SiteKey newPageNav, UIPortalApplication uiPortalApp, UserPortalConfig userPortalConfig) throws Exception
      {
         DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
         if(storage == null){
            return null;
         }
         PortalConfig portalConfig = storage.getPortalConfig(newPageNav.getTypeName(), newPageNav.getName());
         Container layout = portalConfig.getPortalLayout();
         if(layout != null)
         {
            userPortalConfig.setPortal(portalConfig);
         }
         UIPortal uiPortal = uiPortalApp.createUIComponent(UIPortal.class, null, null);
         
         //Reset selected navigation on userPortalConfig
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
         pcontext.ignoreAJAXUpdateOnPortlets(false);
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
            pcontext.ignoreAJAXUpdateOnPortlets(false);
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

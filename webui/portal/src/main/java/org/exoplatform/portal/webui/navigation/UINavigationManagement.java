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

package org.exoplatform.portal.webui.navigation;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.page.UIPageNodeForm2;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.List;

@ComponentConfig(template = "system:/groovy/portal/webui/navigation/UINavigationManagement.gtmpl", events = {
   @EventConfig(listeners = UINavigationManagement.SaveActionListener.class),
   @EventConfig(listeners = UINavigationManagement.AddRootNodeActionListener.class)})
public class UINavigationManagement extends UIContainer
{

   private String owner;

   private String ownerType;

   @SuppressWarnings("unused")
   public UINavigationManagement() throws Exception
   {
      addChild(UINavigationNodeSelector.class, null, null);
   }

   public void setOwner(String owner)
   {
      this.owner = owner;
   }

   public String getOwner()
   {
      return this.owner;
   }

   public <T extends UIComponent> T setRendered(boolean b)
   {
      return super.<T> setRendered(b);
   }

   public void loadView(Event<? extends UIComponent> event) throws Exception
   {
      UINavigationNodeSelector uiNodeSelector = getChild(UINavigationNodeSelector.class);
      UITree uiTree = uiNodeSelector.getChild(UITree.class);
      uiTree.createEvent("ChangeNode", event.getExecutionPhase(), event.getRequestContext()).broadcast();
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public String getOwnerType()
   {
      return this.ownerType;
   }

   static public class SaveActionListener extends EventListener<UINavigationManagement>
   {

      public void execute(Event<UINavigationManagement> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UINavigationManagement uiManagement = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiManagement.getChild(UINavigationNodeSelector.class);
         DataStorage dataService = uiManagement.getApplicationComponent(DataStorage.class);
         UserPortalConfigService portalConfigService = uiManagement.getApplicationComponent(UserPortalConfigService.class);
         
         PageNavigation navigation = uiNodeSelector.getSelectedNavigation();
         String editedOwnerType = navigation.getOwnerType();
         String editedOwnerId = navigation.getOwnerId();
         // Check existed
         PageNavigation persistNavigation =  dataService.getPageNavigation(editedOwnerType, editedOwnerId);
         if (persistNavigation == null)
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UINavigationManagement.msg.NavigationNotExistAnymore", null));
            UIPopupWindow uiPopup = uiManagement.getParent();
            uiPopup.setShow(false);
            UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            prContext.setFullRender(true);
            return;
         }
         
         if(PortalConfig.PORTAL_TYPE.equals(navigation.getOwnerType()))
         {
            UserPortalConfig portalConfig = portalConfigService.getUserPortalConfig(navigation.getOwnerId(), prContext.getRemoteUser());
            if(portalConfig != null)
            {
               dataService.save(navigation);
            }
            else
            {
               UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null));
               UIPopupWindow uiPopup = uiManagement.getParent();
               uiPopup.setShow(false);
               UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
               UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
               prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
               prContext.setFullRender(true);
               return;
            }
         }
         else
         {
            dataService.save(navigation);
         }
         
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         setNavigation(uiPortalApp.getNavigations(), navigation);
         
         //Update UIPortal corredponding to edited navigation
         UIPortal targetedUIPortal = uiPortalApp.getCachedUIPortal(editedOwnerType, editedOwnerId);
         if(targetedUIPortal != null)
         {
            targetedUIPortal.setNavigation(navigation);
         }
         
         UIPopupWindow uiPopup = uiManagement.getParent();
         uiPopup.setShow(false);
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         prContext.setFullRender(true);
      }

      private void setNavigation(List<PageNavigation> navs, PageNavigation nav)
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

   static public class AddRootNodeActionListener extends EventListener<UINavigationManagement>
   {

      @Override
      public void execute(Event<UINavigationManagement> event) throws Exception
      {
         UINavigationManagement uiManagement = event.getSource();
         UINavigationNodeSelector uiNodeSelector = uiManagement.getChild(UINavigationNodeSelector.class);
         UIPopupWindow uiManagementPopup = uiNodeSelector.getAncestorOfType(UIPopupWindow.class);
         UIPageNodeForm2 uiNodeForm = uiManagementPopup.createUIComponent(UIPageNodeForm2.class, null, null);
         uiNodeForm.setValues(null);
         uiManagementPopup.setUIComponent(uiNodeForm);
         PageNavigation nav = uiNodeSelector.getSelectedNavigation();
         uiNodeForm.setSelectedParent(nav);

         // set owner type, owner
         uiNodeForm.setOwner(nav.getOwnerId());
         uiNodeForm.setOwnerType(nav.getOwnerType());

         uiManagementPopup.setWindowSize(800, 500);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup.getParent());
      }

   }
}
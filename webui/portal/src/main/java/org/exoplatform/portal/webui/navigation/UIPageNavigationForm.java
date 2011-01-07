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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by The eXo Platform SAS
 * Author : tam.nguyen
 *          tamndrok@gmail.com
 * June 11, 2009  
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIPageNavigationForm.SaveActionListener.class),
   @EventConfig(listeners = UIPageNavigationForm.CloseActionListener.class, phase = Phase.DECODE, name = "ClosePopup")})
public class UIPageNavigationForm extends UIForm
{

   private PageNavigation pageNav_;

   private String ownerId;

   private String ownerType;

   private String priority;

   public UIPageNavigationForm() throws Exception
   {

   }

   public void addFormInput() throws Exception
   {
      List<SelectItemOption<String>> priorties = new ArrayList<SelectItemOption<String>>();
      for (int i = 1; i < 11; i++)
      {
         priorties.add(new SelectItemOption<String>(String.valueOf(i), String.valueOf(i)));
      }
      addUIFormInput(new UIFormStringInput("ownerType", "ownerType", getOwnerType()).setEditable(false))
         .addUIFormInput(new UIFormStringInput("ownerId", "ownerId", ownerId).setEditable(false)).addUIFormInput(
            new UIFormSelectBox("priority", null, priorties).setValue(getPriority()));
   }

   public void setValues(PageNavigation pageNavigation) throws Exception
   {
      setPageNav(pageNavigation);
      invokeGetBindingBean(pageNavigation);
      removeChildById("ownerId");      
      UIFormStringInput ownerId = new UIFormStringInput("ownerId", "ownerId", pageNavigation.getOwnerId());
      ownerId.setEditable(false);
      ownerId.setParent(this);
      getChildren().add(1, ownerId);
      UIFormSelectBox uiSelectBox = findComponentById("priority");
      uiSelectBox.setValue(String.valueOf(pageNavigation.getPriority()));
   }

   public void setOwnerId(String ownerId)
   {
      this.ownerId = ownerId;
   }

   public String getOwnerId()
   {
      return ownerId;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setPriority(String priority)
   {
      this.priority = priority;
   }

   public String getPriority()
   {
      return priority;
   }

   public void setPageNav(PageNavigation pageNav_)
   {
      this.pageNav_ = pageNav_;
   }

   public PageNavigation getPageNav()
   {
      return pageNav_;
   }

   static public class SaveActionListener extends EventListener<UIPageNavigationForm>
   {
      public void execute(Event<UIPageNavigationForm> event) throws Exception
      {
         UIPageNavigationForm uiForm = event.getSource();
         PageNavigation pageNav = uiForm.getPageNav();

         // Check existed
         PortalRequestContext prContext = Util.getPortalRequestContext();
         DataStorage dataService = uiForm.getApplicationComponent(DataStorage.class);
         PageNavigation persistNavigation = dataService.getPageNavigation(pageNav.getOwnerType(), pageNav.getOwnerId());
         if (persistNavigation == null)
         {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UINavigationManagement.msg.NavigationNotExistAnymore", null));
            UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            UIPopupWindow uiPopup = uiForm.getParent();
            uiPopup.setShow(false);
            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            return;
         }

         WebuiRequestContext pcontext = event.getRequestContext();
         uiForm.invokeSetBindingBean(pageNav);
         UIFormSelectBox uiSelectBox = uiForm.findComponentById("priority");
         int priority = Integer.parseInt(uiSelectBox.getValue());
         pageNav.setPriority(priority);

         // update navigation
         dataService.save(pageNav);

         pageNav = dataService.getPageNavigation(pageNav.getOwnerType(), pageNav.getOwnerId());

         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         updateNavPriority(uiPortalApp.getNavigations(), pageNav);

         uiPortalApp.localizeNavigations();

         UIPopupWindow uiPopup = uiForm.getParent();
         uiPopup.setShow(false);
         UIComponent opener = uiPopup.getParent();
         UIWorkingWorkspace uiWorkingWS =
            Util.getUIPortal().getAncestorOfType(UIPortalApplication.class).getChild(UIWorkingWorkspace.class);
         uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
         pcontext.addUIComponentToUpdateByAjax(opener);
      }
      
      private void updateNavPriority(List<PageNavigation> navs, PageNavigation nav)
      {
         for (int i = 0; i < navs.size(); i++)
         {
            if (navs.get(i).getId() == nav.getId())
            {
               navs.set(i, nav);
               break;
            }
         }

         Collections.sort(navs, new Comparator<PageNavigation>()
         {
            public int compare(PageNavigation nav1, PageNavigation nav2)
            {
               return nav1.getPriority() - nav2.getPriority();
            }
         });
      }
   }

   static public class CloseActionListener extends EventListener<UIPageNavigationForm>
   {
      public void execute(Event<UIPageNavigationForm> event) throws Exception
      {
         UIPageNavigationForm uiForm = event.getSource();
         uiForm.<UIComponent> getParent().broadcast(event, Phase.ANY);
      }
   }
}

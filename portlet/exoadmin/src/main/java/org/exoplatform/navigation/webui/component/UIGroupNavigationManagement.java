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

package org.exoplatform.navigation.webui.component;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.UIAddGroupNavigation;
import org.exoplatform.portal.webui.navigation.UINavigationManagement;
import org.exoplatform.portal.webui.navigation.UINavigationNodeSelector;
import org.exoplatform.portal.webui.navigation.UIPageNavigationForm;
import org.exoplatform.portal.webui.page.UIPageNodeForm;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/*
 * Created by The eXo Platform SAS
 * Author : tam.nguyen
 *          tamndrok@gmail.com
 * May 28, 2009  
 */
@ComponentConfigs({
   @ComponentConfig(template = "app:/groovy/navigation/webui/component/UIGroupNavigationManagement.gtmpl", events = {
      @EventConfig(listeners = UIGroupNavigationManagement.EditNavigationActionListener.class),
      @EventConfig(listeners = UIGroupNavigationManagement.EditPropertiesActionListener.class),
      @EventConfig(listeners = UIGroupNavigationManagement.AddNavigationActionListener.class),
      @EventConfig(listeners = UIGroupNavigationManagement.DeleteNavigationActionListener.class, confirm = "UIGroupNavigationManagement.Delete.Confirm")}),
   @ComponentConfig(id = "UIGroupNavigationGrid", type = UIRepeater.class, template = "app:/groovy/navigation/webui/component/UINavigationGrid.gtmpl"),
   @ComponentConfig(type = UIPageNodeForm.class, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
      @EventConfig(listeners = UIPageNodeForm.SaveActionListener.class),
      @EventConfig(listeners = UIGroupNavigationManagement.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SwitchVisibleActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.ClearPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.CreatePageActionListener.class, phase = Phase.DECODE)})})
public class UIGroupNavigationManagement extends UIContainer
{

   private List<PageNavigation> navigations;

   private PageNavigation selectedNavigation;

   public UIGroupNavigationManagement() throws Exception
   {
      UIVirtualList virtualList = addChild(UIVirtualList.class, null, "GroupNavigationList");
      virtualList.setPageSize(4);
      UIRepeater repeater = createUIComponent(UIRepeater.class, "UIGroupNavigationGrid", null);
      virtualList.setUIComponent(repeater);
      UIPopupWindow editNavigation = addChild(UIPopupWindow.class, null, null);
      editNavigation.setId(editNavigation.getId() + "-" + UUID.randomUUID().toString().replaceAll("-", ""));
   }

   public void loadNavigations() throws Exception
   {
      UserPortalConfigService userPortalConfigService = getApplicationComponent(UserPortalConfigService.class);
      navigations = userPortalConfigService.loadEditableNavigations();
      UIVirtualList virtualList = getChild(UIVirtualList.class);
      virtualList.dataBind(new ObjectPageList<PageNavigation>(navigations, navigations.size()));
   }

   public List<PageNavigation> getNavigations()
   {
      return navigations;
   }

   public void addPageNavigation(PageNavigation navigation)
   {
      if (navigations == null)
      {
         navigations = new ArrayList<PageNavigation>();
      }
      navigations.add(navigation);
   }

   public void deletePageNavigation(PageNavigation navigation)
   {
      if (navigations == null || navigations.size() < 1)
      {
         return;
      }
      navigations.remove(navigation);
   }

   public PageNavigation getPageNavigation(int id)
   {
      for (PageNavigation ele : getPageNavigations())
      {
         if (ele.getId() == id)
         {
            return ele;
         }
      }
      return null;
   }

   public List<PageNavigation> getPageNavigations()
   {
      if (navigations == null)
      {
         navigations = new ArrayList<PageNavigation>();
      }
      return navigations;
   }

   public PageNavigation getNavigationById(Integer navId)
   {
      PageNavigation navigation = new PageNavigation();
      for (PageNavigation nav : navigations)
      {
         if (nav.getId() == navId)
         {
            navigation = nav;
            break;
         }
      }
      return navigation;
   }

   public PageNavigation getSelectedNavigation()
   {
      return selectedNavigation;
   }

   public void setSelectedNavigation(PageNavigation navigation)
   {
      selectedNavigation = navigation;
   }

   /**
    * User has right to add navigation to a group in below cases
    * 
    * 1. He/She is member of admin groups
    * 
    * 2. He/She is manager of the group
    * 
    * @param pcontext
    * @return
    */
   private boolean userHasRightToAddNavigation()
   {
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      String remoteUser = pcontext.getRemoteUser();
      if (remoteUser == null)
      {
         return false;
      }

      UserACL userACL = this.getApplicationComponent(UserACL.class);
      if (userACL.isUserInGroup(userACL.getAdminGroups()))
      {
         return true;
      }

      OrganizationService orgService = this.getApplicationComponent(OrganizationService.class);
      try
      {
         Collection<?> groups = orgService.getGroupHandler().findGroupByMembership(remoteUser, userACL.getMakableMT());
         if (groups != null && groups.size() > 0)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      catch (Exception ex)
      {
         return false;
      }
   }
   
   static public class EditNavigationActionListener extends EventListener<UIGroupNavigationManagement>
   {
      public void execute(Event<UIGroupNavigationManagement> event) throws Exception
      {

         UIGroupNavigationManagement uicomp = event.getSource();

         // get navigation id
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         Integer navId = Integer.parseInt(id);
         // get PageNavigation by navigation id
         PageNavigation navigation = uicomp.getNavigationById(navId);
         uicomp.setSelectedNavigation(navigation);
         WebuiRequestContext context = event.getRequestContext();
         UIApplication uiApplication = context.getUIApplication();

         // check edit permission, ensure that user has edit permission on that
         // navigation
         UserACL userACL = uicomp.getApplicationComponent(UserACL.class);

         if (!userACL.hasEditPermission(navigation))
         {
            uiApplication.addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.Invalid-editPermission", null));
            return;
         }

         // ensure this navigation is exist
         DataStorage service = uicomp.getApplicationComponent(DataStorage.class);
         if (service.getPageNavigation(navigation.getOwnerType(), navigation.getOwnerId()) == null)
         {
            uiApplication.addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.navigation-not-exist", null));
            return;
         }

         UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);

         UINavigationManagement pageManager = popUp.createUIComponent(UINavigationManagement.class, null, null, popUp);
         pageManager.setOwner(navigation.getOwnerId());
         pageManager.setOwnerType(navigation.getOwnerType());

         UINavigationNodeSelector selector = pageManager.getChild(UINavigationNodeSelector.class);
       
         selector.setEdittedNavigation(navigation);
         selector.initTreeData();
         popUp.setUIComponent(pageManager);
         popUp.setWindowSize(400, 400);
         popUp.setShowMask(true);
         popUp.setShow(true);
      }
   }

   static public class EditPropertiesActionListener extends EventListener<UIGroupNavigationManagement>
   {
      public void execute(Event<UIGroupNavigationManagement> event) throws Exception
      {

         UIGroupNavigationManagement uicomp = event.getSource();

         // get navigation id
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         Integer navId = Integer.parseInt(id);

         // get PageNavigation by navigation id
         PageNavigation navigation = uicomp.getNavigationById(navId);

         // open a add navigation popup
         UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);
         UIPageNavigationForm pageNavigation = popUp.createUIComponent(UIPageNavigationForm.class, null, null, popUp);
         pageNavigation.setOwnerId(navigation.getOwnerId());
         pageNavigation.setOwnerType(navigation.getOwnerType());
         pageNavigation.setPriority(String.valueOf(navigation.getPriority()));
         pageNavigation.addFormInput();
         pageNavigation.setPageNav(navigation);
         popUp.setUIComponent(pageNavigation);
         popUp.setWindowSize(600, 400);
         popUp.setShowMask(true);
         popUp.setShow(true);
      }
   }

   static public class DeleteNavigationActionListener extends EventListener<UIGroupNavigationManagement>
   {
      public void execute(Event<UIGroupNavigationManagement> event) throws Exception
      {
         UIGroupNavigationManagement uicomp = event.getSource();

         WebuiRequestContext context = event.getRequestContext();
         UIApplication uiApplication = context.getUIApplication();

         // get navigation id
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         Integer navId = Integer.parseInt(id);

         // get PageNavigation by navigation id
         PageNavigation navigation = uicomp.getNavigationById(navId);

         // check edit permission, ensure that user has edit permission on that
         // navigation
         UserACL userACL = uicomp.getApplicationComponent(UserACL.class);

         if (!userACL.hasEditPermission(navigation))
         {
            uiApplication.addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.Invalid-editPermission", null));
            return;
         }

         // TODO ensure this navigation is exist
         DataStorage service = uicomp.getApplicationComponent(DataStorage.class);
         if (service.getPageNavigation(navigation.getOwnerType(), navigation.getOwnerId()) == null)
         {
            uiApplication.addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.navigation-not-exist", null));
            return;
         }

         // remove selected navigation
         if (uicomp.navigations == null || uicomp.navigations.size() < 1)
         {
            return;
         }
         uicomp.navigations.remove(navigation);

         // remove navigation from UIPortalApplication
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         removeNavigationByID(uiPortalApp.getUserPortalConfig().getNavigations(), navigation);
         removeNavigationByID(uiPortalApp.getNavigations(), navigation);
         
         
         service.remove(navigation);
         event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);

         //Update UserToolbarGroupPortlet
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);
         uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
      }

      private void removeNavigationByID(List<PageNavigation> navs, PageNavigation target)
      {
         if (navs == null)
         {
            return;
         }
         for (PageNavigation nav : navs)
         {
            if (nav.getId() == target.getId())
            {
               navs.remove(nav);
               return;
            }
         }
      }

   }

   static public class AddNavigationActionListener extends EventListener<UIGroupNavigationManagement>
   {
      public void execute(Event<UIGroupNavigationManagement> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         //UIGroupNavigationManagement uicomp = event.getSource();
         UIPortalApplication uiApp = (UIPortalApplication)prContext.getUIApplication();
         //UIGroupNavigationPortlet uiPortlet = (UIGroupNavigationPortlet) uicomp.getParent();
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         UIAddGroupNavigation uiNewPortal = uiMaskWS.createUIComponent(UIAddGroupNavigation.class, null, null);
         uiMaskWS.setUIComponent(uiNewPortal);
         uiMaskWS.setShow(true);
         prContext.addUIComponentToUpdateByAjax(uiMaskWS);

      }
   }

   static public class BackActionListener extends EventListener<UIPageNodeForm>
   {

      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
         UIPageNodeForm uiPageNodeForm = event.getSource();
         PageNavigation contextNavigation = uiPageNodeForm.getContextPageNavigation();
         
         UIGroupNavigationManagement uiGroupNavigation =
            uiPageNodeForm.getAncestorOfType(UIGroupNavigationManagement.class);
         PageNavigation selectedNavigation = uiGroupNavigation.getSelectedNavigation();
         UIPopupWindow uiNavigationPopup = uiGroupNavigation.getChild(UIPopupWindow.class);
         UINavigationManagement navigationManager =
            uiPageNodeForm.createUIComponent(UINavigationManagement.class, null, null);
         navigationManager.setOwner(contextNavigation.getOwnerId());
         navigationManager.setOwnerType(contextNavigation.getOwnerType());
         UINavigationNodeSelector selector = navigationManager.getChild(UINavigationNodeSelector.class);
         selector.setEdittedNavigation(contextNavigation);
         selector.initTreeData();
         
         if (uiPageNodeForm.getSelectedParent() instanceof PageNode)
         {
            PageNode selectedParent = (PageNode)uiPageNodeForm.getSelectedParent();
            selector.selectPageNodeByUri(selectedParent.getUri());
         }
         
         uiNavigationPopup.setUIComponent(navigationManager);
         uiNavigationPopup.setWindowSize(400, 400);
         uiNavigationPopup.setRendered(true);

         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigationPopup.getParent());
      }

   }
}

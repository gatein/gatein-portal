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

package org.exoplatform.portal.webui.portal;

import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.exoplatform.portal.webui.application.PortletState;
import org.exoplatform.portal.webui.application.UIApplicationList;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainerList;
import org.exoplatform.portal.webui.login.UILogin;
import org.exoplatform.portal.webui.login.UIResetPassword;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rss.parser.DefaultRSSChannel;
import org.exoplatform.services.rss.parser.DefaultRSSItem;
import org.exoplatform.services.rss.parser.RSSDocument;
import org.exoplatform.services.rss.parser.RSSParser;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

import java.net.URI;
import java.util.Date;
import java.util.List;

/** Author : Nhu Dinh Thuan nhudinhthuan@yahoo.com Jun 14, 2006 */
public class UIPortalComponentActionListener
{

   static public class ViewChildActionListener extends EventListener<UIContainer>
   {
      public void execute(Event<UIContainer> event) throws Exception
      {
         UIContainer uiContainer = event.getSource();
         String id = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         uiContainer.setRenderedChild(id);
      }
   }

   static public class ShowLoginFormActionListener extends EventListener<UIPortalComponent>
   {
      public void execute(Event<UIPortalComponent> event) throws Exception
      {
         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UILogin uiLogin = uiMaskWS.createUIComponent(UILogin.class, null, null);
         uiMaskWS.setUIComponent(uiLogin);
         uiMaskWS.setWindowSize(630, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

   static public class DeleteComponentActionListener extends EventListener<UIComponent>
   {
      private final static String UI_CONTAINER = "UIContainer";

      private final static String UI_PORTLET = "UIPortlet";

      public void execute(Event<UIComponent> event) throws Exception
      {
         String id = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIComponent uiComponent = event.getSource();
         UIPortalComponent uiParent = (UIPortalComponent)uiComponent.getParent();
         UIComponent uiRemoveComponent = uiParent.findComponentById(id);
         UIPortalApplication uiApp = Util.getUIPortalApplication();
         if (uiRemoveComponent.findFirstComponentOfType(UIPageBody.class) != null)
         {
            uiApp.addMessage(new ApplicationMessage("UIPortalApplication.msg.deletePageBody", new Object[]{},
               ApplicationMessage.WARNING));
            return;
         }

         uiParent.removeChildById(id);
         UIPortalComposer portalComposer = uiApp.findFirstComponentOfType(UIPortalComposer.class);
         portalComposer.setEditted(true);

         UIPage uiPage = uiParent.getAncestorOfType(UIPage.class);
         if (uiPage != null && uiPage.getMaximizedUIPortlet() != null)
         {
            if (id.equals(uiPage.getMaximizedUIPortlet().getId()))
            {
               uiPage.setMaximizedUIPortlet(null);
            }
         }
         else
         {
            UIPortal uiPortal = Util.getUIPortal();
            if (uiPortal != null && uiPortal.getMaximizedUIComponent() != null)
            {
               if (id.equals(uiPortal.getMaximizedUIComponent().getId()))
               {
                  uiPortal.setMaximizedUIComponent(null);
               }
            }
            else
            {
               UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
               if (uiPageBody != null && uiPageBody.getMaximizedUIComponent() != null)
               {
                  if (id.equals(uiPageBody.getMaximizedUIComponent().getId()))
                  {
                     uiPageBody.setMaximizedUIComponent(null);
                  }
               }
            }
         }
         Util.showComponentLayoutMode(uiRemoveComponent.getClass());

         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         String componentType = null;
         if (uiComponent instanceof UIPortlet)
         {
            componentType = UI_PORTLET;
         }
         else if (uiComponent instanceof org.exoplatform.portal.webui.container.UIContainer)
         {
            componentType = UI_CONTAINER;
            org.exoplatform.portal.webui.container.UIContainer topAncestor = getTopBlockContainer((org.exoplatform.portal.webui.container.UIContainer)uiParent);
            
            //Case of nested container like tab container, mixed container
            if(topAncestor != null){
               String topAncestorId = topAncestor.getId();
               //Add UIContainer- prefix to the id as it is required to be updated by Ajax
               if(!topAncestorId.startsWith("UIContainer-")){
                  topAncestor.setId("UIContainer-" + topAncestorId);
               }
               pcontext.addUIComponentToUpdateByAjax(topAncestor);
               return;
            }
         }

         if (componentType != null)
         {
            JavascriptManager jsManager = pcontext.getJavascriptManager();
            jsManager.addJavascript(scriptRemovingComponent(id, componentType));
            jsManager.addJavascript("eXo.portal.UIPortal.changeComposerSaveButton();");
         }
      }

      private String scriptRemovingComponent(String componentId, String componentType)
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("eXo.portal.UIPortal.removeComponent('");
         buffer.append(componentType);
         buffer.append("-");
         buffer.append(componentId);
         buffer.append("');");
         return buffer.toString();
      }

      /**
       * Returns the top ancestor( of type
       * org.exoplatform.portal.webui.container.UIContainer but not of type
       * UIPortal) of a given container
       */
      private static org.exoplatform.portal.webui.container.UIContainer getTopBlockContainer(
         org.exoplatform.portal.webui.container.UIContainer container)
      {
         if(container instanceof UIPortal){
            return null;
         }
         org.exoplatform.portal.webui.container.UIContainer topAncestor = container;
         org.exoplatform.portal.webui.container.UIContainer intermediateCont;
         try
         {
            intermediateCont = topAncestor.getParent();
            while (intermediateCont != null && !(intermediateCont instanceof UIPortal))
            {
               topAncestor = intermediateCont;
               intermediateCont = topAncestor.getParent();
            }
         }
         catch (ClassCastException ex)
         {

         }
         return topAncestor;
      }
   }

   static public class MoveChildActionListener extends EventListener<UIContainer>
   {
      public void execute(Event<UIContainer> event) throws Exception
      {
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         String insertPosition = pcontext.getRequestParameter("insertPosition");
         int position = -1;
         try
         {
            position = Integer.parseInt(insertPosition);
         }
         catch (Exception exp)
         {
            position = -1;
         }

         boolean newComponent = false;
         String paramNewComponent = pcontext.getRequestParameter("newComponent");

         if (paramNewComponent != null)
            newComponent = Boolean.valueOf(paramNewComponent).booleanValue();

         UIPortalApplication uiApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
         UIPortalComposer portalComposer = uiApp.findFirstComponentOfType(UIPortalComposer.class);

         if (newComponent)
         {
            portalComposer.updateWorkspaceComponent();
            pcontext.setFullRender(true);
         }

         UIWorkingWorkspace uiWorkingWS = uiApp.getChild(UIWorkingWorkspace.class);
         UIComponent uiWorking = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         if (!uiWorking.isRendered())
         {
            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
            uiWorking = uiEditWS.getUIComponent();
         }

         String sourceId = pcontext.getRequestParameter("srcID");
         UIComponent uiSource = uiWorking.findComponentById(sourceId);

         UIContainer uiTarget = uiWorking.findComponentById(pcontext.getRequestParameter("targetID"));
         if (position < 0 && uiTarget.getChildren().size() > 0)
         {
            position = uiTarget.getChildren().size();
         }
         else if (position < 0)
         {
            position = 0;
         }

         if (uiSource == null)
         {
            UITabPane subTabPane = portalComposer.getChild(UITabPane.class);
            UIContainerList uiContainerConfig = subTabPane.getChild(UIContainerList.class);
            if (uiContainerConfig != null && subTabPane.getSelectedTabId().equals(uiContainerConfig.getId()))
            {
               org.exoplatform.portal.webui.container.UIContainer uiContainer =
                  uiTarget.createUIComponent(org.exoplatform.portal.webui.container.UIContainer.class, null, null);
               Container container = uiContainerConfig.getContainer(sourceId);
               container.setId(String.valueOf(container.hashCode()));
               uiContainer.setStorageId(container.getStorageId());
               PortalDataMapper.toUIContainer(uiContainer, container);
               String[] accessPers = uiContainer.getAccessPermissions();
               for (String accessPer : accessPers)
               {
                  if (accessPer.equals(""))
                     accessPer = null;
               }
               if (accessPers == null || accessPers.length == 0)
                  accessPers = new String[]{UserACL.EVERYONE};
               uiContainer.setAccessPermissions(accessPers);
               uiSource = uiContainer;
            }
            else
            {
               Application app = null;
               UIApplicationList appList = uiApp.findFirstComponentOfType(UIApplicationList.class);
               app = appList.getApplication(sourceId);
               ApplicationType applicationType = app.getType();

               // TanPD: Hardcoded to fix bug GTNPORTAL-91
               Application temp = null;
               if (applicationType.equals(ApplicationType.GADGET))
               {
                  applicationType = ApplicationType.PORTLET;
                  temp = app;
                  app = appList.getApplication("dashboard/Gadget_Wrapper_Portlet");
               }

               //
               UIPortlet uiPortlet = uiTarget.createUIComponent(UIPortlet.class, null, null);
               if (app.getDisplayName() != null)
               {
                  uiPortlet.setTitle(app.getDisplayName());
               }
               else if (app.getApplicationName() != null)
               {
                  uiPortlet.setTitle(app.getApplicationName());
               }
               uiPortlet.setDescription(app.getDescription());
               List<String> accessPersList = app.getAccessPermissions();
               String[] accessPers = accessPersList.toArray(new String[accessPersList.size()]);
               for (String accessPer : accessPers)
               {
                  if (accessPer.equals(""))
                     accessPers = null;
               }
               if (accessPers == null || accessPers.length == 0)
                  accessPers = new String[]{UserACL.EVERYONE};
               uiPortlet.setAccessPermissions(accessPers);
               UIPage uiPage = uiTarget.getAncestorOfType(UIPage.class);

               //
               CloneApplicationState state = new CloneApplicationState<Object>(app.getStorageId());

               //
               uiPortlet.setState(new PortletState(state, applicationType));

               // TanPD: Fix bug GTNPORTAL-91
               if (temp != null && applicationType.equals(ApplicationType.PORTLET))
               {
                  Portlet pref = uiPortlet.getPreferences();
                  try
                  {
                     UIGadget uiGadget = uiPortlet.createUIComponent(UIGadget.class, null, null);
                     uiGadget.setState(new TransientApplicationState<Gadget>(temp.getApplicationName()));
                     pref.setValue("url", uiGadget.getUrl());
                  }
                  catch (Exception e)
                  {
                     // Fix in case: RSS Reader Gadget
                     Preference aggIdPref = pref.getPreference("aggregatorId");
                     String aggregatorId = null;
                     if (aggIdPref == null || aggIdPref.getValue() == null || aggIdPref.getValue().length() == 0)
                        aggregatorId = "rssAggregator";
                     else
                        aggregatorId = aggIdPref.getValue();
                     GadgetRegistryService gadgetSrv = uiApp.getApplicationComponent(GadgetRegistryService.class);
                     org.exoplatform.application.gadget.Gadget gadget = gadgetSrv.getGadget(aggregatorId);
                     // TODO make sure it's an rss feed
                     // TODO make sure that we did not add it already
                     UIGadget uiGadget = uiPortlet.createUIComponent(UIGadget.class, null, null);
                     uiGadget.setState(new TransientApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget>(
                        gadget.getName()));
                     pref.setValue("url", uiGadget.getUrl());
                  }
               }

               uiPortlet.setPortletInPortal(uiTarget instanceof UIPortal);
               uiPortlet.setShowEditControl(true);
               uiSource = uiPortlet;

            }
            List<UIComponent> children = uiTarget.getChildren();
            uiSource.setParent(uiTarget);
            children.add(position, uiSource);
            return;
         }

         UIContainer uiParent = uiSource.getParent();
         if (uiParent == uiTarget)
         {
            int currentIdx = uiTarget.getChildren().indexOf(uiSource);
            if (position <= currentIdx)
            {
               uiTarget.getChildren().add(position, uiSource);
               currentIdx++;
               uiTarget.getChildren().remove(currentIdx);
               return;
            }
            uiTarget.getChildren().remove(currentIdx);
            if (position >= uiTarget.getChildren().size())
            {
               position = uiTarget.getChildren().size();
            }
            uiTarget.getChildren().add(position, uiSource);
            return;
         }
         uiParent.getChildren().remove(uiSource);
         uiTarget.getChildren().add(position, uiSource);
         uiSource.setParent(uiTarget);
      }
   }

   public static class ChangeLanguageActionListener extends EventListener<UIPortal>
   {

      @Override
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWorkspace = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         uiMaskWorkspace.createUIComponent(UILanguageSelector.class, null, null);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWorkspace);
      }

   }

   public static class RecoveryPasswordAndUsernameActionListener extends EventListener<UIPortal>
   {
      @Override
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         String date = event.getRequestContext().getRequestParameter("datesend");
         String email = event.getRequestContext().getRequestParameter("email");
         OrganizationService orgSrc = uiPortal.getApplicationComponent(OrganizationService.class);
         // get user
         PageList userPageList = orgSrc.getUserHandler().findUsers(new Query());
         List userList = userPageList.currentPage();
         User user = null;
         for (int i = 0; i < userList.size(); i++)
         {
            User tmpUser = (User)userList.get(i);
            if (tmpUser.getEmail().equals(email))
            {
               user = tmpUser;
               break;
            }
         }
         if (user == null)
         {
            throw new MessageException(new ApplicationMessage("UIForgetPassword.msg.user-delete", null));
         }
         // delete link active by one day
         long now = new Date().getTime();
         if (now - Long.parseLong(date) > 86400000)
         {
            user.setPassword(Long.toString(now));
            orgSrc.getUserHandler().saveUser(user, true);
            throw new MessageException(new ApplicationMessage("UIForgetPassword.msg.expration", null));
         }
         UIResetPassword uiReset = uiMaskWS.createUIComponent(UIResetPassword.class, null, null);
         uiReset.setData(user);
         uiMaskWS.setUIComponent(uiReset);
         uiMaskWS.setWindowSize(630, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

   static public class ChangeSkinActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         UISkinSelector uiChangeSkin = uiMaskWS.createUIComponent(UISkinSelector.class, null, null);
         uiMaskWS.setUIComponent(uiChangeSkin);
         uiMaskWS.setWindowSize(640, 400);
         uiMaskWS.setShow(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

   static public class ChangeApplicationListActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         UIPortalApplication application = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIPortalComposer composer = application.findFirstComponentOfType(UIPortalComposer.class);
         UITabPane uiTabPane = composer.getChild(UITabPane.class);
         String appListId = uiTabPane.getChild(UIApplicationList.class).getId();
         uiTabPane.replaceChild(appListId, composer.createUIComponent(UIApplicationList.class, null, null));
      }
   }

   public static class EditPortalPropertiesActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         String portalName = event.getRequestContext().getRequestParameter("portalName");

         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);

         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPortalForm portalForm = uiMaskWS.createUIComponent(UIPortalForm.class, null, "UIPortalForm");
         portalForm.setPortalOwner(portalName);
         uiMaskWS.setWindowSize(700, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);

      }
   }
}

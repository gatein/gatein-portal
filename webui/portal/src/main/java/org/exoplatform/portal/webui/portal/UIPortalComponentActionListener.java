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

import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.webui.application.PortletState;
import org.exoplatform.portal.webui.application.UIApplicationList;
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
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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
      private final static String UI_CONTAINER_PREFIX = "UIContainer-";

      private final static String UI_PORTLET_PREFIX = "UIPortlet-";

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

         // Case1: current component is a portlet
         if (uiComponent instanceof UIPortlet)
         {
            removeComponent(id, UI_PORTLET_PREFIX, pcontext);
            return;
         }

         // Case 2: current component is a container
         if (uiComponent instanceof org.exoplatform.portal.webui.container.UIContainer)
         {
            org.exoplatform.portal.webui.container.UIContainer topAncestor =
               getTopBlockContainer((org.exoplatform.portal.webui.container.UIContainer)uiParent);

            /**
             * topAncestor is null if the uiParent is either UIPortal or UIPage,
             * that happens when our container is a simple container
             */
            if (topAncestor == null)
            {
               removeComponent(id, UI_CONTAINER_PREFIX, pcontext);
               return;
            }
            /** Case of nested container like tab container, mixed container */
            else
            {
               String topAncestorId = topAncestor.getId();

               /** If the topAncestor has no child, then it is removed */
               if (topAncestor.getChildren().size() == 0)
               {
                  /** Update server-side */
                  UIContainer parentOfTopAncestor = topAncestor.getParent();
                  parentOfTopAncestor.removeChildById(topAncestorId);

                  /** Update client side */
                  if (topAncestorId.startsWith(UI_CONTAINER_PREFIX))
                  {
                     topAncestorId = topAncestorId.substring(UI_CONTAINER_PREFIX.length());
                     topAncestor.setId(topAncestorId);
                  }
                  removeComponent(topAncestorId, UI_CONTAINER_PREFIX, pcontext);
                  return;
               }

               /**
                * If the uiParent is not the topAncestor and having no child,
                * then it is removed
                */
               if (uiParent.getChildren().size() == 0)
               {
                  /** Update server-side */
                  UIContainer itsParent = uiParent.getParent();
                  itsParent.removeChildById(uiParent.getId());
               }

               /**
                * Update the topAncestor by Ajax
                */
               if (!topAncestorId.startsWith(UI_CONTAINER_PREFIX))
               {
                  topAncestor.setId(UI_CONTAINER_PREFIX + topAncestorId);
               }
               pcontext.addUIComponentToUpdateByAjax(topAncestor);
               return;
            }
         }

      }

      /** Add Javascript script to remove component */
      private void removeComponent(String componentId, String componentType, PortalRequestContext pcontext)
      {
         String scriptRemovingComponent = scriptRemovingComponent(componentId, componentType);
         JavascriptManager jsManager = pcontext.getJavascriptManager();
         jsManager.addJavascript(scriptRemovingComponent);
         jsManager.addJavascript("eXo.portal.UIPortal.changeComposerSaveButton();");
      }

      private String scriptRemovingComponent(String componentId, String prefix)
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("eXo.portal.UIPortal.removeComponent('");
         buffer.append(prefix);
         buffer.append(componentId);
         buffer.append("');");
         return buffer.toString();
      }

      /**
       * Returns the top ancestor( of type
       * org.exoplatform.portal.webui.container.UIContainer but not of type
       * UIPortal or UIPage) of a given container
       */
      private static org.exoplatform.portal.webui.container.UIContainer getTopBlockContainer(
         org.exoplatform.portal.webui.container.UIContainer container)
      {
         if (container instanceof UIPortal || container instanceof UIPage)
         {
            return null;
         }
         org.exoplatform.portal.webui.container.UIContainer topAncestor = container;
         org.exoplatform.portal.webui.container.UIContainer intermediateCont;
         try
         {
            intermediateCont = topAncestor.getParent();
            while (intermediateCont != null && !(intermediateCont instanceof UIPortal)
               && !(intermediateCont instanceof UIPage))
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

               // Hardcode on state to fix error while drag/drop Dashboard
               if ("dashboard/DashboardPortlet".equals(app.getContentId()))
               {
                  TransientApplicationState state = new TransientApplicationState<Object>(app.getContentId());
                  uiPortlet.setState(new PortletState(state, applicationType));
               }
               else
               {
                  CloneApplicationState state = new CloneApplicationState<Object>(app.getStorageId());
                  uiPortlet.setState(new PortletState(state, applicationType));
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
         RemindPasswordTokenService tokenService = uiPortal.getApplicationComponent(RemindPasswordTokenService.class);
         String tokenId = event.getRequestContext().getRequestParameter("tokenId");

         GateInToken token = tokenService.getToken(tokenId);
         
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         OrganizationService orgSrc = uiPortal.getApplicationComponent(OrganizationService.class);
         // get user
         User user = orgSrc.getUserHandler().findUserByName(token.getPayload().getUsername());

         UIResetPassword uiReset = uiMaskWS.createUIComponent(UIResetPassword.class, null, null);
         uiReset.setUser(user);
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
         UserPortalConfigService service = uiApp.getApplicationComponent(UserPortalConfigService.class);
         if (portalName != null
            && service.getUserPortalConfig(portalName, event.getRequestContext().getRemoteUser()) == null)
         {
            uiApp.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist", new String[]{portalName}));
            return;
         }

         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPortalForm portalForm = uiMaskWS.createUIComponent(UIPortalForm.class, null, "UIPortalForm");
         portalForm.setPortalOwner(portalName);
         portalForm.setBindingBean();
         uiMaskWS.setWindowSize(700, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);

      }
   }
}

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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.application.UIApplicationList;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainerList;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageForm;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Pham Thanh Tung
 * thanhtungty@gmail.com Jun 10, 2009
 */
@ComponentConfigs({
   @ComponentConfig(template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
      @EventConfig(listeners = UIPortalComposer.ViewPropertiesActionListener.class),
      @EventConfig(listeners = UIPortalComposer.AbortActionListener.class),
      @EventConfig(listeners = UIPortalComposer.FinishActionListener.class),
      @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class)}),
   @ComponentConfig(id = "UIPageEditor", template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
      @EventConfig(name = "ViewProperties", listeners = UIPortalComposer.ViewProperties2ActionListener.class),
      @EventConfig(name = "Abort", listeners = UIPortalComposer.Abort2ActionListener.class),
      @EventConfig(name = "Finish", listeners = UIPortalComposer.Finish2ActionListener.class),
      @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class)}),
   @ComponentConfig(id = "UIPortalComposerTab", type = UITabPane.class, template = "app:/groovy/portal/webui/portal/UIPortalComposerContent.gtmpl", events = {@EventConfig(listeners = UIPortalComposer.SelectTabActionListener.class)})})
public class UIPortalComposer extends UIContainer
{

   private boolean isEditted = false;

   private boolean isCollapse = false;

   private boolean isShowControl = true;

   public UIPortalComposer() throws Exception
   {
      UITabPane uiTabPane = addChild(UITabPane.class, "UIPortalComposerTab", null);
      uiTabPane.addChild(UIApplicationList.class, null, null).setRendered(true);
      uiTabPane.addChild(UIContainerList.class, null, null);
      uiTabPane.setSelectedTab(1);
   }

   public void setPortalMode(int mode)
   {
      if (mode < 0 || mode > 4)
         return;
      getAncestorOfType(UIPortalApplication.class).setModeState(mode);
   }

   public int getPortalMode()
   {
      return getAncestorOfType(UIPortalApplication.class).getModeState();
   }

   public boolean isEditted()
   {
      return isEditted;
   }

   public void setEditted(boolean b)
   {
      isEditted = b;
   }

   public boolean isCollapse()
   {
      return isCollapse;
   }

   public void setCollapse(boolean isCollapse)
   {
      this.isCollapse = isCollapse;
   }

   public boolean isShowControl()
   {
      return isShowControl;
   }

   public void setShowControl(boolean state)
   {
      isShowControl = state;
   }

   public void save() throws Exception
   {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
      UIWorkingWorkspace uiWorkingWS = uiPortalApp.findFirstComponentOfType(UIWorkingWorkspace.class);
      UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
      UIPortal editPortal = (UIPortal)uiEditWS.getUIComponent();
      UIPortal uiPortal = Util.getUIPortal();

      PortalConfig portalConfig = (PortalConfig)PortalDataMapper.buildModelObject(editPortal);
      UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);
      configService.update(portalConfig);
      uiPortalApp.getUserPortalConfig().setPortal(portalConfig);
      String remoteUser = prContext.getRemoteUser();
      String ownerUser = prContext.getPortalOwner();
      UserPortalConfig userPortalConfig = configService.getUserPortalConfig(ownerUser, remoteUser);
      if (userPortalConfig != null)
      {
         editPortal.setModifiable(userPortalConfig.getPortalConfig().isModifiable());
      }
      else
      {
         editPortal.setModifiable(false);
      }
      LocaleConfigService localeConfigService = uiPortalApp.getApplicationComponent(LocaleConfigService.class);
      LocaleConfig localeConfig = localeConfigService.getLocaleConfig(portalConfig.getLocale());
      if (localeConfig == null)
         localeConfig = localeConfigService.getDefaultLocaleConfig();
      // TODO dang.tung - change layout when portal get language from UIPortal
      // (user and browser not support)
      // ----------------------------------------------------------------------------------------------------
      String portalAppLanguage = uiPortalApp.getLocale().getLanguage();
      OrganizationService orgService = getApplicationComponent(OrganizationService.class);
      UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(remoteUser);
      String userLanguage = userProfile.getUserInfoMap().get("user.language");
      String browserLanguage = prContext.getRequest().getLocale().getLanguage();

      // in case: edit current portal, set skin and language for uiPortalApp
      if (uiPortal == null)
      {
         if (!portalAppLanguage.equals(userLanguage) && !portalAppLanguage.equals(browserLanguage))
         {
            uiPortalApp.setLocale(localeConfig.getLocale());
            editPortal.refreshNavigation(localeConfig.getLocale());
         }
         uiPortalApp.setSkin(editPortal.getSkin());
      }
      prContext.refreshResourceBundle();
      SkinService skinService = getApplicationComponent(SkinService.class);
      skinService.invalidatePortalSkinCache(editPortal.getName(), editPortal.getSkin());
   }

   public void updateWorkspaceComponent() throws Exception
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      UIEditInlineWorkspace uiEditWS = uiApp.findFirstComponentOfType(UIEditInlineWorkspace.class);
      List<UIComponent> children = uiEditWS.getChildren();
      for (UIComponent child : children)
      {
         if (!child.isRendered() || child.getClass().equals(UIPortalComposer.class))
            continue;
         rcontext.addUIComponentToUpdateByAjax(child);
      }
      int portalMode = uiApp.getModeState();
      if (portalMode != UIPortalApplication.NORMAL_MODE)
      {
         if (portalMode % 2 != 0)
            Util.showComponentLayoutMode(UIPortlet.class);
         else
            Util.showComponentEditInViewMode(UIPortlet.class);
      }
      JavascriptManager jsManager = Util.getPortalRequestContext().getJavascriptManager();
      jsManager.addJavascript("eXo.portal.portalMode=" + portalMode + ";");
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      int portalMode = uiPortalApp.getModeState();
      if (portalMode != UIPortalApplication.NORMAL_MODE)
      {
         UITabPane uiTabPane = this.getChild(UITabPane.class);
         UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
         if (uiComponent instanceof UIApplicationList)
         {
            if (portalMode == UIPortalApplication.APP_VIEW_EDIT_MODE)
            {
               Util.showComponentEditInViewMode(UIPortlet.class);
            }
            else
            {
               uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
               Util.showComponentLayoutMode(UIPortlet.class);
            }
         }
         else if (uiComponent instanceof UIContainerList)
         {
            if (portalMode == UIPortalApplication.CONTAINER_VIEW_EDIT_MODE)
            {
               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
            }
            else
            {
               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
            }
         }
      }
      super.processRender(context);
   }

   static public class ViewPropertiesActionListener extends EventListener<UIPortalComposer>
   {

      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);

         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPortalForm portalForm = uiMaskWS.createUIComponent(UIPortalForm.class, null, "UIPortalForm");
         portalForm.setPortalOwner(((PortalRequestContext)WebuiRequestContext.getCurrentInstance()).getPortalOwner());
         uiMaskWS.setWindowSize(700, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

   static public class AbortActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
         uiEditWS.getComposer().setEditted(false);

         UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
         UIPortal uiEditPortal = (UIPortal)uiEditWS.getUIComponent();
         UIPortal uiPortal = (UIPortal)siteBody.getUIComponent();

         String uri = null;
         if (uiPortal == null)
         {
            uri = (uiEditPortal.getSelectedNode() != null) ? uiEditPortal.getSelectedNode().getUri() : null;
            UserPortalConfigService configService = uiPortalApp.getApplicationComponent(UserPortalConfigService.class);
            String remoteUser = prContext.getRemoteUser();
            String ownerUser = prContext.getPortalOwner();
            UserPortalConfig userPortalConfig = configService.getUserPortalConfig(ownerUser, remoteUser);
            UIPortal newPortal = uiWorkingWS.createUIComponent(UIPortal.class, null, null);
            PortalDataMapper.toUIPortal(newPortal, userPortalConfig);
            siteBody.setUIComponent(newPortal);
         }
         else
         {
            siteBody.setUIComponent(uiEditPortal);
         }
         // uiEditWS.setUIComponent(null);
         // uiWorkingWS.removeChild(UIEditInlineWorkspace.class);
         uiWorkingWS.getChild(UIEditInlineWorkspace.class).setRendered(false);

         uiPortal = (UIPortal)siteBody.getUIComponent();
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);

         if (uri == null)
            uri = (uiPortal.getSelectedNode() != null) ? uiPortal.getSelectedNode().getUri() : null;
         PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         JavascriptManager jsManager = prContext.getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }

   }

   static public class FinishActionListener extends EventListener<UIPortalComposer>
   {

      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalComposer uiComposer = event.getSource();
         uiComposer.save();
         uiComposer.setEditted(false);

         PortalRequestContext prContext = Util.getPortalRequestContext();

         UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
         UIPortal editPortal = (UIPortal)uiEditWS.getUIComponent();

         UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
         UIPortal uiPortal = (UIPortal)siteBody.getUIComponent();

         String uri = null;
         if (uiPortal == null)
            siteBody.setUIComponent(editPortal);
         // uiEditWS.setUIComponent(null);
         // uiWorkingWS.removeChild(UIEditInlineWorkspace.class);
         uiWorkingWS.getChild(UIEditInlineWorkspace.class).setRendered(false);
         uiPortal = (UIPortal)siteBody.getUIComponent();

         if (PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive()))
            uiPortalApp.setSessionOpen(true);
         else
            uiPortalApp.setSessionOpen(false);
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);

         if (uri == null)
            uri = uiPortal.getSelectedNode() != null ? uiPortal.getSelectedNode().getUri() : null;
         PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         JavascriptManager jsManager = prContext.getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }

   }

   static public class SelectTabActionListener extends UITabPane.SelectTabActionListener
   {
      public void execute(Event<UITabPane> event) throws Exception
      {
         super.execute(event);
         UITabPane uiTabPane = event.getSource();
         UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         int portalMode = uiPortalApp.getModeState();

         if (uiComponent instanceof UIApplicationList)
         { // Swicth to Porlets Tab
            if (portalMode % 2 == 0)
            {
               uiPortalApp.setModeState(UIPortalApplication.APP_VIEW_EDIT_MODE);
               Util.showComponentEditInViewMode(UIPortlet.class);
            }
            else
            {
               uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
               Util.showComponentLayoutMode(UIPortlet.class);
            }
         }
         else if (uiComponent instanceof UIContainerList)
         { // Swicth to
            // Containers Tab
            if (portalMode % 2 == 0)
            {
               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_VIEW_EDIT_MODE);
               Util.showComponentEditInViewMode(org.exoplatform.portal.webui.container.UIContainer.class);
            }
            else
            {
               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
               Util.showComponentLayoutMode(org.exoplatform.portal.webui.container.UIContainer.class);
            }
         }
         event.getRequestContext().addUIComponentToUpdateByAjax(
            Util.getUIPortalApplication().getChildById(UIPortalApplication.UI_WORKING_WS_ID));
      }
   }

   static public class SwitchModeActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         int portalMode = uiPortalApp.getModeState();

         switch (portalMode)
         {
            case UIPortalApplication.APP_BLOCK_EDIT_MODE :
               uiPortalApp.setModeState(UIPortalApplication.APP_VIEW_EDIT_MODE);
               break;
            case UIPortalApplication.APP_VIEW_EDIT_MODE :
               uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
               break;
            case UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE :
               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_VIEW_EDIT_MODE);
               break;
            case UIPortalApplication.CONTAINER_VIEW_EDIT_MODE :
               uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
               break;
            default :
               uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
               return;
         }

         event.getSource().updateWorkspaceComponent();
         Util.getPortalRequestContext().setFullRender(true);
      }
   }

   static public class ChangeEdittedStateActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event)
      {
         UIPortalComposer uiComposer = event.getSource();
         uiComposer.setEditted(true);
      }
   }

   static public class ToggleActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalComposer uiComposer = event.getSource();
         uiComposer.setCollapse(!uiComposer.isCollapse());
         event.getRequestContext().addUIComponentToUpdateByAjax(uiComposer);
      }
   }

   // -----------------------------Page's
   // Listeners------------------------------------------//

   static public class ViewProperties2ActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
         UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPageForm uiPageForm = uiPortalApp.createUIComponent(UIPageForm.class, null, null);

         UIComponent uiPage = uiToolPanel.findFirstComponentOfType(UIPage.class);
         uiPageForm.setValues((UIPage)uiPage);
         uiMaskWS.setUIComponent(uiPageForm);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

   static public class Abort2ActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
         UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         uiToolPanel.setUIComponent(null);
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);

         UIPortal uiPortal = Util.getUIPortal();
         uiPortal.setRenderSibbling(UIPortal.class);
         UIPortalComposer composer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         composer.setEditted(false);

         PageNodeEvent<UIPortal> pnevent =
            new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, (uiPortal.getSelectedNode() != null
               ? uiPortal.getSelectedNode().getUri() : null));
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }
   }

   static public class Finish2ActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
         UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         UIPortalComposer composer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         composer.setEditted(false);
         UIPage uiPage = uiToolPanel.findFirstComponentOfType(UIPage.class);

         //
         Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
         UserPortalConfigService portalConfigService =
            uiWorkingWS.getApplicationComponent(UserPortalConfigService.class);

         // Perform mop update
         List<ModelChange> changes = portalConfigService.update(page);

         uiToolPanel.setUIComponent(null);
         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         if (PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive()))
            uiPortalApp.setSessionOpen(true);
         else
            uiPortalApp.setSessionOpen(false);
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         PageNodeEvent<UIPortal> pnevent =
            new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, (uiPortal.getSelectedNode() != null
               ? uiPortal.getSelectedNode().getUri() : null));
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }
   }
}

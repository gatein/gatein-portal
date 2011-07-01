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

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.StaleModelException;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.application.UIPortlet;
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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/** Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Jun 10, 2009 */
@ComponentConfigs({
   @ComponentConfig(template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
      @EventConfig(listeners = UIPortalComposer.ViewPropertiesActionListener.class),
      @EventConfig(listeners = UIPortalComposer.CloseComposerActionListener.class),
      @EventConfig(listeners = UIPortalComposer.AbortActionListener.class),
      @EventConfig(listeners = UIPortalComposer.FinishActionListener.class),
      @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class)}),
   @ComponentConfig(id = "UIPageEditor", template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
      @EventConfig(name = "ViewProperties", listeners = UIPortalComposer.ViewProperties2ActionListener.class),
      @EventConfig(listeners = UIPortalComposer.CloseComposerActionListener.class),
      @EventConfig(name = "Abort", listeners = UIPortalComposer.Abort2ActionListener.class),
      @EventConfig(name = "Finish", listeners = UIPortalComposer.Finish2ActionListener.class),
      @EventConfig(name = "Back", listeners = UIPortalComposer.BackActionListener.class),
      @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
      @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class)}),
   @ComponentConfig(id = "UIPortalComposerTab", type = UITabPane.class, template = "app:/groovy/portal/webui/portal/UIPortalComposerContent.gtmpl", events = {@EventConfig(listeners = UIPortalComposer.SelectTabActionListener.class)})})
public class UIPortalComposer extends UIContainer
{

   private boolean isEditted = false;

   private boolean isCollapsed = false;

   private boolean isShowControl = true;

   public UIPortalComposer() throws Exception
   {
      UITabPane uiTabPane = addChild(UITabPane.class, "UIPortalComposerTab", null);
      uiTabPane.addChild(UIApplicationList.class, null, null).setRendered(true);
      uiTabPane.addChild(UIContainerList.class, null, null);
      uiTabPane.setSelectedTab(1);     
   }

   public int getPortalMode()
   {
      return getAncestorOfType(UIPortalApplication.class).getModeState();
   }

   /**
    * Returns <code>true</code> there was at least one change
    * has been done in this edition time.
    * 
    * <p>This value is used in the template of this component 
    * to notice the user if there was something has been changed
    * 
    * @return
    */
   public boolean isEditted()
   {
      return isEditted;
   }

   public void setEditted(boolean b)
   {
      isEditted = b;
   }

   /**
    * Return a value of <code>boolean</code> to tell current state of the composer
    * 
    * @return <code>true</code> if the composer is collapsed currently
    */
   public boolean isCollapse()
   {
      return isCollapsed;
   }

   public void setCollapse(boolean isCollapse)
   {
      this.isCollapsed = isCollapse;
   }

   public boolean isShowControl()
   {
      return isShowControl;
   }

   public void setShowControl(boolean state)
   {
      isShowControl = state;
   }

   /**
    * Return true if the edition is in the page creation wizard
    * 
    * @return
    */
   private boolean isUsedInWizard()
   {
      UIWorkingWorkspace uiWorkingWS = getAncestorOfType(UIWorkingWorkspace.class);
      UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
      UIComponent uicomponent = uiToolPanel.getUIComponent();
      if (uicomponent != null && uicomponent instanceof UIWizard)
      {
         return true;
      }
      return false;
   }

   /**
    * Perform saving changes of the edition of SiteConfig into database
    * 
    * @throws Exception if there is anything wrong in saving process
    */
   private void save() throws Exception
   {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
      UIWorkingWorkspace uiWorkingWS = uiPortalApp.findFirstComponentOfType(UIWorkingWorkspace.class);
      UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
      UIPortal editPortal = (UIPortal)uiEditWS.getUIComponent();
      UIPortal uiPortal = Util.getUIPortal();
      String remoteUser = prContext.getRemoteUser();
      String ownerUser = prContext.getPortalOwner();

      PortalConfig portalConfig = (PortalConfig)PortalDataMapper.buildModelObject(editPortal);
      UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);
      DataStorage dataStorage = getApplicationComponent(DataStorage.class);

      if (!isPortalExist(editPortal))
      {
         return;
      }
      
      SkinService skinService = getApplicationComponent(SkinService.class);
      skinService.invalidatePortalSkinCache(editPortal.getName(), editPortal.getSkin());
      try
      {
         dataStorage.save(portalConfig);
      }
      catch (StaleModelException ex)
      {
         //Temporary solution for concurrency-related issue. The StaleModelException should be
         //caught in the ApplicationLifecycle
         rebuildUIPortal(uiPortalApp, editPortal, dataStorage);
      }
      uiPortalApp.getUserPortalConfig().setPortal(portalConfig);
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
      {
         localeConfig = localeConfigService.getDefaultLocaleConfig();
      }
      // TODO dang.tung - change layout when portal get language from UIPortal
      // (user and browser not support)
      // ----------------------------------------------------------------------------------------------------
      String portalAppLanguage = prContext.getLocale().getLanguage();
      OrganizationService orgService = getApplicationComponent(OrganizationService.class);
      UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(remoteUser);
      String userLanguage = userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
      String browserLanguage = prContext.getRequest().getLocale().getLanguage();

      // in case: edit current portal, set skin and language for uiPortalApp
      if (uiPortal == null)
      {
         if (!portalAppLanguage.equals(userLanguage) && !portalAppLanguage.equals(browserLanguage))
         {
            prContext.setLocale(localeConfig.getLocale());
            //editPortal.refreshNavigation(localeConfig.getLocale());
            //uiPortalApp.localizeNavigations();
         }
         uiPortalApp.setSkin(editPortal.getSkin());
      }
      prContext.refreshResourceBundle();
   }

   private void rebuildUIPortal(UIPortalApplication uiPortalApp, UIPortal uiPortal, DataStorage storage) throws Exception
   {
      PortalConfig portalConfig = storage.getPortalConfig(uiPortal.getOwnerType(), uiPortal.getOwner());
      UserPortalConfig userPortalConfig = uiPortalApp.getUserPortalConfig();
      userPortalConfig.setPortal(portalConfig);
      uiPortal.getChildren().clear();
      PortalDataMapper.toUIPortal(uiPortal, userPortalConfig);
      
      uiPortalApp.putCachedUIPortal(uiPortal);
      
   }
   /**
    * Check the <code>editPortal</code> whether it is existing in database or not
    * 
    * @param editPortal
    * @return
    * @throws Exception
    */
   private boolean isPortalExist(UIPortal editPortal) throws Exception
   {
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();

      String portalOwner = null;
      if (editPortal.getOwnerType().equals(PortalConfig.PORTAL_TYPE))
      {
         portalOwner = editPortal.getOwner();
      }
      else
      {
         portalOwner = Util.getPortalRequestContext().getPortalOwner();
      }

      UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);

      return configService.getUserPortalConfig(portalOwner, remoteUser) != null;
   }

   /**
    * Updates the availability children of the UIEditInlineWorkspace except to the UIPortalComposer
    * 
    * @throws Exception
    */
   public void updateWorkspaceComponent() throws Exception
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      UIEditInlineWorkspace uiEditWS = uiApp.findFirstComponentOfType(UIEditInlineWorkspace.class);
      List<UIComponent> children = uiEditWS.getChildren();
      for (UIComponent child : children)
      {
         if (!child.isRendered() || child.getClass().equals(UIPortalComposer.class))
         {
            continue;
         }
         rcontext.addUIComponentToUpdateByAjax(child);
      }
      int portalMode = uiApp.getModeState();
      if (portalMode != UIPortalApplication.NORMAL_MODE)
      {
         switch(portalMode)
         {
            case UIPortalApplication.APP_BLOCK_EDIT_MODE:
            case UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE:
               Util.showComponentLayoutMode(UIPortlet.class);
               break;
            case UIPortalApplication.APP_VIEW_EDIT_MODE:
            case UIPortalApplication.CONTAINER_VIEW_EDIT_MODE:
               Util.showComponentEditInViewMode(UIPortlet.class);
               break;
         }
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
         UIComponent temp = null;
         UIPortal uiPortal = null;
         String portalOwner = null;
         UIEditInlineWorkspace uiEditWS = event.getSource().getAncestorOfType(UIEditInlineWorkspace.class);
         temp = uiEditWS.getUIComponent();
         if (temp != null && (temp instanceof UIPortal))
         {
            uiPortal = (UIPortal)temp;
            if (uiPortal.getOwnerType().equals(PortalConfig.PORTAL_TYPE))
            {
               portalOwner = uiPortal.getOwner();
            }
            else
            {
               portalOwner = Util.getPortalRequestContext().getPortalOwner();
            }
         }
         else
         {
            uiPortal = Util.getUIPortal();
            portalOwner = Util.getPortalRequestContext().getPortalOwner();
         }

         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPortalForm portalForm = uiMaskWS.createUIComponent(UIPortalForm.class, null, "UIPortalForm");
         portalForm.setPortalOwner(portalOwner);
         portalForm.setBindingBean();
         if (PortalConfig.USER_TYPE.equals(uiPortal.getOwnerType()))
         {
            portalForm.removeChildById("PermissionSetting");
         }
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
         uiEditWS.setRendered(false);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         prContext.ignoreAJAXUpdateOnPortlets(true);
         UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);

         UIPortal uiPortal = uiWorkingWS.getBackupUIPortal();
         siteBody.setUIComponent(uiPortal);

         String uri = uiPortal.getSelectedUserNode() != null ? uiPortal.getSelectedUserNode().getURI() : null;
         PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         JavascriptManager jsManager = prContext.getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }

   }

   /**
    * Listens the <code>save</code> action of the composer while editing SiteConfig
    * 
    * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
    * @version $Revision$
    */
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

         if (uiPortal == null)
         {
            siteBody.setUIComponent(editPortal);
         }
         uiEditWS.setRendered(false);
         uiPortal = (UIPortal)siteBody.getUIComponent();

         uiPortalApp.setSessionOpen(PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive()));
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         prContext.ignoreAJAXUpdateOnPortlets(true);

         String uri = (uiPortal.getSelectedUserNode() != null)? (uiPortal.getSelectedUserNode().getURI()) : null;
        
         if (uiComposer.isPortalExist(editPortal))
         {
            DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
            PortalConfig pConfig =
               storage.getPortalConfig(uiPortal.getSiteKey().getTypeName(), uiPortal.getSiteKey().getName());
            if (pConfig != null)
            {
               uiPortalApp.getUserPortalConfig().setPortal(pConfig);
            }
            uiPortal.getChildren().clear();
            PortalDataMapper.toUIPortal(uiPortal, uiPortalApp.getUserPortalConfig());

            //Update the cache of UIPortal from UIPortalApplication
            uiPortalApp.putCachedUIPortal(uiPortal);
            uiPortalApp.setShowedUIPortal(uiPortal);
            
            //To init the UIPage, that fixed a bug on AdminToolbarPortlet when edit the layout. Here is only a
            //temporal solution. Complete solution is to avoid mapping UIPortal -- model, that requires
            //multiple UIPortal (already available) and concept of SiteConfig
            uiPortal.refreshUIPage();
            
            PageNodeEvent<UIPortal> pnevent =
               new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
            uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            JavascriptManager jsManager = prContext.getJavascriptManager();
            jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
         }
         else
         {
            if (editPortal.getOwner().equals(prContext.getPortalOwner()))
            {
               HttpServletRequest request = prContext.getRequest();
               LogoutControl.wantLogout();
               prContext.setResponseComplete(true);
               prContext.getResponse().sendRedirect(request.getContextPath());
               return;
            }
            else
            {
               UIApplication uiApp = prContext.getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null));
               prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            }
         }
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
         Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
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
   
   static public class CloseComposerActionListener extends EventListener<UIPortalComposer> 
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalComposer uiPortalComposer = event.getSource();
         UIEditInlineWorkspace uiEditInlineWorkspace = uiPortalComposer.getAncestorOfType(UIEditInlineWorkspace.class);         
         if (uiPortalComposer.isEditted()) 
         {
            ResourceBundle resourceBundle = event.getRequestContext().getApplicationResourceBundle();
            String closeMessage = resourceBundle.getString("UIEditInlineWorkspace.confirm.close");
            
            uiEditInlineWorkspace.showConfirmWindow(closeMessage);
         }
         else 
         {
            Event<UIComponent> abortEvent = uiPortalComposer.createEvent("Abort", event.getExecutionPhase(), event.getRequestContext());
            abortEvent.broadcast();
         }
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
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         PortalRequestContext prContext = Util.getPortalRequestContext();
         prContext.ignoreAJAXUpdateOnPortlets(true);

         UIPortal uiPortal = uiPortalApp.getShowedUIPortal();
         uiPortal.setRenderSibling(UIPortal.class);
         UIPortalComposer composer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         composer.setEditted(false);

         uiPortal.refreshUIPage();
         
         PageNodeEvent<UIPortal> pnevent =
            new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, (uiPortal.getSelectedUserNode() != null
               ? uiPortal.getSelectedUserNode().getURI() : null));
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }
   }

   /**
    * This action listener is for the page edition
    * 
    * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
    * @version $Revision$
    */
   static public class Finish2ActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         UIPortal uiPortal = uiPortalApp.getShowedUIPortal();
         UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
         UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
         
         UIPage uiPage = uiToolPanel.findFirstComponentOfType(UIPage.class);
         Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
         String pageId = page.getPageId();

         UserPortalConfigService portalConfigService =
            uiWorkingWS.getApplicationComponent(UserPortalConfigService.class);
         
         /*
          * if it is a edition of the current page
          */
         if (page.getStorageId() != null && portalConfigService.getPage(pageId) == null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.PageNotExist", new String[]{pageId}, 1));
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
            PageNodeEvent<UIPortal> pnevent =
               new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE,
                  (uiPortal.getSelectedUserNode() != null ? uiPortal.getSelectedUserNode().getURI() : null));
            uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
            JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
            jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
            return;
         }
         UIPortalComposer composer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         composer.setEditted(false);
         
         // If it is a page creation wizard
         if (composer.isUsedInWizard())
         {
            UIWizard wizard = (UIWizard)uiToolPanel.getUIComponent();
            int step = wizard.getCurrentStep();
            step++;
            Event<UIComponent> uiEvent =
               wizard.createEvent("ViewStep" + step, Phase.PROCESS, event.getRequestContext());
            uiEvent.broadcast();
            return;
         }

         // Perform model update
         DataStorage dataService = uiWorkingWS.getApplicationComponent(DataStorage.class);
         try
         {
            dataService.save(page);
         }
         catch (StaleModelException ex)
         {
            //Temporary solution to concurrency-related issue
            //This catch block should be put in an appropriate ApplicationLifecyclec
         }
         uiToolPanel.setUIComponent(null);

         // Synchronize model object with UIPage object, that seems  redundant but in fact
         // mandatory to have consequent edit actions (on the same page) work properly
         page = dataService.getPage(page.getPageId());
         uiPage.getChildren().clear();
         PortalDataMapper.toUIPage(uiPage, page);
         
         // Update UIPage cache on UIPortal
         uiPortal.setUIPage(pageId, uiPage);
         uiPortal.refreshUIPage();
         
         if (PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive()))
         {
            uiPortalApp.setSessionOpen(true);
         }
         else
         {
            uiPortalApp.setSessionOpen(false);
         }
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
         PageNodeEvent<UIPortal> pnevent =
            new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, (uiPortal.getSelectedUserNode() != null
               ? uiPortal.getSelectedUserNode().getURI() : null));
         uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");
      }
   }

   static public class BackActionListener extends EventListener<UIPortalComposer>
   {
      public void execute(Event<UIPortalComposer> event) throws Exception
      {
         UIPortalComposer composer = event.getSource();
         if (composer.isUsedInWizard())
         {
            UIWorkingWorkspace uiWorkingWS = composer.getAncestorOfType(UIWorkingWorkspace.class);
            UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
            UIWizard wizard = (UIWizard)uiToolPanel.getUIComponent();
            int step = wizard.getCurrentStep();
            step--;
            Event<UIComponent> uiEvent =
               wizard.createEvent("ViewStep" + step, Phase.PROCESS, event.getRequestContext());
            uiEvent.broadcast();
         }
      }
   }
}

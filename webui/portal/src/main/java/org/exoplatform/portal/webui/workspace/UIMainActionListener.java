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

package org.exoplatform.portal.webui.workspace;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UIPageFactory;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.portal.UIPortalForm;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.lang.reflect.Method;

/**
 * Created by The eXo Platform SAS 
 * 
 * Author : Pham Thanh Tung
 * thanhtungty@gmail.com May 5, 2009
 */
public class UIMainActionListener
{

   static public class EditCurrentPageActionListener extends EventListener<UIWorkingWorkspace>
   {
      public void execute(Event<UIWorkingWorkspace> event) throws Exception
      {
         UIPortalApplication uiApp = Util.getUIPortalApplication();
         UIWorkingWorkspace uiWorkingWS = event.getSource();

         // check edit permission for page
         UIPageBody pageBody = uiWorkingWS.findFirstComponentOfType(UIPageBody.class);
         UIPage uiPage = (UIPage)pageBody.getUIComponent();
         if (uiPage == null)
         {
            uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.PageNotExist", null));
            return;
         }
         Page page = PortalDataMapper.toPageModel(uiPage);

         UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
         if (!userACL.hasEditPermission(page))
         {
            uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-EditPage-Permission", null));
            return;
         }

         uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

         UIPortalComposer portalComposer =
            uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(true);
         portalComposer.setComponentConfig(UIPortalComposer.class, UIPortalComposer.UIPAGE_EDITOR);
         portalComposer.setId(UIPortalComposer.UIPAGE_EDITOR);
         portalComposer.setShowControl(true);
         portalComposer.setEditted(false);
         portalComposer.setCollapse(false);

         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         uiToolPanel.setShowMaskLayer(false);
         uiApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);

         // We clone the edited UIPage object, that is required for Abort action
         UIPageFactory clazz = UIPageFactory.getInstance(page.getFactoryId());
         UIPage newUIPage = clazz.createUIPage(null);
         PortalDataMapper.toUIPage(newUIPage, page);
         uiToolPanel.setWorkingComponent(newUIPage);

         // Remove current UIPage from UIPageBody
         pageBody.setUIComponent(null);

         event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
         Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
      }
   }

   static public class PageCreationWizardActionListener extends EventListener<UIWorkingWorkspace>
   {
      public void execute(Event<UIWorkingWorkspace> event) throws Exception
      {         
         UIPortalApplication uiApp = Util.getUIPortalApplication();
         UIPortal uiPortal = Util.getUIPortal();
         UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         
         UserNavigation currNav = uiPortal.getUserNavigation();
         if (currNav == null)
         {
            uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.navigation.deleted", null));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
            return;
         }                 
         
         if (!currNav.isModifiable())
         {
            uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-CreatePage-Permission", null));
            return;
         }
         
         //Should renew the selectedNode. Don't reuse the cached selectedNode
         UserNode selectedNode = Util.getUIPortal().getSelectedUserNode();
         UserNodeFilterConfig filterConfig = createFilterConfig();         
         UserNode resolvedNode = resolveNode(selectedNode, filterConfig);                 
         if (resolvedNode == null)
         {
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            context.getUIApplication().addMessage(new ApplicationMessage("UIPortalManagement.msg.node.deleted", null));         
            event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
            return;
         }

         uiApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
         uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

         UIPortalComposer portalComposer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class);
         portalComposer.setRendered(false);
         portalComposer.setComponentConfig(UIPortalComposer.class, UIPortalComposer.UIPAGE_EDITOR);
         portalComposer.setId(UIPortalComposer.UIPAGE_EDITOR);
         portalComposer.setShowControl(true);
         portalComposer.setEditted(true);
         portalComposer.setCollapse(false);

         UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         uiToolPanel.setShowMaskLayer(false);
         uiToolPanel.setWorkingComponent(UIPageCreationWizard.class, null);
         UIPageCreationWizard uiWizard = (UIPageCreationWizard)uiToolPanel.getUIComponent();
         uiWizard.configure(resolvedNode);
         
         UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
         uiPageSetInfo.setShowPublicationDate(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);                                             
      }

      private UserNode resolveNode(UserNode selectedNode, UserNodeFilterConfig filterConfig) throws Exception
      {         
         UserNavigation currNav = selectedNode.getNavigation();
         UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
         if (currNav.getKey().getType().equals(SiteType.USER))
         {            
            return userPortal.getNode(currNav, Scope.CHILDREN, filterConfig, null);
         }
         else
         {
            return userPortal.resolvePath(currNav, filterConfig, selectedNode.getURI());
         }
      }

      private UserNodeFilterConfig createFilterConfig()
      {
         UserNodeFilterConfig.Builder filterConfigBuilder = UserNodeFilterConfig.builder();
         filterConfigBuilder.withReadWriteCheck();
         return filterConfigBuilder.build();
      }
   }

   static public class EditInlineActionListener extends EventListener<UIWorkingWorkspace>
   {
      public void execute(Event<UIWorkingWorkspace> event) throws Exception
      {
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         UIPortalApplication portalApp = (UIPortalApplication)pcontext.getUIApplication();
         UIPortal currentPortal = portalApp.getCurrentSite();
         UIWorkingWorkspace uiWorkingWS = event.getSource();

         UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
         if (!userACL.hasEditPermissionOnPortal(currentPortal.getSiteType().getName(), currentPortal.getName(),
            currentPortal.getEditPermission()))
         {
            portalApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-EditLayout-Permission",
               new String[]{currentPortal.getName()}));
            return;
         }

         DataStorage dataStorage = portalApp.getApplicationComponent(DataStorage.class);
         PortalConfig portalConfig = dataStorage.getPortalConfig(pcontext.getSiteType().getName(), pcontext.getSiteName());
         UIPortal transientPortal = uiWorkingWS.createUIComponent(UIPortal.class, null, null);
         PortalDataMapper.toUIPortal(transientPortal, portalConfig);
         transientPortal.setNavPath(currentPortal.getNavPath());
         transientPortal.refreshUIPage();

         uiWorkingWS.setBackupUIPortal(currentPortal);
         portalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);

         UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
         uiEditWS.setUIComponent(transientPortal);
         UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
         siteBody.setUIComponent(null);

         UIPortalComposer uiComposer = uiEditWS.getComposer().setRendered(true);
         uiComposer.setComponentConfig(UIPortalComposer.class, null);
         uiComposer.setShowControl(true);
         uiComposer.setEditted(false);
         uiComposer.setCollapse(false);
         uiComposer.setId(UIPortalComposer.UIPORTAL_COMPOSER);

         uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);
         pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
         pcontext.ignoreAJAXUpdateOnPortlets(true);
      }
   }

   public static class CreatePortalActionListener extends EventListener<UIWorkingWorkspace>
   {
      public void execute(Event<UIWorkingWorkspace> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIPortalApplication uiApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
         UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
         if (!userACL.hasCreatePortalPermission())
         {
            uiApp.addMessage(new ApplicationMessage("UIPortalBrowser.msg.Invalid-createPermission", null));
            return;
         }
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class, "CreatePortal", "UIPortalForm");
         uiMaskWS.setUIComponent(uiNewPortal);
         uiMaskWS.setShow(true);
         prContext.addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }
   
   public static class EditBackgroundActionListener extends EventListener<UIWorkingWorkspace>
   {
      private Log log = ExoLogger.getExoLogger(this.getClass());
      
      @Override
      public void execute(Event<UIWorkingWorkspace> event) throws Exception
      {
         
         UIWorkingWorkspace workingWorkspace = event.getSource();
         UIPage uiPage = workingWorkspace.findFirstComponentOfType(UIPage.class);
         
         Method showEditBackgroundPopupMethod = null;
         try
         {
            if (uiPage == null)
            {
               return;
            }
            showEditBackgroundPopupMethod = uiPage.getClass().getDeclaredMethod("showEditBackgroundPopup", WebuiRequestContext.class);
         }
         catch (NoSuchMethodException ex)
         {
            log.warn(ex.getMessage(), ex);  
         }
         if(showEditBackgroundPopupMethod != null)
         {
            showEditBackgroundPopupMethod.invoke(uiPage, event.getRequestContext());
         }
      }
   }

}

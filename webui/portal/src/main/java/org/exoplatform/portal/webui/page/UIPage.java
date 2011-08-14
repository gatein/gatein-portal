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
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.MoveChildActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * May 19, 2006
 */
@ComponentConfig(lifecycle = UIPageLifecycle.class, template = "system:/groovy/portal/webui/page/UIPage.gtmpl", events = {@EventConfig(listeners = MoveChildActionListener.class),
	@EventConfig(name = "EditCurrentPage", listeners = UIPage.EditCurrentPageActionListener.class)})
public class UIPage extends UIContainer
{
   /** . */
   private String pageId;

   private SiteKey siteKey;

   private String editPermission;

   private boolean showMaxWindow = false;

   private UIPortlet maximizedUIPortlet;

   public static String DEFAULT_FACTORY_ID = "Default";

   public SiteKey getSiteKey()
   {
      return siteKey;
   }
   
   public void setSiteKey(SiteKey key)
   {
      siteKey = key;
   }

   /**
    * @deprecated use {@link #getSiteKey()} instead
    * 
    * @return
    */
   @Deprecated
   public String getOwnerType()
   {
      return getSiteKey().getTypeName();
   }
   
   /**
    * @deprecated use {@link #getSiteKey()} instead
    * 
    * @return
    */
   @Deprecated
   public String getOwnerId()
   {
      return getSiteKey().getName();
   }

   public boolean isShowMaxWindow()
   {
      return showMaxWindow;
   }

   public void setShowMaxWindow(Boolean showMaxWindow)
   {
      this.showMaxWindow = showMaxWindow;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public void setEditPermission(String editPermission)
   {
      this.editPermission = editPermission;
   }

   public String getPageId()
   {
      return pageId;
   }

   public void setPageId(String id)
   {
      pageId = id;
   }

   public UIPortlet getMaximizedUIPortlet()
   {
      return maximizedUIPortlet;
   }

   public void setMaximizedUIPortlet(UIPortlet maximizedUIPortlet)
   {
      this.maximizedUIPortlet = maximizedUIPortlet;
   }

   public void switchToEditMode() throws Exception
   {
      Page page = PortalDataMapper.toPageModel(this);
      switchToEditMode(page);
   }

   public void switchToEditMode(Page page) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();

      // check edit permission for page
      UserACL userACL = getApplicationComponent(UserACL.class);
      if (!userACL.hasEditPermission(page)) {
         context.getUIApplication().addMessage(new ApplicationMessage(
               "UIPortalManagement.msg.Invalid-EditPage-Permission", null));
         return;
      }

      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIWorkingWorkspace uiWorkingWS = uiApp
         .getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

      UIPortalComposer portalComposer = uiWorkingWS.findFirstComponentOfType(
            UIPortalComposer.class).setRendered(true);
      portalComposer.setComponentConfig(UIPortalComposer.class, "UIPageEditor");
      portalComposer.setId("UIPageEditor");
      portalComposer.setShowControl(true);
      portalComposer.setEditted(false);
      portalComposer.setCollapse(false);

      UIPortalToolPanel uiToolPanel = uiWorkingWS
            .findFirstComponentOfType(UIPortalToolPanel.class);
      uiToolPanel.setShowMaskLayer(false);
      uiApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);

      // We clone the edited UIPage object, that is required for Abort action
      UIPage newUIPage = uiWorkingWS.createUIComponent(UIPage.class, null, null);
      PortalDataMapper.toUIPage(newUIPage, page);
      uiToolPanel.setWorkingComponent(newUIPage);

      // Remove current UIPage from UIPageBody
      UIPageBody pageBody = uiWorkingWS
            .findFirstComponentOfType(UIPageBody.class);
      pageBody.setUIComponent(null);

      PortalRequestContext prContext = Util.getPortalRequestContext();
      prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      prContext.setFullRender(true);
   }

   public static class EditCurrentPageActionListener extends EventListener<UIPage>
   {
      @Override
      public void execute(Event<UIPage> event) throws Exception
      {
         event.getSource().switchToEditMode();
      }
   }
}
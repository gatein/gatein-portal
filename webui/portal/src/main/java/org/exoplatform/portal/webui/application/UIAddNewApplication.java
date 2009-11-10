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

package org.exoplatform.portal.webui.application;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.gadget.GadgetId;
import org.exoplatform.portal.config.model.portlet.PortletId;
import org.exoplatform.portal.config.model.wsrp.WSRPId;
import org.exoplatform.portal.pom.spi.portlet.Preferences;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.pom.spi.wsrp.WSRPState;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Created by The eXo Platform SARL Author : Anh Nguyen ntuananh.vn@gmail.com Oct 18, 2007 */
@ComponentConfig(template = "system:/groovy/portal/webui/application/UIAddNewApplication.gtmpl", events = {
   @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class),
   @EventConfig(listeners = UIAddNewApplication.AddApplicationActionListener.class),
   @EventConfig(listeners = UIAddNewApplication.AddToStartupActionListener.class)})
public class UIAddNewApplication extends UIContainer
{

   private List<ApplicationCategory> listAppCategories;

   private UIComponent uiComponentParent;

   private boolean isInPage;

   public List<ApplicationCategory> getApplicationCategories() throws Exception
   {
      return listAppCategories;
   }

   public List<ApplicationCategory> getApplicationCategories(String remoteUser,
                                                             String[] applicationType) throws Exception
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      ApplicationRegistryService prService = (ApplicationRegistryService)container.getComponentInstanceOfType(ApplicationRegistryService.class);

      if (applicationType == null)
      {
         applicationType = new String[]{};
      }

      List<ApplicationCategory> appCategories = prService.getApplicationCategories(remoteUser,
         applicationType);

      if (appCategories == null)
      {
         appCategories = new ArrayList();
      }
      else
      {
         Iterator<ApplicationCategory> cateItr = appCategories.iterator();
         while (cateItr.hasNext())
         {
            ApplicationCategory cate = cateItr.next();
            List<Application> applications = cate.getApplications();
            if (applications.size() < 1)
            {
               cateItr.remove();
            }
         }
      }
      listAppCategories = appCategories;

      return listAppCategories;

   }

   public UIComponent getUiComponentParent()
   {
      return uiComponentParent;
   }

   public void setUiComponentParent(UIComponent uiComponentParent)
   {
      this.uiComponentParent = uiComponentParent;
   }

   public boolean isInPage()
   {
      return isInPage;
   }

   public void setInPage(boolean isInPage)
   {
      this.isInPage = isInPage;
   }

   private Application getApplication(String id) throws Exception
   {

      List<ApplicationCategory> pCategories = getApplicationCategories();

      for (ApplicationCategory pCategory : pCategories)
      {
         List<Application> applications = pCategory.getApplications();
         for (Application application : applications)
         {
            if (application.getId().equals(id))
            {
               return application;
            }
         }
      }

      return null;
   }

   /**
    * Add Application to UiPage
    *
    * @param event
    * @throws Exception
    */
   private static void addApplicationToPage(Event<UIAddNewApplication> event, boolean atStartup) throws Exception
   {
      UIPortal uiPortal = Util.getUIPortal();

      UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      UIPage uiPage;
      if (uiPortal.isRendered())
      {
         uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
      }
      else
      {
         UIPortalToolPanel uiPortalToolPanel = uiPortalApp.findFirstComponentOfType(UIPortalToolPanel.class);
         uiPage = uiPortalToolPanel.findFirstComponentOfType(UIPage.class);
      }

      String applicationId = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

      Application application = event.getSource().getApplication(applicationId);
      String appType = application.getApplicationType();
      String portletName = application.getApplicationName();
//      String appGroup = application.getApplicationGroup();

      // TODO review windowId for eXoWidget and eXoApplication
      UIComponent component = null;
      if (org.exoplatform.web.application.Application.EXO_GADGET_TYPE.equals(appType))
      {
         UIGadget uiGadget = uiPage.createUIComponent(event.getRequestContext(), UIGadget.class, null, null);

         uiGadget.setGadgetId(new GadgetId(portletName));

         // Set Properties For gadget
         int posX = (int)(Math.random() * 400);
         int posY = (int)(Math.random() * 200);

         uiGadget.getProperties().put(UIApplication.locationX, String.valueOf(posX));
         uiGadget.getProperties().put(UIApplication.locationY, String.valueOf(posY));

         component = uiGadget;
      }
      else
      {
         boolean remote = org.exoplatform.web.application.Application.WSRP_TYPE.equals(appType);

         UIPortlet uiPortlet = uiPage.createUIComponent(UIPortlet.class, null, null);

         CloneApplicationState appState;
         Object appId;
         if (!remote)
         {
            appState = new CloneApplicationState<Preferences>(application.getId());
         }
         else
         {
            appState = new CloneApplicationState<WSRPState>(application.getId());
         }

         ApplicationType applicationType = remote ? ApplicationType.WSRP_PORTLET : ApplicationType.PORTLET;
         PortletState portletState = new PortletState(appState, applicationType, null);

         uiPortlet.setState(portletState);
         uiPortlet.setPortletInPortal(false);

         if (atStartup)
         {
            uiPortlet.getProperties().setProperty("appStatus", "HIDE");
         }

         if (application != null)
         {
            String displayName = application.getDisplayName();
            if (displayName != null)
            {
               uiPortlet.setTitle(displayName);
            }
            else if (portletName != null)
            {
               uiPortlet.setTitle(portletName);
            }
            uiPortlet.setDescription(application.getDescription());
            List<String> accessPers = application.getAccessPermissions();
            String[] accessPermissions = accessPers.toArray(new String[accessPers.size()]);
            uiPortlet.setAccessPermissions(accessPermissions);

            component = uiPortlet;
         }
      }

      // Add component to page
      uiPage.addChild(component);

      // Save all changes
      if (uiPage.isModifiable())
      {
         Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
         UserPortalConfigService configService = uiPortalApp.getApplicationComponent(UserPortalConfigService.class);
         if (page.getChildren() == null)
         {
            page.setChildren(new ArrayList<ModelObject>());
         }
         configService.update(page);
      }

      PortalRequestContext pcontext = Util.getPortalRequestContext();
      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
      pcontext.setFullRender(true);
   }

   static public class AddApplicationActionListener extends EventListener<UIAddNewApplication>
   {
      public void execute(Event<UIAddNewApplication> event) throws Exception
      {
         if (event.getSource().isInPage())
         {
            addApplicationToPage(event, false);
         }
      }
   }

   static public class AddToStartupActionListener extends EventListener<UIAddNewApplication>
   {
      public void execute(Event<UIAddNewApplication> event) throws Exception
      {
         if (event.getSource().isInPage())
         {
            addApplicationToPage(event, true);
         }
      }
   }
}

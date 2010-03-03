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

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UIPageNodeSelector;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 23, 2006 */
@ComponentConfigs(@ComponentConfig(template = "system:/groovy/webui/core/UIWizard.gtmpl", events = {
   @EventConfig(listeners = UIPageCreationWizard.ViewStep1ActionListener.class),
   @EventConfig(listeners = UIPageCreationWizard.ViewStep2ActionListener.class),
   @EventConfig(listeners = UIPageCreationWizard.ViewStep3ActionListener.class),
   @EventConfig(listeners = UIPageCreationWizard.ViewStep4ActionListener.class),
   @EventConfig(listeners = UIPageWizard.AbortActionListener.class)}))
public class UIPageCreationWizard extends UIPageWizard
{

   final public static int FIRST_STEP = 1;

   final public static int SECONDE_STEP = 2;

   final public static int THIRD_STEP = 3;

   final public static int NUMBER_OF_STEPs = 3;

   public UIPageCreationWizard() throws Exception
   {
      UIWizardPageSetInfo uiPageInfo = addChild(UIWizardPageSetInfo.class, null, null).setRendered(false);
      addChild(UIWizardPageSelectLayoutForm.class, null, null).setRendered(false);
      addChild(UIPagePreview.class, null, null).setRendered(false);
      setNumberSteps(NUMBER_OF_STEPs);
      viewStep(FIRST_STEP);
      setShowWelcomeComponent(false);
      boolean isUserNav = Util.getUIPortal().getSelectedNavigation().getOwnerType().equals(PortalConfig.USER_TYPE);
      if (isUserNav)
      {
         uiPageInfo.getChild(UIPageNodeSelector.class).setRendered(false);
      }
   }

   private void saveData() throws Exception
   {
      DataStorage dataService = getApplicationComponent(DataStorage.class); 
      UIPagePreview uiPagePreview = getChild(UIPagePreview.class);
      UIPage uiPage = (UIPage)uiPagePreview.getUIComponent();
      UIPortal uiPortal = Util.getUIPortal();
      /*if (PortalConfig.PORTAL_TYPE.equals(uiPage.getOwnerType()))
      {
         uiPage.setAccessPermissions(uiPortal.getAccessPermissions());
         uiPage.setEditPermission(uiPortal.getEditPermission());
      }
      else if (PortalConfig.GROUP_TYPE.equals(uiPage.getOwnerType()))
      {
         UserACL acl = getApplicationComponent(UserACL.class);
         String ownerId = uiPage.getOwnerId();
         ownerId = ownerId.startsWith("/") ? ownerId : "/" + ownerId;
         uiPage.setAccessPermissions(new String[]{"*:" + ownerId});
         uiPage.setEditPermission(acl.getMakableMT() + ":" + ownerId);
      }
       */
      UIWizardPageSetInfo uiPageInfo = getChild(UIWizardPageSetInfo.class);
      UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
      PageNode selectedNode = uiNodeSelector.getSelectedPageNode();
      PageNavigation pageNav = uiNodeSelector.getSelectedNavigation();
      if (PortalConfig.USER_TYPE.equals(pageNav.getOwnerType()))
         selectedNode = null;

      Page page = (Page)PortalDataMapper.buildModelObject(uiPage);
      PageNode pageNode = uiPageInfo.getPageNode();
      pageNode.setPageReference(page.getPageId());
      if (selectedNode != null)
      {
         List<PageNode> children = selectedNode.getChildren();
         if (children == null)
         {
            children = new ArrayList<PageNode>();
         }
         children.add(pageNode);
         selectedNode.setChildren((ArrayList<PageNode>)children);
      }
      else
      {
         pageNav.addNode(pageNode);
      }
      uiNodeSelector.selectPageNodeByUri(pageNode.getUri());

      dataService.create(page);
      dataService.save(pageNav);
      setNavigation(uiPortal.getNavigations(), uiNodeSelector.getSelectedNavigation());
      String uri = pageNav.getId() + "::" + pageNode.getUri();
      PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
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

   public boolean isSelectedNodeExist() throws Exception
   {
      UIWizardPageSetInfo uiPageSetInfo = getChild(UIWizardPageSetInfo.class);
      PageNavigation navigation = uiPageSetInfo.getChild(UIPageNodeSelector.class).getSelectedNavigation();
      PageNode pageNode = uiPageSetInfo.getPageNode();
      PageNode selectedPageNode = uiPageSetInfo.getSelectedPageNode();
      List<PageNode> sibbling = null;
      if (selectedPageNode != null)
      {
         sibbling = selectedPageNode.getChildren();
      }
      else
      {
         sibbling = navigation.getNodes();
      }
      if (sibbling != null)
      {
         for (PageNode ele : sibbling)
         {
            if (ele.getUri().equals(pageNode.getUri()))
            {
               return true;
            }
         }
      }

      return false;
   }

   static public class ViewStep1ActionListener extends EventListener<UIPageWizard>
   {
      public void execute(Event<UIPageWizard> event) throws Exception
      {
         UIPageWizard uiWizard = event.getSource();
         uiWizard.setDescriptionWizard(FIRST_STEP);

         uiWizard.updateWizardComponent();
         uiWizard.viewStep(FIRST_STEP);

         uiWizard.setShowActions(true);

         UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
         uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
      }
   }

   static public class ViewStep2ActionListener extends EventListener<UIPageCreationWizard>
   {
      public void execute(Event<UIPageCreationWizard> event) throws Exception
      {
         UIPageCreationWizard uiWizard = event.getSource();
         uiWizard.setShowActions(true);
         UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
         UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
         uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         uiWizard.viewStep(SECONDE_STEP);

         if (uiWizard.getSelectedStep() < SECONDE_STEP)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep", null));
            uiWizard.setDescriptionWizard(FIRST_STEP);
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }

         if (uiWizard.isSelectedNodeExist())
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }

         UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
         UIPageNodeSelector uiNodeSelector = uiPageSetInfo.getChild(UIPageNodeSelector.class);
         uiWizard.setDescriptionWizard(SECONDE_STEP);
         uiWizard.updateWizardComponent();
         PageNavigation navigation = uiNodeSelector.getSelectedNavigation();
         if (navigation == null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.notSelectedPageNavigation",
               new String[]{}));;
            uiWizard.viewStep(FIRST_STEP);
            return;
         }

         if (uiPageSetInfo.getUIFormCheckBoxInput(UIWizardPageSetInfo.SHOW_PUBLICATION_DATE).isChecked())
         {
            Calendar startCalendar =
               uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.START_PUBLICATION_DATE).getCalendar();
            Date startDate = startCalendar.getTime();
            Calendar endCalendar =
               uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.END_PUBLICATION_DATE).getCalendar();
            Date endDate = endCalendar.getTime();
            if (startDate.after(endDate))
            {
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm2.msg.startDateBeforeEndDate", null));
               uiWizard.viewStep(FIRST_STEP);
               return;
            }
         }

      }
   }

   static public class ViewStep3ActionListener extends EventListener<UIPageCreationWizard>
   {

      private void setDefaultPermission(Page page, String ownerType, String ownerId)
      {
         UIPortal uiPortal = Util.getUIPortal();
         if (PortalConfig.PORTAL_TYPE.equals(ownerType))
         {
            page.setAccessPermissions(uiPortal.getAccessPermissions());
            page.setEditPermission(uiPortal.getEditPermission());
         }
         else if (PortalConfig.GROUP_TYPE.equals(ownerType))
         {
            UserACL acl = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);
            ownerId = ownerId.startsWith("/") ? ownerId : "/" + ownerId;
            page.setAccessPermissions(new String[]{"*:" + ownerId});
            page.setEditPermission(acl.getMakableMT() + ":" + ownerId);
         }         
      }

      public void execute(Event<UIPageCreationWizard> event) throws Exception
      {
         UIPageCreationWizard uiWizard = event.getSource();
         uiWizard.setShowActions(false);
         UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
         UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
         WebuiRequestContext context = Util.getPortalRequestContext();

         if (uiWizard.isSelectedNodeExist())
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
            uiWizard.setDescriptionWizard(FIRST_STEP);
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }
         uiWizard.viewStep(THIRD_STEP);

         if (uiWizard.getSelectedStep() < THIRD_STEP)
         {
            uiWizard.setShowActions(true);
            uiWizard.setDescriptionWizard(uiWizard.getSelectedStep());
            uiWizard.updateWizardComponent();
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep", null));
            return;
         }

         uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(true);

         UIPageTemplateOptions uiPageTemplateOptions = uiWizard.findFirstComponentOfType(UIPageTemplateOptions.class);
         UIWizardPageSetInfo uiPageInfo = uiWizard.getChild(UIWizardPageSetInfo.class);

         UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
         PageNavigation pageNavi = uiNodeSelector.getSelectedNavigation();
         String ownerType = pageNavi.getOwnerType();
         String ownerId = pageNavi.getOwnerId();

         PageNode pageNode = uiPageInfo.getPageNode();
         Page page = uiPageTemplateOptions.createPageFromSelectedOption(ownerType, ownerId);
         page.setName("page" + page.hashCode());
         String pageId = pageNavi.getOwnerType() + "::" + pageNavi.getOwnerId() + "::" + page.getName();
         DataStorage storage = uiWizard.getApplicationComponent(DataStorage.class);
         if (storage.getPage(pageId) != null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
            uiWizard.setDescriptionWizard(FIRST_STEP);
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
         }
         page.setModifiable(true);

         // Set default permissions on the page
         setDefaultPermission(page, ownerType, ownerId);

         if (page.getTitle() == null || page.getTitle().trim().length() == 0)
         {
            page.setTitle(pageNode.getName());
         }

         boolean isDesktopPage = Page.DESKTOP_PAGE.equals(page.getFactoryId());
         if (isDesktopPage)
         {
            page.setShowMaxWindow(true);
         }

         UIPagePreview uiPagePreview = uiWizard.getChild(UIPagePreview.class);
         UIPage uiPage;
         if (Page.DESKTOP_PAGE.equals(page.getFactoryId()))
         {
            uiPage = uiWizard.createUIComponent(context, UIDesktopPage.class, null, null);
         }
         else
         {
            uiPage = uiWizard.createUIComponent(context, UIPage.class, null, null);
         }

         PortalDataMapper.toUIPage(uiPage, page);
         uiPagePreview.setUIComponent(uiPage);

         if (isDesktopPage)
         {
            uiWizard.saveData();
            uiWizard.updateUIPortal(uiPortalApp, event);
            return;
         }

         uiWizard.updateWizardComponent();
         uiPageTemplateOptions.setSelectedOption(null);
      }
   }

   static public class ViewStep4ActionListener extends EventListener<UIPageCreationWizard>
   {
      public void execute(Event<UIPageCreationWizard> event) throws Exception
      {
         UIPageCreationWizard uiWizard = event.getSource();
         uiWizard.setShowActions(true);
         UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
         UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
         uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
         if (uiWizard.isSelectedNodeExist())
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
            uiWizard.setDescriptionWizard(FIRST_STEP);
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         uiWizard.saveData();
         UIPortalToolPanel toolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         toolPanel.setUIComponent(null);
         uiWizard.updateUIPortal(uiPortalApp, event);
         JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
         jsManager.addJavascript("eXo.portal.portalMode=" + UIPortalApplication.NORMAL_MODE + ";");

         UIWizardPageSetInfo uiPageInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
         UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
         PageNode selectedNode = uiNodeSelector.getSelectedPageNode();

         String uri = Util.getPortalRequestContext().getPortalURI() + selectedNode.getUri();
         //Util.getPortalRequestContext().sendRedirect(uri);
         jsManager.addJavascript("window.location = '" + uri + "';");
      }
   }

}

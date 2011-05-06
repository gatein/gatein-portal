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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UIPageNodeSelector;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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

   final public static int SECOND_STEP = 2;

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
      UIPagePreview uiPagePreview = getChild(UIPagePreview.class);
      UIPage uiPage = (UIPage)uiPagePreview.getUIComponent();
      

      UIWizardPageSetInfo uiPageInfo = getChild(UIWizardPageSetInfo.class);
      UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
      PageNode selectedNode = uiNodeSelector.getSelectedPageNode();
      PageNavigation pageNav = uiNodeSelector.getSelectedNavigation();
      
      // reload page navigation
      DataStorage dataService = getApplicationComponent(DataStorage.class);
      pageNav = dataService.getPageNavigation(pageNav.getOwnerType(), pageNav.getOwnerId());
      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      setNavigation(uiPortalApp.getNavigations(), pageNav);
      uiPortalApp.localizeNavigations();
      UIPortal uiPortal = Util.getUIPortal();
      uiPortal.setNavigation(pageNav);
      uiNodeSelector.selectNavigation(pageNav);
      if (selectedNode != null)
      {
         uiNodeSelector.selectPageNodeByUri(selectedNode.getUri());
         selectedNode = uiNodeSelector.getSelectedPageNode();
      }
      
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

   /**
    * Returns <code>true</code> if the creating node is existing already.
    * Otherwise it returns <code>false</code>
    * 
    * @return true if the creating node is existing, otherwise it's false
    * @throws Exception
    */
   private boolean isSelectedNodeExist() throws Exception
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
         uiWizard.viewStep(SECOND_STEP);

         if (uiWizard.getSelectedStep() < SECOND_STEP)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep", null));
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
         	
         	Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            Date currentDate = currentCalendar.getTime();
            
            Calendar startCalendar =
               uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.START_PUBLICATION_DATE).getCalendar();
            Date startDate = (startCalendar != null) ? startCalendar.getTime() : currentDate;
            Calendar endCalendar =
               uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.END_PUBLICATION_DATE).getCalendar();
            Date endDate = (endCalendar != null) ? endCalendar.getTime() : null;
            
            // Case 1: current date after start date
            if (currentDate.after(startDate))
            {
            	Object[] args = {};
            	uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeStartDate", args, ApplicationMessage.WARNING));
               uiWizard.viewStep(FIRST_STEP);
               return;
            }
            // Case 2: start date after end date
            else if ((endCalendar != null) && (startCalendar != null) && (startDate.after(endDate)))
            {
            	Object[] args = {};
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.startDateBeforeEndDate", args, ApplicationMessage.WARNING));
               uiWizard.viewStep(FIRST_STEP);
               return;
            }
            // Case 3: start date is null and current date after end date
            else if((endCalendar != null) && (currentDate.after(endDate)))
            {
            	Object[] args = {};
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeEndDate", args, ApplicationMessage.WARNING));
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
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }
         uiWizard.viewStep(THIRD_STEP);

         if (uiWizard.getSelectedStep() < THIRD_STEP)
         {
            uiWizard.setShowActions(true);
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

         UIPagePreview uiPagePreview = uiWizard.getChild(UIPagePreview.class);
         
         UIPageFactory clazz = UIPageFactory.getInstance(page.getFactoryId());
         UIPage uiPage = clazz.createUIPage(context);
         
         PortalDataMapper.toUIPage(uiPage, page);
         uiPagePreview.setUIComponent(uiPage);

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
            uiWizard.viewStep(FIRST_STEP);
            uiWizard.updateWizardComponent();
            return;
         }
         uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
         uiWizard.saveData();
         UIPortalToolPanel toolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
         toolPanel.setUIComponent(null);
         uiWizard.updateUIPortal(event);
         UIWizardPageSetInfo uiPageInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
         UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
         PageNode selectedNode = uiNodeSelector.getSelectedPageNode();
         
         PortalRequestContext pcontext = Util.getPortalRequestContext();
         String uri = pcontext.getPortalURI() + selectedNode.getUri();
         pcontext.getResponse().sendRedirect(uri);
      }
   }

}

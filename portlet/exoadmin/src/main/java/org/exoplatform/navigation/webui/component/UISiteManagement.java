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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.navigation.UINavigationManagement;
import org.exoplatform.portal.webui.navigation.UINavigationNodeSelector;
import org.exoplatform.portal.webui.page.UIPageNodeForm2;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.util.ReflectionUtil;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@ComponentConfigs({
   @ComponentConfig(template = "app:/groovy/navigation/webui/component/UISiteManagement.gtmpl", events = {
      @EventConfig(name = "EditPortalLayout", listeners = UISiteManagement.EditPortalLayoutActionListener.class),
      @EventConfig(listeners = UISiteManagement.EditNavigationActionListener.class),
      @EventConfig(listeners = UISiteManagement.DeletePortalActionListener.class, confirm = "UIPortalBrowser.deletePortal")}),
   @ComponentConfig(type = UIPageNodeForm2.class, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
      @EventConfig(listeners = UIPageNodeForm2.SaveActionListener.class),
      @EventConfig(listeners = UISiteManagement.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.SwitchVisibleActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.ClearPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.CreatePageActionListener.class, phase = Phase.DECODE)})})
public class UISiteManagement extends UIContainer
{

   public static String[] ACTIONS = {"EditNavigation", "DeletePortal", "EditPortalLayout"};

   private LazyPageList pageList;

   private PageNavigation selectedNavigation;

   public UISiteManagement() throws Exception
   {
      UIPopupWindow editNavigation = addChild(UIPopupWindow.class, null, null);
      editNavigation.setWindowSize(400, 400);
      editNavigation.setId(editNavigation.getId() + "-" + UUID.randomUUID().toString().replaceAll("-", ""));
   }

   public List<PortalConfig> getPortalConfigs() throws Exception
   {
      return pageList.getAll();
   }

   public String[] getActions()
   {
      return ACTIONS;
   }

   public Object getFieldValue(Object bean, String field) throws Exception
   {
      Method method = ReflectionUtil.getGetBindingMethod(bean, field);
      return method.invoke(bean, ReflectionUtil.EMPTY_ARGS);
   }

   @SuppressWarnings("unchecked")
   public void loadPortalConfigs() throws Exception
   {
      DataStorage service = getApplicationComponent(DataStorage.class);

      Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class);
      LazyPageList<PortalConfig> temp = service.find(query, new Comparator<PortalConfig>()
      {
         public int compare(PortalConfig pconfig1, PortalConfig pconfig2)
         {
            return pconfig1.getName().toLowerCase().compareTo(pconfig2.getName().toLowerCase());
         }
      });

      final ArrayList<PortalConfig> tempArrayList = new ArrayList<PortalConfig>();
      if (temp != null)
      {
         tempArrayList.addAll(temp.getAll());
      }

      // Get portals without edit permission
      UserACL userACL = getApplicationComponent(UserACL.class);
      Iterator<PortalConfig> iterPortals = tempArrayList.iterator();
      PortalConfig portalConfig;
      while (iterPortals.hasNext())
      {
         portalConfig = iterPortals.next();
         if (!userACL.hasEditPermission(portalConfig))
         {
            iterPortals.remove();
         }
      }

      this.pageList = new LazyPageList(new ListAccess<PortalConfig>()
      {

         public int getSize() throws Exception
         {
            return tempArrayList.size();
         }

         public PortalConfig[] load(int index, int length) throws Exception, IllegalArgumentException
         {
            PortalConfig[] pcs = new PortalConfig[tempArrayList.size()];

            if (index < 0)
            {
               throw new IllegalArgumentException("Illegal index: index must be a positive number");
            }

            if (length < 0)
            {
               throw new IllegalArgumentException("Illegal length: length must be a positive number");
            }

            if (index + length > tempArrayList.size())
            {
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");
            }

            for (int i = 0; i < length; i++)
            {
               pcs[i] = tempArrayList.get(i + index);
            }

            return pcs;
         }

      }, 10);

   }

   public PageNavigation getOriginalSelectedNavigation()
   {
      return selectedNavigation;
   }

   public void setOriginalSelectedNavigation(PageNavigation navigation)
   {
      selectedNavigation = navigation;
   }

   static public class DeletePortalActionListener extends EventListener<UISiteManagement>
   {
      public void execute(Event<UISiteManagement> event) throws Exception
      {
         UISiteManagement uicomp = event.getSource();
         String portalName = event.getRequestContext().getRequestParameter(OBJECTID);
         UserPortalConfigService service = event.getSource().getApplicationComponent(UserPortalConfigService.class);
         String defaultPortalName = service.getDefaultPortal();

         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();

         if (defaultPortalName.equals(portalName))
         {
            uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.delete-default-portal",
               new String[]{defaultPortalName}, ApplicationMessage.WARNING));
            return;
         }

         UserPortalConfig config = service.getUserPortalConfig(portalName, prContext.getRemoteUser());
         if (config != null && config.getPortalConfig().isModifiable())
         {
            service.removeUserPortalConfig(portalName);
         }
         else if (config != null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-deletePermission",
               new String[]{config.getPortalConfig().getName()}));;
            return;
         }
         else
         {
            uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
               new String[]{portalName}));
            return;
         }

         if (config == null && !Util.getUIPortal().getName().equals(portalName))
         {
            uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-deletePermission",
               new String[]{portalName}));
            return;
         }

         if (config == null || Util.getUIPortal().getName().equals(portalName))
         {
            HttpServletRequest request = prContext.getRequest();
            request.getSession().invalidate();
            prContext.setResponseComplete(true);
            prContext.getResponse().sendRedirect(request.getContextPath());
            return;
         }

         //event.getSource().loadPortalConfigs();
         //UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);    
         event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);
      }
   }

   static public class EditPortalLayoutActionListener extends EventListener<UISiteManagement>
   {
      public void execute(Event<UISiteManagement> event) throws Exception
      {
         UISiteManagement uicomp = event.getSource();
         String portalName = event.getRequestContext().getRequestParameter(OBJECTID);
         UserPortalConfigService service = uicomp.getApplicationComponent(UserPortalConfigService.class);
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UIPortalApplication portalApp = (UIPortalApplication)prContext.getUIApplication();

         UserPortalConfig userConfig = service.getUserPortalConfig(portalName, prContext.getRemoteUser());

         if (userConfig == null)
         {
            portalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
               new String[]{portalName}));
            return;
         }
         PortalConfig portalConfig = userConfig.getPortalConfig();

         UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
         if (!userACL.hasEditPermission(portalConfig))
         {
            portalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-editPermission",
               new String[]{portalConfig.getName()}));
            return;
         }

         UIWorkingWorkspace uiWorkingWS = portalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         //UIEditInlineWorkspace uiEditWS = uiWorkingWS.addChild(UIEditInlineWorkspace.class, null, UIPortalApplication.UI_EDITTING_WS_ID);
         UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChildById(UIPortalApplication.UI_EDITTING_WS_ID);
         UIPortalComposer uiComposer = uiEditWS.getComposer().setRendered(true);
         uiComposer.setEditted(false);
         uiComposer.setCollapse(false);
         uiComposer.setShowControl(true);
         uiComposer.setComponentConfig(UIPortalComposer.class, null);

         UIPortal uiPortal = Util.getUIPortal();
         uiWorkingWS.setBackupUIPortal(uiPortal);

         UIPortal editPortal = uiWorkingWS.createUIComponent(UIPortal.class, null, null);
         PortalDataMapper.toUIPortal(editPortal, userConfig);
         uiEditWS.setUIComponent(editPortal);

         if (uiPortal.getName().equals(editPortal.getName()))
         {
            editPortal.setSelectedNode(uiPortal.getSelectedNode());
            editPortal.setSelectedNavigation(uiPortal.getSelectedNavigation());
            editPortal.setSelectedPath(uiPortal.getSelectedPath());
            editPortal.refreshUIPage();
            UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
            siteBody.setUIComponent(null);
         }

         portalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
         uiWorkingWS.setRenderedChild(UIPortalApplication.UI_EDITTING_WS_ID);

         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         prContext.setFullRender(true);
      }
   }

   static public class EditNavigationActionListener extends EventListener<UISiteManagement>
   {
      public void execute(Event<UISiteManagement> event) throws Exception
      {
         UISiteManagement uicomp = event.getSource();
         String portalName = event.getRequestContext().getRequestParameter(OBJECTID);
         UserPortalConfigService service = uicomp.getApplicationComponent(UserPortalConfigService.class);
         DataStorage dataService = uicomp.getApplicationComponent(DataStorage.class);
         PortalRequestContext prContext = Util.getPortalRequestContext();
         WebuiRequestContext context = event.getRequestContext();
         UIApplication uiApplication = context.getUIApplication();

         UserPortalConfig userConfig = service.getUserPortalConfig(portalName, prContext.getRemoteUser());
         if (userConfig == null)
         {
            uiApplication.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
               new String[]{portalName}));
            return;
         }
         PortalConfig portalConfig = userConfig.getPortalConfig();

         UserACL userACL = uicomp.getApplicationComponent(UserACL.class);
         if (!userACL.hasEditPermission(portalConfig))
         {
            uiApplication.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-editPermission", null));;
            return;
         }

         UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);

         UINavigationManagement naviManager = popUp.createUIComponent(UINavigationManagement.class, null, null, popUp);

         naviManager.setOwner(portalName);
         naviManager.setOwnerType(PortalConfig.PORTAL_TYPE);

         PageNavigation originalNavi = dataService.getPageNavigation(PortalConfig.PORTAL_TYPE, portalName);
         PageNavigation filteredNavi = null;
         try{
            filteredNavi = PageNavigationUtils.filterNavigation(originalNavi, prContext.getRemoteUser(), true);
         }
         catch(Exception ex)
         {
            
         }
         
         uicomp.setOriginalSelectedNavigation(originalNavi);
         UINavigationNodeSelector selector = naviManager.getChild(UINavigationNodeSelector.class);
         //ArrayList<PageNavigation> list = new ArrayList<PageNavigation>();
         //list.add(navi);
         //selector.initNavigations(list);
         selector.setOriginalEdittedNavigation(originalNavi);
         //selector.setFilteredEdittedNavigation(filteredNavi);
         selector.setFilteredEdittedNavigation(originalNavi);
         selector.initTreeData();

         popUp.setUIComponent(naviManager);
         popUp.setShowMask(true);
         popUp.setShow(true);

      }
   }

   static public class BackActionListener extends EventListener<UIPageNodeForm2>
   {

      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
         UIPageNodeForm2 uiPageNodeForm = event.getSource();
         UISiteManagement uiSiteManagement = uiPageNodeForm.getAncestorOfType(UISiteManagement.class);
         PageNavigation selectedNavigation = uiSiteManagement.getOriginalSelectedNavigation();
         UIPopupWindow uiNavigationPopup = uiSiteManagement.getChild(UIPopupWindow.class);
         UINavigationManagement pageManager =
            uiPageNodeForm.createUIComponent(UINavigationManagement.class, null, null);
         pageManager.setOwner(selectedNavigation.getOwnerId());
         UINavigationNodeSelector selector = pageManager.getChild(UINavigationNodeSelector.class);
         ArrayList<PageNavigation> navis = new ArrayList<PageNavigation>();
         navis.add(selectedNavigation);
         selector.initNavigations(navis);
         //selector.removeChild(UIRightClickPopupMenu.class);
         uiNavigationPopup.setUIComponent(pageManager);
         uiNavigationPopup.setWindowSize(400, 400);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigationPopup.getParent());
      }

   }

}

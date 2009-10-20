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

package org.exoplatform.portal.webui.navigation;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * Created by The eXo Platform SAS
 * Author : tam.nguyen
 *          tamndrok@gmail.com
 * May 28, 2009  
 */
@ComponentConfigs({
   @ComponentConfig(template = "system:/groovy/portal/webui/navigation/UIAddGroupNavigation.gtmpl", events = {
      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class),
      @EventConfig(listeners = UIAddGroupNavigation.AddNavigationActionListener.class)}),
   @ComponentConfig(id = "UIAddGroupNavigationGrid", type = UIRepeater.class, template = "system:/groovy/portal/webui/navigation/UIGroupGrid.gtmpl")})
public class UIAddGroupNavigation extends UIContainer
{

   public UIAddGroupNavigation() throws Exception
   {
      UIVirtualList virtualList = addChild(UIVirtualList.class, null, "AddGroupNavList");
      virtualList.setPageSize(6);
      UIRepeater repeater = createUIComponent(UIRepeater.class, "UIAddGroupNavigationGrid", null);
      virtualList.setUIComponent(repeater);
      UIPopupWindow editGroup = addChild(UIPopupWindow.class, null, "EditGroup");
   }

   public void loadGroups() throws Exception
   {

      PortalRequestContext pContext = Util.getPortalRequestContext();
      UserPortalConfigService dataService = getApplicationComponent(UserPortalConfigService.class);
      UserACL userACL = getApplicationComponent(UserACL.class);
      OrganizationService orgService = getApplicationComponent(OrganizationService.class);
      List<String> listGroup = null;
      // get all group that user has permission
      if (userACL.isUserInGroup(userACL.getAdminGroups()) && !userACL.getSuperUser().equals(pContext.getRemoteUser()))
      {
         Collection<?> temp = (List)orgService.getGroupHandler().findGroupsOfUser(pContext.getRemoteUser());
         if (temp != null)
         {
            listGroup = new ArrayList<String>();
            for (Object group : temp)
            {
               Group m = (Group)group;
               String groupId = m.getId().trim();
               listGroup.add(groupId);
            }
         }
      }
      else
      {
         listGroup = dataService.getMakableNavigations(pContext.getRemoteUser());
      }

      List<PageNavigation> navigations = new ArrayList<PageNavigation>();
      DataStorage dataStorage = getApplicationComponent(DataStorage.class);
      // get all group navigation that user have edit permission
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.GROUP_TYPE, null, PageNavigation.class);
      // filter, only get group don't have navigation
      if (listGroup == null)
      {
         listGroup = new ArrayList<String>();
      }

      List<PageNavigation> navis = dataStorage.find(query).getAll();
      for (PageNavigation ele : navis)
      {
         if (listGroup.contains(ele.getOwnerId()))
         {
            listGroup.remove(ele.getOwnerId());
         }
      }

      UIVirtualList virtualList = getChild(UIVirtualList.class);
      virtualList.dataBind(new ObjectPageList<String>(listGroup, listGroup.size()));
   }

   static public class AddNavigationActionListener extends EventListener<UIAddGroupNavigation>
   {
      public void execute(Event<UIAddGroupNavigation> event) throws Exception
      {
         WebuiRequestContext ctx = event.getRequestContext();
         UIAddGroupNavigation uicomp = event.getSource();
         UserPortalConfigService service = uicomp.getApplicationComponent(UserPortalConfigService.class);

         // get navigation id
         String ownerId = event.getRequestContext().getRequestParameter(OBJECTID);
         ownerId = URLDecoder.decode(ownerId);

         //Add navigation
         PageNavigation pageNav = new PageNavigation();

         // set properties for navigation
         pageNav.setPriority(1);
         pageNav.setModifiable(true);
         pageNav.setCreator(ctx.getRemoteUser());
         pageNav.setOwnerId(ownerId);
         pageNav.setOwnerType(PortalConfig.GROUP_TYPE);
         //UIPortalApplication uiPortalApp = uiForm.getAncestorOfType(UIPortalApplication.class);      
         UIPortalApplication uiPortalApp = Util.getUIPortal().getAncestorOfType(UIPortalApplication.class);

         // ensure this navigation is not exist
         DataStorage dataService = uicomp.getApplicationComponent(DataStorage.class);
         if (dataService.getPageNavigation(pageNav.getOwnerType(), pageNav.getOwnerId()) != null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageNavigationForm.msg.existPageNavigation",
               new String[]{pageNav.getOwnerId()}));
            return;
         }

         // create navigation for group
         service.create(pageNav);

         // add navigation to UIPortal 's navigations
         UIPortal uiPortal = Util.getUIPortal();
         uiPortal.getNavigations().add(pageNav); //TODO: Reorder the list in term of navigation priority

         //Update group navigation list
         ctx.addUIComponentToUpdateByAjax(uicomp);

         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);
         uiWorkingWS.updatePortletsByName("GroupNavigationPortlet");
         uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
      }
   }
}

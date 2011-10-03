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

package org.exoplatform.webui.organization;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Jun 27, 2006 */
@ComponentConfigs({
   @ComponentConfig(template = "system:/groovy/organization/webui/component/UIGroupMembershipSelector.gtmpl", events = {
      @EventConfig(phase = Phase.DECODE, listeners = UIGroupMembershipSelector.ChangeNodeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIGroupMembershipSelector.SelectMembershipActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIGroupMembershipSelector.SelectPathActionListener.class)}),
   @ComponentConfig(type = UITree.class, id = "UITreeGroupSelector", template = "system:/groovy/webui/core/UITree.gtmpl", events = @EventConfig(phase = Phase.DECODE, listeners = UITree.ChangeNodeActionListener.class)),
   @ComponentConfig(type = UIBreadcumbs.class, id = "BreadcumbGroupSelector", template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl", events = @EventConfig(phase = Phase.DECODE, listeners = UIBreadcumbs.SelectPathActionListener.class))})
@Serialized
public class UIGroupMembershipSelector extends UIContainer
{

   private Group selectGroup_;

   private List<String> listMemberhip;

   public UIGroupMembershipSelector() throws Exception
   {
      UIBreadcumbs uiBreadcumbs = addChild(UIBreadcumbs.class, "BreadcumbGroupSelector", "BreadcumbGroupSelector");
      UITree tree = addChild(UITree.class, "UITreeGroupSelector", "TreeGroupSelector");
      tree.setIcon("GroupAdminIcon");
      tree.setSelectedIcon("PortalIcon");
      tree.setBeanIdField("id");
      tree.setBeanLabelField("label");
      tree.setEscapeHTML(true);
      uiBreadcumbs.setBreadcumbsStyle("UIExplorerHistoryPath");
   }


   /**
    * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
    */
   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      UITree tree = getChild(UITree.class);
      if (tree != null && tree.getSibbling() == null)
      {
         Collection<?> sibblingsGroup = service.getGroupHandler().findGroups(null);
         tree.setSibbling((List)sibblingsGroup);
      }

      Collection<?> collection = service.getMembershipTypeHandler().findMembershipTypes();
      listMemberhip = new ArrayList<String>(5);
      for (Object obj : collection)
      {
         listMemberhip.add(((MembershipType)obj).getName());
      }
      if(!listMemberhip.contains("*"))
      {
         listMemberhip.add("*");
      }
      
      super.processRender(context);
   }

   public Group getCurrentGroup()
   {
      return selectGroup_;
   }

   public void changeGroup(String groupId) throws Exception
   {
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      UIBreadcumbs uiBreadcumb = getChild(UIBreadcumbs.class);
      uiBreadcumb.setPath(getPath(null, groupId));

      UITree tree = getChild(UITree.class);
      Collection<?> sibblingGroup;

      if (groupId == null)
      {
         sibblingGroup = service.getGroupHandler().findGroups(null);
         tree.setSibbling((List)sibblingGroup);
         tree.setChildren(null);
         tree.setSelected(null);
         selectGroup_ = null;
         return;
      }

      selectGroup_ = service.getGroupHandler().findGroupById(groupId);
      String parentGroupId = null;
      if (selectGroup_ != null)
      {
         parentGroupId = selectGroup_.getParentId();
      }
      Group parentGroup = null;
      if (parentGroupId != null)
      {
         parentGroup = service.getGroupHandler().findGroupById(parentGroupId);
      }

      Collection childrenGroup = service.getGroupHandler().findGroups(selectGroup_);
      sibblingGroup = service.getGroupHandler().findGroups(parentGroup);

      tree.setSibbling((List)sibblingGroup);
      tree.setChildren((List)childrenGroup);
      tree.setSelected(selectGroup_);
      tree.setParentSelected(parentGroup);
   }

   private List<LocalPath> getPath(List<LocalPath> list, String id) throws Exception
   {
      if (list == null)
      {
         list = new ArrayList<LocalPath>(5);
      }
      if (id == null)
      {
         return list;
      }
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      Group group = service.getGroupHandler().findGroupById(id);
      if (group == null)
      {
         return list;
      }
      list.add(0, new LocalPath(group.getId(), group.getGroupName()));
      getPath(list, group.getParentId());
      return list;
   }

   public List<String> getListMemberhip()
   {
      return listMemberhip;
   }

   public String event(String name, String beanId) throws Exception
   {
      UIForm uiForm = getAncestorOfType(UIForm.class);
      if (uiForm != null)
      {
         return uiForm.event(name, getId(), beanId);
      }
      return super.event(name, beanId);
   }

   static public class ChangeNodeActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
         UIComponent uiComp = event.getSource();
         UIGroupMembershipSelector uiSelector = uiComp.getParent();
         uiSelector.changeGroup(groupId);
         UIComponent uiPermission = uiSelector.<UIComponent> getParent().getParent();
         uiPermission.setRenderSibling(uiPermission.getClass());
         uiPermission.broadcast(event, Event.Phase.PROCESS);
         UIPopupWindow uiPopup = uiSelector.getParent();
         uiPopup.setShow(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);//TODO: Update relevant tab panes
      }
   }

   static public class SelectMembershipActionListener extends EventListener<UIGroupMembershipSelector>
   {
      public void execute(Event<UIGroupMembershipSelector> event) throws Exception
      {
         UIGroupMembershipSelector uiSelector = event.getSource();
         UIComponent uiPermission = uiSelector.<UIComponent> getParent().getParent();
         uiPermission.setRenderSibling(uiPermission.getClass());
         WebuiRequestContext pcontext = event.getRequestContext();

         UIPopupWindow uiPopup = uiSelector.getParent();
         UIForm uiForm = uiSelector.getAncestorOfType(UIForm.class);

         if (uiSelector.getCurrentGroup() == null)
         {
            UIApplication uiApp = pcontext.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIGroupMembershipSelector.msg.selectGroup", null));
            uiPopup.setShow(true);
            return;
         }
         event.getRequestContext().addUIComponentToUpdateByAjax(uiPermission);

         uiPermission.broadcast(event, event.getExecutionPhase());
         uiPopup.setShow(false);

      }
   }

   static public class SelectPathActionListener extends EventListener<UIBreadcumbs>
   {
      public void execute(Event<UIBreadcumbs> event) throws Exception
      {
         UIBreadcumbs uiBreadcumbs = event.getSource();
         UIGroupMembershipSelector uiSelector = uiBreadcumbs.getParent();
         String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
         uiBreadcumbs.setSelectPath(objectId);
         String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
         uiSelector.changeGroup(selectGroupId);

         UIPopupWindow uiPopup = uiSelector.getParent();
         uiPopup.setShow(true);

         event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);//TODO: Update relevant tab panes
      }
   }

}

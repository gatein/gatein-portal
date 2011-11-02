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

package org.exoplatform.organization.webui.component;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/** Created by The eXo Platform SARL Author : chungnv nguyenchung136@yahoo.com Jun 27, 2006 8:48:47 AM */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIGroupForm.SaveActionListener.class),
   @EventConfig(phase = Phase.DECODE, listeners = UIGroupForm.BackActionListener.class)})
@Serialized
public class UIGroupForm extends UIForm
{

   private String groupId;

   private String componentName_ = "AddGroup";

   private static String GROUP_NAME = "groupName", GROUP_LABEL = "label", GROUP_DESCRIPSION = "description";

   public UIGroupForm() throws Exception
   {
      addUIFormInput(new UIFormStringInput(GROUP_NAME, GROUP_NAME, null).addValidator(MandatoryValidator.class)
         .addValidator(StringLengthValidator.class, 3, 30).addValidator(IdentifierValidator.class));
      addUIFormInput(new UIFormStringInput(GROUP_LABEL, GROUP_LABEL, null).addValidator(StringLengthValidator.class, 3,
         30));
      addUIFormInput(new UIFormTextAreaInput(GROUP_DESCRIPSION, GROUP_DESCRIPSION, null).addValidator(
         StringLengthValidator.class, 0, 255));
   }

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroup(Group group) throws Exception
   {
      if (group != null)
      {
         this.groupId = group.getId();
         getUIStringInput(GROUP_NAME).setReadOnly(true);
         invokeGetBindingBean(group);
      }
      else
      {
         this.groupId = null;
         getUIStringInput(GROUP_NAME).setReadOnly(false);
         reset();
      }
   }

   public String getName()
   {
      return componentName_;
   }

   public void setName(String componentName)
   {
      componentName_ = componentName;
   }

   static public class SaveActionListener extends EventListener<UIGroupForm>
   {
      public void execute(Event<UIGroupForm> event) throws Exception
      {
         UIGroupForm uiGroupForm = event.getSource();
         UIGroupDetail uiGroupDetail = uiGroupForm.getParent();
         UIGroupManagement uiGroupManagement = uiGroupDetail.getParent();
         OrganizationService service = uiGroupForm.getApplicationComponent(OrganizationService.class);
         UIGroupExplorer uiGroupExplorer = uiGroupManagement.getChild(UIGroupExplorer.class);

         String currentGroupId = uiGroupForm.getGroupId();
         if (currentGroupId != null)
         {
            Group currentGroup = service.getGroupHandler().findGroupById(currentGroupId);

            if (currentGroup == null)
            {
               Object[] args = {uiGroupForm.getUIStringInput(GROUP_NAME).getValue()};
               UIApplication uiApp = event.getRequestContext().getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIGroupForm.msg.group-not-exist", args));
               uiGroupExplorer.changeGroup(null);
               uiGroupDetail.getChild(UIGroupForm.class).setGroup(null);
               uiGroupDetail.setRenderedChild(UIGroupInfo.class);
               return;
            }

            uiGroupForm.invokeSetBindingBean(currentGroup);
            if (currentGroup.getLabel() == null || currentGroup.getLabel().trim().length() == 0)
            {
               currentGroup.setLabel(currentGroup.getGroupName());
            }
            
            service.getGroupHandler().saveGroup(currentGroup, true);
            uiGroupForm.reset();
            uiGroupForm.setGroup(null);
            uiGroupExplorer.changeGroup(currentGroup.getId());
            uiGroupForm.setRenderSibling(UIGroupInfo.class);
            return;
         }

         //UIGroupExplorer uiGroupExplorer = uiGroupManagement.getChild(UIGroupExplorer.class) ;      
         Group currentGroup = uiGroupExplorer.getCurrentGroup();
         if (currentGroup != null)
         {
            currentGroupId = currentGroup.getId();
         }
         else
         {
            currentGroupId = null;
         }
         String groupName = "/" + uiGroupForm.getUIStringInput(GROUP_NAME).getValue();

         GroupHandler groupHandler = service.getGroupHandler();

         if (currentGroupId != null)
         {
            groupName = currentGroupId + groupName;
         }

         Group newGroup = groupHandler.findGroupById(groupName);
         if (newGroup != null)
         {
            Object[] args = {groupName};
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIGroupForm.msg.group-exist", args));
            return;
         }
         newGroup = groupHandler.createGroupInstance();
         uiGroupForm.invokeSetBindingBean(newGroup);
         if (newGroup.getLabel() == null || newGroup.getLabel().trim().length() == 0)
         {
            newGroup.setLabel(newGroup.getGroupName());
         }
         String changeGroupId;
         if (currentGroupId == null)
         {
            groupHandler.addChild(null, newGroup, true);
            //uiGroupExplorer.changeGroup(groupName) ;
            changeGroupId = groupName;
         }
         else
         {
            Group parrentGroup = groupHandler.findGroupById(currentGroupId);
            groupHandler.addChild(parrentGroup, newGroup, true);
            //uiGroupExplorer.changeGroup(currentGroupId) ;
            changeGroupId = currentGroupId;
         }

         // change group
         String username = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser();
         User user = service.getUserHandler().findUserByName(username);
         MembershipType membershipType =
            service.getMembershipTypeHandler().findMembershipType(GroupManagement.getUserACL().getAdminMSType());
         
         if(membershipType !=null){
            service.getMembershipHandler().linkMembership(user, newGroup, membershipType, true);
         }
         
         uiGroupExplorer.changeGroup(changeGroupId);
         uiGroupForm.reset();
         uiGroupForm.setGroup(null);
         uiGroupForm.setRenderSibling(UIGroupInfo.class);
      }
   }

   static public class BackActionListener extends EventListener<UIGroupForm>
   {
      public void execute(Event<UIGroupForm> event) throws Exception
      {
         UIGroupForm uiGroupForm = event.getSource();
         uiGroupForm.reset();
         uiGroupForm.setGroup(null);
         uiGroupForm.setRenderSibling(UIGroupInfo.class);
         event.getRequestContext().setProcessRender(true);
      }
   }

}

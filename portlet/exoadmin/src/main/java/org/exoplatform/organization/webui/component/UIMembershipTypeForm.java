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

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIMembershipTypeForm.SaveActionListener.class),
   @EventConfig(listeners = UIMembershipTypeForm.ResetActionListener.class, phase = Phase.DECODE)})
@Serialized
public class UIMembershipTypeForm extends UIForm
{

   private static String MEMBERSHIP_TYPE_NAME = "name", DESCRIPTION = "description";

   private String membershipTypeName;

   public UIMembershipTypeForm() throws Exception
   {
      addUIFormInput(new UIFormStringInput(MEMBERSHIP_TYPE_NAME, MEMBERSHIP_TYPE_NAME, null).setReadOnly(false)
         .addValidator(MandatoryValidator.class).addValidator(StringLengthValidator.class, 3, 30)
         .addValidator(NameValidator.class).addValidator(SpecialCharacterValidator.class));

      addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
   }

   public void setMembershipType(MembershipType membershipType) throws Exception
   {
      if (membershipType == null)
      {
         membershipTypeName = null;
         getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
         return;
      }
      else
      {
         membershipTypeName = membershipType.getName();
         getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(true);
      }
      invokeGetBindingBean(membershipType);
   }

   public String getMembershipTypeName()
   {
      return membershipTypeName;
   }

   static public class SaveActionListener extends EventListener<UIMembershipTypeForm>
   {
      public void execute(Event<UIMembershipTypeForm> event) throws Exception
      {
         UIMembershipTypeForm uiForm = event.getSource();
         UIMembershipManagement uiMembershipManagement = uiForm.getParent();
         OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
         String msTypeName = uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).getValue();

         MembershipType mt = service.getMembershipTypeHandler().findMembershipType(msTypeName);

         if (uiForm.getMembershipTypeName() == null)
         {
            //For create new membershipType case
            if (mt != null)
            {
               UIApplication uiApp = event.getRequestContext().getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIMembershipTypeForm.msg.SameName", null));
               return;
            }
            mt = service.getMembershipTypeHandler().createMembershipTypeInstance();
            uiForm.invokeSetBindingBean(mt);
            service.getMembershipTypeHandler().createMembershipType(mt, true);
            uiMembershipManagement.addOptions(mt);
         }
         else
         {
            //For edit a membershipType case
            if (mt == null)
            {
               UIApplication uiApp = event.getRequestContext().getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIMembershipTypeForm.msg.MembershipNotExist",
                  new String[]{msTypeName}));
            }
            else
            {
               uiForm.invokeSetBindingBean(mt);
               service.getMembershipTypeHandler().saveMembershipType(mt, true);
            }
         }

         uiMembershipManagement.getChild(UIListMembershipType.class).loadData();
         uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
         uiForm.setMembershipType(null);
         uiForm.reset();
      }
   }

   static public class ResetActionListener extends EventListener<UIMembershipTypeForm>
   {
      public void execute(Event<UIMembershipTypeForm> event) throws Exception
      {
         UIMembershipTypeForm uiForm = event.getSource();
         uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
         uiForm.setMembershipType(null);
         uiForm.reset();
      }
   }
}

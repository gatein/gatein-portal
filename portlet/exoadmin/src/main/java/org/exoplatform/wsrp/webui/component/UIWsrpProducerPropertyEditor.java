/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.wsrp.webui.component;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;


/** @author Wesley Hales */
@ComponentConfig(template = "app:/groovy/wsrp/webui/component/UIWsrpProducerPropertyEditor.gtmpl", lifecycle = UIFormLifecycle.class, events = {
   @EventConfig(listeners = UIWsrpProducerPropertyEditor.SaveActionListener.class)})
public class UIWsrpProducerPropertyEditor extends UIForm
{
   protected static final String DESCRIPTION = "description";
   protected static final String LABEL = "label";
   protected static final String HINT = "hint";
   private RegistrationPropertyDescription registrationPropertyDescription_;

   public UIWsrpProducerPropertyEditor() throws Exception
   {
      addUIFormInput(new UIFormStringInput(DESCRIPTION, DESCRIPTION, null).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput(LABEL, LABEL, null).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput(HINT, HINT, null).addValidator(MandatoryValidator.class));
   }

   public void setRegistrationPropertyDescription(RegistrationPropertyDescription registrationPropertyDescription) throws Exception
   {

      this.registrationPropertyDescription_ = registrationPropertyDescription;

      if (registrationPropertyDescription_ == null)
      {
         getUIStringInput(DESCRIPTION).setEditable(UIFormStringInput.ENABLE);
         reset();
         return;
      }
      getUIStringInput(DESCRIPTION).setEditable(UIFormStringInput.ENABLE);

      invokeGetBindingBean(registrationPropertyDescription_);

   }

   public RegistrationPropertyDescription getRegistrationPropertyDescription()
   {
      return registrationPropertyDescription_;
   }

   static public class SaveActionListener extends EventListener<UIWsrpProducerPropertyEditor>
   {
      public void execute(Event<UIWsrpProducerPropertyEditor> event) throws Exception
      {
         // todo: implement
      }
   }

   public boolean save(WebuiRequestContext context) throws Exception
   {

      ProducerConfigurationService pconfService = getProducerConfigurationService();
      ProducerConfiguration producerConfiguration = pconfService.getConfiguration();


      RegistrationPropertyDescription regPropDesc = getRegistrationPropertyDescription();
      if (regPropDesc != null)
      {
         System.out.println("---------old1: " + regPropDesc.getDescription());
         RegistrationPropertyDescription oldRegPropDesc = producerConfiguration.getRegistrationRequirements().getRegistrationProperties().get(regPropDesc);
         invokeSetBindingBean(oldRegPropDesc);
         System.out.println("---------new1: " + oldRegPropDesc.getNameAsString());
         //return;
      }
      else
      {

         RegistrationPropertyDescription newRegProcDesc = new RegistrationPropertyDescription();
         newRegProcDesc.setDescription(new LocalizedString(getUIStringInput(DESCRIPTION).getValue()));
         newRegProcDesc.setDescription(new LocalizedString(getUIStringInput(LABEL).getValue()));
         newRegProcDesc.setDescription(new LocalizedString(getUIStringInput(HINT).getValue()));
         System.out.println("---------new2: " + newRegProcDesc);
         producerConfiguration.getRegistrationRequirements().addRegistrationProperty(newRegProcDesc);
      }

      UIApplication uiApp = context.getUIApplication();

      try
      {
         pconfService.saveConfiguration();
         uiApp.addMessage(new ApplicationMessage("Producer Successfully Changed", null));
      }
      catch (Exception e)
      {
         uiApp.addMessage(new ApplicationMessage("Producer Modification Error", null));

      }

      return true;
   }

   public ProducerConfigurationService getProducerConfigurationService() throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      return (ProducerConfigurationService)manager.getComponentInstanceOfType(ProducerConfigurationService.class);
   }
}

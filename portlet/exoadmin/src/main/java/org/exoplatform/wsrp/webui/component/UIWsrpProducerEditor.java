/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;

/** @author Wesley Hales */
@ComponentConfig(template = "app:/groovy/wsrp/webui/component/UIWsrpProducerEditor.gtmpl", lifecycle = UIFormLifecycle.class, events = {
   @EventConfig(listeners = UIWsrpProducerEditor.SaveActionListener.class)})
public class UIWsrpProducerEditor extends UIForm
{

   protected static final String REG_REQUIRED_FOR_DESCRIPTION = "registrationrequiredforfulldescription";
   protected static final String STRICT_MODE = "strictmode";
   protected static final String REQUIRES_REGISTRATION = "requiresregistration";
   protected static final String POLICY_CLASS = "policyClassName";
   protected static final String VALIDATOR_CLASS = "validatorClassName";

   public UIWsrpProducerEditor() throws Exception
   {
      addUIFormInput(new UIFormCheckBoxInput(REG_REQUIRED_FOR_DESCRIPTION, REG_REQUIRED_FOR_DESCRIPTION, null));
      addUIFormInput(new UIFormCheckBoxInput(STRICT_MODE, STRICT_MODE, null));
      addUIFormInput(new UIFormCheckBoxInput(REQUIRES_REGISTRATION, REQUIRES_REGISTRATION, null));
      /*addUIFormInput(new UIFormStringInput(POLICY_CLASS, POLICY_CLASS, null).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput(VALIDATOR_CLASS, VALIDATOR_CLASS, null).addValidator(MandatoryValidator.class));*/
   }

   public void setProducerConfig(ProducerConfiguration producerConfiguration) throws Exception
   {

      ProducerRegistrationRequirements registrationRequirements = producerConfiguration.getRegistrationRequirements();
      getUIFormCheckBoxInput(REG_REQUIRED_FOR_DESCRIPTION).setValue(registrationRequirements.isRegistrationRequiredForFullDescription());
      getUIFormCheckBoxInput(STRICT_MODE).setValue(producerConfiguration.isUsingStrictMode());
      getUIFormCheckBoxInput(REQUIRES_REGISTRATION).setValue(registrationRequirements.isRegistrationRequired());

      /*RegistrationPolicy policy = registrationRequirements.getPolicy();
      String policyClassName = policy.getClass().getName();
      getUIStringInput(POLICY_CLASS).setValue(policyClassName);

      if (ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
      {
         DefaultRegistrationPolicy defaultPolicy = (DefaultRegistrationPolicy)policy;
         getUIStringInput(VALIDATOR_CLASS).setValue(defaultPolicy.getValidator().getClass().getName());
      }*/
   }


   static public class SaveActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event) throws Exception
      {
         UIWsrpProducerEditor producerEditor = event.getSource();

         WebuiRequestContext ctx = event.getRequestContext();
         producerEditor.save(ctx);
         //producerEditor.reset();

      }
   }

   public boolean save(WebuiRequestContext context) throws Exception
   {
      //ConsumerRegistry consumerRegistry = getConsumerRegistry();
      //ProducerInfo producerInfo = consumerRegistry.getProducerInfoByKey("").getRegistrationInfo().
      UIApplication uiApp = context.getUIApplication();

      ProducerConfigurationService pconfService = getProducerConfigurationService();
      ProducerConfiguration producerConfiguration = pconfService.getConfiguration();
      try
      {
         ProducerRegistrationRequirements registrationRequirements = producerConfiguration.getRegistrationRequirements();
         registrationRequirements.setRegistrationRequiredForFullDescription(Boolean.parseBoolean(getUIFormCheckBoxInput(REG_REQUIRED_FOR_DESCRIPTION).getValue().toString()));
         producerConfiguration.setUsingStrictMode(Boolean.parseBoolean(getUIFormCheckBoxInput(STRICT_MODE).getValue().toString()));

         registrationRequirements.setRegistrationRequired(Boolean.parseBoolean(getUIFormCheckBoxInput(REQUIRES_REGISTRATION).getValue().toString()));
//         registrationRequirements.reloadPolicyFrom(getUIStringInput(POLICY_CLASS).getValue(), getUIStringInput(VALIDATOR_CLASS).getValue());

         pconfService.saveConfiguration();
         uiApp.addMessage(new ApplicationMessage("Producer Successfully Changed", null));
      }
      catch (Exception e)
      {
         uiApp.addMessage(new ApplicationMessage("Producer Modification Error", null, ApplicationMessage.ERROR));
         e.printStackTrace();
      }

      return true;
   }

   public ProducerConfigurationService getProducerConfigurationService() throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      return (ProducerConfigurationService)manager.getComponentInstanceOfType(ProducerConfigurationService.class);
   }
}

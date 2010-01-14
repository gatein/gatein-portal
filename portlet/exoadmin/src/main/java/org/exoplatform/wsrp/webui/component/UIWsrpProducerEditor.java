/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
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

/**
 * @author Wesley Hales
 * @author Chris Laprun
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/wsrp/webui/component/UIWsrpProducerEditor.gtmpl",
   events = {
      @EventConfig(listeners = UIWsrpProducerEditor.SaveActionListener.class),
      @EventConfig(listeners = UIWsrpProducerEditor.RegistrationOnChangeActionListener.class)
   }
)
public class UIWsrpProducerEditor extends UIForm
{
   private static final String REG_REQUIRED_FOR_DESCRIPTION = "registrationrequiredforfulldescription";
   private static final String STRICT_MODE = "strictmode";
   private static final String REQUIRES_REGISTRATION = "requiresregistration";
   private static final String REGISTRATION_DETAILS = "registrationdetails";

   private ProducerConfigurationService configService;
   private UIWsrpRegistrationDetails registrationDetails;
   private UIFormCheckBoxInput regReqForDesc;
   private UIFormCheckBoxInput strictMode;
   private UIFormCheckBoxInput<Boolean> regRequired;

   public UIWsrpProducerEditor() throws Exception
   {
      configService = Util.getUIPortalApplication().getApplicationComponent(ProducerConfigurationService.class);

      regReqForDesc = new UIFormCheckBoxInput(REG_REQUIRED_FOR_DESCRIPTION, REG_REQUIRED_FOR_DESCRIPTION, null);
      addUIFormInput(regReqForDesc);
      strictMode = new UIFormCheckBoxInput(STRICT_MODE, STRICT_MODE, null);
      addUIFormInput(strictMode);

      // registration required check box with onChange event
      regRequired = new UIFormCheckBoxInput<Boolean>(REQUIRES_REGISTRATION, REQUIRES_REGISTRATION, null);
      regRequired.setOnChange("RegistrationOnChange");
      addUIFormInput(regRequired);
      // because when we use setOnChange method, new eventListener will add to this form, we must re-set the actions of this form.
      // thif form has no action, so i'll put empty string array
      setActions(new String[]{});

      // registration details
      // form set to gather registration information
      registrationDetails = addChild(UIWsrpRegistrationDetails.class, null, null);

      init(false);
   }

   ProducerConfigurationService getService()
   {
      return configService;
   }

   private void init(boolean getRegistrationRequiredFromCheckBox) throws Exception
   {
      ProducerConfiguration configuration = configService.getConfiguration();

      ProducerRegistrationRequirements registrationRequirements = configuration.getRegistrationRequirements();
      regReqForDesc.setValue(registrationRequirements.isRegistrationRequiredForFullDescription());
      strictMode.setValue(configuration.isUsingStrictMode());

      boolean registrationRequired = getRegistrationRequiredFromCheckBox ?
         regRequired.getValue() :
         registrationRequirements.isRegistrationRequired();
      regRequired.setValue(registrationRequired);

      // if registration is required then we display more information
      if (registrationRequired)
      {
         registrationDetails.init(registrationRequirements);
         registrationDetails.setRendered(true);
      }
      else
      {
         registrationDetails.setRendered(false);
      }
   }


   static public class SaveActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event) throws Exception
      {
         UIWsrpProducerEditor source = event.getSource();
         ProducerConfigurationService service = source.getService();
         ProducerConfiguration producerConfiguration = service.getConfiguration();
         ProducerRegistrationRequirements registrationRequirements = producerConfiguration.getRegistrationRequirements();
         registrationRequirements.setRegistrationRequiredForFullDescription(Boolean.parseBoolean(source.regReqForDesc.getValue().toString()));
         producerConfiguration.setUsingStrictMode(Boolean.parseBoolean(source.strictMode.getValue().toString()));

         boolean requiresReg = Boolean.parseBoolean(source.regRequired.getValue().toString());
         registrationRequirements.setRegistrationRequired(requiresReg);
         if (requiresReg)
         {
            source.registrationDetails.updateRegistrationDetailsFromForm(registrationRequirements);
         }

         try
         {
            service.saveConfiguration();
         }
         catch (Exception e)
         {
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("Producer Modification Error", null, ApplicationMessage.ERROR));
            e.printStackTrace();
         }

         source.init(false);
      }
   }

   /*public static class RegistrationOnChangeActionListener extends EventListener<UIFormCheckBoxInput>
   {
      public void execute(Event<UIFormCheckBoxInput> event) throws Exception
      {
         UIFormCheckBoxInput source = event.getSource();
         UIWsrpProducerEditor parent = source.getAncestorOfType(UIWsrpProducerEditor.class);
         parent.init();

         //update only the parent, avoid updating the full portlet
         event.getRequestContext().addUIComponentToUpdateByAjax(parent);
      }
   }*/

   public static class RegistrationOnChangeActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event) throws Exception
      {
         UIWsrpProducerEditor source = event.getSource();
         source.init(true);

         //update only the parent, avoid updating the full portlet
         event.getRequestContext().addUIComponentToUpdateByAjax(source);
      }
   }

}

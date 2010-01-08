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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** @author Wesley Hales */
@ComponentConfigs({
   @ComponentConfig(id = "RegistrationPropertySelector", type = UIGrid.class, template = "system:/groovy/webui/core/UIGrid.gtmpl"),
   @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "app:/groovy/wsrp/webui/component/UIWsrpProducerEditor.gtmpl",
      events = {
         @EventConfig(listeners = UIWsrpProducerEditor.AddPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerEditor.EditPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerEditor.DeletePropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerEditor.SaveActionListener.class)
      }
   )})
public class UIWsrpProducerEditor extends UIForm
{
   private static String[] FIELDS = {"name", "description", "label", "hint"};
   private static String[] SELECT_ACTIONS = {"AddProperty", "EditProperty", "DeleteProperty"};

   private static final String REG_REQUIRED_FOR_DESCRIPTION = "registrationrequiredforfulldescription";
   private static final String STRICT_MODE = "strictmode";
   private static final String REQUIRES_REGISTRATION = "requiresregistration";
   private static final String POLICY_CLASS = "policyClassName";
   private static final String VALIDATOR_CLASS = "validatorClassName";
   private static final String REGISTRATION_DETAILS = "registrationdetails";

   private ProducerConfigurationService configService;
   private static final String REGISTRATION_PROPERTIES = "RegistrationPropertySelector";
   private static final String REGISTRATION_PROPERTIES_ITERATOR = "ProducerPropPageIterator";
   private UIFormInputSet registrationDetails;
   private UIFormInputBase<String> policy;
   private UIFormInputBase<String> validator;
   private UIFormCheckBoxInput regReqForDesc;
   private UIFormCheckBoxInput strictMode;
   private UIFormCheckBoxInput<Boolean> regRequired;
   private UIGrid registrationProperties;

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
      registrationDetails = new UIWSRPFormInputSet(REGISTRATION_DETAILS);
      addUIFormInput(registrationDetails);

      // policy
      policy = new UIFormStringInput(POLICY_CLASS, POLICY_CLASS, null);
      registrationDetails.addUIFormInput(policy);

      // validator
      validator = new UIFormStringInput(VALIDATOR_CLASS, VALIDATOR_CLASS, null);
      registrationDetails.addUIFormInput(validator);

      // registration properties
      registrationProperties = registrationDetails.addChild(UIGrid.class, REGISTRATION_PROPERTIES, null);
      //configure the edit and delete buttons based on an id from the data list - this will also be passed as param to listener
      registrationProperties.configure("name", FIELDS, SELECT_ACTIONS);
      registrationProperties.getUIPageIterator().setId(REGISTRATION_PROPERTIES_ITERATOR);
      registrationDetails.addChild(registrationProperties.getUIPageIterator());
      registrationProperties.getUIPageIterator().setRendered(false);

      init();

      //add the popup for property edit and adding new properties
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, null);
      popup.setWindowSize(400, 300);
      UIWsrpProducerPropertyEditor propertyForm = createUIComponent(UIWsrpProducerPropertyEditor.class, null, "Producer Property Editor");
      popup.setUIComponent(propertyForm);
      popup.setRendered(false);
   }

   ProducerConfigurationService getService()
   {
      return configService;
   }

   private void init() throws Exception
   {
      ProducerConfiguration configuration = configService.getConfiguration();

      ProducerRegistrationRequirements registrationRequirements = configuration.getRegistrationRequirements();
      regReqForDesc.setValue(registrationRequirements.isRegistrationRequiredForFullDescription());
      strictMode.setValue(configuration.isUsingStrictMode());

      boolean registrationRequired = registrationRequirements.isRegistrationRequired();
      regRequired.setValue(registrationRequired);

      // if registration is required then we display more information
      if (registrationRequired)
      {
         registrationDetails.setRendered(true);

         RegistrationPolicy policy = registrationRequirements.getPolicy();
         String policyClassName = policy.getClass().getName();
         this.policy.setValue(policyClassName);

         // if policy is the default one, display information about the validator
         if (ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
         {
            DefaultRegistrationPolicy defaultPolicy = (DefaultRegistrationPolicy)policy;
            validator.setValue(defaultPolicy.getValidator().getClass().getName());
            validator.setRendered(true);
         }
         else
         {
            validator.setRendered(false);
         }

         // registration properties
         Map<QName, RegistrationPropertyDescription> regProps = configuration.getRegistrationRequirements().getRegistrationProperties();
         registrationProperties.getUIPageIterator().setPageList(createPageList(getPropertyList(regProps)));
      }
      else
      {
         registrationDetails.setRendered(false);
      }
   }

   private List<RegistrationPropertyDescription> getPropertyList(Map<QName, RegistrationPropertyDescription> descriptions) throws Exception
   {
      Comparator<RegistrationPropertyDescription> descComparator = new Comparator<RegistrationPropertyDescription>()
      {
         public int compare(RegistrationPropertyDescription o1, RegistrationPropertyDescription o2)
         {
            return o1.getName().toString().compareTo(o2.getName().toString());
         }
      };

      List<RegistrationPropertyDescription> result = new ArrayList<RegistrationPropertyDescription>(descriptions.values());
      Collections.sort(result, descComparator);
      return result;
   }

   private LazyPageList<RegistrationPropertyDescription> createPageList(final List<RegistrationPropertyDescription> pageList)
   {
      return new LazyPageList<RegistrationPropertyDescription>(new ListAccess<RegistrationPropertyDescription>()
      {

         public int getSize() throws Exception
         {
            return pageList.size();
         }

         public RegistrationPropertyDescription[] load(int index, int length) throws Exception, IllegalArgumentException
         {
            RegistrationPropertyDescription[] pcs = new RegistrationPropertyDescription[pageList.size()];

            if (index < 0)
            {
               throw new IllegalArgumentException("Illegal index: index must be a positive number");
            }

            if (length < 0)
            {
               throw new IllegalArgumentException("Illegal length: length must be a positive number");
            }

            if (index + length > pageList.size())
            {
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");
            }

            for (int i = 0; i < length; i++)
            {
               pcs[i] = pageList.get(i + index);
            }

            return pcs;
         }

      }, 10);
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
            registrationRequirements.reloadPolicyFrom(source.policy.getValue(), source.validator.getValue());
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

         source.init();
      }
   }

   static public class EditPropertyActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event) throws Exception
      {
         UIWsrpProducerEditor source = event.getSource();
         UIPopupWindow popup = source.getChild(UIPopupWindow.class);
         UIWsrpProducerPropertyEditor editor = (UIWsrpProducerPropertyEditor)popup.getUIComponent();
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         try
         {
            editor.setRegistrationPropertyDescription(source.getService().getConfiguration().getRegistrationRequirements().getRegistrationPropertyWith(id));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         popup.setUIComponent(editor);
         popup.setRendered(true);
         popup.setShow(true);

      }
   }

   static public class DeletePropertyActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event)
      {
         UIWsrpProducerEditor source = event.getSource();
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         UIPopupWindow popup = source.getChild(UIPopupWindow.class);
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         try
         {
            ProducerConfigurationService service = source.getService();
            service.getConfiguration().getRegistrationRequirements().removeRegistrationProperty(id);
            service.saveConfiguration();
         }
         catch (Exception e)
         {
            e.printStackTrace();
            uiApp.addMessage(new ApplicationMessage("Failed to delete Producer Property. Cause: " + e.getCause(), null, ApplicationMessage.ERROR));
         }

      }
   }

   static public class AddPropertyActionListener extends EventListener<UIWsrpProducerEditor>
   {
      public void execute(Event<UIWsrpProducerEditor> event) throws Exception
      {
         UIWsrpProducerEditor source = event.getSource();
         UIPopupWindow popup = source.getChild(UIPopupWindow.class);
         UIWsrpProducerPropertyEditor editor = (UIWsrpProducerPropertyEditor)popup.getUIComponent();

         //reset the form
         editor.setRegistrationPropertyDescription(null);
         popup.setRendered(true);
         popup.setShow(true);
         popup.setShowCloseButton(true);
         //popup.setShowMask(true);

      }
   }

   public static class RegistrationOnChangeActionListener extends EventListener<UIFormCheckBoxInput>
   {
      public void execute(Event<UIFormCheckBoxInput> event) throws Exception
      {
         UIFormCheckBoxInput source = event.getSource();
         UIWsrpProducerEditor parent = source.getAncestorOfType(UIWsrpProducerEditor.class);
         parent.init();

         //update only the parent, avoid updating the full portlet
         event.getRequestContext().addUIComponentToUpdateByAjax(parent);
      }
   }

}

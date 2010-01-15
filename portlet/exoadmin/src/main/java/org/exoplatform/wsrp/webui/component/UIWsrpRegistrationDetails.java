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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.renderers.ValueRenderer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@ComponentConfigs({
   @ComponentConfig(id = UIWsrpRegistrationDetails.REGISTRATION_PROPERTIES, type = UIGrid.class, template = "system:/groovy/webui/core/UIGrid.gtmpl"),
   @ComponentConfig(
      lifecycle = UIContainerLifecycle.class,
      events = {
         @EventConfig(listeners = UIWsrpRegistrationDetails.AddPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpRegistrationDetails.EditPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpRegistrationDetails.DeletePropertyActionListener.class)
      })
})
public class UIWsrpRegistrationDetails extends UIFormInputSet
{
   private UIFormInputBase<String> policy;
   private UIFormInputBase<String> validator;
   private UIGrid registrationProperties;
   private static final String NAME = "name";
   static String[] FIELDS = {NAME, "description", "label", "hint"};
//   static String[] PROPERTIES_ACTIONS = {"EditProperty", "DeleteProperty"};
   static String[] PROPERTIES_ACTIONS = {};
   static final String POLICY_CLASS = "policyClassName";
   static final String VALIDATOR_CLASS = "validatorClassName";
   static final String REGISTRATION_PROPERTIES = "registrationproperties";
   static final String REGISTRATION_PROPERTIES_ITERATOR = "registrationpropertiesiterator";

   public UIWsrpRegistrationDetails() throws Exception
   {
      //super(name);

      // policy
      policy = new UIFormStringInput(POLICY_CLASS, POLICY_CLASS, null);
      addUIFormInput(policy);

      // validator
      validator = new UIFormStringInput(VALIDATOR_CLASS, VALIDATOR_CLASS, null);
      addUIFormInput(validator);

      // registration properties
      registrationProperties = addChild(UIGrid.class, REGISTRATION_PROPERTIES, REGISTRATION_PROPERTIES);

      // add renderer for LocalizedString
      ValueRenderer<LocalizedString> renderer = new ValueRenderer<LocalizedString>()
      {
         @Override
         public String render(LocalizedString value)
         {
            return value.getValue();
         }
      };
      registrationProperties.registerRendererFor(renderer, LocalizedString.class);

      //configure the edit and delete buttons based on an id from the data list - this will also be passed as param to listener
      registrationProperties.configure(NAME, FIELDS, PROPERTIES_ACTIONS);
      registrationProperties.getUIPageIterator().setId(REGISTRATION_PROPERTIES_ITERATOR);
      registrationProperties.getUIPageIterator().setRendered(false);
      addChild(registrationProperties.getUIPageIterator());

      init(getService().getConfiguration().getRegistrationRequirements());

      //add the popup for property edit and adding new properties
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, null);
      popup.setWindowSize(400, 300);
      UIWsrpProducerPropertyEditor propertyForm = createUIComponent(UIWsrpProducerPropertyEditor.class, null, "Producer Property Editor");
      popup.setUIComponent(propertyForm);
      popup.setRendered(false);
   }

   void init(ProducerRegistrationRequirements registrationRequirements) throws Exception
   {
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
      Map<QName, RegistrationPropertyDescription> regProps = registrationRequirements.getRegistrationProperties();
      registrationProperties.getUIPageIterator().setPageList(createPageList(getPropertyList(regProps)));
   }

   ProducerConfigurationService getService()
   {
      return Util.getUIPortalApplication().getApplicationComponent(ProducerConfigurationService.class);
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

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      /*if (getComponentConfig() != null)
      {
         super.processRender(context);
         return;
      }*/

      Writer w = context.getWriter();
      w.write("<div class=\"UIFormInputSet\">");

      ResourceBundle res = context.getApplicationResourceBundle();
      UIForm uiForm = getAncestorOfType(UIForm.class);
      for (UIComponent inputEntry : getChildren())
      {
         if (inputEntry.isRendered())
         {
            w.write("<div class=\"row\">");

            String label;
            try
            {
               label = uiForm.getLabel(res, inputEntry.getId());
            }
            catch (MissingResourceException ex)
            {
               label = inputEntry.getName();
               System.err.println("\n " + uiForm.getId() + ".label." + inputEntry.getId() + " not found value");
            }

            w.write("<label>" + label + "</label>");
            renderUIComponent(inputEntry);

            w.write("</div>");
         }
      }
      w.write("</div>");
   }

   public void updateRegistrationDetailsFromForm(ProducerRegistrationRequirements registrationRequirements)
   {
      registrationRequirements.reloadPolicyFrom(policy.getValue(), validator.getValue());
   }

   static public class EditPropertyActionListener extends EventListener<UIWsrpRegistrationDetails>
   {
      public void execute(Event<UIWsrpRegistrationDetails> event) throws Exception
      {
         UIWsrpRegistrationDetails source = event.getSource();
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

   static public class DeletePropertyActionListener extends EventListener<UIWsrpRegistrationDetails>
   {
      public void execute(Event<UIWsrpRegistrationDetails> event)
      {
         UIWsrpRegistrationDetails source = event.getSource();
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
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("Failed to delete Producer Property. Cause: " + e.getCause(), null, ApplicationMessage.ERROR));
         }

      }
   }

   static public class AddPropertyActionListener extends EventListener<UIWsrpRegistrationDetails>
   {
      public void execute(Event<UIWsrpRegistrationDetails> event) throws Exception
      {
         UIWsrpRegistrationDetails source = event.getSource();
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
}

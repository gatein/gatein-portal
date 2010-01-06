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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** @author Wesley Hales */
@ComponentConfigs({
   @ComponentConfig(id = "RegistrationPropertySelector", type = UIGrid.class, template = "system:/groovy/webui/core/UIGrid.gtmpl"),
   @ComponentConfig(
      lifecycle = UIApplicationLifecycle.class,
      template = "app:/groovy/wsrp/webui/component/UIWsrpProducerOverview.gtmpl",
      events = {
//         @EventConfig(listeners = UIWsrpProducerOverview.AddPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerOverview.EditPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerOverview.DeletePropertyActionListener.class)
      }
   )})
public class UIWsrpProducerOverview extends UIContainer
{

   public static String[] FIELDS = {"name", "description", "label", "hint"};

   public static String[] SELECT_ACTIONS = {"AddProperty", "EditProperty", "DeleteProperty"};

   public UIWsrpProducerOverview() throws Exception
   {
      ProducerConfigurationService service = getProducerConfigurationService();
      ProducerConfiguration producerConfiguration = service.getConfiguration();
      UIWsrpProducerEditor producerForm = createUIComponent(UIWsrpProducerEditor.class, null, "Producer Editor");
      producerForm.setProducerConfig(producerConfiguration);
      addChild(producerForm);

      /*UIGrid uiGrid = addChild(UIGrid.class, "RegistrationPropertySelector", null);
      //configure the edit and delete buttons based on an id from the data list - this will also be passed as param to listener
      uiGrid.configure("name", FIELDS, SELECT_ACTIONS);

      //add the propery grid
      uiGrid.getUIPageIterator().setId("ProducerPropPageIterator");
      addChild(uiGrid.getUIPageIterator());
      uiGrid.getUIPageIterator().setRendered(false);
      LazyPageList propertyList = createPageList(getPropertyList());
      uiGrid.getUIPageIterator().setPageList(propertyList);

      //add the popup for property edit and adding new properties
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, null);
      popup.setWindowSize(400, 300);
      UIWsrpProducerPropertyEditor propertyForm = createUIComponent(UIWsrpProducerPropertyEditor.class, null, "Producer Property Editor");
      popup.setUIComponent(propertyForm);
      popup.setRendered(false);*/
   }

   public List<RegistrationPropertyDescription> getPropertyList() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfigurationService().getConfiguration();
      Map descriptions = producerConfiguration.getRegistrationRequirements().getRegistrationProperties();
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

   public List<String> getSupportedPropertyTypes()
   {
      return Collections.singletonList(("xsd:string"));
   }

   public LazyPageList<RegistrationPropertyDescription> createPageList(final List<RegistrationPropertyDescription> pageList)
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

   static public class EditPropertyActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event) throws Exception
      {
         UIWsrpProducerOverview producerOverview = event.getSource();
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         UIPopupWindow popup = producerOverview.getChild(UIPopupWindow.class);
         UIWsrpProducerPropertyEditor editor = (UIWsrpProducerPropertyEditor)popup.getUIComponent();
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         try
         {
            editor.setRegistrationPropertyDescription(producerOverview.getProducerConfigurationService().getConfiguration().getRegistrationRequirements().getRegistrationPropertyWith(id));
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

   static public class DeletePropertyActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event)
      {
         UIWsrpProducerOverview producerOverview = event.getSource();
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         UIPopupWindow popup = producerOverview.getChild(UIPopupWindow.class);
         UIWsrpProducerPropertyEditor editor = (UIWsrpProducerPropertyEditor)popup.getUIComponent();
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         try
         {
            producerOverview.getProducerConfigurationService().getConfiguration().getRegistrationRequirements().removeRegistrationProperty(id);
            producerOverview.getProducerConfigurationService().saveConfiguration();
         }
         catch (Exception e)
         {
            e.printStackTrace();
            uiApp.addMessage(new ApplicationMessage("Failed to delete Producer Property. Cause: " + e.getCause(), null, ApplicationMessage.ERROR));
         }

      }
   }

   static public class AddPropertyActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event) throws Exception
      {
         UIWsrpProducerOverview wsrpProducerOverview = event.getSource();
         UIPopupWindow popup = wsrpProducerOverview.getChild(UIPopupWindow.class);
         UIWsrpProducerPropertyEditor editor = (UIWsrpProducerPropertyEditor)popup.getUIComponent();

         //reset the form
         editor.setRegistrationPropertyDescription(null);
         popup.setRendered(true);
         popup.setShow(true);
         popup.setShowCloseButton(true);
         //popup.setShowMask(true);

      }
   }

   public ProducerConfigurationService getProducerConfigurationService() throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      return (ProducerConfigurationService)manager.getComponentInstanceOfType(ProducerConfigurationService.class);
   }
}

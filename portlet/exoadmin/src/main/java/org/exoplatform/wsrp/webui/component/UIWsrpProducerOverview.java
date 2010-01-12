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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.renderers.ValueRenderer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** @author Wesley Hales */
@ComponentConfigs({
   @ComponentConfig(id = UIWsrpProducerOverview.REGISTRATION_PROPERTIES, type = UIGrid.class, template = "system:/groovy/webui/core/UIGrid.gtmpl"),
   @ComponentConfig(
      lifecycle = UIApplicationLifecycle.class,
      template = "app:/groovy/wsrp/webui/component/UIWsrpProducerOverview.gtmpl",
      events = {
         @EventConfig(listeners = UIWsrpProducerOverview.AddPropertyActionListener.class),
         @EventConfig(listeners = UIWsrpProducerOverview.EditActionListener.class),
         @EventConfig(listeners = UIWsrpProducerOverview.DeleteActionListener.class)
      }
   )})

public class UIWsrpProducerOverview extends UIContainer
{
   private static String[] FIELDS = {"key", "name", "description", "label", "hint"};
   private static String[] SELECT_ACTIONS = {"Add", "Edit", "Delete"};

   public static final String REGISTRATION_PROPERTIES = "RegistrationPropertySelector";
   private static final String REGISTRATION_PROPERTIES_ITERATOR = "ProducerPropPageIterator";
   private UIGrid registrationProperties;
   private ProducerConfigurationService configService;
   private UIWsrpProducerEditor producerForm;

   public UIWsrpProducerOverview() throws Exception
   {
      //setSelectedTab(2);
      configService = Util.getUIPortalApplication().getApplicationComponent(ProducerConfigurationService.class);
      ProducerConfiguration configuration = configService.getConfiguration();
      //producerForm = createUIComponent();
      addChild(UIWsrpProducerEditor.class, null, null);
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
      registrationProperties.configure("key", FIELDS, SELECT_ACTIONS);
      registrationProperties.getUIPageIterator().setId(REGISTRATION_PROPERTIES_ITERATOR);
      //registrationDetails.addChild(registrationProperties.getUIPageIterator());
      registrationProperties.getUIPageIterator().setRendered(false);

      Map<QName, RegistrationPropertyDescription> regProps = configuration.getRegistrationRequirements().getRegistrationProperties();
      registrationProperties.getUIPageIterator().setPageList(createPageList(getPropertyList(regProps)));

      //add the popup for property edit and adding new properties
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, null);
      popup.setWindowSize(400, 300);
      UIWsrpProducerPropertyEditor propertyForm = createUIComponent(UIWsrpProducerPropertyEditor.class, null, "Producer Property Editor");
      popup.setUIComponent(propertyForm);
      popup.setRendered(false);

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
      List<RegistrationPropertyDescription> result = new ArrayList<RegistrationPropertyDescription>();
      for (Object o : descriptions.entrySet())
      {
         Map.Entry entry = (Map.Entry)o;
         RegistrationPropertyDescription rpd = (RegistrationPropertyDescription)entry.getValue();
         result.add(rpd);

      }


      //List<RegistrationPropertyDescription> result = new ArrayList<RegistrationPropertyDescription>(descriptions.values());
      //Collections.sort(result, descComparator);
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

   static public class EditActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event) throws Exception
      {
         UIWsrpProducerOverview source = event.getSource();
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

   static public class DeleteActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event)
      {
         UIWsrpProducerOverview source = event.getSource();
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

   static public class AddPropertyActionListener extends EventListener<UIWsrpProducerOverview>
   {
      public void execute(Event<UIWsrpProducerOverview> event) throws Exception
      {
         UIWsrpProducerOverview source = event.getSource();
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

   ProducerConfigurationService getService()
   {
      return configService;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
//         UITabPane uiTabPane = context.getUIApplication().findComponentById("UIWsrpConsoleTab");
//         uiTabPane.setSelectedTab(2);
      super.processRender(context);
   }


   public ProducerConfigurationService getProducerConfigurationService() throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      return (ProducerConfigurationService)manager.getComponentInstanceOfType(ProducerConfigurationService.class);
   }


}

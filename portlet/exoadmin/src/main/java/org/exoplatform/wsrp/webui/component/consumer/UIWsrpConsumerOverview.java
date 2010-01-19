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
package org.exoplatform.wsrp.webui.component.consumer;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import java.util.List;

/** @author Wesley Hales */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/groovy/wsrp/webui/component/UIWsrpConsumerOverview.gtmpl",
   events = {
      @EventConfig(listeners = UIWsrpConsumerOverview.CreateConsumerActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.EditActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.DeleteActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.RefreshActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.DeactivateActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.ActivateActionListener.class),
      @EventConfig(listeners = UIWsrpConsumerOverview.RefreshGridActionListener.class)
   })
public class UIWsrpConsumerOverview extends UIContainer
{
   public static String[] FIELDS = {"producerId", "active", "refreshNeeded"};

   //The action names are also your class names on the ui buttons. You should stick with common names like:
   //edit
   //delete
   //
   public static String[] SELECT_ACTIONS = {"Edit", "Delete", "Refresh", "Activate", "Deactivate"};

   private ConsumerController controller;
   private UIPopupWindow consumerEditorPopup;
   private UIPageIterator consumersIterator;

   public List getConfiguredConsumers() throws Exception
   {
      return controller.getConfiguredConsumers();
   }

   public LazyPageList createPageList(final List pageList)
   {
      return new LazyPageList<WSRPConsumer>(new ListAccess<WSRPConsumer>()
      {

         public int getSize() throws Exception
         {
            return pageList.size();
         }

         public WSRPConsumer[] load(int index, int length) throws Exception
         {
            WSRPConsumer[] pcs = new WSRPConsumer[pageList.size()];

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
               pcs[i] = (WSRPConsumer)pageList.get(i + index);
            }

            return pcs;
         }

      }, 10);
   }

   public UIWsrpConsumerOverview() throws Exception
   {
      // controller
      ConsumerRegistry registry = (ConsumerRegistry)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConsumerRegistry.class);
      controller = new ConsumerController(registry);

      //setSelectedTab(1);
      consumerEditorPopup = addChild(UIPopupWindow.class, null, null);
      consumerEditorPopup.setWindowSize(450, 0);
      UIWsrpConsumerEditor consumerForm = createUIComponent(UIWsrpConsumerEditor.class, null, "Consumer Editor");
      consumerEditorPopup.setUIComponent(consumerForm);
      consumerEditorPopup.setRendered(false);

      UIGrid consumers = addChild(UIGrid.class, null, null);
      //configure the edit and delete buttons based on an id from the data list - this will also be passed as param to listener
      consumers.configure("producerId", FIELDS, SELECT_ACTIONS);

      consumersIterator = consumers.getUIPageIterator();
      consumersIterator.setId("ChangeConsumerPageIterator");
      consumersIterator.setRendered(false);

      refreshConsumersList();
   }

   static public class RefreshGridActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event) throws Exception
      {
         UIWsrpConsumerOverview consumerOverview = event.getSource();
         consumerOverview.refreshGrid(event);
      }
   }

   public void refreshGrid(Event<UIWsrpConsumerOverview> event) throws Exception
   {
      //refresh the list
      refreshConsumersList();

      event.getRequestContext().addUIComponentToUpdateByAjax(this);
   }

   private void refreshConsumersList() throws Exception
   {
      LazyPageList pageList = createPageList(getConfiguredConsumers());
      consumersIterator.setPageList(pageList);
   }

   static public class CreateConsumerActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event) throws Exception
      {
         UIWsrpConsumerOverview consumerOverview = event.getSource();
         consumerOverview.displayConsumerEditor(null);
      }
   }

   private void displayConsumerEditor(WSRPConsumer consumer) throws Exception
   {
      UIWsrpConsumerEditor editor = (UIWsrpConsumerEditor)consumerEditorPopup.getUIComponent();

      //reset the form
      editor.reset();
      editor.setConsumer(consumer);
      consumerEditorPopup.setRendered(true);
      consumerEditorPopup.setShow(true);
      consumerEditorPopup.setShowCloseButton(true);
   }

   static public class EditActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event) throws Exception
      {
         UIWsrpConsumerOverview consumerOverview = event.getSource();
         WSRPConsumer consumer = consumerOverview.getConsumerFromEvent(event);

         if (consumer != null)
         {
            consumerOverview.displayConsumerEditor(consumer);
         }
      }
   }

   static public class DeleteActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event)
      {
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         try
         {
            UIWsrpConsumerOverview consumerOverview = event.getSource();
            WSRPConsumer consumer = consumerOverview.getConsumerFromEvent(event);
            ConsumerRegistry consumerRegistry = consumerOverview.getConsumerRegistry();
            String id = event.getRequestContext().getRequestParameter(OBJECTID);
            if (consumer != null)
            {
               consumerRegistry.destroyConsumer(id);
               uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.delete.success", null, ApplicationMessage.INFO));
               consumerOverview.refreshGrid(event);
            }
         }
         catch (Exception e)
         {
            uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.delete.fail", new String[]{e.getCause().toString()}, ApplicationMessage.ERROR));
         }

      }
   }

   static public class DeactivateActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event) throws Exception
      {
         UIWsrpConsumerOverview consumerOverview = event.getSource();
         WSRPConsumer consumer = consumerOverview.getConsumerFromEvent(event);
         UIApplication uiApp = event.getRequestContext().getUIApplication();

         if (consumer != null)
         {
            if (consumer.isActive())
            {
               ConsumerRegistry registry = consumerOverview.getConsumerRegistry();
               registry.deactivateConsumerWith(consumer.getProducerId());
               uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.deactivate.success", null));
               consumerOverview.refreshGrid(event);
            }
            else
            {
               uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.deactivate.fail", null));
            }
         }
      }
   }

   static public class ActivateActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event) throws Exception
      {
         UIWsrpConsumerOverview consumerOverview = event.getSource();
         WSRPConsumer consumer = consumerOverview.getConsumerFromEvent(event);
         UIApplication uiApp = event.getRequestContext().getUIApplication();

         if (consumer != null && !consumer.isActive())
         {
            try
            {
               ConsumerRegistry registry = consumerOverview.getConsumerRegistry();
               registry.activateConsumerWith(consumer.getProducerId());
               uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.activate.success", null));
               consumerOverview.refreshGrid(event);
            }
            catch (Exception e)
            {
               uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.activate.fail", new String[]{e.getCause().toString()}, ApplicationMessage.ERROR));
               e.printStackTrace();
            }
         }
      }
   }

   static public class RefreshActionListener extends EventListener<UIWsrpConsumerOverview>
   {
      public void execute(Event<UIWsrpConsumerOverview> event)
      {
         UIApplication uiApp = event.getRequestContext().getUIApplication();
         try
         {
            UIWsrpConsumerOverview consumerOverview = event.getSource();
            WSRPConsumer consumer = consumerOverview.getConsumerFromEvent(event);

            if (consumer != null)
            {
               RefreshResult result = consumerOverview.controller.refreshConsumer(consumer);

               if (result.hasIssues())
               {
                  consumerOverview.displayConsumerEditor(consumer);
               }
               else
               {
                  uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.refresh.success", null, ApplicationMessage.INFO));
               }

               // refresh consumers
               consumerOverview.refreshGrid(event);
            }
         }
         catch (Exception e)
         {
            uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.grid.action.refresh.fail", new String[]{e.getCause().toString()}, ApplicationMessage.ERROR));
            e.printStackTrace();
         }
      }
   }

   public WSRPConsumer getConsumerFromEvent(Event<?> event) throws Exception
   {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      return controller.getConsumer(id);
   }

   public ConsumerRegistry getConsumerRegistry() throws Exception
   {
      return controller.getRegistry();
   }
}

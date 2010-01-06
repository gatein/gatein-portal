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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.services.ManageableServiceFactory;

/** @author Wesley Hales */
@ComponentConfig(template = "app:/groovy/wsrp/webui/component/UIWsrpConsumerEditor.gtmpl", lifecycle = UIFormLifecycle.class, events = {
   @EventConfig(listeners = UIWsrpConsumerEditor.SaveActionListener.class)})
public class UIWsrpConsumerEditor extends UIForm
{
   protected static final String CONSUMER_NAME = "Consumer name: ";
   protected static final String CACHE_EXPIRATION = "Seconds before cache expiration: ";
   protected static final String TIMEOUT = "Milliseconds before timeout: ";
   protected static final String WSDL_URL = "WSDL URL: ";

   public UIWsrpConsumerEditor() throws Exception
   {

      addUIFormInput(new UIFormStringInput(CONSUMER_NAME, CONSUMER_NAME, null).addValidator(MandatoryValidator.class));
      addUIFormInput(new UIFormStringInput(CACHE_EXPIRATION, CACHE_EXPIRATION, null));
      addUIFormInput(new UIFormStringInput(TIMEOUT, TIMEOUT, null));
      addUIFormInput(new UIFormStringInput(WSDL_URL, WSDL_URL, null));
      //addChild(UIWsrpEndpointConfigForm.class,null,null);
   }

   private String getConsumerName()
   {
      return getUIStringInput(CONSUMER_NAME).getValue();
   }

   private Integer getCacheExpiration()
   {
      Integer cacheExp = 0;
      String cacheExpString = getUIStringInput(CACHE_EXPIRATION).getValue();
      if (cacheExpString != null)
      {
         cacheExp = Integer.parseInt(cacheExpString);
      }
      return cacheExp;
   }

   private Integer getTimeout()
   {
      int timeout = ManageableServiceFactory.DEFAULT_TIMEOUT_MS;
      String timeoutString = getUIStringInput(TIMEOUT).getValue();
      if (!ParameterValidation.isNullOrEmpty(timeoutString))
      {
         timeout = Integer.parseInt(timeoutString);
      }

      return timeout;
   }

   private String getWSDLURL()
   {
      return getUIStringInput(WSDL_URL).getValue();
   }

   private Boolean newConsumer;

   public Boolean isNewConsumer()
   {
      return newConsumer;
   }

   public void setNewConsumer(Boolean newConsumer)
   {
      this.newConsumer = newConsumer;
   }

   public void setConsumer(WSRPConsumer consumer) throws Exception
   {
      //UIWsrpEndpointConfigForm uiWsrpEndpointConfigForm = getChild(UIWsrpEndpointConfigForm.class);
      if (consumer == null)
      {
         getUIStringInput(CONSUMER_NAME).setEditable(UIFormStringInput.ENABLE);
         setNewConsumer(true);
         return;
      }
      getUIStringInput(CONSUMER_NAME).setEditable(UIFormStringInput.ENABLE);

      getUIStringInput(CONSUMER_NAME).setValue(consumer.getProducerId());
      ProducerInfo producerInfo = consumer.getProducerInfo();
      getUIStringInput(CACHE_EXPIRATION).setValue(producerInfo.getExpirationCacheSeconds().toString());
      getUIStringInput(TIMEOUT).setValue("" + producerInfo.getEndpointConfigurationInfo().getWSOperationTimeOut());
      getUIStringInput(WSDL_URL).setValue(producerInfo.getEndpointConfigurationInfo().getWsdlDefinitionURL());
      setNewConsumer(false);
      //invokeGetBindingBean(consumer.getProducerInfo());

      //uiWsrpEndpointConfigForm.setProducerInfo(consumer.getProducerInfo());
      //uiWsrpEndpointConfigForm.invokeGetBindingBean(consumer.getProducerInfo().getEndpointConfigurationInfo());
      //bindingFields(consumer);
   }

   private void bindingFields(WSRPConsumer consumer)
   {
      //ExoContainer manager = ExoContainerContext.getCurrentContainer();
      //ConsumerRegistry consumerRegistry = (ConsumerRegistry)manager.getComponentInstanceOfType(ConsumerRegistry.class);
      ProducerInfo producerInfo = consumer.getProducerInfo();
      producerInfo.setId(getConsumerName());
      producerInfo.setExpirationCacheSeconds(getCacheExpiration());
      //EndpointConfigurationInfo ecinfo = producerInfo.getEndpointConfigurationInfo().setWsdlDefinitionURL();
      //producerInfo.getEndpointConfigurationInfo().setWsdlDefinitionURL(getWsdlUrl());
   }

   static public class SaveActionListener extends EventListener<UIWsrpConsumerEditor>
   {
      public void execute(Event<UIWsrpConsumerEditor> event) throws Exception
      {
         UIWsrpConsumerEditor consumerEditor = event.getSource();

         UIWsrpConsumerOverview consumerOverview = consumerEditor.getAncestorOfType(UIWsrpConsumerOverview.class);

         WebuiRequestContext ctx = event.getRequestContext();
         if (consumerEditor.isNewConsumer())
         {
            consumerEditor.save(ctx);
         }
         else
         {
            consumerEditor.edit(ctx);
         }

         consumerEditor.reset();
         //loose the popup

         //update the consumer grid/list using ajax
         //event.getRequestContext().addUIComponentToUpdateByAjax(consumerEditor.getParent().getParent().findComponentById("ConsumerSelector"));

         //getChild(UIAccountInputSet.class).reset();
         UIPopupWindow popup = consumerEditor.getParent();
         popup.setRendered(false);
         popup.setShow(false);
         //create a new form, clears out the old, probably a better way
         //popup.setUIComponent(consumerOverview.createUIComponent(UIWsrpConsumerEditor.class, null, null));

         LazyPageList pageList = consumerOverview.createPageList(consumerOverview.getConfiguredConsumers());
         UIGrid uiGrid = consumerOverview.getChild(UIGrid.class);
         uiGrid.getUIPageIterator().setPageList(pageList);

         ctx.addUIComponentToUpdateByAjax(consumerOverview);

         //consumerOverview.renderUIComponent(consumerEditor.getParent().getParent().findComponentById("ConsumerSelector"));
         //ctx.sendRedirect(consumerOverview.url("wsrp"));
      }
   }

   public boolean save(WebuiRequestContext context) throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      ConsumerRegistry consumerRegistry = (ConsumerRegistry)manager.getComponentInstanceOfType(ConsumerRegistry.class);
      //WSRPConsumer consumer;

      UIApplication uiApp = context.getUIApplication();

      try
      {
         consumerRegistry.createConsumer(getConsumerName(), getCacheExpiration(), getWSDLURL());
         uiApp.addMessage(new ApplicationMessage("Consumer Successfully Added", null));
      }
      catch (ConsumerException ce)
      {
         //todo - add to resource bundle
         uiApp.addMessage(new ApplicationMessage("Consumer already exists!", null));
      }
      return true;
   }

   public boolean edit(WebuiRequestContext context) throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      ConsumerRegistry consumerRegistry = (ConsumerRegistry)manager.getComponentInstanceOfType(ConsumerRegistry.class);
      ProducerInfo producerInfo = consumerRegistry.getConsumer(getConsumerName()).getProducerInfo();

      //invokeSetBindingBean(consumer.getProducerInfo());
      //UIWsrpEndpointConfigForm uiWsrpEndpointConfigForm = getChild(UIWsrpEndpointConfigForm.class);
      //uiWsrpEndpointConfigForm.invokeSetBindingBean(consumer.getProducerInfo().getEndpointConfigurationInfo());

      producerInfo.setId(getConsumerName());
      producerInfo.setExpirationCacheSeconds(getCacheExpiration());
      producerInfo.getEndpointConfigurationInfo().setWsdlDefinitionURL(getWSDLURL());
      producerInfo.getEndpointConfigurationInfo().setWSOperationTimeOut(getTimeout());

      UIApplication uiApp = context.getUIApplication();

      try
      {
         consumerRegistry.updateProducerInfo(producerInfo);
         uiApp.addMessage(new ApplicationMessage("Edit Consumer Successful!", null));
      }
      catch (ConsumerException ce)
      {
         //todo - add to resource bundle
         uiApp.addMessage(new ApplicationMessage("Edit Consumer Problem!", null));
      }
      return true;
   }

}

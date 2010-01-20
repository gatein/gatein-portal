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
package org.exoplatform.wsrp.webui.component.consumer;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
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
import org.exoplatform.webui.form.UIFormInputBase;
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
   private UIFormInputBase<String> consumerName;
   private UIFormStringInput cache;
   private UIFormStringInput timeoutWS;
   private UIFormStringInput wsdl;

   public UIWsrpConsumerEditor() throws Exception
   {

      consumerName = new UIFormStringInput(CONSUMER_NAME, CONSUMER_NAME, null).addValidator(MandatoryValidator.class);
      addUIFormInput(consumerName);
      cache = new UIFormStringInput(CACHE_EXPIRATION, CACHE_EXPIRATION, null);
      addUIFormInput(cache);
      timeoutWS = new UIFormStringInput(TIMEOUT, TIMEOUT, null);
      addUIFormInput(timeoutWS);
      wsdl = new UIFormStringInput(WSDL_URL, WSDL_URL, null);
      addUIFormInput(wsdl);
   }

   private String getConsumerName()
   {
      return consumerName.getValue();
   }

   private Integer getCacheExpiration()
   {
      Integer cacheExp = 0;
      String cacheExpString = cache.getValue();
      if (cacheExpString != null)
      {
         cacheExp = Integer.parseInt(cacheExpString);
      }
      return cacheExp;
   }

   private Integer getTimeout()
   {
      int timeout = ManageableServiceFactory.DEFAULT_TIMEOUT_MS;
      String timeoutString = timeoutWS.getValue();
      if (!ParameterValidation.isNullOrEmpty(timeoutString))
      {
         timeout = Integer.parseInt(timeoutString);
      }

      return timeout;
   }

   private String getWSDLURL()
   {
      return wsdl.getValue();
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

   public void setConsumer(WSRPConsumer consumer)
   {
      if (consumer == null)
      {
         consumerName.setEditable(UIFormStringInput.ENABLE);
         setNewConsumer(true);
         return;
      }
      consumerName.setEditable(UIFormStringInput.ENABLE);

      consumerName.setValue(consumer.getProducerId());
      ProducerInfo producerInfo = consumer.getProducerInfo();
      cache.setValue(producerInfo.getExpirationCacheSeconds().toString());
      timeoutWS.setValue("" + producerInfo.getEndpointConfigurationInfo().getWSOperationTimeOut());
      wsdl.setValue(producerInfo.getEndpointConfigurationInfo().getWsdlDefinitionURL());
      setNewConsumer(false);
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

         UIPopupWindow popup = consumerEditor.getParent();
         popup.setRendered(false);
         popup.setShow(false);

         //temp way to refresh list, should call broadcast event (below)
         LazyPageList pageList = new LazyPageList<WSRPConsumer>(new ListAccessImpl<WSRPConsumer>(WSRPConsumer.class, consumerOverview.getConfiguredConsumers()), 10);
         UIGrid uiGrid = consumerOverview.getChild(UIGrid.class);
         uiGrid.getUIPageIterator().setPageList(pageList);
         //uiGrid.configure()
         //try to broadcast an event back to consumerOverview to refresh grid... works but shows error
         //PortalRequestContext portalContext = org.exoplatform.portal.webui.util.Util.getPortalRequestContext();
         //Event<UIWsrpConsumerOverview> pnevent = new Event<UIWsrpConsumerOverview>(consumerOverview, "RefreshGrid", portalContext);
         //consumerOverview.broadcast(pnevent, Event.Phase.PROCESS);

         ctx.addUIComponentToUpdateByAjax(consumerOverview);
      }
   }

   public boolean save(WebuiRequestContext context) throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      ConsumerRegistry consumerRegistry = (ConsumerRegistry)manager.getComponentInstanceOfType(ConsumerRegistry.class);

      UIApplication uiApp = context.getUIApplication();

      try
      {
         consumerRegistry.createConsumer(getConsumerName(), getCacheExpiration(), getWSDLURL());
         uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.action.add.success", null));
      }
      catch (ConsumerException ce)
      {
         //todo - add to resource bundle
         uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.action.add.exists", null, ApplicationMessage.ERROR));
      }
      return true;
   }

   public boolean edit(WebuiRequestContext context) throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      ConsumerRegistry consumerRegistry = (ConsumerRegistry)manager.getComponentInstanceOfType(ConsumerRegistry.class);
      ProducerInfo producerInfo = consumerRegistry.getConsumer(getConsumerName()).getProducerInfo();

      producerInfo.setId(getConsumerName());
      producerInfo.setExpirationCacheSeconds(getCacheExpiration());
      producerInfo.getEndpointConfigurationInfo().setWsdlDefinitionURL(getWSDLURL());
      producerInfo.getEndpointConfigurationInfo().setWSOperationTimeOut(getTimeout());

      UIApplication uiApp = context.getUIApplication();

      try
      {
         consumerRegistry.updateProducerInfo(producerInfo);
         uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.action.edit.success", null));
      }
      catch (ConsumerException ce)
      {
         uiApp.addMessage(new ApplicationMessage("UIWsrp.consumer.action.edit.fail", null, ApplicationMessage.ERROR));
      }
      return true;
   }

}

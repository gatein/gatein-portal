/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.gatein.portal.wsrp;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.pc.portlet.container.ContainerPortletInvoker;
import org.gatein.pc.portlet.impl.state.StateConverterV0;
import org.gatein.pc.portlet.impl.state.StateManagementPolicyService;
import org.gatein.pc.portlet.impl.state.producer.PortletStatePersistenceManagerService;
import org.gatein.pc.portlet.state.StateConverter;
import org.gatein.pc.portlet.state.producer.ProducerPortletInvoker;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.RegistrationPersistenceManager;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.api.SessionEvent;
import org.gatein.wsrp.api.SessionEventBroadcaster;
import org.gatein.wsrp.api.SessionEventListener;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.consumer.registry.xml.XMLConsumerRegistry;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.impl.xml.SimpleXMLProducerConfigurationService;
import org.picocontainer.Startable;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ExoKernelIntegration implements Startable
{
   private static final String DEFAULT_PRODUCER_CONFIG_LOCATION = "conf/wsrp-producer-config.xml";

   private static final String CLASSPATH = "classpath:/";

   private final InputStream configurationIS;

   private String configLocation;

   private WSRPProducer producer;

   private ConsumerRegistry consumerRegistry;

   public ExoKernelIntegration(InitParams params, ConfigurationManager configurationManager,
                               org.exoplatform.portal.pc.ExoKernelIntegration pc) throws Exception
   {
      if (params != null)
      {
         configLocation = params.getValueParam("configLocation").getValue();
      }

      if (configLocation == null)
      {
         configLocation = DEFAULT_PRODUCER_CONFIG_LOCATION;
      }

      configurationIS = configurationManager.getInputStream(CLASSPATH + configLocation);
   }

   public void start()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      startProducer(container);
      startConsumers(container);
   }

   private void startProducer(ExoContainer container)
   {

      ProducerConfigurationService producerConfigurationService = new SimpleXMLProducerConfigurationService();
      try
      {
         producerConfigurationService.loadConfigurationFrom(configurationIS);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't load WSRP producer configuration from " + configLocation, e);
      }

      RegistrationPersistenceManager registrationPersistenceManager = new RegistrationPersistenceManagerImpl();
      RegistrationManager registrationManager = new RegistrationManagerImpl();
      registrationManager.setPersistenceManager(registrationPersistenceManager);

      // todo: the multiple instantiation of WSRP service causes the registration policy to not be properly initialized
      // so we end up forcing its instantiation here.
      DefaultRegistrationPolicy registrationPolicy = new DefaultRegistrationPolicy();
      registrationPolicy.setValidator(new DefaultRegistrationPropertyValidator());
      registrationManager.setPolicy(registrationPolicy);

      // retrieve container portlet invoker from eXo kernel
      ContainerPortletInvoker containerPortletInvoker =
         (ContainerPortletInvoker)container.getComponentInstanceOfType(ContainerPortletInvoker.class);

      // The producer persistence manager
      PortletStatePersistenceManagerService producerPersistenceManager = new PortletStatePersistenceManagerService();

      // The producer state management policy
      StateManagementPolicyService producerStateManagementPolicy = new StateManagementPolicyService();
      producerStateManagementPolicy.setPersistLocally(true);

      // The producer state converter
      StateConverter producerStateConverter = new StateConverterV0();

      // The producer portlet invoker
      ProducerPortletInvoker producerPortletInvoker = new ProducerPortletInvoker();
      producerPortletInvoker.setNext(containerPortletInvoker);
      producerPortletInvoker.setPersistenceManager(producerPersistenceManager);
      producerPortletInvoker.setStateManagementPolicy(producerStateManagementPolicy);
      producerPortletInvoker.setStateConverter(producerStateConverter);

      // create and wire WSRP producer
      producer = ProducerHolder.getProducer(true);
      producer.setPortletInvoker(producerPortletInvoker);
      producer.setRegistrationManager(registrationManager);
      producer.setConfigurationService(producerConfigurationService);

      producer.start();
   }

   private void startConsumers(ExoContainer container)
   {
      // retrieve federating portlet invoker from container
      FederatingPortletInvoker federatingPortletInvoker =
         (FederatingPortletInvoker)container.getComponentInstanceOfType(PortletInvoker.class);

      consumerRegistry = new XMLConsumerRegistry();
      consumerRegistry.setFederatingPortletInvoker(federatingPortletInvoker);
      consumerRegistry.setSessionEventBroadcaster(new SimpleSessionEventBroadcaster());

      try
      {
         consumerRegistry.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't start WSRP consumers registry.", e);
      }
   }

   public void stop()
   {
      stopProducer();
      stopConsumers();
   }

   private void stopProducer()
   {
      producer.stop();

      producer = null;
   }

   private void stopConsumers()
   {
      try
      {
         consumerRegistry.stop();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't stop WSRP consumers registry.");
      }

      consumerRegistry = null;
   }

   private static class SimpleSessionEventBroadcaster implements SessionEventBroadcaster
   {
      private Map<String, SessionEventListener> listeners = new ConcurrentHashMap<String, SessionEventListener>();

      public void registerListener(String listenerId, SessionEventListener listener)
      {
         listeners.put(listenerId, listener);
      }

      public void unregisterListener(String listenerId)
      {
         listeners.remove(listenerId);
      }

      public void notifyListenersOf(SessionEvent event)
      {
         for (SessionEventListener listener : listeners.values())
         {
            listener.onSessionEvent(event);
         }
      }

   }
}

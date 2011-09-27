/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.integration.wsrp;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.pc.ExoKernelIntegration;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.integration.wsrp.jcr.JCRPersister;
import org.gatein.integration.wsrp.structure.MOPConsumerStructureProvider;
import org.gatein.integration.wsrp.structure.MOPPortalStructureAccess;
import org.gatein.integration.wsrp.structure.PortalStructureAccess;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.pc.federation.PortletInvokerResolver;
import org.gatein.pc.portlet.PortletInvokerInterceptor;
import org.gatein.pc.portlet.aspects.EventPayloadInterceptor;
import org.gatein.pc.portlet.container.ContainerPortletInvoker;
import org.gatein.pc.portlet.impl.state.StateConverterV0;
import org.gatein.pc.portlet.impl.state.StateManagementPolicyService;
import org.gatein.pc.portlet.state.StateConverter;
import org.gatein.pc.portlet.state.producer.PortletStatePersistenceManager;
import org.gatein.pc.portlet.state.producer.ProducerPortletInvoker;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.RegistrationPersistenceManager;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.consumer.migration.JCRMigrationService;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.consumer.registry.JCRConsumerRegistry;
import org.gatein.wsrp.consumer.registry.RegisteringPortletInvokerResolver;
import org.gatein.wsrp.payload.WSRPEventPayloadInterceptor;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.config.JCRProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.invoker.RegistrationCheckingPortletInvoker;
import org.gatein.wsrp.producer.state.JCRPortletStatePersistenceManager;
import org.gatein.wsrp.registration.JCRRegistrationPersistenceManager;
import org.picocontainer.Startable;

import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPServiceIntegration implements Startable, WebAppListener
{
   private static final Logger log = LoggerFactory.getLogger(WSRPServiceIntegration.class);

   private static final String CLASSPATH = "classpath:/";
   private static final String PRODUCER_CONFIG_LOCATION = "producerConfigLocation";
   private static final String CONSUMERS_CONFIG_LOCATION = "consumersConfigLocation";

   private final InputStream producerConfigurationIS;
   private final String producerConfigLocation;
   private WSRPProducer producer;

   private final InputStream consumersConfigurationIS;
   private final String consumersConfigLocation;
   private JCRConsumerRegistry consumerRegistry;
   private ExoContainer container;
   private final ExoKernelIntegration exoKernelIntegration;
   private final boolean bypass;
   private static final String WSRP_ADMIN_GUI_CONTEXT_PATH = "/wsrp-admin-gui";

   public WSRPServiceIntegration(ExoContainerContext context, InitParams params, ConfigurationManager configurationManager,
                                 ExoKernelIntegration pc, NodeHierarchyCreator nhc) throws Exception
   {
      // IMPORTANT: even though NodeHierarchyCreator is not used anywhere in the code, it's still needed for pico
      // to properly make sure that this service is started after the PC one. Yes, Pico is crap. :/

      // todo: we currently only allow the service to go through initialization if we are running in the default portal
      // as this service is not meant to work with extensions yet...
      if ("portal".equals(context.getName()))
      {
         if (params != null)
         {
            producerConfigLocation = params.getValueParam(PRODUCER_CONFIG_LOCATION).getValue();
            consumersConfigLocation = params.getValueParam(CONSUMERS_CONFIG_LOCATION).getValue();
         }
         else
         {
            throw new IllegalArgumentException("Improperly configured service: missing values for "
               + PRODUCER_CONFIG_LOCATION + "and " + CONSUMERS_CONFIG_LOCATION);
         }

         producerConfigurationIS = configurationManager.getInputStream(CLASSPATH + producerConfigLocation);
         consumersConfigurationIS = configurationManager.getInputStream(CLASSPATH + consumersConfigLocation);

         container = context.getContainer();

         exoKernelIntegration = pc;

         bypass = false;
      }
      else
      {
         log.info("The WSRP service can only be started in the default portal context. WSRP was not started for '"
            + context.getName() + "'");

         producerConfigLocation = null;
         consumersConfigLocation = null;
         producerConfigurationIS = null;
         consumersConfigurationIS = null;
         exoKernelIntegration = null;
         bypass = true;
      }
   }

   public void start()
   {
      if (!bypass)
      {
         try
         {
            startProducer();
            startConsumers();

            // listen for web app events so that we can inject services into WSRP admin UI "cleanly"
            // todo: this service injection should really be done using CDI... :/
            ServletContainerFactory factory = DefaultServletContainerFactory.getInstance();
            ServletContainer servletContainer = factory.getServletContainer();
            servletContainer.addWebAppListener(this);

            log.info("WSRP Service version '" + WSRPConstants.WSRP_SERVICE_VERSION + "' STARTED");
         }
         catch (Exception e)
         {
            log.error("WSRP Service version '" + WSRPConstants.WSRP_SERVICE_VERSION + "' FAILED to start", e);
         }
      }
   }

   private void startProducer()
   {

      JCRProducerConfigurationService producerConfigurationService;
      try
      {
         JCRPersister persister = new JCRPersister(container, JCRPersister.WSRP_WORKSPACE_NAME);
         persister.initializeBuilderFor(JCRProducerConfigurationService.mappingClasses);

         producerConfigurationService = new JCRProducerConfigurationService(persister);
         producerConfigurationService.setConfigurationIS(producerConfigurationIS);
         producerConfigurationService.reloadConfiguration();
      }
      catch (Exception e)
      {
         log.debug("Couldn't load WSRP producer configuration from " + producerConfigLocation, e);
         throw new RuntimeException("Couldn't load WSRP producer configuration from " + producerConfigLocation, e);
      }
      container.registerComponentInstance(ProducerConfigurationService.class, producerConfigurationService);

      RegistrationPersistenceManager registrationPersistenceManager;
      try
      {
         JCRPersister persister = new JCRPersister(container, JCRPersister.WSRP_WORKSPACE_NAME);
         persister.initializeBuilderFor(JCRRegistrationPersistenceManager.mappingClasses);

         registrationPersistenceManager = new JCRRegistrationPersistenceManager(persister);
      }
      catch (Exception e)
      {
         log.debug("Couldn't instantiate RegistrationPersistenceManager", e);
         throw new RuntimeException("Couldn't instantiate RegistrationPersistenceManager", e);
      }
      RegistrationManager registrationManager = new RegistrationManagerImpl();
      registrationManager.setPersistenceManager(registrationPersistenceManager);

      // retrieve container portlet invoker from eXo kernel
      ContainerPortletInvoker containerPortletInvoker =
         (ContainerPortletInvoker)container.getComponentInstanceOfType(ContainerPortletInvoker.class);

      // iterate over the container stack so that we can insert the WSRP-specific event payload interceptor
      PortletInvokerInterceptor previous = containerPortletInvoker;
      PortletInvokerInterceptor next = previous;
      do
      {
         PortletInvoker invoker = previous.getNext();
         if (invoker instanceof EventPayloadInterceptor)
         {
            // create a new WSRPEventPayloadInterceptor and make its next one the current event payload invoker
            WSRPEventPayloadInterceptor eventPayloadInterceptor = new WSRPEventPayloadInterceptor();
            eventPayloadInterceptor.setNext(invoker);

            // replace the current event payload interceptor by the WSRP-specific one
            previous.setNext(eventPayloadInterceptor);

            // we're done
            break;
         }
         else
         {
            previous = next;
            if (invoker instanceof PortletInvokerInterceptor)
            {
               next = (PortletInvokerInterceptor)invoker;
            }
            else
            {
               next = null;
            }
         }
      }
      while (next != null);

      // The producer persistence manager
      PortletStatePersistenceManager producerPersistenceManager;
      try
      {
         JCRPersister persister = new JCRPersister(container, JCRPersister.PORTLET_STATES_WORKSPACE_NAME);
         persister.initializeBuilderFor(JCRPortletStatePersistenceManager.mappingClasses);

         producerPersistenceManager = new JCRPortletStatePersistenceManager(persister);
      }
      catch (Exception e)
      {
         log.debug("Couldn't instantiate PortletStatePersistenceManager", e);
         throw new RuntimeException("Couldn't instantiate PortletStatePersistenceManager", e);
      }

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

      RegistrationCheckingPortletInvoker wsrpPortletInvoker = new RegistrationCheckingPortletInvoker();
      wsrpPortletInvoker.setNext(producerPortletInvoker);
      wsrpPortletInvoker.setRegistrationManager(registrationManager);


      // create and wire WSRP producer
      producer = ProducerHolder.getProducer(true);
      producer.setPortletInvoker(wsrpPortletInvoker);
      producer.setRegistrationManager(registrationManager);
      producer.setConfigurationService(producerConfigurationService);
      exoKernelIntegration.getPortletApplicationRegistry().addListener(producer);

      producer.start();

      log.info("WSRP Producer started");
   }

   private void startConsumers()
   {
      // retrieve federating portlet invoker from container
      FederatingPortletInvoker federatingPortletInvoker =
         (FederatingPortletInvoker)container.getComponentInstanceOfType(FederatingPortletInvoker.class);

      // add our Session event listener to the ListenerService for use in org.exoplatform.web.GenericHttpListener
      ListenerService listenerService = (ListenerService)container.getComponentInstanceOfType(ListenerService.class);
      SessionEventListenerAndBroadcaster sessionEventBroadcaster = new SessionEventListenerAndBroadcaster();

      // events from org.exoplatform.web.GenericHttpListener
      listenerService.addListener("org.exoplatform.web.GenericHttpListener.sessionCreated", sessionEventBroadcaster);
      listenerService.addListener("org.exoplatform.web.GenericHttpListener.sessionDestroyed", sessionEventBroadcaster);

      try
      {
         JCRPersister persister = new JCRPersister(container, JCRPersister.WSRP_WORKSPACE_NAME);
         persister.initializeBuilderFor(JCRConsumerRegistry.mappingClasses);

         consumerRegistry = new JCRConsumerRegistry(persister);
         consumerRegistry.setFederatingPortletInvoker(federatingPortletInvoker);
         consumerRegistry.setSessionEventBroadcaster(sessionEventBroadcaster);
         consumerRegistry.setConfigurationIS(consumersConfigurationIS);

         // if we run in a cluster, use a distributed cache for consumers
         /*if (ExoContainer.getProfiles().contains("cluster"))
         {
            CacheService cacheService = (CacheService)container.getComponentInstanceOfType(CacheService.class);
            DistributedConsumerCache consumerCache = new DistributedConsumerCache(cacheService);
            consumerRegistry.setConsumerCache(consumerCache);
         }*/

         // create ConsumerStructureProvider and register it to listen to page events
         POMSessionManager sessionManager = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
         PortalStructureAccess structureAccess = new MOPPortalStructureAccess(sessionManager);
         MOPConsumerStructureProvider structureprovider = new MOPConsumerStructureProvider(structureAccess);
         listenerService.addListener(DataStorage.PAGE_CREATED, structureprovider);
         listenerService.addListener(DataStorage.PAGE_REMOVED, structureprovider);
         listenerService.addListener(DataStorage.PAGE_UPDATED, structureprovider);

         // migration service
         persister = new JCRPersister(container, JCRPersister.WSRP_WORKSPACE_NAME);
         persister.initializeBuilderFor(JCRMigrationService.mappingClasses);

         MigrationService migrationService = new JCRMigrationService(persister);
         migrationService.setStructureProvider(structureprovider);
         consumerRegistry.setMigrationService(migrationService);

         consumerRegistry.start();

         // set up a PortletInvokerResolver so that when a remote producer is queried, we can start it if needed
         RegisteringPortletInvokerResolver resolver = new RegisteringPortletInvokerResolver();
         resolver.setConsumerRegistry(consumerRegistry);
         federatingPortletInvoker.setPortletInvokerResolver(resolver);
      }
      catch (Exception e)
      {
         log.debug(e);
         throw new RuntimeException("Couldn't start WSRP consumers registry from configuration " + consumersConfigLocation, e);
      }
      container.registerComponentInstance(ConsumerRegistry.class, consumerRegistry);

      log.info("WSRP Consumers started");
   }

   public void stop()
   {
      if (!bypass)
      {
         // stop listening to web app events
         ServletContainerFactory factory = DefaultServletContainerFactory.getInstance();
         ServletContainer servletContainer = factory.getServletContainer();
         servletContainer.removeWebAppListener(this);

         stopProducer();
         stopConsumers();
      }
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
         log.debug(e);
         throw new RuntimeException("Couldn't stop WSRP consumers registry.", e);
      }

      consumerRegistry = null;
   }

   public void onEvent(WebAppEvent event)
   {
      if (event instanceof WebAppLifeCycleEvent)
      {
         WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent)event;
         WebApp webApp = event.getWebApp();
         ServletContext context = webApp.getServletContext();

         // if we see the WSRP admin GUI being deployed or undeployed, inject or remove services 
         if (WSRP_ADMIN_GUI_CONTEXT_PATH.equals(webApp.getContextPath()))
         {
            switch (lifeCycleEvent.getType())
            {
               case WebAppLifeCycleEvent.ADDED:
                  context.setAttribute("ConsumerRegistry", consumerRegistry);
                  context.setAttribute("ProducerConfigurationService", producer.getConfigurationService());
                  break;
               case WebAppLifeCycleEvent.REMOVED:
                  context.removeAttribute("ConsumerRegistry");
                  context.removeAttribute("ProducerConfigurationService");
                  break;
            }
         }
      }
   }

}

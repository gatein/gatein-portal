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

package org.exoplatform.services.organization.idm;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.naming.InitialContextInitializer;

import org.gatein.common.classloader.DelegatingClassLoader;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.infinispan.transaction.lookup.JBossTransactionManagerLookup;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.jgroups.conf.XmlConfigurator;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.cfg.IdentityConfiguration;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.impl.cache.InfinispanAPICacheProviderImpl;
import org.picketlink.idm.impl.cache.InfinispanIdentityStoreCacheProviderImpl;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.picketlink.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
import org.picketlink.idm.spi.configuration.metadata.IdentityConfigurationMetaData;
import org.picocontainer.Startable;

import java.io.InputStream;
import java.net.URL;

import javax.naming.InitialContext;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class PicketLinkIDMServiceImpl implements PicketLinkIDMService, Startable
{

   private static Log log = ExoLogger.getLogger(PicketLinkIDMServiceImpl.class);

   public static final String PARAM_CONFIG_OPTION = "config";

   public static final String PARAM_JNDI_NAME_OPTION = "jndiName";

   public static final String REALM_NAME_OPTION = "portalRealm";

   public static final String CACHE_CONFIG_API_OPTION = "apiCacheConfig";

   public static final String CACHE_CONFIG_STORE_OPTION = "storeCacheConfig";

   private IdentitySessionFactory identitySessionFactory;

   private String config;

   private String realmName = "idm_realm";

   private IdentityConfiguration identityConfiguration;

   private IntegrationCache integrationCache;

   private HibernateService hibernateService;

   // Infinispan cache manager is shared between all portal containers, TODO: Move to separate service?
   private static EmbeddedCacheManager cacheManager;

   private PicketLinkIDMServiceImpl()
   {
   }

   public PicketLinkIDMServiceImpl(
      ExoContainerContext exoContainerContext,
      InitParams initParams,
      HibernateService hibernateService,
      ConfigurationManager confManager,
      PicketLinkIDMCacheService picketLinkIDMCache,
      InitialContextInitializer dependency) throws Exception
   {
      ValueParam config = initParams.getValueParam(PARAM_CONFIG_OPTION);
      ValueParam jndiName = initParams.getValueParam(PARAM_JNDI_NAME_OPTION);
      ValueParam realmName = initParams.getValueParam(REALM_NAME_OPTION);
      ValueParam apiCacheConfig = initParams.getValueParam(CACHE_CONFIG_API_OPTION);
      ValueParam storeCacheConfig = initParams.getValueParam(CACHE_CONFIG_STORE_OPTION);

      this.hibernateService = hibernateService;

      if (config == null && jndiName == null)
      {
         throw new IllegalStateException("Either '" + PARAM_CONFIG_OPTION + "' or '" + PARAM_JNDI_NAME_OPTION
            + "' parameter must " + "be specified");
      }
      if (realmName != null)
      {
         this.realmName = realmName.getValue();
      }

      if (config != null)
      {
         this.config = config.getValue();
         URL configURL = confManager.getURL(this.config);

         if (configURL == null)
         {
            throw new IllegalStateException("Cannot fine resource: " + this.config);
         }



         IdentityConfigurationMetaData configMD =
            JAXB2IdentityConfiguration.createConfigurationMetaData(confManager.getInputStream(this.config));

         identityConfiguration = new IdentityConfigurationImpl().configure(configMD);

         identityConfiguration.getIdentityConfigurationRegistry().register(hibernateService.getSessionFactory(), "hibernateSessionFactory");

         if (apiCacheConfig != null)
         {

            InputStream configStream = confManager.getInputStream(apiCacheConfig.getValue());

            if (configStream == null)
            {
               throw new IllegalArgumentException("Infinispan configuration InputStream is null");
            }

            Cache cache = initInfinispanCache(configStream, exoContainerContext.getPortalContainerName(), "api");

            configStream.close();

            // PLIDM API cache
            InfinispanAPICacheProviderImpl apiCacheProvider = new InfinispanAPICacheProviderImpl();
            apiCacheProvider.initialize(cache);
            picketLinkIDMCache.register(apiCacheProvider);
            identityConfiguration.getIdentityConfigurationRegistry().register(apiCacheProvider, "apiCacheProvider");

            //Integration cache
            integrationCache = new IntegrationCache();
            integrationCache.initialize(cache);
            picketLinkIDMCache.register(integrationCache);

         }


         if (storeCacheConfig != null)
         {
            InputStream configStream = confManager.getInputStream(storeCacheConfig.getValue());

            if (configStream == null)
            {
               throw new IllegalArgumentException("Infinispan configuration InputStream is null");
            }

            Cache cache = initInfinispanCache(configStream, exoContainerContext.getPortalContainerName(), "store");

            configStream.close();

            InfinispanIdentityStoreCacheProviderImpl storeCacheProvider = new InfinispanIdentityStoreCacheProviderImpl();
            storeCacheProvider.initialize(cache);
            picketLinkIDMCache.register(storeCacheProvider);
            identityConfiguration.getIdentityConfigurationRegistry().register(storeCacheProvider, "storeCacheProvider");
         }
      }
      else
      {
         identitySessionFactory = (IdentitySessionFactory)new InitialContext().lookup(jndiName.getValue());
      }

   }

   public void start()
   {
      if (identitySessionFactory == null)
      {
         try
         {
            identitySessionFactory = identityConfiguration.buildIdentitySessionFactory();
         }
         catch (IdentityConfigurationException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void stop()
   {
   }
                                    
   public IdentitySessionFactory getIdentitySessionFactory()
   {
      return identitySessionFactory; //To change body of implemented methods use File | Settings | File Templates.
   }

   public IdentitySession getIdentitySession() throws Exception
   {
      return getIdentitySessionFactory().getCurrentIdentitySession(realmName);
   }

   public IdentitySession getIdentitySession(String realm) throws Exception
   {
      if (realm == null)
      {
         throw new IllegalArgumentException("Realm name cannot be null");
      }
      return getIdentitySessionFactory().getCurrentIdentitySession(realm);
   }

   public IntegrationCache getIntegrationCache()
   {
      return integrationCache;
   }

   public String getRealmName()
   {
      return realmName;
   }

   public HibernateService getHibernateService()
   {
      return hibernateService;
   }

   /**
    * Create, configure and start infinispan cache
    *
    * @param configStream input stream with infinispan configuration. Some things from this stream will be changed
    *                     programmatically before cache creation
    * @param portalContainerName name of portal container
    * @param apiOrStore Value can be either "api" if cache will be for apiCacheProvider or "store" for storeCacheProvider
    * @return created and started infinispan cache
    * @throws Exception
    */
   private Cache initInfinispanCache(InputStream configStream, String portalContainerName, String apiOrStore) throws Exception
   {
      ClassLoader infinispanCl = EmbeddedCacheManager.class.getClassLoader();
      ClassLoader portalCl = Thread.currentThread().getContextClassLoader();

      // Infinispan classloader is first delegate, so in AS7 environment, infinispan is able to see
      // jgroups3 classes with bigger priority than jgroups2 classes
      ClassLoader delegating = new DelegatingClassLoader(infinispanCl, portalCl);

      try
      {
         // Set delegating classloader as tccl
         Thread.currentThread().setContextClassLoader(delegating);

         String cacheName = "idm-" + portalContainerName + "-" + apiOrStore;
         EmbeddedCacheManager cacheManager = getSharedCacheManager(configStream);
         return cacheManager.getCache(cacheName);
      }
      finally
      {
         // Put portal classloader to be tccl again
         Thread.currentThread().setContextClassLoader(portalCl);
      }
   }

   /**
    * Create and configure cacheManager, which will be used to create infinispan caches.
    *
    * @param configStream stream with infinispan configuration
    * @return cacheManager
    * @throws Exception
    */
   private static EmbeddedCacheManager getSharedCacheManager(InputStream configStream) throws Exception
   {
      if (cacheManager == null)
      {
         EmbeddedCacheManager cacheManager = new DefaultCacheManager(configStream, false);
         GlobalConfiguration globalConfigFromXml = cacheManager.getCacheManagerConfiguration();

         ConfigurationBuilderHolder builderHolder = new ConfigurationBuilderHolder();
         builderHolder.getGlobalConfigurationBuilder().read(globalConfigFromXml);

         Configuration configFromXml = cacheManager.getDefaultCacheConfiguration();
         ConfigurationBuilder configBuilder = builderHolder.getDefaultConfigurationBuilder().read(configFromXml);

         // Configure transactionManagerLookup programmatically if not provided in configuration
         TransactionManagerLookup tmLookup = configFromXml.transaction().transactionManagerLookup();
         if (tmLookup == null)
         {
            tmLookup = getTransactionManagerLookup();
            configBuilder.transaction().transactionManagerLookup(tmLookup);
         }
         log.debug("Infinispan transaction manager lookup: " + tmLookup);

         cacheManager = new DefaultCacheManager(builderHolder, true);
         PicketLinkIDMServiceImpl.cacheManager = cacheManager;
      }

      return cacheManager;
   }

   /**
    * @return JBossTransactionManagerLookup if we are in AS7 or JBossStandaloneJTAManagerLookup otherwise
    */
   private static TransactionManagerLookup getTransactionManagerLookup()
   {
      if (new J2EEServerInfo().isJBoss())
      {
         return new JBossTransactionManagerLookup();
      }
      else
      {
         return new JBossStandaloneJTAManagerLookup();
      }
   }
}

/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.services.organization.idm;

import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.gatein.common.classloader.DelegatingClassLoader;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.infinispan.transaction.lookup.JBossTransactionManagerLookup;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.picketlink.idm.cache.APICacheProvider;
import org.picketlink.idm.impl.cache.InfinispanAPICacheProviderImpl;
import org.picketlink.idm.impl.cache.InfinispanIdentityStoreCacheProviderImpl;
import org.picketlink.idm.spi.cache.IdentityStoreCacheProvider;

import java.io.InputStream;

/**
 * Singleton for creating infinispan caches and PLIDM cache providers. It's singleton actually as cacheManager is shared
 * between all portal containers.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class InfinispanCacheFactory
{
   private static final InfinispanCacheFactory INSTANCE = new InfinispanCacheFactory();
   private static final Logger log = LoggerFactory.getLogger(InfinispanCacheFactory.class);

   private InfinispanCacheFactory()
   {
   }

   public static final InfinispanCacheFactory getInstance()
   {
      return INSTANCE;
   }

   // Infinispan cache manager is shared between all portal containers,
   private EmbeddedCacheManager cacheManager;

   // Indicate whether cache entries for Structure can be expired or not
   private boolean skipExpirationOfStructureCacheEntries = false;

   // Used only if skipExpirationOfStructureCacheEntries is false. It's obtained from infinispan configuration (not from service parameter)
   private long cacheLifespanOfLeafNodes = -1;


   public void setSkipExpirationOfStructureCacheEntries(boolean skipExpirationOfStructureCacheEntries)
   {
      if (cacheManager != null && (skipExpirationOfStructureCacheEntries != this.skipExpirationOfStructureCacheEntries))
      {
         log.warn("CacheManager is already initialized. Setting of skipExpirationOfStructureCacheEntries won't have effect");
      }
      else
      {
         this.skipExpirationOfStructureCacheEntries = skipExpirationOfStructureCacheEntries;
      }
   }

   APICacheProvider createAPICacheProvider(long staleCacheNodesLinksCleanerDelay, Cache infinispanCache)
   {
      InfinispanAPICacheProviderImpl apiCacheProvider = new InfinispanAPICacheProviderImpl();
      apiCacheProvider.initialize(infinispanCache, skipExpirationOfStructureCacheEntries, cacheLifespanOfLeafNodes, staleCacheNodesLinksCleanerDelay);
      return apiCacheProvider;
   }

   IntegrationCache createIntegrationCache(Cache infinispanCache)
   {
      IntegrationCache integrationCache = new IntegrationCache();

      // We will use -1 for staleCacheNodesLinksCleanerDelay, as integrationCache is using same cache as apiCacheProvider,
      // so we don't need to start another cleaner thread
      integrationCache.initialize(infinispanCache, skipExpirationOfStructureCacheEntries, cacheLifespanOfLeafNodes, -1);
      return integrationCache;
   }

   IdentityStoreCacheProvider createStoreCacheProvider(long staleCacheNodesLinksCleanerDelay, Cache infinispanCache)
   {
      InfinispanIdentityStoreCacheProviderImpl storeCacheProvider = new InfinispanIdentityStoreCacheProviderImpl();
      storeCacheProvider.initialize(infinispanCache, skipExpirationOfStructureCacheEntries, cacheLifespanOfLeafNodes, staleCacheNodesLinksCleanerDelay);
      return storeCacheProvider;
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
   Cache createInfinispanCache(InputStream configStream, String portalContainerName, String apiOrStore) throws Exception
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
   private EmbeddedCacheManager getSharedCacheManager(InputStream configStream) throws Exception
   {
      if (cacheManager == null)
      {
         EmbeddedCacheManager cacheManager = new DefaultCacheManager(configStream, false);
         GlobalConfiguration globalConfigFromXml = cacheManager.getCacheManagerConfiguration();

         GlobalConfigurationBuilder globalConfigBuilder = new GlobalConfigurationBuilder();
         globalConfigBuilder.read(globalConfigFromXml);

         Configuration configFromXml = cacheManager.getDefaultCacheConfiguration();
         ConfigurationBuilder configBuilder = new ConfigurationBuilder().read(configFromXml);

         // Configure transactionManagerLookup programmatically if not provided in configuration
         TransactionManagerLookup tmLookup = configFromXml.transaction().transactionManagerLookup();
         if (tmLookup == null)
         {
            tmLookup = getTransactionManagerLookup();
            configBuilder.transaction().transactionManagerLookup(tmLookup);
         }
         log.debug("Infinispan transaction manager lookup: " + tmLookup);

         // If we don't want structure cache entries to expire, we need to configure infinite lifespan and keep original
         // lifespan value, which will be send to tree api
         if (skipExpirationOfStructureCacheEntries)
         {
            cacheLifespanOfLeafNodes = configFromXml.expiration().lifespan();
            configBuilder.expiration().lifespan(-1);
            log.debug("Expiration of structure cache entries is disabled. Leaf nodes will use expiration " + cacheLifespanOfLeafNodes);
         }

         cacheManager = new DefaultCacheManager(globalConfigBuilder.build(), configBuilder.build(), true);
         this.cacheManager = cacheManager;
      }

      return cacheManager;
   }

   /**
    * @return JBossTransactionManagerLookup if we are in AS7 or JBossStandaloneJTAManagerLookup otherwise
    */
   private TransactionManagerLookup getTransactionManagerLookup()
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

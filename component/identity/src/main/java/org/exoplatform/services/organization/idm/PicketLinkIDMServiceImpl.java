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

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;

import org.jgroups.JChannelFactory;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.cfg.IdentityConfiguration;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.impl.cache.JBossCacheAPICacheProviderImpl;
import org.picketlink.idm.impl.cache.JBossCacheIdentityStoreCacheProviderImpl;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.picketlink.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
import org.picketlink.idm.spi.configuration.metadata.IdentityConfigurationMetaData;
import org.picocontainer.Startable;

import java.io.IOException;
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

   public static final String PARAM_HIBERNATE_PROPS = "hibernate.properties";

   public static final String PARAM_HIBERNATE_MAPPINGS = "hibernate.mappings";

   public static final String PARAM_HIBERNATE_ANNOTATIONS = "hibernate.annotations";

   public static final String PARAM_JNDI_NAME_OPTION = "jndiName";

   public static final String REALM_NAME_OPTION = "portalRealm";

   public static final String CACHE_CONFIG_API_OPTION = "apiCacheConfig";

   public static final String CACHE_CONFIG_STORE_OPTION = "storeCacheConfig";

   public static final String JGROUPS_CONFIG = "jgroups-configuration";

   public static final String JGROUPS_MUX_ENABLED = "jgroups-multiplexer-stack";

   public static final String CACHE_EXPIRATION = "cacheExpiration";

   private IdentitySessionFactory identitySessionFactory;

   private String config;

   private String realmName = "idm_realm";

   private IdentityConfiguration identityConfiguration;

   private IntegrationCache integrationCache;

   private static final JChannelFactory CHANNEL_FACTORY = new JChannelFactory();

   private HibernateService hibernateService;

   private PicketLinkIDMServiceImpl()
   {
   }

   public PicketLinkIDMServiceImpl(
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
      ValueParam jgroupsStack = initParams.getValueParam(JGROUPS_MUX_ENABLED);
      ValueParam jgroupsConfig = initParams.getValueParam(JGROUPS_CONFIG);
      ValueParam cacheExpirationParam = initParams.getValueParam(CACHE_EXPIRATION);

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

         int expiration = -1;

         if (cacheExpirationParam != null &&
             cacheExpirationParam.getValue() != null &&
             cacheExpirationParam.getValue().length() > 0)
         {
            expiration = Integer.decode(cacheExpirationParam.getValue());
         }

         if (apiCacheConfig != null)
         {

            InputStream configStream = confManager.getInputStream(apiCacheConfig.getValue());

            CacheFactory factory = new DefaultCacheFactory();

            if (configStream == null)
            {
               throw new IllegalArgumentException("JBoss Cache configuration InputStream is null");
            }

            Cache cache = factory.createCache(configStream);

            applyJGroupsConfig(cache, confManager, jgroupsStack, jgroupsConfig);

            cache.create();
            cache.start();

            configStream.close();

            // PLIDM API cache
            JBossCacheAPICacheProviderImpl apiCacheProvider = new JBossCacheAPICacheProviderImpl();
            apiCacheProvider.setExpiration(expiration);
            apiCacheProvider.initialize(cache);
            picketLinkIDMCache.register(apiCacheProvider);
            identityConfiguration.getIdentityConfigurationRegistry().register(apiCacheProvider, "apiCacheProvider");

            //Integration cache
            integrationCache = new IntegrationCache();
            integrationCache.setExpiration(expiration);
            integrationCache.initialize(cache);
            picketLinkIDMCache.register(integrationCache);

         }


         if (storeCacheConfig != null)
         {
            InputStream configStream = confManager.getInputStream(storeCacheConfig.getValue());

            CacheFactory factory = new DefaultCacheFactory();

            if (configStream == null)
            {
               throw new IllegalArgumentException("JBoss Cache configuration InputStream is null");
            }

            Cache cache = factory.createCache(configStream);

            applyJGroupsConfig(cache, confManager, jgroupsStack, jgroupsConfig);

            cache.create();
            cache.start();

            configStream.close();

            JBossCacheIdentityStoreCacheProviderImpl storeCacheProvider = new JBossCacheIdentityStoreCacheProviderImpl();
            storeCacheProvider.setExpiration(expiration);
            storeCacheProvider.initialize(cache);
            picketLinkIDMCache.register(storeCacheProvider);
            identityConfiguration.getIdentityConfigurationRegistry().register(storeCacheProvider, "storeCacheProvider");


            configStream.close();

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

   /**
    * Applying JGroups configuration for JBossCache.
    * Code forked from org.exoplatform.services.jcr.jbosscacheExoJBossCacheFactory
    *
    * @param cache
    * @param configurationManager
    * @param jgroupsEnabledParam
    * @param jgroupsConfigurationParam
    */
   private void applyJGroupsConfig(Cache cache,
                                   ConfigurationManager configurationManager,
                                   ValueParam jgroupsEnabledParam,
                                   ValueParam jgroupsConfigurationParam)
   {

      String jgroupsEnabled = jgroupsEnabledParam != null ? jgroupsEnabledParam.getValue() : null;
      String jgroupsConfiguration = jgroupsConfigurationParam != null ? jgroupsConfigurationParam.getValue() : null;

      // JGroups multiplexer configuration if enabled
      if (jgroupsEnabled != null && jgroupsEnabled.equalsIgnoreCase("true"))
      {
         try
         {
           if (jgroupsConfiguration != null)
            {
               // Create and inject multiplexer factory
               CHANNEL_FACTORY.setMultiplexerConfig(configurationManager.getResource(jgroupsConfiguration));
               cache.getConfiguration().getRuntimeConfig().setMuxChannelFactory(CHANNEL_FACTORY);
               log.info("Multiplexer stack successfully enabled for the cache.");
            }
         }
         catch (Exception e)
         {
            // exception occurred setting mux factory
            throw new IllegalStateException("Error setting multiplexer configuration.", e);
         }
      }
      else
      {
         // Multiplexer is not enabled. If jGroups configuration preset it is applied
         if (jgroupsConfiguration != null)
         {
            try
            {
               cache.getConfiguration().setJgroupsConfigFile(
                  configurationManager.getResource(jgroupsConfiguration));
               log.info("Custom JGroups configuration set:"
                  + configurationManager.getResource(jgroupsConfiguration));
            }
            catch (Exception e)
            {
               throw new IllegalStateException("Error setting JGroups configuration.", e);
            }
         }
      }
   }

   public HibernateService getHibernateService()
   {
      return hibernateService;
   }
}

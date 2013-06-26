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

import java.io.InputStream;
import java.net.URL;

import javax.naming.InitialContext;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.infinispan.Cache;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.SecureRandomProvider;
import org.picketlink.idm.api.cfg.IdentityConfiguration;
import org.picketlink.idm.cache.APICacheProvider;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.picketlink.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
import org.picketlink.idm.impl.credential.DatabaseReadingSaltEncoder;
import org.picketlink.idm.spi.cache.IdentityStoreCacheProvider;
import org.picketlink.idm.spi.configuration.metadata.IdentityConfigurationMetaData;
import org.picocontainer.Startable;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class PicketLinkIDMServiceImpl implements PicketLinkIDMService, Startable {

    private static Log log = ExoLogger.getLogger(PicketLinkIDMServiceImpl.class);

    public static final String PARAM_CONFIG_OPTION = "config";

    public static final String PARAM_JNDI_NAME_OPTION = "jndiName";

    public static final String PARAM_SKIP_EXPIRATION_STRUCTURE_CACHE_ENTRIES = "skipExpirationOfStructureCacheEntries";

    public static final String PARAM_USE_SECURE_RANDOM_SERVICE = "useSecureRandomService";

    public static final String PARAM_STALE_CACHE_NODES_LINKS_CLEANER_DELAY = "staleCacheNodesLinksCleanerDelay";

    public static final int DEFAULT_STALE_CACHE_NODES_LINKS_CLEANER_DELAY = 120000;

    public static final String REALM_NAME_OPTION = "portalRealm";

    public static final String CACHE_CONFIG_API_OPTION = "apiCacheConfig";

    public static final String CACHE_CONFIG_STORE_OPTION = "storeCacheConfig";

    private IdentitySessionFactory identitySessionFactory;

    private String config;

    private String realmName = "idm_realm";

    private IdentityConfiguration identityConfiguration;

    private IntegrationCache integrationCache;

    private HibernateService hibernateService;

    private InfinispanCacheFactory infinispanCacheFactory = InfinispanCacheFactory.getInstance();

    private PicketLinkIDMServiceImpl() {
    }

    public PicketLinkIDMServiceImpl(ExoContainerContext exoContainerContext, InitParams initParams,
            HibernateService hibernateService, ConfigurationManager confManager, PicketLinkIDMCacheService picketLinkIDMCache,
            InitialContextInitializer dependency) throws Exception {
        ValueParam config = initParams.getValueParam(PARAM_CONFIG_OPTION);
        ValueParam jndiName = initParams.getValueParam(PARAM_JNDI_NAME_OPTION);
        ValueParam canExpireStructureCacheEntriesParam = initParams
                .getValueParam(PARAM_SKIP_EXPIRATION_STRUCTURE_CACHE_ENTRIES);
        ValueParam staleCacheNodesLinksCleanerDelayParam = initParams
                .getValueParam(PARAM_STALE_CACHE_NODES_LINKS_CLEANER_DELAY);
        ValueParam realmName = initParams.getValueParam(REALM_NAME_OPTION);
        ValueParam apiCacheConfig = initParams.getValueParam(CACHE_CONFIG_API_OPTION);
        ValueParam storeCacheConfig = initParams.getValueParam(CACHE_CONFIG_STORE_OPTION);
        ValueParam useSecureRandomService = initParams.getValueParam(PARAM_USE_SECURE_RANDOM_SERVICE);

        this.hibernateService = hibernateService;

        if (config == null && jndiName == null) {
            throw new IllegalStateException("Either '" + PARAM_CONFIG_OPTION + "' or '" + PARAM_JNDI_NAME_OPTION
                    + "' parameter must " + "be specified");
        }
        if (realmName != null) {
            this.realmName = realmName.getValue();
        }

        long staleCacheNodesLinksCleanerDelay = staleCacheNodesLinksCleanerDelayParam == null ? DEFAULT_STALE_CACHE_NODES_LINKS_CLEANER_DELAY
                : Long.parseLong(staleCacheNodesLinksCleanerDelayParam.getValue());

        boolean skipExpirationOfStructureCacheEntries = canExpireStructureCacheEntriesParam != null
                && "true".equals(canExpireStructureCacheEntriesParam.getValue());
        // Not ideal, as we are changing field of singleton (could be changed more time with different values for different
        // portal containers)
        infinispanCacheFactory.setSkipExpirationOfStructureCacheEntries(skipExpirationOfStructureCacheEntries);

        if (config != null) {
            this.config = config.getValue();
            URL configURL = confManager.getURL(this.config);

            if (configURL == null) {
                throw new IllegalStateException("Cannot fine resource: " + this.config);
            }

            IdentityConfigurationMetaData configMD = JAXB2IdentityConfiguration.createConfigurationMetaData(confManager
                    .getInputStream(this.config));

            identityConfiguration = new IdentityConfigurationImpl().configure(configMD);

            identityConfiguration.getIdentityConfigurationRegistry().register(hibernateService.getSessionFactory(),
                    "hibernateSessionFactory");

            if (apiCacheConfig != null) {

                InputStream configStream = confManager.getInputStream(apiCacheConfig.getValue());

                if (configStream == null) {
                    throw new IllegalArgumentException("Infinispan configuration InputStream is null");
                }

                Cache cache = infinispanCacheFactory.createInfinispanCache(configStream,
                        exoContainerContext.getPortalContainerName(), "api");

                configStream.close();

                // PLIDM API cache
                APICacheProvider apiCacheProvider = infinispanCacheFactory.createAPICacheProvider(
                        staleCacheNodesLinksCleanerDelay, cache);
                picketLinkIDMCache.register(apiCacheProvider);
                identityConfiguration.getIdentityConfigurationRegistry().register(apiCacheProvider, "apiCacheProvider");

                // Integration cache
                integrationCache = infinispanCacheFactory.createIntegrationCache(cache);
                picketLinkIDMCache.register(integrationCache);

            }

            if (storeCacheConfig != null) {
                InputStream configStream = confManager.getInputStream(storeCacheConfig.getValue());

                if (configStream == null) {
                    throw new IllegalArgumentException("Infinispan configuration InputStream is null");
                }

                Cache cache = infinispanCacheFactory.createInfinispanCache(configStream,
                        exoContainerContext.getPortalContainerName(), "store");

                configStream.close();

                IdentityStoreCacheProvider storeCacheProvider = infinispanCacheFactory.createStoreCacheProvider(
                        staleCacheNodesLinksCleanerDelay, cache);
                picketLinkIDMCache.register(storeCacheProvider);
                identityConfiguration.getIdentityConfigurationRegistry().register(storeCacheProvider, "storeCacheProvider");
            }

            if (useSecureRandomService != null && "true".equals(useSecureRandomService.getValue())) {
                SecureRandomProvider secureRandomProvider = (SecureRandomProvider)exoContainerContext.getContainer().getComponentInstanceOfType(SecureRandomProvider.class);
                identityConfiguration.getIdentityConfigurationRegistry().register(secureRandomProvider, DatabaseReadingSaltEncoder.DEFAULT_SECURE_RANDOM_PROVIDER_REGISTRY_NAME);
            }
        } else {
            identitySessionFactory = (IdentitySessionFactory) new InitialContext().lookup(jndiName.getValue());
        }

    }

    public void start() {
        if (identitySessionFactory == null) {
            try {
                identitySessionFactory = identityConfiguration.buildIdentitySessionFactory();
            } catch (IdentityConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
    }

    public IdentitySessionFactory getIdentitySessionFactory() {
        return identitySessionFactory; // To change body of implemented methods use File | Settings | File Templates.
    }

    public IdentitySession getIdentitySession() throws Exception {
        return getIdentitySessionFactory().getCurrentIdentitySession(realmName);
    }

    public IdentitySession getIdentitySession(String realm) throws Exception {
        if (realm == null) {
            throw new IllegalArgumentException("Realm name cannot be null");
        }
        return getIdentitySessionFactory().getCurrentIdentitySession(realm);
    }

    public IntegrationCache getIntegrationCache() {
        return integrationCache;
    }

    public String getRealmName() {
        return realmName;
    }

    public HibernateService getHibernateService() {
        return hibernateService;
    }
}

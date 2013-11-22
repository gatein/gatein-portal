/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.picketlink.idm.cache.APICacheProvider;
import org.picketlink.idm.spi.cache.IdentityStoreCacheProvider;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
@Managed
@ManagedDescription("PicketLink IDM Cache Service")
@NameTemplate({ @Property(key = "name", value = "plidmcache"), @Property(key = "service", value = "PicketLinkIDMCacheService") })
@RESTEndpoint(path = "plidmcache")
public class PicketLinkIDMCacheService {

    private final List<IntegrationCache> integrationCache = new LinkedList<IntegrationCache>();

    private final List<APICacheProvider> apiCacheProviders = new LinkedList<APICacheProvider>();

    private final List<IdentityStoreCacheProvider> storeCacheProviders = new LinkedList<IdentityStoreCacheProvider>();

    public PicketLinkIDMCacheService() {
    }

    public void register(IntegrationCache cacheProvider) {

        if (cacheProvider != null) {
            integrationCache.add(cacheProvider);
        }

    }

    public void register(APICacheProvider cacheProvider) {

        if (cacheProvider != null) {
            apiCacheProviders.add(cacheProvider);
        }

    }

    public void register(IdentityStoreCacheProvider cacheProvider) {

        if (cacheProvider != null) {
            storeCacheProviders.add(cacheProvider);
        }

    }

    @Managed
    @ManagedDescription("Invalidate cache namespace")
    @Impact(ImpactType.WRITE)
    public void invalidate(@ManagedDescription("Cache namespace") @ManagedName("namespace") String namespace) {
        for (APICacheProvider cacheProvider : apiCacheProviders) {
            cacheProvider.invalidate(namespace);
        }

        for (IntegrationCache cacheProvider  : integrationCache) {
            cacheProvider.invalidate(namespace);
        }

        for (IdentityStoreCacheProvider cacheProvider : storeCacheProviders) {
            cacheProvider.invalidate(namespace);
        }
    }

    @Managed
    @ManagedDescription("Invalidate all caches")
    @Impact(ImpactType.WRITE)
    public void invalidateAll() {
        for (IntegrationCache cacheProvider : integrationCache) {
            cacheProvider.invalidateAll();
        }

        for (APICacheProvider cacheProvider : apiCacheProviders) {
            cacheProvider.invalidateAll();
        }

        for (IdentityStoreCacheProvider cacheProvider : storeCacheProviders) {
            cacheProvider.invalidateAll();
        }
    }

    @Managed
    @ManagedDescription("Print content of all caches")
    @Impact(ImpactType.READ)
    public String printCaches() {
        StringBuilder builder = new StringBuilder("API CACHE PROVIDERS: \n\n");
        for (APICacheProvider cacheProvider : apiCacheProviders) {
            builder.append(cacheProvider.printContent());
        }

        builder.append("\n\n\nSTORE CACHE PROVIDERS: \n\n");
        for (IdentityStoreCacheProvider cacheProvider : storeCacheProviders) {
            builder.append(cacheProvider.printContent());
        }

        builder.append("\n\n\nINTEGRATION CACHES: \n\n");
        for (IntegrationCache cacheProvider : integrationCache) {
            builder.append(cacheProvider.printContent());
        }

        return builder.toString();
    }
}

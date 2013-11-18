/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.web.content;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import juzu.impl.common.Tools;
import juzu.impl.inject.spi.BeanLifeCycle;
import juzu.impl.inject.spi.InjectionContext;

/**
 * @author Julien Viet
 */
@Singleton
public class ProviderRegistry {

    /** . */
    private final InjectionContext<?, ?> context;

    /** . */
    private List<BeanLifeCycle<ContentProvider>> lifeCycles;

    /** . */
    private List<ContentProvider> providers;

    @Inject
    public ProviderRegistry(InjectionContext context) {
        this.context = context;
        this.lifeCycles = Collections.synchronizedList(new ArrayList<BeanLifeCycle<ContentProvider>>());
        this.providers = Collections.synchronizedList(new ArrayList<ContentProvider>());
    }

    @PostConstruct
    public void start() {

        // Get from IOC
        Tools.addAll(lifeCycles, context.resolve(ContentProvider.class));
        for (BeanLifeCycle<ContentProvider> lifeCycle : lifeCycles) {
            try {
                ContentProvider provider = lifeCycle.get();
                providers.add(provider);
            } catch (InvocationTargetException e) {
                System.out.println("Could not obtain content provider");
                e.printStackTrace();
            }
        }

        // Load provides with service loader
        Iterator<ContentProvider> loaded = ServiceLoader.load(ContentProvider.class).iterator();
        while (true) {
            try {
                if (loaded.hasNext()) {
                    providers.add(loaded.next());
                } else {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Could not obtain content provider");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        try {
            for (BeanLifeCycle<ContentProvider> lifeCycle : lifeCycles) {
                lifeCycle.close();
            }
        } finally {
            lifeCycles.clear();
            providers.clear();
        }
    }

    public ContentProvider resolveProvider(String contentType) {
        for (ContentProvider provider : providers) {
            if (provider.getContentType().equals(contentType)) {
                return provider;
            }
        }
        return null;
    }
}

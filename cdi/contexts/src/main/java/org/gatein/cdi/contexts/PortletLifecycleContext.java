/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.cdi.contexts;

import org.gatein.api.cdi.context.PortletLifecycleScoped;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletLifecycleContext implements Context {

    private static final ThreadLocal<BeanStore> beanStoreCache = new ThreadLocal<BeanStore>();

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletLifecycleScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        BeanStore beanStore = beanStoreCache.get();
        if (beanStore == null) {
            return null;
        }
        if (contextual == null) {
            throw new IllegalArgumentException();
        }

        T bean = beanStore.getBean(contextual);
        if (bean == null && creational != null) {
            bean = contextual.create(creational);
            beanStore.addBean(contextual, bean);
        }

        return bean;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return isAttached();
    }

    static boolean isAttached() {
        BeanStore store = beanStoreCache.get();
        return store != null;
    }

    static void attach() {
        beanStoreCache.set(new BeanStore());
    }

    static void detach() {
        beanStoreCache.remove();
    }
}

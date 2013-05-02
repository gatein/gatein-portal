/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.gatein.cdi.contexts.beanstore.serial;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.gatein.cdi.contexts.beanstore.BeanStoreInstance;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SerializableBeanStoreInstance<T> implements BeanStoreInstance<T>, Serializable {
    private final Contextual<T> contextual;
    private final T instance;
    private final CreationalContext<T> creationalContext;

    public SerializableBeanStoreInstance(Contextual<T> contextual, T instance, CreationalContext<T> creationalContext) {
        this.contextual = new SerializableContextual<T>(contextual);
        this.instance = instance;
        this.creationalContext = creationalContext;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public CreationalContext<T> getCreationalContext() {
        return creationalContext;
    }

    @Override
    public Contextual<T> getContextual() {
        return contextual;
    }
}

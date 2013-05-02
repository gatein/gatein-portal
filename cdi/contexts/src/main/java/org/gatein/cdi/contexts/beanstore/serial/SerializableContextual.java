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

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SerializableContextual<T> implements Contextual<T>, Serializable {

    private transient Contextual<T> contextual;
    private Contextual<T> serializable;

    public SerializableContextual(Contextual<T> contextual) {
        this.contextual = contextual;
        if (contextual instanceof Serializable) {
            this.serializable = contextual;
        }
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return get().create(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        get().destroy(instance, creationalContext);
    }

    private Contextual<T> get() {
        //TODO: Do we need to support serialization of non serialized contextual's ?
        if (contextual == null && serializable == null) {
            throw new IllegalStateException("No support for non-serializable contexual.");
        }
        if (serializable != null) {
            this.contextual = serializable;
        }
        return contextual;
    }
}

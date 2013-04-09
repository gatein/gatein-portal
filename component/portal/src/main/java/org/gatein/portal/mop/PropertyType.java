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

package org.gatein.portal.mop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class PropertyType<T> implements Serializable {

    /** . */
    private static final ConcurrentHashMap<Class<?>, PropertyType> REGISTRY = new ConcurrentHashMap<Class<?>, PropertyType>();

    /** . */
    private final String name;

    /** . */
    private final Class<T> type;

    public PropertyType(String name, Class<T> type) throws NullPointerException {
        if (name == null) {
            throw new NullPointerException("No null name accepted");
        }
        if (type == null) {
            throw new NullPointerException("No null type accepted");
        }
        REGISTRY.put(getClass(), this);
        this.name = name;
        this.type = type;
    }

    public final String getName() {
        return name;
    }

    public final Class<T> getType() {
        return type;
    }

    protected final Object readResolve() throws ObjectStreamException {
        return REGISTRY.get(getClass());
    }
}

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

/**
 * @author Julien Viet
 */
public class ValueType<T> implements Serializable {

    /** . */
    public static final ValueType<String> STRING = new ValueType<String>(String.class);

    /** . */
    public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>(Boolean.class);

    /** . */
    private final Class<T> javaClass;

    public ValueType(Class<T> javaClass) {
        this.javaClass = javaClass;
    }

    public final T cast(Object o) {
        return javaClass.cast(o);
    }

    protected final Object readResolve() throws ObjectStreamException {
        if (javaClass.equals(String.class)) {
            return STRING;
        } else if (javaClass.equals(Boolean.class)) {
            return BOOLEAN;
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return javaClass.getSimpleName();
    }
}

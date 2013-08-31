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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class Properties implements Iterable<Property>, Serializable {

    /** . */
    private static final Map<String, Property> EMPTY_MAP = Collections.emptyMap();

    /** . */
    public static Properties EMPTY = new Properties(Collections.<String, Property>emptyMap());

    /** . */
    private final Map<String, Property> state;

    private Properties(Map<String, Property> state) {
        this.state = state;
    }

    public <T> T get(PropertyType<T> type) {
        Property<?> property = state.get(type.getName());
        if (property != null && property.getType() == type.getType()) {
            return type.cast(property.value);
        }
        return null;
    }

    public Object get(String name) {
        Property<?> property = state.get(name);
        if (property != null) {
            return property.value;
        } else {
            return null;
        }
    }

    public Set<String> keys() {
        return state.keySet();
    }

    @Override
    public Iterator<Property> iterator() {
        return state.values().iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Properties) {
            Properties that = (Properties) obj;
            return state.equals(that.state);
        } else {
            return false;
        }
    }

    public static class Builder {

        /** . */
        private final Properties origin;

        /** . */
        private Map<String, Property> state;

        public Builder(Properties origin) {
            this.origin = origin;
            this.state = EMPTY_MAP;
        }

        public <T> Builder set(PropertyType<T> type, T value) {
            if (type == null) {
                throw new NullPointerException();
            }
            if (value != null) {
                if (state == EMPTY_MAP) {
                    state = new HashMap<String, Property>();
                }
                state.put(type.getName(), new Property<T>(type.getName(), value, type.getType()));
            } else {
                if (state != EMPTY_MAP) {
                    state.remove(type.getName());
                }
            }
            return this;
        }

        public <T> Builder set(String name, ValueType<T> type, T value) {
            if (name == null) {
                throw new NullPointerException();
            }
            if (value != null) {
                if (state == EMPTY_MAP) {
                    state = new HashMap<String, Property>();
                }
                state.put(name, new Property<T>(name, value, type));
            } else {
                if (state != EMPTY_MAP) {
                    state.remove(name);
                }
            }
            return this;
        }

/*
        public Builder set(Map<String, ?> entries) {
            for (Map.Entry<String, ?> property : entries.entrySet()) {
                Object value = property.getValue();
                if (value != null) {
                    set(property.getKey(), value.toString());
                }
            }
            return this;
        }
*/

        public Properties build() {
            Map<String, Property> a = new HashMap<String, Property>(origin.state.size() + state.size());
            a.putAll(origin.state);
            a.putAll(state);
            return new Properties(a);
        }
    }

    public Builder builder() {
        return new Builder(this);
    }
}

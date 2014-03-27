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

package org.exoplatform.portal.mop.navigation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Attributes;

/**
 *
 * Immutable attributes. The underlying
 * {@link Map} is granted:
 * <ol>
 * <li>
 * Not to be changed once the given {@link AttributesState} is created.
 * <li>
 * Not to be exposed to any code able to change it outside this class.
 * </ol>
 * How to create:
 * <pre>AttributesState attributesState = new AttributesState.Builder()
 *         .attribute("key1", "value1")
 *         .attribute("key2", "value2")
 *         .build();</pre>
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class AttributesState implements Serializable, Map<String, String> {
    private static final Logger log = LoggerFactory.getLogger(AttributesState.class);

    public static class Builder {

        private Map<String, String> map;

        public Builder() {
        }

        public Builder(Map<? extends String, ? extends String> attributes) {
            super();
            attributes(attributes);
        }

        public AttributesState.Builder attribute(String key, String value) {
            if (value == null) {
                if (map != null) {
                    /* here we follow what is in SimpleAttributes */
                    map.remove(key);
                }
            } else {
                if (map == null) {
                    map = new HashMap<String, String>();
                }
                map.put(key, value);
            }
            return this;
        }

        public AttributesState.Builder attributes(Map<? extends String, ? extends String> attributes) {
            if (attributes != null) {
                if (attributes.size() > 0) {
                    if (map == null) {
                        int attributesSize = attributes.size();
                        map = new HashMap<String, String>(attributesSize + attributesSize / 2);
                    }
                    for (Map.Entry<? extends String, ? extends String> en : attributes.entrySet()) {
                        if (en.getValue() == null) {
                            /* here we follow what is in SimpleAttributes */
                            map.remove(en.getKey());
                        } else {
                            map.put(en.getKey(), en.getValue());
                        }

                    }
                }
            }
            return this;
        }

        public AttributesState.Builder attributes(String filterPrefix, Attributes attributes) {
            if (attributes == null) {
                if (map != null) {
                    map.clear();
                }
            } else {
                Set<String> keys = attributes.getKeys();
                if (keys.size() > 0) {
                    if (map == null) {
                        int attributesSize = keys.size();
                        map = new HashMap<String, String>(attributesSize + attributesSize / 2);
                    }
                    for (String key : attributes.getKeys()) {
                        if (key.startsWith(filterPrefix)) {
                            try {
                                String value = attributes.getString(key);
                                if (value == null) {
                                    map.remove(key);
                                } else {
                                    map.put(key, value);
                                }
                            } catch (ClassCastException  e) {
                                log.warn("Could not cast value of attribute '"+ key +"' to String.", e);
                            }
                        }
                    }
                }
            }
            return this;
        }


        public AttributesState build() {
            return new AttributesState(map);
        }
    }

    public static final AttributesState EMPTY = new AttributesState(null);

    private final Map<String, String> map;

    /**
     * @param map
     */
    private AttributesState(Map<String, String> map) {
        if (map == null) {
            this.map = Collections.emptyMap();
        } else {
            this.map = Collections.unmodifiableMap(map);
        }
    }

    /**
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        map.clear();
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * @param value
     * @return
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * @return
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass() == this.getClass()) {
            AttributesState other = (AttributesState) obj;
            return other.map == this.map || (other.map != null && other.map.equals(this.map));
        }
        return false;
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    public String get(Object key) {
        return map.get(key);
    }

    protected Object get(String name) {
        return map.get(name);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * @return
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * @param key
     * @param value
     * @return
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public String put(String key, String value) {
        return map.put(key, value);
    }

    /**
     * @param m
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends String> m) {
        map.putAll(m);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#remove(java.lang.Object)
     */
    public String remove(Object key) {
        return map.remove(key);
    }

    /**
     * @return
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    public Collection<String> values() {
        return map.values();
    }

    /**
     * Synchronises entries from the given {@link AttributesState} to the given
     * {@link Attributes}. Keys from {@link AttributesState} entries will prefixed with
     * the given {@code prefix} and then put to the given {@link Attributes}.
     * All keys not present in the given {@link AttributesState} will be removed from the given
     * {@link Attributes}.
     *
     * @param src sync from
     * @param prefix used to prefix keys from {@code attributesState} in {@code attrs}
     * @param target sync to
     */
    public static void sync(AttributesState src, String prefix, Attributes target) {
        if (src != null) {

            /* remove the missing ones first */
            /* copy to avoid a concurrent modification exception */
            Set<String> targetKeys = new HashSet<String>(target.getKeys());
            for (String key : targetKeys) {
                if (key.startsWith(prefix)
                        && !src.containsKey(key.substring(prefix.length()))) {
                    target.setObject(key, null);
                }
            }

            for (Map.Entry<String, String> stateEntry : src.entrySet()) {
                target.setObject(prefix + stateEntry.getKey(), stateEntry.getValue());
            }

        } else {
            /* attributesState is null - remove all prefixed */
            /* copy to avoid a concurrent modification exception */
            Set<String> targetKeys = new HashSet<String>(target.getKeys());
            for (String key : targetKeys) {
                if (key.startsWith(prefix)) {
                    target.setObject(key, null);
                }
            }
        }
    }
}
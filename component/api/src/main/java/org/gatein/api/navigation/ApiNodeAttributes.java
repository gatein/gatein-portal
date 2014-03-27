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

package org.gatein.api.navigation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.exoplatform.portal.mop.navigation.AttributesState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.common.Attributes;
import org.gatein.api.internal.Parameters;

/**
 * {@link Attributes} delegating to an underlying {@link NodeContext#getState()} overriding
 * all inherited methods. Unfortunately, the way {@link Attributes} were designed do not allow
 * for a more elegant solution.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class ApiNodeAttributes extends org.gatein.api.common.Attributes {
    transient NodeContext<ApiNode> context;

    /**
     * @param context
     */
    public ApiNodeAttributes(NodeContext<ApiNode> context) {
        super();
        this.context = context;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return context.getState().getAttributesState().size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return context.getState().getAttributesState().isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Key<?>) {
            try {
                return get((Key<?>) key) != null;
            } catch (IllegalArgumentException e) {
                /* as if not there if the cast would not work */
                return false;
            }
        } else {
            return context.getState().getAttributesState().containsKey(key);
        }
    }

    /**
     * Sequential search.
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return context.getState().getAttributesState().containsValue(value);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        return context.getState().getAttributesState().get(key);
    }



    /**
     * @see org.gatein.api.common.Attributes#get(org.gatein.api.common.Attributes.Key)
     */
    @Override
    public <T> T get(Key<T> key) {
        Parameters.requireNonNull(key, "key");
        String name = key.getName();
        Object value = context.getState().getAttributesState().get(name);
        if (value == null) {
            return null;
        } else {
            Class<T> type = key.getType();
            return fromString(type, (String) value);
        }
    }

    /**
     * @see org.gatein.api.common.Attributes#put(org.gatein.api.common.Attributes.Key, java.lang.Object)
     */
    @Override
    public <T> T put(Key<T> key, T value) {
        Parameters.requireNonNull(key, "key");
        if (value != null && !key.getType().equals(value.getClass())) {
            throw new IllegalArgumentException("Value class is not the same as key type");
        }

        T oldValue = get(key);

        NodeState oldState = context.getState();
        AttributesState oldAttributes = oldState.getAttributesState();
        String stringValue = value != null ? toString(key.getType(), value) : null;
        AttributesState newAttributes = new AttributesState.Builder(oldAttributes).attribute(key.getName(), stringValue).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);

        return oldValue;
    }

    /**
     * @see org.gatein.api.common.Attributes#remove(org.gatein.api.common.Attributes.Key)
     */
    @Override
    public <T> T remove(Key<T> key) {
        return put(key, null);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(String key, String value) {
        NodeState oldState = context.getState();
        AttributesState oldAttributes = oldState.getAttributesState();
        AttributesState newAttributes = new AttributesState.Builder(oldAttributes).attribute(key, value).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);
        return oldAttributes != null ? oldAttributes.get(key) : null;
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        return put((String) key, null) ;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        NodeState oldState = context.getState();
        AttributesState oldAttributes = oldState.getAttributesState();
        AttributesState newAttributes = new AttributesState.Builder(oldAttributes).attributes(m).build();
        NodeState newState = new NodeState.Builder(oldState).attributes(newAttributes).build();
        context.setState(newState);
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        NodeState oldState = context.getState();
        NodeState newState = new NodeState.Builder(oldState).attributes(AttributesState.EMPTY).build();
        context.setState(newState);
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return context.getState().getAttributesState().keySet();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<String> values() {
        return context.getState().getAttributesState().values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return context.getState().getAttributesState().entrySet();
    }

    /**
     * @see java.util.AbstractMap#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o.getClass() == this.getClass()) {
            ApiNodeAttributes other = (ApiNodeAttributes) o;
            AttributesState otherState = other.context.getState().getAttributesState();
            AttributesState thisState = this.context.getState().getAttributesState();
            return otherState == thisState || (otherState != null && otherState.equals(thisState));
        }
        return false;
    }

    /**
     * @see java.util.AbstractMap#hashCode()
     */
    @Override
    public int hashCode() {
        return this.context.getState().getAttributesState().hashCode();
    }

    /**
     * @see java.util.AbstractMap#toString()
     */
    @Override
    public String toString() {
        return this.context.getState().getAttributesState().toString();
    }

}

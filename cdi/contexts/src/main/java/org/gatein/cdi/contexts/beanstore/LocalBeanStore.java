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

package org.gatein.cdi.contexts.beanstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Bean store that is backed only by a HashMap.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class LocalBeanStore extends AbstractBeanStore implements BeanStore {
    private final Map<String, Object> map = new HashMap<String, Object>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> BeanStoreInstance<T> getBean(String id) {
        return (BeanStoreInstance<T>) map.get(id);
    }

    @Override
    public <T> void put(String id, BeanStoreInstance<T> instance) {
        map.put(id, instance);
    }

    @Override
    public LockedBean lock(String id) {
        return null;
    }

    @Override
    public void destroy() {
        for (String id : this) {
            destroyBean(getBean(id));
        }
        map.clear();
    }

    @Override
    public void destroy(String windowId) {
        for (Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            BeanStoreInstance<?> beanInstance = (BeanStoreInstance<?>) entry.getValue();
            if (key.startsWith(windowId)) {
                destroyBean(beanInstance);
                iter.remove();
            }
        }
    }

    @Override
    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }
}

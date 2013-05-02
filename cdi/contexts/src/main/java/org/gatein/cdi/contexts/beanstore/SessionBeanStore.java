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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SessionBeanStore implements BeanStore {

    private static final String PREFIX = SessionBeanStore.class.getName();
    private static final String DELIM = "#";

    private static final ThreadLocal<LockStore> CURRENT_LOCK_STORE = new ThreadLocal<LockStore>();
    private transient volatile LockStore lockStore;

    private final HttpServletRequest request;
    private final HttpSession session;

    public SessionBeanStore(HttpServletRequest request) {
        this(request, null);
    }

    public SessionBeanStore(HttpSession session) {
        this(null, session);
    }

    private SessionBeanStore(HttpServletRequest request, HttpSession session) {
        this.request = request;
        this.session = session;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BeanStoreInstance<T> getBean(String id) {
        HttpSession session = getSession(false);
        if (session == null) {
            return null;
        }

        return (BeanStoreInstance<T>) session.getAttribute(prefix(id));
    }

    @Override
    public <T> void put(String id, BeanStoreInstance<T> instance) {
        HttpSession session = getSession(true);
        if (session != null) {
            session.setAttribute(prefix(id), instance);
        }
    }

    @Override
    public LockedBean lock(String id) {
        return getLockStore().lock(id);
    }

    @Override
    public void destroy(String windowId) {
        for (String id : this) {
            if (id.startsWith(windowId)) {
                removeAttribute(id);
            }
        }
    }

    @Override
    public void destroy() {
        for (String id : this) {
            removeAttribute(id);
        }
    }

    @Override
    public Iterator<String> iterator() {
        return getIds(getSession(false)).iterator();
    }

    // Implementation taken from Weld's AbstractSessionBeanStore
    public LockStore getLockStore() {
        LockStore lockStore = this.lockStore;
        if (lockStore == null) {
            //needed to prevent some edge cases
            //where we would otherwise enter an infinite loop
            lockStore = CURRENT_LOCK_STORE.get();
            if (lockStore != null) {
                return lockStore;
            }
            HttpSession session = getSession(false);
            if (session == null) {
                lockStore = new LockStore();
                CURRENT_LOCK_STORE.set(lockStore);
                try {
                    session = getSession(true);
                } finally {
                    CURRENT_LOCK_STORE.remove();
                }
            }
            lockStore = (LockStore) session.getAttribute(LockStore.SESSION_KEY);
            if (lockStore == null) {
                //we don't really have anything we can lock on
                //so we just acquire a big global lock
                //this should only be taken on session creation though
                //so should not be a problem
                synchronized (SessionBeanStore.class) {
                    lockStore = (LockStore) session.getAttribute(LockStore.SESSION_KEY);
                    if (lockStore == null) {
                        lockStore = new LockStore();
                        session.setAttribute(LockStore.SESSION_KEY, lockStore);
                    }
                }
            }
            this.lockStore = lockStore;
        }
        return lockStore;
    }

    public HttpSession getSession(boolean create) {
        return (session != null) ? session : request.getSession(create);
    }

    private void removeAttribute(String id) {
        HttpSession session = getSession(false);
        if (session != null) {
            session.removeAttribute(prefix(id));
        }
    }

    private static List<String> getIds(HttpSession session) {
        if (session == null) return Collections.emptyList();

        List<String> list = new ArrayList<String>();
        Enumeration<String> enumeration = session.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String attributeName = enumeration.nextElement();
            if (attributeName.startsWith(PREFIX)) {
                list.add(deprefix(attributeName));
            }
        }
        return list;
    }

    private static String prefix(String id) {
        return PREFIX + DELIM + id;
    }

    private static String deprefix(String attributeName) {
        return attributeName.substring(PREFIX.length() + DELIM.length());
    }
}

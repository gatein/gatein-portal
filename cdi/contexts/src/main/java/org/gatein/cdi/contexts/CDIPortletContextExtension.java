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

package org.gatein.cdi.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CDIPortletContextExtension implements Extension {

    private Collection<CDIPortletContext> contexts;

    @SuppressWarnings("unused")
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        List<CDIPortletContext> contextList = new ArrayList<CDIPortletContext>(2);
        contextList.add(new PortletLifecycleContextImpl());
        contextList.add(new PortletRedisplayedContextImpl());

        for (CDIPortletContext context : contextList) {
            afterBeanDiscovery.addContext(context);
        }
        contexts = Collections.unmodifiableCollection(contextList);
    }

    @SuppressWarnings("unchecked")
    public <T extends CDIPortletContext> T getContext(Class<T> type) {
        if (type == null) return null;

        for (CDIPortletContext context : contexts) {
            if (type.isAssignableFrom(context.getClass())) {
                return (T) context;
            }
        }

        return null;
    }

    public Collection<CDIPortletContext> getContexts() {
        Collection<CDIPortletContext> collection = contexts;
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection;
    }
}

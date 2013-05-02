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

import java.lang.annotation.Annotation;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gatein.api.cdi.context.PortletRedisplayScoped;
import org.gatein.cdi.contexts.beanstore.SessionBeanStore;

import static javax.portlet.PortletRequest.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletRedisplayedContextImpl extends AbstractCDIPortletContext implements PortletRedisplayedContext {

    private static final String TRANSITION_PREFIX = PortletRequestLifecycle.class.getName();
    private static final String TRANSITION_DELIM = "#";

    public PortletRedisplayedContextImpl() {
        super(true);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletRedisplayScoped.class;
    }

    @Override
    public void transition(final HttpServletRequest request, final String windowId, final PortletRequestLifecycle.State state) {
        if (getBeanStore() == null) {
            setBeanStore(new SessionBeanStore(request));
        }

        SessionBeanStore store = (SessionBeanStore) getBeanStore();
        String attributeName = prefix(windowId);
        HttpSession session = store.getSession(true);

        PortletRequestLifecycle lifecycle = getLifecycle(windowId);
        if (lifecycle == null) {
            lifecycle = (PortletRequestLifecycle) session.getAttribute(attributeName);
            if (lifecycle == null) {
                lifecycle = new PortletRequestLifecycle();
            }
        }

        // This check ensures we don't keep adding transitions and get into some strange state.
        if (lifecycle.size() > 6) { // Most we can have is an action->event->render (with a start and end for each phase, that's 6)
            destroy(windowId);
            lifecycle = new PortletRequestLifecycle();
        }

        // Logic on the lifecycle
        if (state.started()) {
            if (state.isPhase(ACTION_PHASE)) {
                destroy(windowId);
                lifecycle = new PortletRequestLifecycle();
            } else if (state.isPhase(EVENT_PHASE) && lifecycle.last() == null) {
                destroy(windowId);
                lifecycle = new PortletRequestLifecycle();
            }
        } else if (state.ended()) {
            if (state.isPhase(RENDER_PHASE, RESOURCE_PHASE)) {
                lifecycle = null;
            }
        }

        if (lifecycle != null) {
            lifecycle.addNext(state);
            session.setAttribute(attributeName, lifecycle);
        } else { // We assume it's the end of the lifecycle so remove it from the session
            session.removeAttribute(attributeName);
        }

        setCurrentLifecycle(windowId, lifecycle);
    }

    @Override
    public void dissociate(final HttpSession session) {
        setBeanStore(new SessionBeanStore(session));
        destroy();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith(TRANSITION_PREFIX)) {
                session.removeAttribute(name);
            }
        }
    }

    private static String prefix(String id) {
        return TRANSITION_PREFIX + TRANSITION_DELIM + id;
    }
}

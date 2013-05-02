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

import javax.servlet.http.HttpServletRequest;

import org.gatein.api.cdi.context.PortletLifecycleScoped;
import org.gatein.cdi.contexts.beanstore.LocalBeanStore;

import static javax.portlet.PortletRequest.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletLifecycleContextImpl extends AbstractCDIPortletContext implements PortletLifecycleContext {

    public PortletLifecycleContextImpl() {
        super(false);
    }

    @Override
    public void transition(HttpServletRequest request, String windowId, PortletRequestLifecycle.State state) {
        if (getBeanStore() == null) {
            setBeanStore(new LocalBeanStore());
        }

        PortletRequestLifecycle lifecycle = getLifecycle(windowId);
        if (lifecycle == null) {
            lifecycle = new PortletRequestLifecycle();
        }

        // This check ensures we don't keep adding states and get into some strange state.
        if (lifecycle.size() > 3 * 2) { // Most we can have is an action->event->render (with a start and end for each phase) so 6
            destroy(windowId);
            lifecycle = new PortletRequestLifecycle();
        }

        // Perform logic on state (we really don't need to know the history of the transitions as the other scope does)
        if (state.started()) {
            if (state.isPhase(ACTION_PHASE, RESOURCE_PHASE)) {
                destroy(windowId);
                lifecycle = new PortletRequestLifecycle();
            }
        } else if (state.ended()) {
            if (state.isPhase(RENDER_PHASE, RESOURCE_PHASE)) {
                destroy(windowId);
                lifecycle = null;
            }
        }

        if (lifecycle != null) {
            lifecycle.addNext(state);
        }

        setCurrentLifecycle(windowId, lifecycle);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletLifecycleScoped.class;
    }
}

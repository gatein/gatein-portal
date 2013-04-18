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

package org.gatein.cdi.contexts;

import org.exoplatform.portal.pc.aspects.PortletLifecyclePhaseInterceptor;

import javax.portlet.PortletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CDIServletListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        boolean attached = PortletLifecycleContext.isAttached();
        if (!attached) {
            PortletLifecycleContext.attach();
        } else {
            if (isActionRequest()) {
                // Unlikely to occur, but just as a precaution
                PortletLifecycleContext.detach();
                PortletLifecycleContext.attach();
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        boolean attached = PortletLifecycleContext.isAttached();
        if (attached) {
            if (isRenderRequest() || isResourceRequest()) {
                PortletLifecycleContext.detach();
            }
        }
    }

    private boolean isActionRequest() {
        return PortletRequest.ACTION_PHASE.equals(PortletLifecyclePhaseInterceptor.getLifecyclePhase());
    }

    private boolean isEventRequest() {
        return PortletRequest.EVENT_PHASE.equals(PortletLifecyclePhaseInterceptor.getLifecyclePhase());
    }

    private boolean isRenderRequest() {
        return PortletRequest.RENDER_PHASE.equals(PortletLifecyclePhaseInterceptor.getLifecyclePhase());
    }

    private boolean isResourceRequest() {
        return PortletRequest.RESOURCE_PHASE.equals(PortletLifecyclePhaseInterceptor.getLifecyclePhase());
    }
}

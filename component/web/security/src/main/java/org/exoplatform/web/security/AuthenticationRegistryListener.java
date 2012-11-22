/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.exoplatform.web.security;

import javax.servlet.http.HttpSessionEvent;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractHttpSessionListener;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationRegistryListener extends AbstractHttpSessionListener {

    @Override
    protected boolean requirePortalEnvironment() {
        return true;
    }

    @Override
    protected void onSessionCreated(ExoContainer container, HttpSessionEvent event) {
    }

    @Override
    protected void onSessionDestroyed(ExoContainer container, HttpSessionEvent event) {
        AuthenticationRegistry authenticationRegistry = (AuthenticationRegistry) container
                .getComponentInstanceOfType(AuthenticationRegistry.class);
        authenticationRegistry.removeClient(event.getSession().getId());
    }
}

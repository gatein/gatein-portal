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

package org.gatein.api;

import org.gatein.api.common.URIResolver;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.security.User;
import org.gatein.api.site.SiteId;

import java.util.Locale;

/**
 * A basic portal request supplying all the information needed in the constructor.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class BasicPortalRequest extends PortalRequest {
    private final User user;
    private final SiteId siteId;
    private final NodePath nodePath;
    private final Locale locale;
    private final Portal portal;
    private final URIResolver uriResolver;

    public BasicPortalRequest(User user, SiteId siteId, NodePath nodePath, Locale locale, Portal portal, URIResolver uriResolver) {
        this.user = user;
        this.siteId = siteId;
        this.nodePath = nodePath;
        this.locale = locale;
        this.portal = portal;
        this.uriResolver = uriResolver;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public SiteId getSiteId() {
        return siteId;
    }

    @Override
    public NodePath getNodePath() {
        return nodePath;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Portal getPortal() {
        return portal;
    }

    @Override
    public URIResolver getURIResolver() {
        return uriResolver;
    }

    public static void setInstance(BasicPortalRequest request) {
        PortalRequest.setInstance(request);
    }

    public static class BasicURIResolver implements URIResolver {

        private String portalURI;

        public BasicURIResolver(String portalURI) {
            this.portalURI = portalURI;
        }

        @Override
        public String resolveURI(SiteId siteId) {
            String name = siteId.getName();
            if (name.charAt(0) == '/') {
                return portalURI + name;
            } else {
                return portalURI + "/" + name;
            }
        }
    }
}

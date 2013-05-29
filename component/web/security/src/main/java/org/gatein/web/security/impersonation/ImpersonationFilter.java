/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.web.security.impersonation;

import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.jaas.UserPrincipal;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;

/**
 * Filter to wrap real {@link HttpServletRequest} into wrapper, which will be treated as request of impersonated user
 *
 * It should be in filter chain after {@link org.exoplatform.services.security.web.SetCurrentIdentityFilter}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ImpersonationFilter extends AbstractFilter {
    private static final Logger log = LoggerFactory.getLogger(ImpersonationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;

        Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        if (currentIdentity instanceof ImpersonatedIdentity) {
            ImpersonatedIdentity impersonatedIdentity = (ImpersonatedIdentity)currentIdentity;

            String remoteUser = httpRequest.getRemoteUser();
            String impersonatedUser = impersonatedIdentity.getUserId();
            String parentImpersonatedUser = impersonatedIdentity.getParentConversationState().getIdentity().getUserId();

            // Skip impersonation if impersonatedUser is same as remoteUser. This could theoretically happen during http request re-entrance
            if (remoteUser.equals(impersonatedUser)) {
                if (log.isTraceEnabled()) {
                    log.trace("Reentrance detected. Impersonation will be skipped. User: " + remoteUser +
                            ", parentImpersonatedUser: " + parentImpersonatedUser + ", impersonatedUser: " + impersonatedUser);
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Impersonating current HttpServletRequest. User: " + remoteUser +
                            ", parentImpersonatedUser: " + parentImpersonatedUser + ", impersonatedUser: " + impersonatedUser);
                }

                // Impersonate current http request
                httpRequest = new ImpersonatedHttpServletRequestWrapper(httpRequest, impersonatedIdentity);
            }
        }

        // Continue with request in all cases
        chain.doFilter(httpRequest, response);
    }

    @Override
    public void destroy() {
    }

    public static class ImpersonatedHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final ImpersonatedIdentity identity;

        public ImpersonatedHttpServletRequestWrapper(HttpServletRequest request, ImpersonatedIdentity identity) {
            super(request);
            this.identity = identity;
        }

        @Override
        public String getRemoteUser() {
            return this.identity.getUserId();
        }

        @Override
        public boolean isUserInRole(String role) {
            Collection<String> roles = this.identity.getRoles();
            return roles.contains(role);
        }

        @Override
        public Principal getUserPrincipal() {
            return new UserPrincipal(this.identity.getUserId());
        }
    }
}

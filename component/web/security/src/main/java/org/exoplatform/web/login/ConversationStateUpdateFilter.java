/*
 *
 *  JBoss, a division of Red Hat
 *  Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
 *  by the @authors tag. See the copyright.txt in the distribution for a
 *  full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.exoplatform.web.login;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;

/**
 * Filter is used to update {@link ConversationState} with necessary attributes after login of user. It needs to be configured
 * in filter chain after {@link org.exoplatform.services.security.web.SetCurrentIdentityFilter} !!!
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConversationStateUpdateFilter extends AbstractFilter {
    private static final Logger log = LoggerFactory.getLogger(ConversationStateUpdateFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        AuthenticationRegistry authRegistry = (AuthenticationRegistry) getContainer().getComponentInstanceOfType(
                AuthenticationRegistry.class);

        // This should happen during first request of authenticated user. We need to bind credentials to ConversationState
        // and unregister them from authenticationRegistry
        if (hreq.getRemoteUser() != null && authRegistry.getCredentials(hreq) != null) {
            Credentials credentials = authRegistry.removeCredentials(hreq);
            bindCredentialsToConversationState(credentials);
        }

        // Continue with filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Add credentials to {@link ConversationState}.
     *
     * @param credentials
     */
    protected void bindCredentialsToConversationState(Credentials credentials) {
        ConversationState currentConversationState = ConversationState.getCurrent();
        if (currentConversationState != null && credentials != null) {
            log.debug("Binding credentials to conversationState for user " + credentials.getUsername());
            currentConversationState.setAttribute(Credentials.CREDENTIALS, credentials);
        }
    }
}

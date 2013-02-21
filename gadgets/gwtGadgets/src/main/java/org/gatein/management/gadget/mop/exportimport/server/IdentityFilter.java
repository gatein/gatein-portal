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

package org.gatein.management.gadget.mop.exportimport.server;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.StateKey;
import org.exoplatform.services.security.web.HttpSessionStateKey;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Identity filter which checks to see if an identity from the portal can be found before processing request.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class IdentityFilter extends AbstractFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(IdentityFilter.class);

    private String role;
    private String group;
    private String user;

    @Override
    protected void afterInit(FilterConfig config) throws ServletException {
        role = config.getInitParameter("role");
        group = config.getInitParameter("group");
        user = config.getInitParameter("user");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession httpSession = httpRequest.getSession();
        StateKey stateKey = new HttpSessionStateKey(httpSession);

        ExoContainer container = getContainer();

        ConversationRegistry conversationRegistry =
                (ConversationRegistry) container.getComponentInstanceOfType(ConversationRegistry.class);

        ConversationState state = conversationRegistry.getState(stateKey);
        if (state == null) {
            log.error("Conversation state not found. This typically means the GWT Gadget application is either being access anonymously or from outside the portal.");
            handleError(httpRequest, httpResponse, 401);
            return;
        }
        Identity identity = state.getIdentity();
        if (identity == null) {
            log.error("Identity not found from conversation state. This should not happen. GWT Gadget will not process");
            handleError(httpRequest, httpResponse, 401);
            return;
        }
        ConversationState.setCurrent(state);

        // Do not process if nothing was configured
        if (role == null && group == null && user == null) {
            log.error("Neither role, group, or user was configured as part of init-param of IdentityFilter for GWT Gadget application.");
            handleError(httpRequest, httpResponse, 500);
            return;
        }

        // Check roles
        if (role != null) {
            for (String identityRole : identity.getRoles()) {
                if (role.equals(identityRole)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // Check memberships
        if (group != null) {
            if (identity.isMemberOf(group)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Check user
        if (user != null) {
            if (user.equals(identity.getUserId())) {
                chain.doFilter(request, response);
                return;
            }
        }

        log.error("GWT Gadget not authorized for user " + identity.getUserId() + ". Check the filter configuration for IdentityFilter in web.xml.");
        handleError(httpRequest, httpResponse, 401);
    }

    @Override
    public void destroy() {
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, int code) throws IOException {
        if (request.getServletPath().equals("/exportimport/upload")) {
            // Stupid GWT Upload can't properly handle a response.sendError...
            response.setContentType("text/plain");
            Writer writer = response.getWriter();
            writer.write("<response><error>Resource not available</error></response>");
            writer.flush();
            writer.close();
        } else {
            response.sendError(code);
        }
    }
}

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

package org.gatein.security.oauth.webapi;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.sso.agent.filter.api.SSOInterceptor;
import org.gatein.sso.integration.SSODelegateFilter;

/**
 * Filter will delegate the work to configured OAuth interceptors
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthDelegateFilter extends SSODelegateFilter {

    private volatile Map<SSOInterceptor, String> oauthInterceptors;

    private static final Logger log = LoggerFactory.getLogger(OAuthDelegateFilter.class);

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Map<SSOInterceptor, String> oauthInterceptors = getInterceptors();

        // skip this filter if no oauthInterceptors are declared
        if (!oauthInterceptors.isEmpty()) {
            OAuthFilterChain oauthChain = new OAuthFilterChain(chain, getInterceptors(), this);
            oauthChain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private Map<SSOInterceptor, String> getInterceptors() {
        if (oauthInterceptors == null) {
            synchronized (this) {
                if (oauthInterceptors == null) {
                    OAuthFilterIntegrator oauthFilterIntegrator = (OAuthFilterIntegrator)getContainer().getComponentInstanceOfType(OAuthFilterIntegrator.class);
                    oauthInterceptors = oauthFilterIntegrator.getOAuthInterceptors();
                    if (!oauthInterceptors.isEmpty()) {
                        log.info("Initialized OAuth integrator with interceptors: " + oauthInterceptors);
                    }
                }
            }
        }

        return oauthInterceptors;
    }

    private static class OAuthFilterChain extends SSODelegateFilter.SSOFilterChain {

        private OAuthFilterChain(FilterChain containerChain, Map<SSOInterceptor, String> interceptors, OAuthDelegateFilter oauthDelegateFilter) {
            super(containerChain, interceptors, oauthDelegateFilter);
        }
    }
}

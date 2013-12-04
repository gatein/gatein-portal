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

package org.gatein.portal.installer;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter for checking a flag if root user is properly initialized.
 * If not, filter redirects to GateIn root setup page.
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupFilter implements Filter {

    public static final String GATEIN_SETUP_ENABLE = "gatein.portal.setup.enable";

    private static final String SETUP_JSP = "/setup/jsp/setup.jsp";
    private static final String SETUP_ACTION = "/setupaction";
    private static final String[] resourceExtension = {".css",".png",".jpg"};
    private FilterConfig cfg;

    private boolean setupEnable = Boolean.parseBoolean(System.getProperty(GATEIN_SETUP_ENABLE, "false"));

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        String uri = httpReq.getRequestURI();
        String context = httpReq.getContextPath().substring(1);

        if (PortalSetupService.isSetup(context) || isResourceUri(uri)) {
            chain.doFilter(req, resp);
        } else if (setupEnable) {

            if (uri.endsWith(SETUP_ACTION))
                chain.doFilter(req, resp);
            else
                cfg.getServletContext().getRequestDispatcher(SETUP_JSP).forward(req, resp);
        } else
            chain.doFilter(req, resp);
    }

    private boolean isResourceUri(String uri){
        for(String extension : resourceExtension){
            if (uri.endsWith(extension))
                return true;
        }
        return false;
    }

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        this.cfg = cfg;
    }

}

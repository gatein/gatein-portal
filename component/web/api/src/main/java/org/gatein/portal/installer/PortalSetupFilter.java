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

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.filter.Filter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * A filter for checking a flag if root user is properly initialized.
 * If not, filter redirects to GateIn root setup page.
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(PortalSetupFilter.class);

    private static final String SETUP_JSP = "/setup/jsp/setup.jsp";
    private static final String SETUP_ACTION = "/setupaction";
    private static final String[] resourceExtension = {".css",".png",".jpg"};

    private static final String PASSWORD = "password";
    private static final String PASSWORD2 = "password2";
    private static final String SETUP_ERROR = "org.gatein.portal.setup.error";

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        String uri = httpReq.getRequestURI();
        String context = httpReq.getContextPath().substring(1);

        PortalSetupService setupService = (PortalSetupService) PortalContainer.getInstance().getComponentInstance(PortalSetupService.class);
        if (!setupService.isEnable() || setupService.isSetup(context) || isResourceUri(uri)) {
            chain.doFilter(req, resp);
        } else {
            if (uri.endsWith(SETUP_ACTION))
                setupAction((HttpServletRequest)req, (HttpServletResponse)resp);
            else {
                PortalContainer portalContainer = PortalContainer.getInstance();
                ServletContext mergedContext = portalContainer.getPortalContext();
                mergedContext.getRequestDispatcher(SETUP_JSP).forward(req, resp);
            }
        }
    }

    private boolean isResourceUri(String uri) {
        for(String extension : resourceExtension){
            if (uri.endsWith(extension))
                return true;
        }
        return false;
    }

    private void setupAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String portal = uri.substring(0, uri.length() - "/setupaction".length());
        String context = request.getContextPath();
        PortalSetupService setupService = (PortalSetupService) PortalContainer.getInstance().getComponentInstance(PortalSetupService.class);
        if (setupService.isSetup(context)) {
            response.sendRedirect(portal);
        } else {
            String password = request.getParameter(PASSWORD);
            String password2 = request.getParameter(PASSWORD2);
            // Simple validation
            if ("".equals(password) || (!password.equals(password2))) {
                request.setAttribute(SETUP_ERROR, "Passwords are not equal");
                request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            } else {
                RequestLifeCycle.begin(PortalContainer.getInstance());
                try {
                    OrganizationService service = (OrganizationService) ExoContainerContext.getCurrentContainer()
                            .getComponentInstanceOfType(OrganizationService.class);
                    User root = setupService.getRootUser();
                    root.setPassword(password);
                    service.getUserHandler().saveUser(root, true);

                    // Flag
                    setupService.setJcrFlag();
                    request.setAttribute(SETUP_ERROR, null);
                    response.sendRedirect(portal);
                } catch (Exception e) {
                    log.error("Root user cannot be configured", e);
                    request.setAttribute(SETUP_ERROR, "Root user cannot be configured. See log for details.");
                    request.getRequestDispatcher(SETUP_JSP).forward(request, response);
                } finally {
                    RequestLifeCycle.end();
                }
            }
        }
    }
}

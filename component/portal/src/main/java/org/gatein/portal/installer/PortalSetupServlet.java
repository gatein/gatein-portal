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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * This servlet will update root password at first boot of GateIn.
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PortalSetupServlet.class);

    private static final long serialVersionUID = 997716509281091664L;
    private static final String PASSWORD = "password";
    private static final String PASSWORD2 = "password2";
    private static final String SETUP_JSP = "/setup/jsp/setup.jsp";
    private static final String SETUP_ERROR = "org.gatein.portal.setup.error";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String portal = uri.substring(0, uri.length() - "/setupaction".length());
        String context = request.getContextPath();
        if (PortalSetupService.isSetup(context)) {
            response.sendRedirect(portal);
        } else {
            String password = request.getParameter(PASSWORD);
            String password2 = request.getParameter(PASSWORD2);
            // Simple validation
            if ("".equals(password) || (!password.equals(password2))) {
                request.setAttribute(SETUP_ERROR, "Passwords are not equal");
                request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            } else {
                try {
                    OrganizationService service = (OrganizationService) ExoContainerContext.getCurrentContainer()
                            .getComponentInstanceOfType(OrganizationService.class);
                    User root = service.getUserHandler().findUserByName("root", UserStatus.BOTH);
                    // In the case the root user is not present
                    // This case can happens if organization-configuration.xml is not well configured
                    if (root == null) {
                        root = service.getUserHandler().createUserInstance("root");
                        root.setPassword(password);
                        root.setFirstName("Root");
                        root.setLastName("Root");
                        root.setEmail("root@localhost");
                        root.setDisplayName("root");
                        service.getUserHandler().createUser(root, true);
                        // Get memberships
                        MembershipType manager = service.getMembershipTypeHandler().findMembershipType("manager");
                        MembershipType member = service.getMembershipTypeHandler().findMembershipType("member");
                        // Get groups
                        Group administrators = service.getGroupHandler().findGroupById("/platform/administrators");
                        Group users = service.getGroupHandler().findGroupById("/platform/users");
                        Group executive_board = service.getGroupHandler().findGroupById("/organization/management/executive-board");
                        // Assign users
                        service.getMembershipHandler().linkMembership(root, administrators, manager, true);
                        service.getMembershipHandler().linkMembership(root, users, member, true);
                        service.getMembershipHandler().linkMembership(root, executive_board, member, true);
                    } else {
                        root.setPassword(password);
                        service.getUserHandler().saveUser(root, true);
                    }
                    // Flag
                    PortalSetupService.setJcrFlag();
                    request.setAttribute(SETUP_ERROR, null);
                    response.sendRedirect(portal);
                } catch (Exception e) {
                    log.error("Root user cannot be configured", e);
                    request.setAttribute(SETUP_ERROR, "Root user cannot be configured. See log for details.");
                    request.getRequestDispatcher(SETUP_JSP).forward(request, response);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

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

import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.StateKey;
import org.exoplatform.services.security.web.HttpSessionStateKey;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Servlet, which handles impersonation and impersonalization (de-impersonation) of users
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ImpersonationServlet extends AbstractHttpServlet {

    /** Request parameter to track if we want to start new impersonation session or stop existing impersonation session */
    public static final String PARAM_ACTION = "_impersonationAction";
    public static final String PARAM_ACTION_START_IMPERSONATION = "startImpersonation";
    public static final String PARAM_ACTION_STOP_IMPERSONATION = "stopImpersonation";

    /** Request parameter with name of user, who will be impersonated */
    public static final String PARAM_USERNAME = "_impersonationUsername";

    /**
     * Request parameter where is stored URI, which will be used after impersonation session will be finished
     * The point is that admin user will be redirected to same page (navigation node) from which original impersonation session was started
     * */
    public static final String PARAM_RETURN_IMPERSONATION_URI = "_returnImpersonationURI";

    /** Session attribute where return impersonation URI will be saved */
    public static final String ATTR_RETURN_IMPERSONATION_URI = "_returnImpersonationURI";

    /** Impersonation suffix (Actually path of this servlet) */
    public static final String IMPERSONATE_URL_SUFIX = "/impersonate";

    /** Prefix of session attributes, which will be used to backup existing session of root user */
    private static final String BACKUP_ATTR_PREFIX = "_bck.";

    private static final Logger log = LoggerFactory.getLogger(ImpersonationServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(PARAM_ACTION);
        if (action == null) {
            log.error("Parameter '" + PARAM_ACTION + "' not provided");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (PARAM_ACTION_START_IMPERSONATION.equals(action)) {
            startImpersonation(req, resp);
        } else if (PARAM_ACTION_STOP_IMPERSONATION.equals(action)) {
            stopImpersonation(req, resp);
        } else {
            log.error("Unknown impersonation action: " + action);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    protected void startImpersonation(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Obtain username
        String usernameToImpersonate = req.getParameter(PARAM_USERNAME);
        if (usernameToImpersonate == null) {
            log.error("Parameter '" + PARAM_USERNAME + "' not provided");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Find user to impersonate
        OrganizationService orgService = (OrganizationService)getContainer().getComponentInstanceOfType(OrganizationService.class);
        User userToImpersonate;
        try {
            userToImpersonate = orgService.getUserHandler().findUserByName(usernameToImpersonate);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        if (userToImpersonate == null) {
            log.error("User '" + usernameToImpersonate + "' not found!");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ConversationState currentConversationState = ConversationState.getCurrent();
        Identity currentIdentity = currentConversationState.getIdentity();
        if (currentIdentity instanceof ImpersonatedIdentity) {
            log.error("Already impersonated as identity: " + currentIdentity);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!checkPermission(userToImpersonate)) {
            log.error("Current user represented by identity " + currentIdentity.getUserId() + " doesn't have permission to impersonate as "
                    + userToImpersonate);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        log.debug("Going to impersonate as user: " + usernameToImpersonate);

        // Backup and clear current HTTP session
        backupAndClearCurrentSession(req);

        // Obtain URI where we need to redirect after finish impersonation session. Save it to current HTTP session
        String returnImpersonationURI = req.getParameter(PARAM_RETURN_IMPERSONATION_URI);
        if (returnImpersonationURI == null) {
            returnImpersonationURI = req.getContextPath();
        }
        req.getSession().setAttribute(ATTR_RETURN_IMPERSONATION_URI, returnImpersonationURI);
        if (log.isTraceEnabled()) {
            log.trace("Saved URI " + returnImpersonationURI + " which will be used after finish of impersonation");
        }

        // Real impersonation done here
        boolean success = impersonate(req, currentConversationState, usernameToImpersonate);
        if (success) {
            // Redirect to portal for now
            resp.sendRedirect(req.getContextPath());
        } else {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }


    /**
     * Check if current user has permission to impersonate as user 'userToImpersonate'
     *
     * @param userToImpersonate user to check
     * @return true if current user has permission to impersonate as user 'userToImpersonate'
     */
    protected boolean checkPermission(User userToImpersonate) {
        UserACL userACL = (UserACL)getContainer().getComponentInstanceOfType(UserACL.class);
        return userACL.hasImpersonateUserPermission(userToImpersonate);
    }


    /**
     * Backup all session attributes of admin user as we will have new session for "impersonated" user
     *
     * @param req http servlet request
     */
    protected void backupAndClearCurrentSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Enumeration attrNames = session.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String)attrNames.nextElement();
                Object attrValue = session.getAttribute(attrName);

                // Backup attribute and clear old
                String backupAttrName =  BACKUP_ATTR_PREFIX + attrName;
                session.setAttribute(backupAttrName, attrValue);
                session.removeAttribute(attrName);

                if (log.isTraceEnabled()) {
                    log.trace("Finished backup of attribute: " + attrName);
                }
            }
        }
    }

    /**
     * Start impersonation session and update ConversationRegistry with new impersonated Identity
     *
     * @param req servlet request
     * @param currentConvState current Conversation State. It will be wrapped inside impersonated identity, so we can later restore it
     * @param usernameToImpersonate
     * @return true if impersonation was successful
     */
    protected boolean impersonate(HttpServletRequest req, ConversationState currentConvState, String usernameToImpersonate) {
        // Create new identity for user, who will be impersonated
        Identity newIdentity = createIdentity(usernameToImpersonate);
        if (newIdentity == null) {
            return false;
        }

        ImpersonatedIdentity impersonatedIdentity = new ImpersonatedIdentity(newIdentity, currentConvState);

        // Create new entry to ConversationState
        log.debug("Set ConversationState with current session. Admin user "
                + impersonatedIdentity.getParentConversationState().getIdentity().getUserId()
                + " will use identity of user " + impersonatedIdentity.getUserId());

        ConversationState impersonatedConversationState = new ConversationState(impersonatedIdentity);

        registerConversationState(req, impersonatedConversationState);
        return true;
    }


    /**
     * Stop impersonation session and restore previous Conversation State
     *
     * @param req servlet request
     * @param resp servlet response
     */
    protected void stopImpersonation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        if (!(currentIdentity instanceof ImpersonatedIdentity)) {
            log.error("Can't stop impersonation session. Current identity is not instance of Impersonated Identity! Current identity: " + currentIdentity);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ImpersonatedIdentity impersonatedIdentity = (ImpersonatedIdentity)currentIdentity;

        log.debug("Cancel impersonation session. Impersonated user was: " + impersonatedIdentity.getUserId()
                + ", Admin user is: " + impersonatedIdentity.getParentConversationState().getIdentity().getUserId());

        // Restore old conversation state
        restoreConversationState(req, impersonatedIdentity);

        // Restore return URI from session
        String returnURI = getReturnURI(req);

        // Restore session attributes of root user
        restoreOldSessionAttributes(req);

        if (log.isTraceEnabled()) {
            log.trace("Impersonation finished. Redirecting to " + returnURI);
        }
        resp.sendRedirect(returnURI);
    }

    protected void restoreConversationState(HttpServletRequest req, ImpersonatedIdentity impersonatedIdentity) {
        ConversationState adminConvState = impersonatedIdentity.getParentConversationState();
        registerConversationState(req, adminConvState);

        // Possibly restore identity if it's not available anymore in IdentityRegistry. This could happen during parallel logout of admin user from another session
        IdentityRegistry identityRegistry = (IdentityRegistry)getContainer().getComponentInstanceOfType(IdentityRegistry.class);
        String adminUsername = adminConvState.getIdentity().getUserId();
        Identity adminIdentity = identityRegistry.getIdentity(adminUsername);
        if (adminIdentity == null) {
            log.debug("Restore of identity of user " + adminUsername + " in IdentityRegistry");
            adminIdentity = createIdentity(adminUsername);
            identityRegistry.register(adminIdentity);
        }
    }

    protected void restoreOldSessionAttributes(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            int prefixLength = BACKUP_ATTR_PREFIX.length();

            // Remove all session attributes of current (impersonated) user. Restore attributes of admin user
            Enumeration attrNames = session.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String)attrNames.nextElement();

                // Remove attribute of impersonated user
                if (!attrName.startsWith(BACKUP_ATTR_PREFIX)) {
                    session.removeAttribute(attrName);
                    if (log.isTraceEnabled()) {
                        log.trace("Removed attribute: " + attrName);
                    }
                } else {
                    // Restore attribute as it's one of the attributes of admin user, which was backup-ed
                    Object attrValue = session.getAttribute(attrName);

                    String restoredAttributeName = attrName.substring(prefixLength);
                    session.setAttribute(restoredAttributeName, attrValue);
                    session.removeAttribute(attrName);

                    if (log.isTraceEnabled()) {
                        log.trace("Finished restore of attribute: " + attrName);
                    }
                }
            }
        }
    }

    // Register given conversationState into ConversationRegistry. Key will be current Http session
    private void registerConversationState(HttpServletRequest req, ConversationState conversationState) {
        // Obtain stateKey of current HttpSession
        HttpSession httpSession = req.getSession();
        StateKey stateKey = new HttpSessionStateKey(httpSession);

        // Update conversationRegistry
        ConversationRegistry conversationRegistry = (ConversationRegistry)getContainer().getComponentInstanceOfType(ConversationRegistry.class);
        conversationRegistry.register(stateKey, conversationState);
    }

    private Identity createIdentity(String username) {
        Authenticator authenticator = (Authenticator) getContainer().getComponentInstanceOfType(Authenticator.class);
        try {
            return authenticator.createIdentity(username);
        } catch (Exception e) {
            log.error("New identity for user: " + username + " not created.", e);
            return null;
        }
    }

    private String getReturnURI(HttpServletRequest req) {
        String returnURI = null;
        HttpSession session = req.getSession(false);
        if (session != null) {
            returnURI = (String)session.getAttribute(ATTR_RETURN_IMPERSONATION_URI);
        }

        if (returnURI == null) {
            returnURI = req.getContextPath();
        }

        return returnURI;
    }
}

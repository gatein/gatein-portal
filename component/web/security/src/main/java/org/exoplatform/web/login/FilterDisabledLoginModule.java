/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.web.login;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.security.jaas.AbstractLoginModule;

public class FilterDisabledLoginModule extends AbstractLoginModule {

    private static final Log log = ExoLogger.getLogger(FilterDisabledLoginModule.class);

    /** JACC get context method. */
    private static Method getContextMethod;

    public static final String DISABLED_USER_NAME = "_disabledUserName";

    static {
        try {
            Class<?> policyContextClass = Thread.currentThread().getContextClassLoader()
                    .loadClass("javax.security.jacc.PolicyContext");
            getContextMethod = policyContextClass.getDeclaredMethod("getContext", String.class);
        } catch (ClassNotFoundException ignore) {
            log.debug("JACC not found ignoring it", ignore);
        } catch (Exception e) {
            log.error("Could not obtain JACC get context method", e);
        }
    }

    @Override
    public boolean login() throws LoginException {
        log.debug("In login of FilterDisabledLoginModule.");

        try {
            Callback[] callbacks = new Callback[] { new NameCallback("Username") };
            callbackHandler.handle(callbacks);

            String username = ((NameCallback) callbacks[0]).getName();
            if (username != null) {
                OrganizationService service = (OrganizationService) getContainer().getComponentInstanceOfType(
                        OrganizationService.class);

                UserHandler uHandler = service.getUserHandler();
                User user = uHandler.findUserByName(username, UserStatus.BOTH);

                if (user == null) {
                    log.debug("user {0} doesn't exists. FilterDisabledLoginModule will be ignored.", username);
                } else if (user instanceof UserImpl && !((UserImpl) user).isEnabled()) {
                    HttpServletRequest request = getCurrentHttpServletRequest();
                    if (request != null) {
                        request.setAttribute(DISABLED_USER_NAME, username);
                    }

                    throw new LoginException("Can't authenticate. user " + username + " is disabled");
                }
            } else {
                log.debug("No username has been committed. FilterDisabledLoginModule will be ignored.");
            }

            return true;
        } catch (final Exception e) {
            log.warn(e.getMessage());
            throw new LoginException(e.getMessage());
        }
    }

    protected HttpServletRequest getCurrentHttpServletRequest() {
        HttpServletRequest request = null;

        // JBoss way
        if (getContextMethod != null) {
            try {
                request = (HttpServletRequest) getContextMethod.invoke(null, "javax.servlet.http.HttpServletRequest");
            } catch (Throwable e) {
                log.error("LoginModule error. Turn off session credentials checking with proper configuration option of "
                        + "LoginModule set to false");
                log.error(this, e);
            }
        } else {
            // Tomcat way (Assumed that ServletAccessValve has been configured in context.xml)
            try {
                // TODO: improve this
                Class<?> clazz = Thread.currentThread().getContextClassLoader()
                        .loadClass("org.gatein.sso.agent.tomcat.ServletAccess");
                Method getRequestMethod = clazz.getDeclaredMethod("getRequest");
                request = (HttpServletRequest) getRequestMethod.invoke(null);
            } catch (Exception e) {
                log.error("Unexpected exception when trying to obtain HttpServletRequest from ServletAccess thread-local", e);
            }
        }

        log.trace("Returning HttpServletRequest {0}", request);
        return request;
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }

    @Override
    protected Log getLogger() {
        return log;
    }
}

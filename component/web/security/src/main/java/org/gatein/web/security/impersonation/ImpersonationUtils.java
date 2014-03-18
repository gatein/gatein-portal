/*
 * JBoss, a division of Red Hat
 * Copyright 2014, Red Hat Middleware, LLC, and individual
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ImpersonationUtils {

    /**
     * Create URL for redirection to impersonationServlet to start impersonation session
     *
     * @param portalContextPath context-path of portal (assumption is that ImpersonationServlet is available on this path too)
     * @param usernameToImpersonate username to impersonate
     * @param returnImpersonationUri URI, where should be request redirected from ImpersonationServlet after finish of Impersonation workflow
     * @return uri to send to impersonationServlet including all parameters
     */
    public static String createStartImpersonationURL(String portalContextPath, String usernameToImpersonate, String returnImpersonationUri) {
        String impersonationServletUri = portalContextPath + ImpersonationServlet.IMPERSONATE_URL_SUFIX;

        try {
            return new StringBuilder(impersonationServletUri)
                    .append("?")
                    .append(ImpersonationServlet.PARAM_ACTION)
                    .append("=")
                    .append(ImpersonationServlet.PARAM_ACTION_START_IMPERSONATION)
                    .append("&")
                    .append(ImpersonationServlet.PARAM_USERNAME)
                    .append("=")
                    .append(URLEncoder.encode(usernameToImpersonate, "UTF-8"))
                    .append("&")
                    .append(ImpersonationServlet.PARAM_RETURN_IMPERSONATION_URI)
                    .append("=")
                    .append(URLEncoder.encode(returnImpersonationUri, "UTF-8"))
                    .toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Create URL for redirection to impersonationServlet to stop impersonation session
     *
     * @param portalContextPath context-path of portal (assumption is that ImpersonationServlet is available on this path too)
     * @return uri to send to impersonationServlet including all parameters
     */
    public static String createExitImpersonationURL(String portalContextPath) {
        // Redirect to ImpersonationServlet and trigger stop of Impersonation session
        String impersonationServletUri = portalContextPath + ImpersonationServlet.IMPERSONATE_URL_SUFIX;

        return new StringBuilder(impersonationServletUri)
                .append("?")
                .append(ImpersonationServlet.PARAM_ACTION)
                .append("=")
                .append(ImpersonationServlet.PARAM_ACTION_STOP_IMPERSONATION)
                .toString();
    }
}

/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.web.redirect.api;

/**
 * There can be three different types of redirects:
 *
 * REDIRECT means we should perform the redirect
 *
 * NOREDIRECT means we should not perform the redirect
 *
 * NEEDDEVICEINFO means we could potentially perform a redirect if we had more information about the browser
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public enum RedirectType {
    /**
     * Perform the redirect
     */
    REDIRECT {
        @Override
        public String toString() {
            return "gtn.web.redirect";
        }
    },

    /**
     * Do not perform a redirect
     */
    NOREDIRECT {
        @Override
        public String toString() {
            return "gtn.web.no-redirect";
        }
    },

    /**
     * We could potential perform a redirect, but we need more information about the browser first
     */
    NEEDDEVICEINFO {
        @Override
        public String toString() {
            return "gtn.web.need-device-info";
        }
    };
}

/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.web.security.sso;

import org.exoplatform.container.xml.InitParams;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Helper for SSO related things
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SSOHelper {
    private final boolean ssoEnabled;
    private final boolean skipJSPRedirection;
    private final String ssoRedirectURLSuffix;

    private static final Logger log = LoggerFactory.getLogger(SSOHelper.class);

    public SSOHelper(InitParams params) {
        String ssoEnabledParam = params.getValueParam("isSSOEnabled").getValue();
        this.ssoEnabled = Boolean.parseBoolean(ssoEnabledParam);

        // Needs to be explicitly specified as "false", otherwise will have same value like ssoEnabled
        String ssoJSPRedirectionEnabledParam = params.getValueParam("skipJSPRedirection").getValue();
        if ("false".equals(ssoJSPRedirectionEnabledParam)) {
            this.skipJSPRedirection = false;
        } else {
            this.skipJSPRedirection = this.ssoEnabled;
        }

        this.ssoRedirectURLSuffix = params.getValueParam("SSORedirectURLSuffix").getValue();
        log.debug("SSOHelper initialized. ssoEnabled: " + ssoEnabled + ", skipJSPRedirection: " + skipJSPRedirection
                + ", ssoRedirectURLSuffix: " + ssoRedirectURLSuffix);
    }

    public boolean isSSOEnabled() {
        return ssoEnabled;
    }

    public boolean skipJSPRedirection() {
        return skipJSPRedirection;
    }

    public String getSSORedirectURLSuffix() {
        return ssoRedirectURLSuffix;
    }

}

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

package org.exoplatform.portal.webui.component;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.application.UIGroovyPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.security.sso.SSOHelper;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIBannerPortlet extends UIGroovyPortlet {

    private final SSOHelper ssoHelper;

    public UIBannerPortlet() throws Exception {
        ssoHelper = getApplicationComponent(SSOHelper.class);
    }

    public String renderLoginLink(String signInAction, String signInLocalizedText) {
        // If SSO is enabled, we need to redirect to "/portal/sso" instead of showing login window
        if (ssoHelper.isSSOEnabled()) {
            PortalRequestContext pContext = Util.getPortalRequestContext();
            String ssoRedirectURL = pContext.getRequest().getContextPath() + ssoHelper.getSSORedirectURLSuffix();
            return "<a class=\"Login\" href=\"" + ssoRedirectURL + "\">" + signInLocalizedText + "</a>";
        } else {
            return "<a class=\"Login\" onclick=\"" + signInAction + "\">" + signInLocalizedText + "</a>";
        }
    }
}

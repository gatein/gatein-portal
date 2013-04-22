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

package org.exoplatform.portal.application.oauth;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.gatein.api.oauth.OAuthProvider;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.gatein.api.oauth.BasicOAuthProviderImpl;
import org.gatein.api.oauth.exception.OAuthApiException;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.SocialNetworkService;

/**
 * Extended {@link BasicOAuthProviderImpl} with access to WebUI, so it has support for all {@link OAuthProvider} methods
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthProviderImpl extends BasicOAuthProviderImpl {

    public OAuthProviderImpl(OAuthProviderType internalOAuthProvider, SocialNetworkService socialNetworkService) {
        super(internalOAuthProvider, socialNetworkService);
    }

    @Override
    public void startOAuthWorkflow(String neededCustomScope) throws OAuthApiException, IOException {
        PortalRequestContext prc = Util.getPortalRequestContext();

        // Save session attribute with URL to redirect. It will be used by GateIn to return to current page after finish OAuth flow
        HttpSession session = prc.getRequest().getSession();
        session.setAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT, prc.getRequestURI());

        // Redirect to start OAuth2 flow
        String reqContextPath = prc.getRequestContextPath();
        OAuthProviderType<?> oauthProviderType = getInternalOAuthProvider();
        String initOauthFlowURL = oauthProviderType.getInitOAuthURL(reqContextPath);

        // Attach custom scope
        if (neededCustomScope != null) {
            initOauthFlowURL = initOauthFlowURL + "&" + OAuthConstants.PARAM_CUSTOM_SCOPE + "=" + neededCustomScope;
        }

        prc.getResponse().sendRedirect(initOauthFlowURL);
    }
}

/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.ui.register;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import juzu.request.RequestContext;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

public class OauthProviderHelper {
    @Inject
    OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

    public List<OauthProviderDescriptor> getOauthProviderDescriptors(String contextPath) {
        List<OauthProviderDescriptor> oauthProviders = null;
        if (oAuthProviderTypeRegistry.isOAuthEnabled()) {
            oauthProviders = new LinkedList<OauthProviderDescriptor>();
            for (OAuthProviderType provider : oAuthProviderTypeRegistry.getEnabledOAuthProviders()) {
                String type = "twitter";
                if (provider.getKey().equals("GOOGLE")) {
                    type = "google-plus";
                } else if (provider.getKey().equals("FACEBOOK")) {
                    type = "facebook";
                }
                oauthProviders.add(new OauthProviderDescriptor(provider.getFriendlyName(), provider.getInitOAuthURL(contextPath), type, provider.getKey()));
            }
        }
        return oauthProviders;
    }
}

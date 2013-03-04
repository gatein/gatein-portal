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

package org.gatein.security.oauth.utils;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.security.oauth.generic.OAuthPrincipal;
import org.gatein.security.oauth.generic.OAuthProviderType;
import org.picketlink.social.standalone.fb.FacebookPrincipal;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthUtils {

    public static OAuthPrincipal convertFacebookPrincipalToOAuthPrincipal(FacebookPrincipal facebookPrincipal) {
        return new OAuthPrincipal(facebookPrincipal.getUsername(), facebookPrincipal.getFirstName(), facebookPrincipal.getLastName(),
                facebookPrincipal.getAttribute("name"), facebookPrincipal.getEmail(), facebookPrincipal.getAccessToken(), OAuthProviderType.FACEBOOK);
    }

    public static User convertOAuthPrincipalToGateInUser(OAuthPrincipal principal) {
        User gateinUser = new UserImpl(principal.getUserName());
        gateinUser.setFirstName(principal.getFirstName());
        gateinUser.setLastName(principal.getLastName());
        gateinUser.setEmail(principal.getEmail());
        gateinUser.setDisplayName(principal.getDisplayName());
        return gateinUser;
    }
}

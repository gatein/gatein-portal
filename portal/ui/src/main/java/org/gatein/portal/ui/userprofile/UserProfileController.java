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
package org.gatein.portal.ui.userprofile;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.request.RequestContext;
import juzu.request.SecurityContext;
import juzu.template.Template;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.common.kernel.ThreadContext;
import org.gatein.portal.ui.register.OauthProviderDescriptor;
import org.gatein.portal.ui.register.OauthProviderHelper;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.spi.AccessTokenContext;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;
import org.gatein.security.oauth.spi.SocialNetworkService;

public class UserProfileController {

    @Inject
    private OauthProviderHelper oauthProviderHelper;

    @Inject
    private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

    @Inject
    private SocialNetworkService socialNetworkService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private SSOHelper ssoHelper;

    @Inject
    private Message message;

    @Inject
    @Path("index.gtmpl")
    Template index;

    @View
    public Response.Content index(RequestContext requestContext) {
        String userName = null;
        SecurityContext securityContext = requestContext.getSecurityContext();
        if(securityContext != null) {
            userName = securityContext.getRemoteUser();
        }

        if(userName == null) {
            String contextPath = requestContext.getHttpContext().getContextPath();
            String loginURL = contextPath + (ssoHelper.isSSOEnabled() ? ssoHelper.getSSORedirectURLSuffix() : "") + "/dologin";
            return Response.ok("Need login: <a href='" + loginURL + "'>" + loginURL + "</a>");
        }

        UserProfile userProfile = null;
        try {
            userProfile = organizationService.getUserProfileHandler().findUserProfileByName(userName);
        } catch (Exception ex) {
        } finally {
            if(userProfile == null) {
                userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(userName);
            }
        }

        List<OauthProviderDescriptor> oauthProviders = oauthProviderHelper.getOauthProviderDescriptors(requestContext.getHttpContext().getContextPath());
        if(oauthProviders == null) {
            oauthProviders = Collections.emptyList();
        }
        for(OauthProviderDescriptor oauth : oauthProviders) {
            String key = "user.social-info." + oauth.getType() + ".userName";
            String socialAccount = userProfile.getAttribute(key);
            if(socialAccount != null && !socialAccount.isEmpty()) {
                oauth.setSocialAccount(socialAccount);
            }
        }

        //Process message
        HttpServletRequest request = ThreadContext.getCurentHttpServletRequest();
        if(request != null) {
            HttpSession session = request.getSession(false);
            if(session != null) {
                String oauthLinkedName = (String)session.getAttribute(OAuthConstants.ATTRIBUTE_LINKED_OAUTH_PROVIDER);
                if(oauthLinkedName != null && !oauthLinkedName.isEmpty()) {
                    message.setType("success");
                    message.setMessage("Connect to " + oauthLinkedName + " successful");

                    session.removeAttribute(OAuthConstants.ATTRIBUTE_LINKED_OAUTH_PROVIDER);
                } else {
                    OAuthException exception = (OAuthException)session.getAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK);
                    if(exception != null) {
                        session.removeAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK);
                        if(exception.getExceptionCode() == OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME) {
                            message.setType("error");
                            message.setMessage("user with this oauth-username already exists");
                        }
                    }
                }
            }
        }


        return index.with().set("oauthProviders", oauthProviders).ok();
    }

    @Action
    public Response removeOauthLink(String oauthProviderKey, RequestContext requestContext) throws Exception {
        String username = null;
        if(requestContext.getSecurityContext() != null) {
            username = requestContext.getSecurityContext().getRemoteUser();
        }
        if(username == null) {
            message.setType("error");
            message.setMessage("User is not login.");
            return UserProfileController_.index();
        }

        UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
        if(userProfile == null) {
            message.setType("error");
            message.setMessage("Can not find userProfile");
            return UserProfileController_.index();
        }

        OAuthProviderType<AccessTokenContext> oauthProviderTypeToUnlink = oAuthProviderTypeRegistry.getOAuthProvider(oauthProviderKey, AccessTokenContext.class);
        AccessTokenContext accessToken = socialNetworkService.getOAuthAccessToken(oauthProviderTypeToUnlink, username);
        if (oauthProviderTypeToUnlink != null) {
            userProfile.setAttribute(oauthProviderTypeToUnlink.getUserNameAttrName(), null);
            organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);
            if (accessToken != null) {
                oauthProviderTypeToUnlink.getOauthProviderProcessor().revokeToken(accessToken);
            }

            message.setType("success");
            message.setMessage("Unlink successful");
        } else {
            message.setType("error");
            message.setMessage("Not successful");
        }

        return UserProfileController_.index();
    }
}

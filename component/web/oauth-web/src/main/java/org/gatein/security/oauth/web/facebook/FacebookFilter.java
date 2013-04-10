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

package org.gatein.security.oauth.web.facebook;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.common.OAuthPrincipal;
import org.gatein.security.oauth.data.SocialNetworkService;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.facebook.FacebookInteractionState;
import org.gatein.security.oauth.facebook.GateInFacebookProcessor;
import org.gatein.security.oauth.registry.OAuthProviderTypeRegistry;
import org.gatein.security.oauth.social.FacebookPrincipal;
import org.gatein.security.oauth.social.FacebookProcessor;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

/**
 * Filter for integration with authentication handhsake via Facebook with usage of OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookFilter extends AbstractSSOInterceptor {

    private static Logger log = LoggerFactory.getLogger(FacebookFilter.class);

    private AuthenticationRegistry authenticationRegistry;
    private GateInFacebookProcessor facebookProcessor;
    private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;
    private SocialNetworkService socialNetworkService;

    @Override
    protected void initImpl() {
        authenticationRegistry = (AuthenticationRegistry)getExoContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
        facebookProcessor = (GateInFacebookProcessor)getExoContainer().getComponentInstanceOfType(GateInFacebookProcessor.class);
        oAuthProviderTypeRegistry = (OAuthProviderTypeRegistry)getExoContainer().getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
        socialNetworkService = (SocialNetworkService)getExoContainer().getComponentInstanceOfType(SocialNetworkService.class);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        // Restart current state if 'oauthInteraction' param has value 'start'
        String interaction = httpRequest.getParameter(OAuthConstants.PARAM_OAUTH_INTERACTION);
        if (OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START.equals(interaction)) {
            httpRequest.getSession().removeAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);
        }

        // Possibility to init interaction with custom scope. It's needed when custom portlets want bigger scope then the one available in configuration
        String scopeToUse = obtainCustomScopeIfAvailable(httpRequest);

        FacebookInteractionState interactionState;

        try {
            if (scopeToUse == null) {
                interactionState = facebookProcessor.processFacebookAuthInteraction(httpRequest, httpResponse);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Process facebook oauth interaction with scope: " + scopeToUse);
                }
                interactionState = facebookProcessor.processFacebookAuthInteraction(httpRequest, httpResponse, scopeToUse);
            }
        } catch (OAuthException ex) {
            log.warn("Error during Facebook OAuth flow: " + ex.getMessage());

            // Save exception to request, it will be processed later on portal side
            httpRequest.setAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_OAUTH, ex);
            chain.doFilter(request, response);
            return;
        }

        if (FacebookProcessor.STATES.FINISH.name().equals(interactionState.getState())) {
            FacebookPrincipal principal = interactionState.getFacebookPrincipal();

            if (principal == null) {
                log.error("Principal was null. Maybe login modules need to be configured properly.");
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Finished Facebook OAuth2 flow with state: " + interactionState);
                    log.trace("Facebook accessToken: " + principal.getAccessToken());
                }

                OAuthPrincipal<FacebookAccessTokenContext> oauthPrincipal = OAuthUtils.convertFacebookPrincipalToOAuthPrincipal(principal, oAuthProviderTypeRegistry, interactionState.getScope());

                // Remove attribute with state of facebookLogin
                httpRequest.getSession().removeAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);

                if (httpRequest.getRemoteUser() == null) {
                    // Save authenticated OAuthPrincipal to authenticationRegistry in case that we are anonymous user
                    // Other filter should take care of processing it and perform GateIn login or registration
                    authenticationRegistry.setAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL, oauthPrincipal);
                } else {
                    // For authenticated user, we will save it as request attribute and process it by other filter, which will update
                    // userProfile with new username and accessToken
                    httpRequest.setAttribute(OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL, oauthPrincipal);
                }

                // Continue with request
                chain.doFilter(request, response);
            }
        }
    }



    protected String obtainCustomScopeIfAvailable(HttpServletRequest httpRequest) {
        // It's sufficient to use request parameter, because scope is needed only for facebookProcessor.initialInteraction
        String customScope = httpRequest.getParameter(OAuthConstants.PARAM_CUSTOM_SCOPE);
        if (customScope != null) {
            String currentUser = httpRequest.getRemoteUser();
            if (currentUser == null) {
                log.warn("Parameter " + OAuthConstants.PARAM_CUSTOM_SCOPE + " found but there is no user available. Ignoring it.");
                return null;
            } else {
                FacebookAccessTokenContext currentAccessToken = (FacebookAccessTokenContext)socialNetworkService.getOAuthAccessToken(
                        oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_FACEBOOK), currentUser);

                if (currentAccessToken != null) {
                    // Add new customScope to set of existing scopes, so accessToken will be obtained for all of them
                    currentAccessToken.addScope(customScope);
                    return currentAccessToken.getScopesAsString();
                } else {
                    return customScope;
                }
            }
        } else {
            return null;
        }
    }
}

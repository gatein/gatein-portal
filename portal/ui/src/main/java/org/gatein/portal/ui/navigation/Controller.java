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
package org.gatein.portal.ui.navigation;

import javax.inject.Inject;

import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.request.RequestContext;
import juzu.request.UserContext;
import juzu.template.Template;
import org.exoplatform.web.security.sso.SSOHelper;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteKey;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    NavigationService navigationService;

    @Inject
    DescriptionService descriptionService;

    @Inject
    SSOHelper ssoHelper;

    @Inject
    @Path("index.gtmpl")
    Template index;

    @View
    public Response.Content index(UserContext userContext, RequestContext requestContext) {

        //
        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));

        //
        UserNode.Model model = new UserNode.Model(descriptionService, userContext.getLocale());

        //
        NodeContext<UserNode, NodeState> root = navigationService.loadNode(model, navigation, Scope.CHILDREN, null);

        String username = requestContext.getSecurityContext().getRemoteUser();
        if(username == null) username = "";

        //Init Login and Logout URL
        //TODO: how to init portalURL for login and logout (it's not portletURL)
        //TODO: initURL = ??
        String contextPath = requestContext.getHttpContext().getContextPath();
        String loginURL = new StringBuilder(contextPath)
                                .append(ssoHelper.isSSOEnabled() ? ssoHelper.getSSORedirectURLSuffix() : "")
                                .append("/dologin").toString();
        String logoutURL = new StringBuilder(contextPath)
                                .append("/dologout")
                                .append(ssoHelper.isSSOEnabled() ? "?portal:action=Logout" : "").toString();

        return index.with()
                .set("root", root.getNode())
                .set("username", username)
                .set("loginURL", loginURL)
                .set("logoutURL", logoutURL)
                .ok();
    }

}

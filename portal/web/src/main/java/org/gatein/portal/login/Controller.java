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

package org.gatein.portal.login;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.request.RequestContext;
import juzu.template.Template;
import org.exoplatform.container.PortalContainer;
import org.gatein.portal.mop.PropertyType;
import org.gatein.portal.servlet.Context;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.security.Credentials;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

public class Controller {
  /**
   * .
   */
  private static final int AUTHENTICATED = 1;

  /**
   * .
   */
  private static final int FAILED = 2;

  @Inject
  private Flash flash;

  @Inject
  @Path("login.gtmpl")
  private Template login;

  @View
  @Route(value = "/dologout", priority = 1)
  public Response doLogout() {
    try {
      ServletContainer container = ServletContainerFactory.getServletContainer();
      container.logout(Context.getCurrentRequest(), Context.getCurrentResponse());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    HttpServletRequest req = Context.getCurrentRequest();
    String initURL = req.getParameter("initURL");
    if (initURL == null || initURL.isEmpty()) {
      initURL = req.getContextPath();
    }
    return Response.redirect(initURL);
  }

  @View
  @Route(value = "/dologin", priority = 1)
  public Response doLogin() {
    try {
      HttpServletRequest req = Context.getCurrentRequest();
      int status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

      String initURL = req.getParameter("initURL");
      if (initURL == null || initURL.isEmpty()) {
        initURL = req.getContextPath();
      }

      if (status == AUTHENTICATED) {
        return Response.redirect(initURL);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    String loginURL = Context.getCurrentRequest().getContextPath() + "/login";
    return Response.redirect(loginURL);
  }

  @Action
  @Route(value = "/actionLogin", priority = 1)
  public Response actionLogin(String username, String password) {
    try {
      Credentials credentials = new Credentials(username, password);
      ServletContainer container = ServletContainerFactory.getServletContainer();
      container.login(Context.getCurrentRequest(), Context.getCurrentResponse(), credentials);
      return Controller_.doLogin();
    } catch (ServletException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AuthenticationException ex) {
      ex.printStackTrace();
    }
    flash.setError("Username or password incorrect!");
    return Controller_.login();
  }

  @View
  @Route(value = "/login", priority = 1)
  public Response login(RequestContext context) {
    ServletContext servletContext = null;
    String contextPath = "";
    try {
      HttpServletRequest req = Context.getCurrentRequest();
      servletContext = req.getServletContext();
      contextPath = req.getContextPath();

      int status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

      String initURL = req.getParameter("initURL");
      if (initURL == null || initURL.isEmpty()) {
        initURL = req.getContextPath();
      }

      String username = req.getParameter("username");
      String password = req.getParameter("password");

      if (status == AUTHENTICATED) {
        return Response.redirect(initURL);

      } else if (username != null && password != null) {
        Credentials credentials = new Credentials(username, password);
        ServletContainer container = ServletContainerFactory.getServletContainer();
        container.login(Context.getCurrentRequest(), Context.getCurrentResponse(), credentials);
        return Controller_.doLogin();
      }
    } catch (Exception ex) {
      servletContext = null;
      contextPath = context.getHttpContext().getContextPath();
    }

    PortalContainer container = null;
    if (servletContext == null) {
      container = PortalContainer.getInstance();
    } else {
      container = PortalContainer.getCurrentInstance(servletContext);
    }
    OAuthProviderTypeRegistry registry = (OAuthProviderTypeRegistry) container.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);

    List<OauthProviderDescriptor> oauthProviders = null;
    if (registry.isOAuthEnabled()) {
      oauthProviders = new LinkedList<OauthProviderDescriptor>();
      for (OAuthProviderType provider : registry.getEnabledOAuthProviders()) {
        String type = "twitter";
        if (provider.getKey().equals("GOOGLE")) {
          type = "google-plus";
        } else if (provider.getKey().equals("FACEBOOK")) {
          type = "facebook";
        }
        oauthProviders.add(new OauthProviderDescriptor(provider.getFriendlyName(), provider.getInitOAuthURL(contextPath), type));
      }
    }

    return login.with().set("oauthProviders", oauthProviders).ok().with(juzu.PropertyType.STYLESHEET, "login-stylesheet").with(juzu.PropertyType.STYLESHEET, "social-buttons");
  }
}

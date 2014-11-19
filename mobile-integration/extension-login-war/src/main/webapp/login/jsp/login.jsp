<%--
  JBoss, Home of Professional Open Source.
  Copyright 2012, Red Hat, Inc., and individual contributors
  as indicated by the @author tags. See the copyright.txt file in the
  distribution for a full listing of individual contributors.
  
  This is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2.1 of
  the License, or (at your option) any later version.
  
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this software; if not, write to the Free
  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
--%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="javax.servlet.http.Cookie"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.gatein.security.oauth.spi.OAuthProviderType"%>
<%@ page import="org.gatein.security.oauth.spi.OAuthProviderTypeRegistry"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.gatein.common.text.EntityEncoder"%>
<%@ page language="java"%>
<%
  String contextPath = request.getContextPath() ;

  String username = request.getParameter("username");
  if(username == null) {
    username = "";
  } else {
    EntityEncoder encoder = EntityEncoder.FULL;
    username = encoder.encode(username);
  }

  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  ResourceBundleService service = (ResourceBundleService) portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;
  
  OAuthProviderTypeRegistry registry = (OAuthProviderTypeRegistry) portalContainer.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);

  Cookie cookie = new Cookie(org.exoplatform.web.login.LoginServlet.COOKIE_NAME, "");
    cookie.setPath(request.getContextPath());
    cookie.setMaxAge(0);
    response.addCookie(cookie);

  //
  String uri = (String)request.getAttribute("org.gatein.portal.login.initial_uri");
  boolean error = request.getAttribute("org.gatein.portal.login.error") != null;
  
  response.setCharacterEncoding("UTF-8"); 
  response.setContentType("text/html; charset=UTF-8");
%>
<!DOCTYPE html 
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title><%=res.getString("UILoginForm.label.Signin")%></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>        
        <meta name="description" content="Login screen of the Portal"/>
        <meta name="viewport" content="initial-scale=1, maximum-scale=1"/>
        <link rel="stylesheet" type="text/css" href="/gatein-mobile-login/login/css/default.css"/>
        <link rel="stylesheet" type="text/css" href="/gatein-mobile-login/login/css/enchanced.css"/>        
        <!--[if lte IE 8]>
        <link rel="stylesheet" type="text/css" href="/gatein-mobile-login/login/css/ie8.css"/>
        <![endif]-->
        <script type="text/javascript" src="/gatein-mobile-login/login/js/login.js"></script>
    </head>
    <body>
        <h1><a href="#login-form" title="Gate In"><%=res.getString("UILoginForm.label.mobile.Login")%></a></h1>
        <%/*Begin form*/%>
        <% if (error) {%>
        <div id="error-pane"><p>
        <span><%=res.getString("UILoginForm.label.SigninFail")%></span>
        <button id="button-close-alert" type="button"><%=res.getString("UILoginForm.label.mobile.login.Close")%></button></p></div>
        <% }%>
        <form id="login-form" action="<%= contextPath + "/login"%>" method="post">
            <fieldset>
                <legend><%=res.getString("UILoginForm.label.Signin")%></legend>
                <label for="username"><%=res.getString("UILoginForm.label.UserName")%></label>
                <input type="text" id="username" name="username" value=""/>

                <label for="password"><%=res.getString("UILoginForm.label.password")%></label>
                <input type="password" id="password" name="password" value=""/>

                <label for="rememberme"><%=res.getString("UILoginForm.label.RememberOnComputer")%></label>
                <input type="checkbox" id="rememberme" name="rememberme" value="true"/>

                <input type="submit" name="signIn" value="<%=res.getString("UILoginForm.label.Signin")%>"/>               
                <% 
                  if (uri != null) { 
                    uri = EntityEncoder.FULL.encode(uri);
                %>
                <input type="hidden" name="initialURI" value="<%=uri%>"/>
                <%
                  }
                %>
            </fieldset>
          <% if(registry.isOAuthEnabled()) { %>
          <script type="text/javascript">
              function goSocialLoginUrl(url) {
                  if(document.getElementById('rememberme').checked) {
                      url += '&_rememberme=true';
                  }
                  window.location = url;
                  return false;
              }
          </script>
          <div id="social-pane">
          <div class="SignInDelimiter">
            <span><%=res.getString("UILoginForm.label.mobile.login.oauth.Delimiter")%></span>
          </div>
          <div id="social-login">
            <% for (OAuthProviderType oauthProvType : registry.getEnabledOAuthProviders()) { %>
            <a href="javascript:void(0)" onclick="goSocialLoginUrl('<%= oauthProvType.getInitOAuthURL(contextPath, uri) %>')" id="login-<%= oauthProvType.getKey() %>" class="login-button">
              <div><%= oauthProvType.getFriendlyName() %></div>
            </a>
            <% } %>
          </div>
          </div>
          <% } %>
        </form>
        <div id="footer">
            <p>
                <%=res.getString("UILoginForm.label.mobile.copyright.Intro")%>
                <a href="http://www.redhat.com/"><%=res.getString("UILoginForm.label.mobile.copyright.RH")%></a>
                <%=res.getString("UILoginForm.label.mobile.copyright.And")%>
                <a href="http://www.exoplatform.com"><%=res.getString("UILoginForm.label.mobile.copyright.Exo")%></a>
            </p>
        </div>
    </body>     
</html>
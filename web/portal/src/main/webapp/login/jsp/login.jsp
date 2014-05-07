<%--

    Copyright (C) 2009 eXo Platform SAS.
    
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
<%@ page import="org.exoplatform.web.login.LoginError"%>
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

  String uri = (String)request.getAttribute("org.gatein.portal.login.initial_uri");
  boolean error = request.getAttribute("org.gatein.portal.login.error") != null;
  String errorParam = (String)request.getParameter(org.exoplatform.web.login.LoginError.ERROR_PARAM);
  LoginError errorData = null;
  if (errorParam != null) {
      errorData = LoginError.parse(errorParam);
  }

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
    <link rel="shortcut icon" type="image/x-icon" href="<%=contextPath%>/favicon.ico"/>
    <link rel="stylesheet" type="text/css"
          href="<%=contextPath%>/../eXoResources/skin/DefaultSkin/portal/webui/component/widget/UILoginForm/Stylesheet.css"/>
    <link rel="stylesheet" type="text/css"
          href="<%=contextPath%>/../eXoResources/skin/DefaultSkin/portal/webui/component/UIPortalApplicationSkin.css"/>
    <link rel="stylesheet" type="text/css"
          href="<%=contextPath%>/../eXoResources/skin/DefaultSkin/webui/component/UIBarDecorator/UIAction/Stylesheet.css"/>
    <link rel="stylesheet" type="text/css"
          href="<%=contextPath%>/../eXoResources/skin/DefaultSkin/webui/component/UIForms/UIForm/Stylesheet.css"/>
    <link rel="stylesheet" type="text/css" href="<%=contextPath%>/login/skin/Stylesheet.css"/>
  </head>
  <body>

    <div class="UILoginForm">
      <%
      	if (errorData != null) {
      	  if (org.exoplatform.web.login.LoginError.DISABLED_USER_ERROR == errorData.getCode()) {
              %>
              <span id="login-error"><%=errorData.getData()%> <%=res.getString("UILoginForm.label.DisabledUserSignin")%></span>        
              <%
           }
			} else if (error) {%>      		      
      <span id="login-error"><%=res.getString("UILoginForm.label.SigninFail")%></span>
      <% }  %>
      <div class="LoginDecorator">
        <div class="TopLeftLoginDecorator">
          <div class="TopRightLoginDecorator">
            <div class="TopCenterLoginDecorator">
              <div class="SigninTitle">Sign In</div>
            </div>
          </div>
        </div>
        <div class="MiddleLeftLoginDecorator">
          <div class="MiddleRightLoginDecorator">
            <div class="LoginDecoratorBackground">
              <div class="LoginDetailBox">

                <form class="UIForm" id="$uicomponent.id" name="loginForm" action="<%= contextPath + "/login"%>" method="post">
                  <input type="hidden" name="initialURI" value=""/>

                  <div class="VerticalLayout">
                    <table class="UIFormGrid" summary="Login form">
                      <tr class="UserNameField">
                        <td class="FieldLabel" scope="row"><label for="username"><%=res.getString("UILoginForm.label.UserName")%></label>
                        </td>
                        <td><input class="UserName" name="username" value="<%=username%>" id="username"/></td>
                      </tr>
                      <tr class="PasswordField" id="UIPortalLoginFormControl"
                      ">
                      <td class="FieldLabel" scope="row"><label for="password"><%=res.getString("UILoginForm.label.password")%></label>
                      </td>
                      <td><input class="Password" type="password" name="password" id="password" value=""/></td>
                      </tr>
                      <tr class="RememberField">
                        <td class="FieldLabel"><input type="checkbox" class="checkbox" value="true" name="rememberme" id="rememberme"/></td>
                        <td scope="row"><label for="rememberme"><%=res.getString("UILoginForm.label.RememberOnComputer")%></label>
                        </td>
                      </tr>
                    </table>
                    <div class="UIAction">
                      <input type="submit" name="signIn" class="ActionButton SimpleStyle"
                             value="<%=res.getString("UILoginForm.label.Signin")%>"/>
                      <% if (uri != null) {
                        uri = EntityEncoder.FULL.encode(uri);
                      %>
                      <input type="hidden" name="initialURI" value="<%=uri%>"/>
                      <% } %>
                    </div>
                  </div>
                </form>
              </div>
              <%
                if (registry.isOAuthEnabled()) {
              %>
              <script type="text/javascript">
                  function goSocialLoginUrl(url) {
                      if(document.getElementById('rememberme').checked) {
                          url += '&_rememberme=true';
                      }
                      window.location = url;
                      return false;
                  }
              </script>
              <div class="LoginDelimiter">
                <span><%= res.getString("UILoginForm.label.Delimiter")%></span>
              </div>
              <div class='SocialLoginButtons'>
                <% for (OAuthProviderType oauthProvType : registry.getEnabledOAuthProviders()) { %>
                <a href="javascript:void(0)" onclick="goSocialLoginUrl('<%= oauthProvType.getInitOAuthURL(contextPath, uri) %>')" id="login-<%= oauthProvType.getKey() %>"
                   class="login-button">
                  <div><%= oauthProvType.getFriendlyName() %>
                  </div>
                </a>
                <% } %>
              </div>
              <% } %>
            </div>
          </div>
        </div>
        <div class="BottomLeftLoginDecorator">
          <div class="BottomRightLoginDecorator">
            <div class="BottomCenterLoginDecorator"><span></span></div>
          </div>
        </div>
      </div>
    </div>
    <p id="login-footer"><%=res.getString("UILoginForm.label.Copyright")%></p>
  </body>
</html>

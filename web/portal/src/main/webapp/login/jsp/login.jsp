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
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
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

  ResourceBundleService service = (ResourceBundleService) PortalContainer.getCurrentInstance(session.getServletContext())
                                                        .getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;
  
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
    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />
    <link rel="stylesheet" type="text/css" href="<%=contextPath%>/login/skin/Stylesheet.css"/>
  </head>
  <body style="text-align: center; background: #b5b6b6; font-family: arial, tahoma, verdana">
    <div class="UILogin">
      <div class="LoginHeader"></div>
      <div class="LoginContent">
        <div class="CenterLoginContent">
          <%/*Begin form*/%>
          <% if(error) { %>
          <span style="color: #ff0000"><%=res.getString("UILoginForm.label.SigninFail")%></span>
          <% } %>
          <form class="ClearFix" id="loginForm" action="<%= contextPath + "/login"%>" method="post" style="margin: 0px;">
                <table> 
                  <tr class="FieldContainer">
                      <td class="FieldLabel"><%=res.getString("UILoginForm.label.UserName")%></td>
                      <td><input class="UserName" name="username" value="<%=username%>"/></td>
                      </tr>
                    <tr class="FieldContainer" id="UIPortalLoginFormControl">
                      <td class="FieldLabel"><%=res.getString("UILoginForm.label.password")%></td>
                      <td><input class="Password" type="password" name="password" value=""/></td>
                    </tr>
                    <tr class="FieldContainer">
                      <td class="FieldLabel"><input type="checkbox" name="rememberme" value="true"/></td>
                      <td><%=res.getString("UILoginForm.label.RememberOnComputer")%></td>
                    </tr>
                  </table>
                  <div class="LoginButton">
                    <table class="LoginButtonContainer">
                        <tr>
                          <td class="Button">
                            <input type="submit" name="signIn" value="<%=res.getString("UILoginForm.label.Signin")%>"/>
                            <% if (uri != null) { 
                               uri = EntityEncoder.FULL.encode(uri);
                            %>
                            <input type="hidden" name="initialURI" value="<%=uri%>"/>
                            <% } %>
                          </td>
                         </tr>
                    </table>
                  </div>
                </form>
                <%/*End form*/%>
        </div>
      </div>
    </div>
    <p style="font-size: 11px; color: #3f3f3f; text-align: center"><%=res.getString("UILoginForm.label.Copyright")%></p>
  </body>
</html>

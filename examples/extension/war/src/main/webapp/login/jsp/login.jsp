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
<%@ page import="org.exoplatform.web.login.InitiateLoginServlet"%>
<%@ page import="org.gatein.common.text.EntityEncoder"%>
<%@ page language="java" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%
  String contextPath = request.getContextPath() ;

  String username = (String)request.getParameter("j_username");
  if(username == null) username = "";
 	String password = (String)request.getParameter("j_password");
 	if(password == null) password = "";

 PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());	
  ResourceBundleService service = (ResourceBundleService) portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;
  
  String uri = (String)request.getAttribute("org.gatein.portal.login.initial_uri");

  Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
	cookie.setPath(request.getContextPath());
	cookie.setMaxAge(0);
	response.addCookie(cookie);
%>
<!DOCTYPE html 
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
           "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title><%=res.getString("UILoginForm.label.Signin")%></title>
    <link rel="shortcut icon" type="image/x-icon"  href="/<%=portalContainer.getName()%>/favicon.ico" />
    <link rel='stylesheet' type='text/css' href='/<%=portalContainer.getName()%>/login/skin/Stylesheet.css'/>
    <script type="text/javascript" src="/eXoResources/javascript/eXo.js"></script>
    <script type="text/javascript" src="/eXoResources/javascript/eXo/portal/UIPortalControl.js"></script>
  </head>
  <body style="text-align: center; background: #f5f5f5; font-family: arial, tahoma, verdana">
    <div class="UILogin">
      <div class="LoginHeader"><%=res.getString("UILoginForm.label.Signin")%></div>
      <div class="LoginContent">
        <div class="WelcomeText"><%=res.getString("UILoginForm.label.welcome")%></div>
        <div class="CenterLoginContent">
          <%/*Begin form*/%>
          <%
            if(username.length() > 0 || password.length() > 0) {
               EntityEncoder encoder = EntityEncoder.FULL;
               username = encoder.encode(username);
          %>
            <font color="red"><%=res.getString("UILoginForm.label.SigninFail")%></font><%}%>
          <form name="loginForm" action="<%= contextPath + "/login"%>" method="post" style="margin: 0px;">    
                <% if (uri != null) { %>
                <input type="hidden" name="initialURI" value="<%=uri%>"/>
                <% } %>
          		<table>
	              <tr class="FieldContainer">
		              <td class="FieldLabel"><%=res.getString("UILoginForm.label.UserName")%></td>
		              <td><input class="UserName" name="username" value="<%=username%>"/></td>
			          </tr>
		            <tr class="FieldContainer" id="UIPortalLoginFormControl" onkeypress="eXo.portal.UIPortalControl.onEnterPress(event);">
		              <td class="FieldLabel"><%=res.getString("UILoginForm.label.password")%></td>
		              <td><input class="Password" type="password" name="password" value=""/></td>
		            </tr>
		            <tr class="FieldContainer">
		              <td class="FieldLabel"><input type="checkbox" name="rememberme" value="true"/></td>
		              <td><%=res.getString("UILoginForm.label.RememberOnComputer")%></td>
		            </tr>
		          </table>
		          <div id="UIPortalLoginFormAction" class="LoginButton" onclick="login();">
		            <div class="LoginButtonContainer">
		              <div class="Button">
		                <div class="LeftButton">
		                  <div class="RightButton">
		                    <div class="MiddleButton">
		                    	<a href="#"><%=res.getString("UILoginForm.label.Signin")%></a>
		                    </div>
		                  </div>
		                </div>
		              </div>
		            </div>
		          </div>
		          <div class="ClearLeft"><span></span></div>
		          <script type='text/javascript'>			            
              function login() {
                document.loginForm.submit();                   
              }
            </script>
		        </form>
		        <%/*End form*/%>
        </div>
      </div>
    </div>
    <span style="margin: 10px 0px 0px 5px; font-size: 11px; color: #6f6f6f; text-align: center"><%=res.getString("UILoginForm.label.Copyright")%></span>
  </body>
</html>

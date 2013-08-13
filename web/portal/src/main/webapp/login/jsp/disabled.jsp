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
<%@ page import="org.gatein.security.oauth.spi.OAuthProviderType"%>
<%@ page import="org.gatein.security.oauth.spi.OAuthProviderTypeRegistry"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.gatein.common.text.EntityEncoder"%>
<%@ page language="java"%>
<%
  String contextPath = request.getContextPath() ;



  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  ResourceBundleService service = (ResourceBundleService) portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;


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
            <div style="font-size: 1.1em;">
              <p><%=res.getString("UILoginForm.label.SSODisabledUserSignin")%></p>
              <p>
                <a href="<%=contextPath%>?portal:action=Logout" class="" style="font-weight: bolder" title="Sign Out"><%=res.getString("UILoginForm.label.Signout")%></a>
                <%=res.getString("UILoginForm.label.SSODisabledUserSignoutRequest")%>
              </p>
            </div>
         </div>
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

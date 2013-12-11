<%
  /*
   * JBoss, Home of Professional Open Source.
   * Copyright 2012, Red Hat, Inc., and individual contributors
   * as indicated by the @author tags. See the copyright.txt file in the
   * distribution for a full listing of individual contributors.
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
%>
<%@ page language="java" %>
<%@ page import="org.gatein.portal.installer.PortalSetupService"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="java.util.ResourceBundle"%>
<%

  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  PortalSetupService setupService = (PortalSetupService)portalContainer.getComponentInstance(PortalSetupService.class);
  
  if (setupService.isSetup(request.getContextPath().substring(1))) {
      response.sendRedirect(request.getContextPath());
  }

  String contextPath = request.getContextPath() ;
  String error = (String)request.getAttribute("org.gatein.portal.setup.error");

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
    <title><%=res.getString("UISetupForm.label.setup.SetPasswordTitle")%></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>        
    <meta name="description" content="Password setup screen of the Portal"/>
    <meta name="viewport" content="initial-scale=1, maximum-scale=1"/>
    <link rel="stylesheet" type="text/css" href="/portal/setup/css/default.css"/>
    <link rel="stylesheet" type="text/css" href="/portal/setup/css/enchanced.css"/>
    <!--[if lte IE 8]>
    <link rel="stylesheet" type="text/css" href="/portal/setup/css/ie8.css"/>
    <![endif]-->
  </head>
  <body>
    <h1><a href="#login-form" title="Setup"><%=res.getString("UISetupForm.label.setup.SetPasswordTitle")%></a></h1>
    <%/*Begin form*/%>
    <% if(error != null) { %>
    <div id="error-pane"><p><span><%= error %></span></p></div>
    <% } %>
    <form id="setup-form" name="setup-form" action="<%= contextPath + "/setupaction"%>" method="post">
        <fieldset>
            <legend><%=res.getString("UISetupForm.label.setup.SetPassword")%></legend>
            
            <label for="password"><%=res.getString("UISetupForm.label.setup.Password")%></label>
            <input type="password" id="password" name="password" value=""/>
            
            <label for="password2"><%=res.getString("UISetupForm.label.setup.PasswordRep")%></label>
            <input type="password" id="password2" name="password2" value=""/>
            
            <input type="submit" name="setup" value="<%=res.getString("UISetupForm.label.setup.Setup")%>"/>

        </fieldset>
    </form>
    <div id="footer">
        <p>
            <%=res.getString("UISetupForm.label.mobile.copyright.Intro")%>
            <a href="http://www.redhat.com/"><%=res.getString("UISetupForm.label.mobile.copyright.RH")%></a>
            <%=res.getString("UISetupForm.label.mobile.copyright.And")%>
            <a href="http://www.exoplatform.com"><%=res.getString("UISetupForm.label.mobile.copyright.Exo")%></a>
        </p>
    </div>
</body>   
</html>
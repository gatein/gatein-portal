<%--
JBoss, Home of Professional Open Source
Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
contributors by the @authors tag. See the copyright.txt in the
distribution for a full listing of individual contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="org.exoplatform.webui.application.WebuiRequestContext" %>
<%@ page import="org.exoplatform.web.application.JavascriptManager" %>

<%-- Hack because web ui can't properly handle javascript modules after a full ajax page reload --%>
<%
  JavascriptManager jsMan = ((WebuiRequestContext)WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
  jsMan.require("SHARED/dropdownmenu_jquery", "dropdownmenu_nav").addScripts("dropdownmenu_nav.init();");
  jsMan.require("SHARED/org_gatein_navigation", "navigation").addScripts("navigation.init();");
%>

<portlet:defineObjects />
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />
<div id="id<portlet:namespace/>_gtnResponsiveNavigationPortlet" class="gtnResponsiveNavigationPortlet ">
    <%-- Button opening the navigation for small screen devices or very long navigation--%>
    <div class="collapsibleToggle"><a class=" btn" onclick="return false" href="#">Show portal navigation</a></div>
    <%-- Render the main menu, if nodes are available --%>
    <c:if test="${fn:length(navigationRootNode.children) > 0}">
        <c:set var="parentNode" value="${navigationRootNode}" scope="request" />
        <c:set var="menuType" value="topmenu" scope="request" />
        <jsp:include page="node.jsp" />
    </c:if>
</div>
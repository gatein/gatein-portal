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
<portlet:defineObjects />

    <c:if test="${empty resourceRequest.locale}">
        <c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />
    </c:if>

<ol class="menu">
    <c:forEach var="node" items="${parentNode.children}">

        <portlet:resourceURL var="ajaxResourceURL" id="node">
           <portlet:param name="uri" value="${node.path}" />
           <portlet:param name="siteId" value="${node.siteId}"/>
        </portlet:resourceURL>        

        <c:choose>
          <%--node contains a link and a category --%>
          <c:when test="${node.page && node.parent}">
            <li class="menucategory menulink">
              <a class="link" href="${node.URI}"><span>${node.name}</span></a>
              <a class="menutoggle" href="#${ajaxResourceURL}"><i class="caret">${resourceBundle.getString("label.children")}</i></a>
            </li>
          </c:when>
          <%--node is a category --%>
          <c:when test="${!node.page && node.parent}">
            <li class="menucategory">
              <a class="menutoggle" href="#${ajaxResourceURL}">
                <span>${node.name}</span>
                <i class="caret">${resourceBundle.getString("label.children")}</i>
              </a>
            </li>
          </c:when>
          <%--node is a link --%>
          <c:when test="${node.page && !node.parent}">
            <li class="menulink">
              <a class="link" href="${node.URI}"><span>${node.name}</span></a>
            </li>
          </c:when>
        </c:choose>
        
    </c:forEach>
</ol>

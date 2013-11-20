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

<c:choose>
    <c:when test="${empty resourceRequest.locale}">
        <c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />
    </c:when>
    <c:otherwise>
        <c:set var="resourceBundle" value="${portletConfig.getResourceBundle(resourceRequest.locale)}" />
        <c:set var="menuType" value="submenu" />
    </c:otherwise>
</c:choose>

<%-- Using the comment tag hack to get rid of the whitespace --%>
<ol class="${menuType} collapsibleContent"><!--
    <c:forEach var="child" items="${parentNode.children}">
        <c:if test="${!child.system}">
            --><li class="menuitem close <c:if test='${child.active}'>active</c:if> <c:if test='${(child.parent) && (child.page)}'> multilevel</c:if>">
        <portlet:resourceURL var="ajaxResourceUrl">
            <portlet:param name="uri" value="${child.path}" />
        </portlet:resourceURL>
        <c:choose>
            <%-- Node is a clickable page and contains children nodes --%>
            <c:when test="${child.page && child.parent}">
                <a href="${child.URI}"><span>${child.name}</span></a><!--
                --><a href="#${ajaxResourceUrl}" class="caret menuhandler close"><i>${resourceBundle.getString("label.children")}</i></a>
            </c:when>
            <%-- Node is a clickable page but doesn't contain any children nodes --%>
            <c:when test="${child.page && !child.parent}">
                <a href="${child.URI}"><span>${child.name}</span></a>
            </c:when>
            <%-- Node is not a clickable page but contains children nodes, it's a "category" node --%>
            <c:when test="${(showEmptyCategory && !child.page && child.parent) || (!showEmptyCategory && !child.page && !child.emptyCategory)}">
                <a href="#${ajaxResourceUrl}" class="menuhandler close"><span>${child.name}</span><i class="caret">${resourceBundle.getString("label.children")}</i></a>
            </c:when>
            <%-- Do nothing with non-clickable "category" nodes without children nodes --%>
            <c:otherwise>
                <c:if test="${showEmptyCategory}">
                    <span>${child.name}</span>
                </c:if>
            </c:otherwise>
        </c:choose>
        </li><!--
        </c:if>
    </c:forEach>
    -->
</ol>
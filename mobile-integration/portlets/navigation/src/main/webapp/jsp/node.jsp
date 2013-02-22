<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page trimDirectiveWhitespaces="true" %>
<portlet:defineObjects />
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>

<%-- Using the comment tag hack to get rid of the whitespace --%>
<ol class="${menuType} collapsibleContent"><!--
    <c:forEach var="child" items="${parentNode.children}">
        <c:if test="${(child.page || child.menuCategory) && !child.system}">
            --><li class="menuitem close <c:if test='${child.active}'>active</c:if> <c:if test='${(child.menuCategory) && (child.page)}'> multilevel</c:if>">
            <c:choose>
                <%-- Node is a clickable page and contains children nodes --%>
                <c:when test="${child.page && child.menuCategory}">
                    <a href="${child.URI}"><span>${child.name}</span></a>
                    <a href="#" class="caret menuhandler close"><i>${resourceBundle.getString("label.children")}</i></a>
                </c:when>
                <%-- Node is a clickable page but doesn't contain any children nodes --%>
                <c:when test="${child.page && !child.menuCategory}">
                    <a href="${child.URI}"><span>${child.name}</span></a>
                </c:when>
                <%-- Node is not a clickable page but contains children nodes, it's a "category" node --%>
                <c:when test="${!child.page && child.menuCategory}">
                    <a href="#" class="menuhandler close"><span>${child.name}</span><i class="caret">${resourceBundle.getString("label.children")}</i></a>
                </c:when>
                <%-- Do nothing with non-clickable "category" nodes without children nodes --%>
                <c:otherwise>
                    
                </c:otherwise>
            </c:choose>
            <%-- Render a submenu if available --%>
            <c:if test='${child.menuCategory}'>
                <c:set var="parentNode" value="${child}" scope="request"/>
                <c:set var="menuType" value="submenu" scope="request"/>                
                <jsp:include page="node.jsp"/>
            </c:if>
            </li><!--
        </c:if>
    </c:forEach>
    -->
</ol>
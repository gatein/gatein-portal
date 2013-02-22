<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page trimDirectiveWhitespaces="true" %>

<portlet:defineObjects />
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>
<div id="id<portlet:namespace/>_gtnResponsiveNavigationPortlet" class="gtnResponsiveNavigationPortlet ">
    <%-- Button opening the navigation for small screen devices or very long navigation--%>
    <a class="collapsibleToggle" href="#"><span class="show">${resourceBundle.getString("label.show")}</span><span class="hide">${resourceBundle.getString("label.hide")}</span></a>
    <%-- Render the main menu, if nodes are available --%>
    <c:if test="${fn:length(navigationRootNode.children) > 0}">
        <c:set var="parentNode" value="${navigationRootNode}" scope="request"/>
        <c:set var="menuType" value="topmenu" scope="request"/>        
        <jsp:include page="node.jsp"/>
    </c:if>
</div>
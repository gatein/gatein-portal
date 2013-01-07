<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<portlet:defineObjects />
<jsp:useBean id="navigation" class="org.gatein.portlet.responsive.navigation.NavigationBean" scope="request"/>

<div id="<portlet:namespace/>_gtnResponsiveNavigationPortlet" class="gtnResponsiveNavigationPortlet">

<c:if test="${fn:length(navigation.nodes) > 0}">
<ul>
<c:forEach var="node" items="${navigation.nodes}">
  <li>
    <c:choose>
      <c:when test="${!empty node.URI}">
        <a href="${node.URI}">${node.name}</a>
      </c:when>
      <c:otherwise>
        <p>${node.name}</p>
      </c:otherwise>
    </c:choose>
  </li>
</c:forEach>
</ul>
</c:if>

<script type="text/javascript">

</script>

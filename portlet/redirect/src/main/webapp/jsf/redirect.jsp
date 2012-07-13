<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<f:view>
<c:if test="${not empty redirect.alternativeSites}">
<div id="gtnRedirect">
<p><h:outputText value="Site Preference :"/></p>
<ul>
<c:forEach var="alternativeSite" items="#{redirect.alternativeSites}">
    <h:outputLink value="#{alternativeSite.redirectURI}">
      <li>
      <h:outputText value="#{alternativeSite.redirectName}"/>
      </li>
    </h:outputLink>
</c:forEach>
</ul>
</div>
</c:if>
</f:view>

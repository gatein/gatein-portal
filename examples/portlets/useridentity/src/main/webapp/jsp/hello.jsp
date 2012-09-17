<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

<div class="portlet-section-header">Currently Authenticated User: <%= renderRequest.getRemoteUser() %></div>
<div>Authentication Type: <%= renderRequest.getAuthType() %></div>
<div>UserPrincipal: <%= renderRequest.getUserPrincipal() %></div>
<% if (renderRequest.getUserPrincipal() != null) { %>
<div>UserPrincipal.getName: <%= renderRequest.getUserPrincipal().getName() %></div>
<% } %>
<div>Has users role: <%= renderRequest.isUserInRole("users") %></div>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

<div class="portlet-section-header">Remember we love  you: <%= renderRequest.getParameter("yourname") %></div>

<portlet:renderURL var="myRenderURL"/>
<br/>
<a href="<%= myRenderURL %>">Ask me again</a>

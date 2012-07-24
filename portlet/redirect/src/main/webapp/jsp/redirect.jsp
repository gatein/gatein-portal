
<jsp:useBean id="redirect" class="org.gatein.portlet.redirect.RedirectBean"/>

<% 
if (redirect.getAlternativeSites() != null && !redirect.getAlternativeSites().isEmpty())
{
%>
  <div id="gtnRedirect">
  <p>Site Preference :</p>
  <ul>
    <% for (org.gatein.portlet.redirect.RedirectLink redirectLink: redirect.getAlternativeSites())
       { %>
         <a href="<%= redirectLink.getRedirectURI() %>">
           <li><%=redirectLink.getRedirectName() %></li>
         </a> 
    <% } %>
  </ul>
  </div>
<%
}
%>  

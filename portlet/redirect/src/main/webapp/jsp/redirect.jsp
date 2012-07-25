
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
         <li>
           <a href="<%= redirectLink.getRedirectURI() %>">
             <%=redirectLink.getRedirectName() %>
           </a>
         </li>
    <% } %>
  </ul>
  </div>
<%
}
%>  

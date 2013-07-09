<%@ page import="java.util.List"%>
<%@ page import="org.gatein.portlet.redirect.RedirectLink"%>

<jsp:useBean id="redirect" class="org.gatein.portlet.redirect.RedirectBean"/>

<%
List<RedirectLink> alternativeSites = redirect.getAlternativeSites();
if (alternativeSites != null && !alternativeSites.isEmpty())
{
%>
  <div id="gtnRedirect">
  <p>Site Preference :</p>
  <ul>
    <% for (RedirectLink redirectLink: alternativeSites)
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

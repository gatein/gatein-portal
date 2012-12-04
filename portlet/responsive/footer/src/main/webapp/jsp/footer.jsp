<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="footer" class="org.gatein.portlet.responsive.footer.FooterBean"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);
%>

<div class="gtnResponsiveFooterPortlet">
  <div class="gtn_CopyrightInfo"><%= resourceBundle.getString("copyrightText")%>
     <a href="http://www.redhat.com/"><%= resourceBundle.getString("RedHatInc") %></a>
     <%= resourceBundle.getString("and") %>
     <a href="http://www.exoplatform.com/"><%= resourceBundle.getString("eXoPlatformSAS")%></a>
  </div>
  
  <div class="gtn_options">
    <ul>
      <li><a href="http://www.gatein.org">gatein.org</a></li>
      <li><a href="#"><%= resourceBundle.getString("contactUs") %></a></li>
      <li>
        <select>
          <option value="foo">Foo</option>
          <option value="bar">Bar</option>
        </select>
      </li>
    </ul>
  </div>
</div>

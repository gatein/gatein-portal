<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="features" class="org.gatein.portlet.responsive.features.FeaturesBean"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);
%>

<div class="gtnResponsiveFeaturesPortlet">
  <div id="title">
    <h1><%= resourceBundle.getString("whyUseGatein") %></h1>
  </div>
  <div id="ssoFeature" class="feature">
    <h2><%= resourceBundle.getString("sso.label") %></h2>
    <p><%= resourceBundle.getString("sso.text") %></p>
  </div>
  <div id="nuiFeature" class="feature">
    <h2><%= resourceBundle.getString("nui.label") %></h2>
    <p><%= resourceBundle.getString("nui.text") %></p>
  </div>
  <div id="psFeature" class="feature">
    <h2><%= resourceBundle.getString("ps.label") %></h2>
    <p><%= resourceBundle.getString("ps.text") %></p>
  </div>
  <div id="footer">
   <a href="http://www.gatein.org/"><%= resourceBundle.getString("browseFeaturesPages") %></a>
  </div>
</div>

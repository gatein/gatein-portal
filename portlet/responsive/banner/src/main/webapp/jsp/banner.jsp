<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="banner" class="org.gatein.portlet.responsive.banner.BannerBean"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);
%>

<div class="gtnResponsiveBannerPortlet">
  <div class="gtnResponsvieBanner_slogan">
    <h1><%= resourceBundle.getString("slogan") %></h1>
  </div>
  <div class="gtnResponsvieBanner_madeBy">
    <h2><%= resourceBundle.getString("madeBy") %> <a href="http://www.redhat.com/">Red Hat</a> + <a href="http://www.exoplatform.com/">eXo Platform SAS</a> + <a href="https://community.jboss.org/en/gatein/dev"><%= resourceBundle.getString("you") %></a></h2>
  </div>
</div>

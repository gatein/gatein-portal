<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<portlet:defineObjects/>

<%-- The resourceBundle used to retrieve locale string values --%>
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>

<div class="gtnResponsiveFeaturesPortlet">
  <div class="title">
    <h1>${resourceBundle.getString("whyUseGatein")}</h1>
  </div>
  <div class="feature ssoFeature">
    <img alt="${resourceBundle.getString('sso.alttext')}" src="${renderRequest.contextPath}/images/feature-sso.svg">
    <div class="text">
    <h2>${resourceBundle.getString("sso.label")}</h2>
    <p>${resourceBundle.getString("sso.text")}</p>
    </div>
  </div>
  <div class="separator"/></div>
  <div class="feature nuiFeature">
   <img alt="${resourceBundle.getString('nui.alttext')}" src="${renderRequest.contextPath}/images/feature-ui.svg">
   <div class="text">
   <h2>${resourceBundle.getString("nui.label")}</h2>
   <p>${resourceBundle.getString("nui.text")}</p>
   </div>
  </div>
  <div class="separator"></div>
  <div class="feature psFeature">
    <img alt="${resourceBundle.getString('ps.alttext')}" src="${renderRequest.contextPath}/images/feature-plugin.svg">
    <div class="text">
    <h2>${resourceBundle.getString("ps.label")}</h2>
    <p>${resourceBundle.getString("ps.text")}</p>
    </div>
  </div>
  <div class="footer">
   <a href="http://www.gatein.org/">${resourceBundle.getString("browseFeaturesPages")}</a>
  </div>
</div>

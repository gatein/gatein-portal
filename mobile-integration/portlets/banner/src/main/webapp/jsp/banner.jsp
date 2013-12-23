<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<portlet:defineObjects />

<%-- The resourceBundle used to retrieve locale string values --%>
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />

<div class="gtnResponsiveBannerPortlet">
    <div class="gtnResponsiveBanner_slogan">
        <h1>${resourceBundle.getString("slogan")}</h1>
    </div>
    <div class="gtnResponsiveBanner_message">
        <h2>${resourceBundle.getString("message")}</h2>
    </div>
</div>
<div class="gtnResponsiveBannerPortletShadow"></div>

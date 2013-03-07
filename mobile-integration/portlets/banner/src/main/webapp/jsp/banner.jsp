<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<portlet:defineObjects />

<%-- The resourceBundle used to retrieve locale string values --%>
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />

<div class="gtnResponsiveBannerPortlet">
    <div class="gtnResponsvieBanner_slogan">
        <h1>${resourceBundle.getString("slogan")}</h1>
    </div>
    <div class="gtnResponsvieBanner_madeBy">
        <h2>
            ${resourceBundle.getString("madeBy")} <a href="http://www.redhat.com/">Red Hat</a> + <a
                href="http://www.exoplatform.com/">eXo Platform SAS</a> + <a href="https://community.jboss.org/en/gatein/dev">${resourceBundle.getString("you")}</a>
        </h2>
    </div>
</div>
<div class="gtnResponsiveBannerPortletShadow"></div>

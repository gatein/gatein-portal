<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="org.exoplatform.webui.application.WebuiRequestContext" %>
<%@ page import="org.exoplatform.web.application.JavascriptManager" %>
<portlet:defineObjects />

<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>

<c:set var="urlContribute" value="${renderRequest.getPreferences().getValue('url.contribute', '/#')}" />
<c:set var="urlDocumentation" value="${renderRequest.getPreferences().getValue('url.docs', '/#')}" />
<c:set var="urlJira" value="${renderRequest.getPreferences().getValue('url.jira', '/#')}" />

<%-- Hack because web ui can't properly handle javascript modules after a full ajax page reload --%>
<%
  JavascriptManager jsMan = ((WebuiRequestContext)WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
  jsMan.require("SHARED/accordion_jquery", "accordion_jquery").addScripts("accordion_jquery.init();");
  jsMan.require("SHARED/org_gatein_community", "community").addScripts("community.init();");
%>

<div id="id<portlet:namespace/>_gtnResponsiveCommunityPortlet" class="gtnResponsiveCommunityPortlet ">

    <div id="willing">
        <h4>${resourceBundle.getString("contribute.label")}</h4>
        <p>${resourceBundle.getString("contribute.content")}</p>
        <div>
            <a class="btn" href="${urlJira}">${resourceBundle.getString("contribute.jira.link")}</a>
            <span>${resourceBundle.getString("contribute.or")}</span>
            <a href="${urlContribute}">${resourceBundle.getString("contribute.link")}</a>
        </div>
    </div>

    <div id="conversation">
        <h4>${resourceBundle.getString("conversation.label")}</h4>
        <div id="conversation-accordion" class="accordion">
            <div class="accordion-group">
                <a href="#<portlet:namespace/>blog-content-mobile" id="<portlet:namespace/>blog-content-mobile" class="accordion-toggle">
                    <i class="icon-blog icon-gray"></i>${resourceBundle.getString("conversation.blog.label")}
                </a>
                <div class="accordion-body collapse in" id="blog-content-mobile">
                  <portlet:resourceURL var="ajaxResourceUrl">
                    <portlet:param name="type" value="blog" />
                  </portlet:resourceURL>
                  <a href="${ajaxResourceUrl}" class="ajaxLoader">${resourceBundle.getString("ajax.load")}</a>
                </div>
            </div>

            <div class="accordion-group">
                <a href="#<portlet:namespace/>vimeo-content-mobile" id="<portlet:namespace/>vimeo-content-mobile" class="accordion-toggle">
                    <i class="icon-vimeo icon-gray"></i>${resourceBundle.getString("conversation.vimeo.label")}
                </a>
                <div class="accordion-body collapse" id="vimeo-content-mobile">
                  <portlet:resourceURL var="ajaxResourceUrl">
                    <portlet:param name="type" value="vimeo" />
                  </portlet:resourceURL>
                  <a href="${ajaxResourceUrl}" class="ajaxLoader">${resourceBundle.getString("ajax.load")}</a>
                </div>
            </div>
        </div>
    </div>

    <div id="documentation">
        <i class="book"></i>
        <h4>${resourceBundle.getString("documentation.label")}</h4>
        <p>${resourceBundle.getString("documentation.content")} <a href="${urlDocumentation}"> ${resourceBundle.getString("documentation.link")}</a></p>
    </div>
    <div class="ClearFix"></div>
</div>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page import="org.exoplatform.webui.application.WebuiRequestContext" %>
<%@ page import="org.exoplatform.web.application.JavascriptManager" %>

<portlet:defineObjects />

<%-- Hack because web ui can't properly handle javascript modules after a full ajax page reload --%>
<%
JavascriptManager jsMan = ((WebuiRequestContext)WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
jsMan.require("SHARED/org_gatein_responsive_collapsiblecontainer", "collapsibleContainer").addScripts("collapsibleContainer.init();");
jsMan.require("SHARED/org_gatein_responsive_dropdownmenu_jquery", "dropdownmenu").addScripts("dropdownmenu.init();");
jsMan.require("SHARED/org_gatein_responsive_menu", "menu").addScripts("menu.init();");
%>

<%-- The resourceBundle used to retrieve locale string values --%>
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}" />

<%-- The action to perform to bring up the webui signin modal --%>
<c:set var="SIGN_IN_LINK"
    value="${headerbean.generateLoginLink()}" />
<c:set var="DEFAULT_SIGN_IN_ACTION"
    value="if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true));" />
<c:set var="SIGN_IN_ACTION"
    value="${headerbean.generateLoginAction(DEFAULT_SIGN_IN_ACTION)}" />

<%-- The action to perform to log out the current user --%>
<c:set var="SIGN_OUT_ACTION" value="eXo.portal.logout();" />
<%-- The action to bring up the user profile modal --%>
<c:set var="USER_PROFILE_ACTION"
    value="javascript:if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'AccountSettings', true));" />

<%-- Link to the registration page --%>
<c:set var="registerLink" value="${headerbean.generateRegisterLink()}" />

<%-- Link to the home page --%>
<c:set var="homePageLink" value="${headerbean.generateHomePageLink()}" />

<div id="<portlet:namespace/>_gtnResponsiveHeaderPortlet" class="gtnResponsiveHeaderPortlet">
    <div class="collapsibleRow clearfix">
        <div id="<portlet:namespace/>_logo" class="logo" onclick="window.location = '${homePageLink}';"><span class="skipHidden">GateIn</span></div>
        <div id="<portlet:namespace/>_icon" class="gtnResponsiveMenuCollapseButton">
            <input type="hidden" class="data target" value="#<portlet:namespace/>_options" />
            <input type="hidden" class="data action" value="toggleCSS" />
            <input type="hidden" class="data target class" value="display" />
            <input type="hidden" class="data self class" value="enabled" />
            <div class="collapseButtonIcon" title="${resourceBundle.getString('label.ShowGroupPages')}"></div>
        </div>
        <div id="<portlet:namespace/>_options" class="gtnResponsiveMenu options collapsibleContent collapsibleRow">
            <ol class="menu">
                <c:choose>
                    <c:when test="${renderRequest.getRemoteUser() eq null}">
                        <li class="menulink"><a class="link" href="${SIGN_IN_LINK}" onclick="${SIGN_IN_ACTION}">${resourceBundle.getString("label.SignIn")}</a></li>
                        <li class="menulink"><a class="link" href="${registerLink}">${resourceBundle.getString("label.Register")}</a></li>
                    </c:when>
                    <c:otherwise>
                        <c:set var="useDashboardLink" value="${renderRequest.getPreferences().getValue('enable.dashboard.link', false)}" />
                        <c:if test="${useDashboardLink.equals('true')}">
                            <li class="menulink"><a class="link" href="${headerbean.generateDashboardLink()}">${resourceBundle.getString("label.Dashboard")}</a></li>
                        </c:if>
                        <c:set var="useGroupPagesLink" value="${renderRequest.getPreferences().getValue('enable.grouppages.link', false)}" />
                        <c:if test="${useGroupPagesLink.equals('true')}">
                            <li class="menucategory"><a href="#" class="menutoggle"><span>${resourceBundle.getString("label.GroupPages")}</span><i class="caret">${resourceBundle.getString("label.ShowGroupPages")}</i></a><ol class="menu">
                                    <c:forEach var="groupNode" items="${headerbean.getGroupNodes()}">
                                        <li class="menucategory">
                                                    <%-- Having to specify the replace here may not be the best option. TODO: will defining this in the headerbean work out better? Bean does not current have access to the portlet's resource bundle...--%>
                                                    <c:set var="groupNodeTitle" value="${fn:replace(resourceBundle.getString('label.GroupPageTitleFormat'), '{groupName}', groupNode.key)}" />
                                                        <a class="menutoggle" href="#"><span>${groupNodeTitle}</span><i class="caret">${resourceBundle.getString("label.children")}</i></a>
                                                        <div class="menutoggle"></div>
                                                        <c:set var="parentNode" value="${groupNode.value}" scope="request" />
                                                        <c:set var="menuType" value="submenu" scope="request" />
                                                       <jsp:include page="node.jsp" />
                                        </li>
                                    </c:forEach>
                                </ol></li>
                        </c:if>
                        <li class="menulink"><a class="link" href="#" onclick="${SIGN_OUT_ACTION}">${resourceBundle.getString("label.SignOut")}</a></li>
                        <li class="menulink"><a class="link" href="#" onclick="${USER_PROFILE_ACTION}">${resourceBundle.getString("label.UserProfile")}</a></li>
                    </c:otherwise>
                </c:choose>
            </ol>
        </div>
    </div>
    <div class="clearfix"></div>
</div>

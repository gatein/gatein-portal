<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<portlet:defineObjects />

<%-- The resourceBundle used to retrieve locale string values --%>
<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>

<%-- The action to perform to bring up the webui signin modal --%>
<c:set var="SIGN_IN_ACTION" value="if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true));"/>
<%-- The action to perform to log out the current user --%>
<c:set var="SIGN_OUT_ACTION" value="eXo.portal.logout();"/>

<%-- Link to the registration page --%>
<c:set var="registerLink" value="${headerbean.generateRegisterLink()}"/>

<%-- Link to the home page --%>
<c:set var="homePageLink" value="${headerbean.generateHomePageLink()}"/>

<%-- Link to the dashboard --%>
<c:set var="dashboardLink" value="${headerbean.generateDashboardLink()}"/>

<%-- Link to the groupPages --%>
<c:set var="groupPagesLink" value="${headerbean.generateGroupPagesLink()}"/>

<div id="<portlet:namespace/>_gtnResponsiveHeaderPortlet" class="gtnResponsiveHeaderPortlet">
    <div class="collapsibleRow">
	<div id="<portlet:namespace/>_logo" class="logo" onclick="window.location = '${homePageLink}';"></div>
	<div id="<portlet:namespace/>_icon" class="userpreferencebutton collapseButton" data-target="#<portlet:namespace/>_options" data-action="toggleCSS" data-target-class="display" data-self-class="enabled"></div>
	<div id="<portlet:namespace/>_options" class="options collapsibleContent collapsibleRow">
		<ol class="topmenu">
		    <c:choose>
		    <c:when test="${renderRequest.getRemoteUser() eq null}">
		     <li class="menuelement">
			      	<a href="#" onclick="${SIGN_IN_ACTION}">${resourceBundle.getString("label.SignIn")}</a>
			      </li>
			      <li class="menuelement">
			      	<a href="${registerLink}">${resourceBundle.getString("label.Register")}</a>
			      </li>
		    </c:when>
		    <c:otherwise>
		     <li class="menuelement">
	              	<a href="${dashboardLink}">${resourceBundle.getString("label.Dashboard")}</a>
              	  </li>
	              <li class="menuelement">
	              	<a href="${groupPagesLink}">${resourceBundle.getString("label.GroupPages")}</a>
	              	<div class="menuarrow"></div>
	              	      	
	              <ol class="submenu">
	                <c:forEach var="groupNode" items="${headerbean.getGroupNodes()}">
	                    <li>
	                      <ol class="submenu">
                                <li class="menuelement">
	                        <div class="menucategory">${groupNode.key}'s Pages
	                          <div class="menuarrow"></div>
	                        </div>
	                        <ol class="submenu">
	                        	<c:forEach var="groupnode" items="${groupNode.value}">
	                        	  <c:set var="node" value="${groupnode}" scope="request"/>
	                        	  <jsp:include page="node.jsp"/>
	                        	</c:forEach>
	                        </ol>
                              </li>
	                      </ol>
	                    </li>
	                </c:forEach>
				  </ol>
                 </li>
	              <li><a href="#" onclick="${SIGN_OUT_ACTION}">${resourceBundle.getString("label.SignOut")}</a></li>
	              <li><a href="#">${resourceBundle.getString("label.UserProfile")}</a></li>		
	        </c:otherwise>
	        </c:choose>
		</ol>
	</div>
	</div>
	<div class="clear"></div>
</div>

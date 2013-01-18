<%@page import="java.util.List"%>
<%@page import="org.gatein.portlet.responsive.header.Node"%>
<%@page import="java.util.Map"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<portlet:defineObjects />
<jsp:useBean id="header" class="org.gatein.portlet.responsive.header.HeaderBean" />

<%
   Locale locale = renderRequest.getLocale();
			ResourceBundle resourceBundle = portletConfig
					.getResourceBundle(locale);

			String signinAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true));";
			String registerLink = header.generateRegisterLink();
%>

<%!String generateNodeTree(Node node)
{
        String markup = "<li class=\"menuelement\">";

        if (node.getURI() != null) {
            markup += "<a href=\"" + node.getURI() + "\">" + node.getName() + "</a>";
        } else {
            markup += "<div class=\"menucategory\">";
            markup += node.getName();
            markup += "<div class=\"menuarrow\">";
            markup += "</div>";
            markup += "</div>";
        }

        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            markup += "<ol class=\"submenu\">";
            for (Node child : node.getChildren()) {
                markup += generateNodeTree(child);
            }
            markup += "</ol>";
        }
        markup += "</li>";

        return markup;
    }%>

<div id="<portlet:namespace/>_gtnResponsiveHeaderPortlet" class="gtnResponsiveHeaderPortlet">
    <div class="collapsibleRow">
	<div id="<portlet:namespace/>_logo" class="logo" onclick="window.location = '<%= header.generateHomePageLink()%>';"></div>
	<div id="<portlet:namespace/>_icon" class="userpreferencebutton collapseButton" data-target="#<portlet:namespace/>_options" data-action="toggleCSS" data-target-class="display" data-self-class="enabled"></div>
	<div id="<portlet:namespace/>_options" class="options collapsibleContent collapsibleRow">
		<ol class="topmenu">
		    <% if (renderRequest.getRemoteUser() == null) 
		       {%>
			      <li class="menuelement">
			      	<a href="#" onclick="<%=signinAction%>"><%=resourceBundle.getString("label.SignIn")%></a>
			      </li>
			      <li class="menuelement">
			      	<a href="<%=registerLink%>"><%=resourceBundle.getString("label.Register")%></a>
			      </li>
	        <% } 
	           else
	           {
	              String dashboardLink = header.generateDashboardLink();
	              String groupPagesLink = header.generateGroupPagesLink();
	           %>
	              <li class="menuelement">
	              	<a href="<%= dashboardLink%>"><%= resourceBundle.getString("label.Dashboard")%></a>
              	  </li>
	              <li class="menuelement">
	              	<a href="<%= groupPagesLink%>"><%= resourceBundle.getString("label.GroupPages") %></a>
	              	<div class="menuarrow"></div>
	              <ol class="submenu">
	              <% Map<String, List<Node>> groupNodes = header.getGroupNodes();
          			 for (String groupName : groupNodes.keySet()) { %>
          			 <li>
				     <ol class="submenu">
					 <li class="menuelement">
					     <div class="menucategory"><%=groupName%>'s Pages
					     <div class="menuarrow"/>
					     </div>
					     </div>
					     <ol class="submenu">
							<%
 						    for (Node node : groupNodes.get(groupName)) {
							%>
							<%=generateNodeTree(node)%> 
							<%
 							    }
							%>
 						</ol></li>
				</ol></li> <%
     } %>   </ol>
                 </li>
	              <li><a href="#" onclick="eXo.portal.logout();"><%=resourceBundle.getString("label.SignOut")%></a></li>
	              <li><a href="#"><%= resourceBundle.getString("label.UserProfile") %></a></li>
	        <% }%>		
		</ol>
	</div>
	</div>
	<div class="clear"></div>
</div>

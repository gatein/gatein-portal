<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.gatein.portlet.responsive.footer.RedirectLink" %>
<portlet:defineObjects/>
<jsp:useBean id="footer" class="org.gatein.portlet.responsive.footer.FooterBean" scope="request"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);

String changeLanguageAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true));";

%>

<div class="gtnResponsiveFooterPortlet" id="<portlet:namespace/>_footerPortlet">
    <div class="options" id="<portlet:namespace/>_options">
		<ol>
		    <% if (footer.getAlternativeSites() != null && !footer.getAlternativeSites().isEmpty()) 
		       {
		          for (RedirectLink redirectLink: footer.getAlternativeSites())
		          {
		             String siteLinkLabel = resourceBundle.getString("site").replace("${SiteName}", redirectLink.getRedirectName());
		          %>
			         <li class="sitePreference">
				        <a href="<%= redirectLink.getRedirectURI()%>"><%=siteLinkLabel%></a>
			         </li>
			    <%}
			   } %>
			<li class="language">
		    	 <a href="#" onclick="<%= changeLanguageAction %>"><%= locale.getDisplayLanguage(locale) %></a>
                 <div class="downCaret"></div>
			</li>
		</ol>
	</div>
	<div class="copyright" id="<portlet:namespace/>_copyright">
	  <p><%= resourceBundle.getString("copyrightText") %></p>
	</div>
	<div class="clear"></div>
</div>

<script type="text/javascript">
		
	checkFooterSize();
	
	function checkFooterSize()
	{
	        var footer = document.getElementById("<portlet:namespace/>_footerPortlet");
	        var copyright = document.getElementById("<portlet:namespace/>_copyright");
	        var options = document.getElementById("<portlet:namespace/>_options");
	        
	        if (typeof (footer.collapseWidth) === "undefined")
	        {
	           copyright.style.whiteSpace="nowrap"; //make sure that the copyright information is all on one line before checking it's lenght
	           footer.collapseWidth = options.clientWidth + copyright.clientWidth;
	           copyright.style.whiteSpace="normal"; //set back to normal the white space
	        }
	 
	        if (footer.clientWidth <= footer.collapseWidth)
	        {
	                copyright.style.float="none";
	                copyright.style.clear="both";
	        }
	        else
	        {
	                copyright.style.float="left";
	                copyright.style.clear="none";
	        }
	}

	
</script>

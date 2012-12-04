<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="header" class="org.gatein.portlet.responsive.header.HeaderBean"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);

String signinAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true));" ;
String registerAction = header.generateRegisterLink();
String changeLanguageAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true));" ;

%>

<div id="gtnResponsiveHeaderPortlet">
  <div id="grh_logo"></div>
  <div id="grh_icon" onclick="toggleOptions()"></div>
  <div id="grh_options">
     <ul>
       <li><a onclick="<%= signinAction %>"><%= resourceBundle.getString("label.Signin") %></a></li>
       <li><a href="<%=registerAction%>"><%= resourceBundle.getString("label.Register") %></a></li>
       <li><a onclick="<%= changeLanguageAction %>"><%= resourceBundle.getString("label.ChangeLanguage") %></a></li>
     </ul>
  </div>
  <div class="grh_clear"></div>
</div>

<script type="text/javascript">

	var GRHMainDiv = document.getElementById("gtnResponsiveHeaderPortlet");
	var GRHLogoDiv = document.getElementById("grh_logo");
	var GRHOptionsDiv = document.getElementById("grh_options");
	var GRHIconDiv = document.getElementById("grh_icon");
	
	var logoWidth = GRHLogoDiv.clientWidth;
	var optionsWidth = GRHOptionsDiv.clientWidth;

	var maxWidthForOptions = logoWidth + optionsWidth;


// 	$(window).resize(function(){checkGRHSize();});
	
//     window.onresize = function(){checkGRHSize();};
	checkGRHSize();
	
	function checkGRHSize()
	{
		var mainWidth = GRHMainDiv.clientWidth;

		if ((mainWidth - maxWidthForOptions) < 0) {
			GRHOptionsDiv.style.display="none";
			GRHIconDiv.style.display="block";
		}
		else
		{
			GRHOptionsDiv.style.display="block";
			GRHIconDiv.style.display="none";
		}
	}
	

	function toggleOptions()
	{
		if (GRHOptionsDiv.style.display == 'none') {
			GRHOptionsDiv.style.display = 'block';
		} else {
			GRHOptionsDiv.style.display = 'none';
		}
	}
</script>
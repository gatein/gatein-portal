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

<div id="<portlet:namespace/>_gtnResponsiveHeaderPortlet" class="gtnResponsiveHeaderPortlet">
	<div id="<portlet:namespace/>_logo" class="logo" onclick="window.location = '<%= header.generateHomePageLink()%>';"></div>
	<div id="<portlet:namespace/>_icon" class="icon hidden normal" onclick="toggleOptions()"></div>
	<div id="<portlet:namespace/>_options" class="options expanded normal">
		<ul>
		    <% if (renderRequest.getRemoteUser() == null) 
		       {%>
			      <li><a href="#" onclick="<%=signinAction%>"><%=resourceBundle.getString("label.SignIn")%></a></li>
			      <li><a href="<%=registerLink%>"><%=resourceBundle.getString("label.Register")%></a></li>
	        <% } 
	           else
	           {
	              String dashboardLink = header.generateDashboardLink();
	              String groupPagesLink = header.generateGroupPagesLink();
	           %>
	              <li><a href="<%= dashboardLink%>"><%= resourceBundle.getString("label.Dashboard")%></a></li>
	              <li><a href="<%= groupPagesLink%>"><%= resourceBundle.getString("label.GroupPages") %></a></li>
	              <li><a href="#" onclick="eXo.portal.logout();"><%=resourceBundle.getString("label.SignOut")%></a></li>
	              <li><a href="#"><%= resourceBundle.getString("label.UserProfile") %></a></li>
	        <% }%>		
		</ul>
	</div>
	<div class="clear"></div>
</div>

<script type="text/javascript">

    document.getElementById("<portlet:namespace/>_icon").selected = false; //by default its not selected
    document.getElementById("<portlet:namespace/>_gtnResponsiveHeaderPortlet").collapsed = false; //by default not collapsed
    document.getElementById("<portlet:namespace/>_options").collapsed = false //by default not collapsed
    checkGRHSize();
    
    function checkGRHSize(){
    	var mainDiv = document.getElementById("<portlet:namespace/>_gtnResponsiveHeaderPortlet");
    	var logoDiv = document.getElementById("<portlet:namespace/>_logo");
    	var optionsDiv = document.getElementById("<portlet:namespace/>_options");
    	var iconDiv = document.getElementById("<portlet:namespace/>_icon");
    	
    	if (typeof (mainDiv.collapseWidth) === "undefined")
    	{
    		var optionsDivOriginalWhiteSpace = optionsDiv.style.whiteSpace;
    		optionsDiv.style.whiteSpace="nowrap";
    		mainDiv.collapseWidth = logoDiv.clientWidth + optionsDiv.scrollWidth;
    		optionsDiv.style.whiteSpace=optionsDivOriginalWhiteSpace;
    	}
    	
    	if ((mainDiv.clientWidth < mainDiv.collapseWidth) && !mainDiv.collapsed) //need to collapse
        {
    		mainDiv.collapsed = true;
    		collapseElement(optionsDiv);
    		showElement(iconDiv);
        }
        else if ((mainDiv.clientWidth >= mainDiv.collapseWidth) && mainDiv.collapsed) //need to expand
        {
        	mainDiv.collapsed = false;
        	expandElement(optionsDiv);
        	hideElement(iconDiv);

            if (iconDiv.selected) //if we are expanding and the iconDiv is already open, we need to unhighlight
            {
            	iconDiv.selected = false;
            	unHighlightElement(iconDiv);
            	unHighlightElement(optionsDiv);
            }
        }
        checkOptionsSize();
    };

    function checkOptionsSize()
    {
        var optionsDiv = document.getElementById("<portlet:namespace/>_options");
        if (optionsDiv.clientWidth != 0) //only perform the check if the options div is visible
        {
        if (typeof (optionsDiv.collapseWidth) === "undefined")
        {
        	var optionsDivOriginalWhiteSpace = optionsDiv.style.whiteSpace;
                optionsDiv.style.whiteSpace="nowrap";
                optionsDiv.collapseWidth = optionsDiv.scrollWidth;
                optionsDiv.style.whiteSpace=optionsDivOriginalWhiteSpace;
        }
        
        if ((optionsDiv.clientWidth < optionsDiv.collapseWidth) && !optionsDiv.collapsed)
        {
            optionsDiv.collapsed = true;
            var lis = optionsDiv.getElementsByTagName("li");
            for (var i = 0; i < lis.length; i++)
            {
              lis[i].className = lis[i].className + " singleline";
	    }
        }
        else if ((optionsDiv.clientWidth >= optionsDiv.collapseWidth) && optionsDiv.collapsed)
        {
           optionsDiv.collapsed = false;
           var lis = optionsDiv.getElementsByTagName("li");
           for (var i = 0; i < lis.length; i++)
           {
              lis[i].className = lis[i].className.replace(" singleline", "");
           }
        }
        }
    }
    
    function toggleOptions()
    {
    	var iconDiv = document.getElementById("<portlet:namespace/>_icon");
    	var optionsDiv = document.getElementById("<portlet:namespace/>_options");
    	
    	if (iconDiv.selected) //need to toggle off
    	{
    		iconDiv.selected = false;
    		unHighlightElement(iconDiv);
    		unHighlightElement(optionsDiv);
	        collapseElement(optionsDiv);
                checkOptionsSize();
    	}
    	else //need to toggle on
    	{
    		iconDiv.selected = true;
    		highlightElement(iconDiv);
    		highlightElement(optionsDiv);
    		expandElement(optionsDiv);
                checkOptionsSize();
    	}
    }
    
    function highlightElement(element)
    {
    	element.className = element.className.replace("normal", "highlight");
    }
    
    function unHighlightElement(element)
    {
    	element.className = element.className.replace("highlight", "normal");
    }
    
    function expandElement(element)
    {
    	element.className = element.className.replace("collapsed", "expanded");
    }
    
    function collapseElement(element)
    {
    	element.className = element.className.replace("expanded", "collapsed");
    }
    
    function hideElement(element)
    {
    	element.className = element.className.replace("visible", "hidden");
    }
    
    function showElement(element)
    {
    	element.className = element.className.replace("hidden", "visible");
    }

</script>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<div class="portlet-section-header">Welcome !</div>

<br/>

<div class="portlet-font">Welcome on the JSP Hello User portlet,
my name is JBoss Portal. What's yours ?</div>

<br/>

<div class="portlet-font">Method 1: We simply pass the parameter to the render phase:<br/>
<a href="<portlet:renderURL><portlet:param name="yourname" value="John Doe"/></portlet:renderURL>">John Doe</a></div>

<br/>

<div class="portlet-font">Method 2: We pass the parameter to the render phase, using valid markup:
Please check the source code to see the difference with Method 1.
<portlet:renderURL var="myRenderURL">
    <portlet:param name="yourname" value='John Doe'/>
</portlet:renderURL>
<br/>
<a href="<%= myRenderURL %>">John Doe</a></div>

<br/>

<div class="portlet-font">Method 3: We use a form:<br/>

<portlet:actionURL var="myActionURL"/>
<form action="<%= myActionURL %>" method="POST">
         <span class="portlet-form-field-label">Name:</span>
         <input class="portlet-form-input-field" type="text" name="yourname"/>
         <input class="portlet-form-button" type="Submit"/>
</form>
</div>

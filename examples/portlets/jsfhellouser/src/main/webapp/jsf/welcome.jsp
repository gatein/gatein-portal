<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>

<div class="portlet-section-header">Welcome !</div>

<br/>

<div class="portlet-font">Welcome on the JSP Hello User portlet,
my name is JBoss Portal. What's yours ?</div>

<br/>

<h:form>
   <h:inputText value="#{user.userName}"/>
   <h:commandButton  action="sayHello" value="Say Hello"/>
</h:form>

</f:view>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:view>
<portlet:defineObjects/>

<div class="portlet-section-header">Remember we love  you: <h:outputText value="#{user.userName}"/></div>

<br/>
<h:form>
   <h:commandLink action="back">
      <h:outputText value="Ask Me Again"/>
   </h:commandLink>
</h:form>
</f:view>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="footer" class="org.gatein.portlet.responsive.footer.FooterBean" scope="request"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);

List<Locale> languages = footer.getLanguages();
%>

<div class="gtnResponsiveFooterPortlet">
  
  <div class="gtn_options">
    <ul>
      <li>
        <portlet:actionURL var="setLanguageURL"/>
        <form action="<%= setLanguageURL %>" method="POST" name="languageForm">
        <select id="<portlet:namespace/>_languageSelect" name="languageSelect" onchange="document.languageForm.submit()"> 
        
          <% for (Locale language : languages)
          {
             String languageName = language.getDisplayLanguage(locale);
             String languageNameinLanguage = language.getDisplayLanguage(language);
             
             if (language.getCountry() != null && !language.getCountry().isEmpty())
             {
                languageName += "(" + language.getDisplayCountry(locale) + ")";
                languageNameinLanguage += "(" + language.getDisplayCountry(language) + ")";
             }
             
             if (!language.equals(locale))
             {
             %>
                 <option value="<%= language.getLanguage()%>"><%= languageName + "-" + languageNameinLanguage %></option>
          <% }
             else
             { %>
                 <option value="<%= language.getLanguage()%>" selected="selected" class="selected"><%= languageName %></option>
          <% }
          }
          %>
        </select>
        </form>
      </li>
    </ul>
  </div>
</div>

<script type="text/javascript">
   var languageSelect = document.getElementById("<portlet:namespace/>_languageSelect");
   languageSelect.style.width = languageSelect.options[languageSelect.selectedIndex].text.length + "em";
</script>

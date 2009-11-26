<%--

    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>

<%@ page import="java.util.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.portletcontainer.PortletContainerService"%>
<%@ page import="org.exoplatform.services.portletcontainer.pci.PortletData"%>
<%@ page import="org.exoplatform.services.portal.skin.model.*"%>
<%@ page import="org.exoplatform.services.portal.skin.SkinConfigService"%>

<%
  Enumeration e = session.getAttributeNames() ;
  while(e.hasMoreElements()) {
    String key = (String) e.nextElement() ;
    session.removeAttribute(key);
  }
  
  PortalContainer manager = PortalContainer.getCurrentInstance(session.getServletContext()) ;
  PortletContainerService service = 
    (PortletContainerService) manager.getComponentInstanceOfType(PortletContainerService.class) ;
  Map allPortletMetaData = service.getAllPortletMetaData() ;
  SkinConfigService skinService = 
    (SkinConfigService) manager.getComponentInstanceOfType(SkinConfigService.class) ;

  String[] users = { "exo", "admin" } ;
  String contextPath = request.getContextPath() ;
%>


<HTML>
 <HEAd>
  <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8"/>
 </HEAd>
  <TITLE>Bookmarks</TITLE>
  <BODY width="100%">
  <H1>Bookmarks</H1>
  <table width="100%" border="1">
    <tr>
      <th width="20%">Public Link</th>
      <th width="60%">Description</th>
    </tr>

    <tr>
      <th colspan="3">Available default users (All the default users use "exo" as password</th>
    </tr>
    <%for(int i= 0; i < users.length; i++) { %>
      <tr>
        <td><a href="<%=contextPath%>/faces/public/<%=users[i]%>"><%=users[i]%></a></td>
        <td>Portal of the <%=users[i]%> user </td>
      </tr>
    <%}%>
    <tr>
      <th colspan="3">Available Portlets</th>
    </tr>
    <%Set keys = allPortletMetaData.keySet(); %>
    <%Iterator i = keys.iterator() ; %>
    <%while(i.hasNext()) { %>
    <%  String key = (String) i.next() ;%>
    <%  PortletData portlet = (PortletData) allPortletMetaData.get(key) ;  %>
    <%  String portletTitle = portlet.getPortletName() ; %>
    <%  String portletDescription = portlet.getDescription("en") ; %>
    <%  String portletApp = key.replace('.' , '/') ; %>
    <%  List styles = skinService.getPortletStyles(portletApp) ; %>
    <%  String styleParam  = "" ; %>
    <%  if (styles != null && styles.size() > 0) { %>
    <%    Style style = (Style) styles.get(0) ; %>
    <%    styleParam  = "&style=" + style.getName() ; %>
    <%  }%>
      <tr>
        <td><a href="<%=contextPath%>/faces/public/exotest?portletName=<%=portletApp%><%=styleParam%>"><%=portletTitle%></a></td>
        <td><%=portletDescription%> </td>
      </tr>
    <%}%>
  </table>

  <H3>Exo Project Links</H3>
  <a href="http://exo.sourceforge.net/forum/index.php">Exo PHP Forum</a><br>
  <a href="http://exoplatform.org">Exo platform community home page</a><br>
  <a href="http://exoplatform.com">Exo platform company home page</a><br>
</BODY>
</HTML>

<%
  //session.setAttribute("_username", Constants.DEFAUL_PORTAL_OWNER) ;  
   session.invalidate() ;
%>

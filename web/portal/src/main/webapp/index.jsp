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

<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%
   PortalContainer manager = PortalContainer.getCurrentInstance(session.getServletContext());
   UserPortalConfigService userPortalConfigService = (UserPortalConfigService)manager.getComponentInstanceOfType(UserPortalConfigService.class);
   String remoteUser = request.getRemoteUser();
   String accessMode = "public";
   if (remoteUser != null && remoteUser.trim().length() > 0)
   {
      accessMode = "private";
   }
   response.sendRedirect(request.getContextPath() + "/" + accessMode + "/" + userPortalConfigService.getDefaultPortal() + "/");
%>


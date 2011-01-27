/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.webui.portal;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

/**
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * June 3, 2008
 */
public class UIPortalActionListener
{

   @SuppressWarnings("unused")
   static public class LogoutActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         LogoutControl.wantLogout();
         HttpServletRequest request = prContext.getRequest();
         String portalName = URLEncoder.encode(Util.getUIPortal().getName(), "UTF-8");
         String redirect = request.getContextPath() + "/public/" + portalName + "/";
         prContext.getResponse().sendRedirect(redirect);
         prContext.setResponseComplete(true);
      }
   }

   static public class ChangeWindowStateActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         String portletId = event.getRequestContext().getRequestParameter("portletId");
         UIPortlet uiPortlet = uiPortal.findComponentById(portletId);
         WebuiRequestContext context = event.getRequestContext();
         uiPortlet.createEvent("ChangeWindowState", event.getExecutionPhase(), context).broadcast();
      }
   }

   static public class PingActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         PortalRequestContext pContext = (PortalRequestContext)event.getRequestContext();
         HttpServletRequest request = pContext.getRequest();
         pContext.ignoreAJAXUpdateOnPortlets(false);
         pContext.setResponseComplete(true);
         pContext.getWriter().write("" + request.getSession().getMaxInactiveInterval());
      }
   }
}
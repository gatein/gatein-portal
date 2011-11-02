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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * June 3, 2008
 */
public class UIPortalActionListener
{

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
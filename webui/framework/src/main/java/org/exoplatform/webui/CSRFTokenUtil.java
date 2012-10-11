/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.webui;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.gatein.common.util.UUIDGenerator;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 * 
 */
public class CSRFTokenUtil
{
   public static final String CSRF_TOKEN = "gtn:csrf";
   
   private static Log log = ExoLogger.getExoLogger(CSRFTokenUtil.class);

   private static final UUIDGenerator generator = new UUIDGenerator();

   public static boolean check() throws Exception
   {
      HttpServletRequest request = getRequest();
      if (request != null)
      {
         String sessionToken = getToken();
         String reqToken = request.getParameter(CSRF_TOKEN);
         
         return reqToken != null && reqToken.equals(sessionToken);         
      }
      else
      {
         log.warn("No HttpServletRequest found, can't check CSRF");
         return false;
      }
   }

   public static String getToken() throws Exception
   {
      HttpServletRequest request = getRequest();
      if (request != null)
      {
         HttpSession session = request.getSession();
         String token = (String)session.getAttribute(CSRF_TOKEN);
         if (token == null)
         {
            token = generator.generateKey();
            session.setAttribute(CSRF_TOKEN, token);
         }
         return token;         
      }
      else
      {
         log.warn("No HttpServletRequest found, can't generate CSRF token");
         return null;
      }
   }

   private static HttpServletRequest getRequest() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      if (context != null && context.getRequest() instanceof PortletRequest)
      {
         context = (WebuiRequestContext)context.getParentAppRequestContext();
      }
      
      if (context != null)
      {
         return context.getRequest();         
      }
      else
      {
         log.warn("Can't find portal context");
         return null;
      }
   }
}
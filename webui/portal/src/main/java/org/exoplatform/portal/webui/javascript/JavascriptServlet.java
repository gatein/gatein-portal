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

package org.exoplatform.portal.webui.javascript;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.ResourceRequestFilter;
import org.exoplatform.web.application.javascript.CachedJavascript;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavascriptServlet extends HttpServlet
{

   public void destroy()
   {
   }

   public ServletConfig getServletConfig()
   {
      return null;
   }

   public String getServletInfo()
   {
      return null;
   }

   public void init(ServletConfig arg0) throws ServletException
   {
   }

   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException
   {
      response.sendError(HttpServletResponse.SC_NO_CONTENT, "Should not be called anymore (" + request.getRequestURL() + ")");
/*
      final JavascriptConfigService service =
         (JavascriptConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            JavascriptConfigService.class);
      final String uri = URLDecoder.decode(request.getRequestURI(), "UTF-8");
*/

/*
      CachedJavascript jScript = service.getCachedJScript(uri);
      if (jScript == null)
      {
         jScript = service.getMergedCommonJScripts();
      }
*/

/*
      long lastModified = jScript.getLastModified();
      long ifModifiedSince = request.getDateHeader(ResourceRequestFilter.IF_MODIFIED_SINCE);
      
      // Julien: should we also set charset along with the content type ?
      response.setContentType("application/x-javascript");
      if (!PropertyManager.isDevelopping()) {
         if (ifModifiedSince >= lastModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
         }
      }
      
      String js = jScript.getText();
      response.setDateHeader(ResourceRequestFilter.LAST_MODIFIED, lastModified);
      response.getWriter().write(js);
*/
   }
}

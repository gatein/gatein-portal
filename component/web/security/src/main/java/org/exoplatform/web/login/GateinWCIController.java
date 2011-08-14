/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.exoplatform.web.login;

import org.gatein.wci.security.Credentials;
import org.gatein.wci.security.WCIController;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class GateinWCIController extends WCIController
{
   private ServletContext servletContext;

   public GateinWCIController(final ServletContext servletContext)
   {
      if (servletContext == null)
      {
         throw new IllegalArgumentException("servletContext is null");
      }
      this.servletContext = servletContext;
   }

   public void showLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String initialURI = getInitialURI(req);
      try
      {
         String queryString = (String)req.getAttribute("javax.servlet.forward.query_string");
         if (req.getAttribute("javax.servlet.forward.query_string") != null)
         {
            initialURI = initialURI + "?" + queryString;
         }
         req.setAttribute("org.gatein.portal.login.initial_uri", initialURI);
         servletContext.getRequestDispatcher("/login/jsp/login.jsp").include(req, resp);
      }
      finally
      {
         req.removeAttribute("org.gatein.portal.login.initial_uri");
      }
   }

   public void showErrorLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String initialURI = req.getHeader("referer");
      if (initialURI == null || initialURI.length() == 0)
      {
         initialURI = req.getContextPath();
      }      

      //
      try
      {
         req.setAttribute("org.gatein.portal.login.initial_uri", initialURI);
         servletContext.getRequestDispatcher("/login/jsp/login.jsp").include(req, resp);
      }
      finally
      {
         req.removeAttribute("org.gatein.portal.login.initial_uri");
      }      
   }

   @Override
   public Credentials getCredentials(final HttpServletRequest req, final HttpServletResponse resp)
   {
      return (Credentials)req.getSession().getAttribute(Credentials.CREDENTIALS);
   }

   @Override
   public String getHomeURI(final HttpServletRequest req)
   {      
      return req.getContextPath();
   }
}

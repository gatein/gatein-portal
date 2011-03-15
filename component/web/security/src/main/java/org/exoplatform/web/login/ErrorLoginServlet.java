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

package org.exoplatform.web.login;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.gatein.wci.security.WCIController;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages an error on login 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 4 oct. 2009  
 */
public class ErrorLoginServlet extends AbstractHttpServlet
{

   /**
    * Serial version ID
    */
   private static final long serialVersionUID = -1565579389217147072L;

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(ErrorLoginServlet.class.getName());

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      WCIController wciController = new GateinWCIController(getServletContext());
      
      PortalContainer pContainer = PortalContainer.getInstance();
      ServletContext context = pContainer.getPortalContext();
      // Unregister the token cookie
      unregisterTokenCookie(req);
      // Clear the token cookie
      clearTokenCookie(req, resp);
      
      //nguyenanhkien2a@gmail.com: We set content-type here for using RequestDispatcher.include() method below
      //We can't use RequestDispatcher.forward() if we want to use some response information for output such as clearing cookies, etc
      resp.setContentType("text/html; charset=UTF-8");
      
      // This allows the customer to define another login page without changing the portal
      wciController.showLoginForm(req, resp);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
   }

   private void clearTokenCookie(HttpServletRequest req, HttpServletResponse resp)
   {
      Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
      cookie.setPath(req.getContextPath());
      cookie.setMaxAge(0);
      resp.addCookie(cookie);
   }

   private void unregisterTokenCookie(HttpServletRequest req)
   {
      String tokenId = getTokenCookie(req);
      if (tokenId != null)
      {
         try
         {
            AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
            tokenService.deleteToken(tokenId);
         }
         catch (Exception e)
         {
            LOG.warn("Cannot delete the token '" + tokenId + "'", e);
         }
      }
   }

   private String getTokenCookie(HttpServletRequest req)
   {
      Cookie[] cookies = req.getCookies();
      if (cookies != null)
      {
         for (Cookie cookie : cookies)
         {
            if (InitiateLoginServlet.COOKIE_NAME.equals(cookie.getName()))
            {
               return cookie.getValue();
            }
         }
      }
      return null;
   }

   /**
    * @see org.exoplatform.container.web.AbstractHttpServlet#requirePortalEnvironment()
    */
   @Override
   protected boolean requirePortalEnvironment()
   {
      return true;
   }
}

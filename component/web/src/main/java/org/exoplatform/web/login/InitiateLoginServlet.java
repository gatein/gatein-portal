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
import org.exoplatform.web.security.Credentials;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.TransientTokenService;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Initiate the login dance.
 * 
 * @author <a href="mailto:trong.tran@exoplatform.com">Tran The Trong</a>
 * @version $Revision$
 */
public class InitiateLoginServlet extends AbstractHttpServlet
{
   /**
    * Serial version ID
    */
   private static final long serialVersionUID = -2553824531076121642L;

   public static final String COOKIE_NAME = "rememberme";

   public static final String CREDENTIALS = "credentials";

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      HttpSession session = req.getSession();
      Credentials credentials = (Credentials)session.getAttribute(InitiateLoginServlet.CREDENTIALS);
      session.setAttribute("initialURI", req.getAttribute("javax.servlet.forward.request_uri"));

      if (credentials == null)
      {
         String token = getTokenCookie(req);
         PortalContainer pContainer = PortalContainer.getInstance();
         ServletContext context = pContainer.getPortalContext();
         if (token != null)
         {
            AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
            credentials = tokenService.validateToken(token, false);
            if (credentials == null)
            {
               Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
               cookie.setPath(req.getContextPath());
               cookie.setMaxAge(0);
               resp.addCookie(cookie);
               // This allows the customer to define another login page without changing the portal
               context.getRequestDispatcher("/login/jsp/login.jsp").include(req, resp);
               return;
            }
         }
         else
         {
            // This allows the customer to define another login page without changing the portal
            context.getRequestDispatcher("/login/jsp/login.jsp").include(req, resp);
            return;
         }
      }
      else
      {
         req.getSession().removeAttribute(InitiateLoginServlet.CREDENTIALS);
      }
      String token = null;
      for (Cookie cookie : req.getCookies())
      {
         if (InitiateLoginServlet.COOKIE_NAME.equals(cookie.getName()))
         {
            token = cookie.getValue();
            break;
         }
      }
      if (token == null)
      {
         TransientTokenService tokenService = AbstractTokenService.getInstance(TransientTokenService.class);
         token = tokenService.createToken(credentials);
      }

      sendAuth(resp, credentials.getUsername(), token);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
   }

   private void sendAuth(HttpServletResponse resp, String jUsername, String jPassword) throws IOException
   {
      String url = "j_security_check?j_username=" + jUsername + "&j_password=" + jPassword;
      url = resp.encodeRedirectURL(url);

      resp.sendRedirect(url);
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

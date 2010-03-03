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

import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.web.security.Credentials;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:trong.tran@exoplatform.com">Tran The Trong</a>
 * @version $Revision$
 */
public class PortalLoginController extends AbstractHttpServlet
{

   /**
    * Serial version ID.
    */
   private static final long serialVersionUID = -9167273087235951389L;

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      //
      String username = req.getParameter("username");
      String password = req.getParameter("password");
      Credentials credentials = new Credentials(username, password);
      req.getSession().setAttribute(InitiateLoginServlet.CREDENTIALS, credentials);

      String uri = (String)req.getSession().getAttribute("initialURI");
      if (uri == null || uri.length() == 0)
      {
         String pathInfo = req.getPathInfo();
         if (pathInfo == null)
         {
            pathInfo = "/";
         }
         uri = req.getContextPath() + "/private" + pathInfo;
      }

      //
      String rememberme = req.getParameter("rememberme");
      if ("true".equals(rememberme))
      {
         boolean isRemember = "true".equals(req.getParameter(InitiateLoginServlet.COOKIE_NAME));
         if (isRemember)
         {
            //Create token
            AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
            String cookieToken = tokenService.createToken(credentials);
            Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, cookieToken);
            cookie.setPath(req.getContextPath());
            cookie.setMaxAge((int)tokenService.getValidityTime() / 1000);
            resp.addCookie(cookie);
         }
      }
      resp.sendRedirect(uri);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
   }

   @Override
   protected boolean requirePortalEnvironment()
   {
      return true;
   }
}

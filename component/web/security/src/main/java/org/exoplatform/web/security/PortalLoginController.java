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

package org.exoplatform.web.security;

import org.exoplatform.web.login.InitiateLoginServlet;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.security.WCILoginController;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class PortalLoginController extends WCILoginController {

   /** . */
   private static final Logger log = LoggerFactory.getLogger(PortalLoginController.class);

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
   {
      super.doGet(req, resp);
      
      String username = req.getParameter("username");
      String password = req.getParameter("password");
      
      //
      if (username != null && password != null)
      {
         // if we do have a remember me
         String rememberme = req.getParameter("rememberme");
         if ("true".equals(rememberme))
         {
            boolean isRemember = "true".equals(req.getParameter(InitiateLoginServlet.COOKIE_NAME));
            if (isRemember)
            {
               //Create token
               AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
               Credentials credentials = (Credentials)req.getSession().getAttribute(Credentials.CREDENTIALS);
               String cookieToken = tokenService.createToken(credentials);

               log.debug("Found a remember me request parameter, created a persistent token " + cookieToken + " for it and set it up " +
                  "in the next response");
               Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, cookieToken);
               cookie.setPath(req.getContextPath());
               cookie.setMaxAge((int)tokenService.getValidityTime());
               resp.addCookie(cookie);
            }
         }
      }

      // Obtain initial URI
      String uri = req.getParameter("initialURI");

      // otherwise compute one
      if (uri == null || uri.length() == 0)
      {
         uri = req.getContextPath();
         log.debug("No initial URI found, will use default " + uri + " instead ");
      }
      else
      {
         log.debug("Found initial URI " + uri);
      }

      //
      String redirectURI = req.getContextPath() + "/dologin?initialURI=" + URLEncoder.encode(uri, "UTF-8");
      resp.sendRedirect(resp.encodeRedirectURL(redirectURI));
   }
}

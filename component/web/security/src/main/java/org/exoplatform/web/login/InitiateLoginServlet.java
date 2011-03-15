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
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.TicketConfiguration;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.WCIController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Initiate the login dance.
 * 
 * @author <a href="mailto:trong.tran@exoplatform.com">Tran The Trong</a>
 * @version $Revision$
 */
public class InitiateLoginServlet extends AbstractHttpServlet
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(InitiateLoginServlet.class);

   /** . */
   public static final String COOKIE_NAME = "rememberme";

   /** . */
   public static final long LOGIN_VALIDITY =
           1000 * TicketConfiguration.getInstance(TicketConfiguration.class).getValidityTime();

   /** . */
   private WCIController wciController;

   /** . */
   private ServletContainer servletContainer = DefaultServletContainerFactory.getInstance().getServletContainer();

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setContentType("text/html; charset=UTF-8");

      Credentials credentials = getWCIController().getCredentials(req, resp);

      //
      if (credentials == null)
      {
         //
         String token = getRememberMeTokenCookie(req);
         if (token != null)
         {
            AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
            credentials = tokenService.validateToken(token, false);
            if (credentials == null)
            {
               log.debug("Login initiated with no credentials in session but found token an invalid " + token + " " +
                  "that will be cleared in next response");

               // We clear the cookie in the next response as it was not valid
               Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
               cookie.setPath(req.getContextPath());
               cookie.setMaxAge(0);
               resp.addCookie(cookie);

               // This allows the customer to define another login page without
               // changing the portal
               getWCIController().showLoginForm(req, resp);
            }
            else
            {
               // Send authentication request
               log.debug("Login initiated with no credentials in session but found token " + token + " with existing credentials, " +
                  "performing authentication");
               getWCIController().sendAuth(req, resp, credentials.getUsername(), token);
            }
         }
         else
         {
            // This allows the customer to define another login page without
            // changing the portal
            log.debug("Login initiated with no credentials in session and no token cookie, redirecting to login page");
            getWCIController().showLoginForm(req, resp);
         }
      }
      else
      {
         // WCI authentication
         servletContainer.login(req, resp, credentials, LOGIN_VALIDITY, wciController.getInitialURI(req));
      }
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
   }

   /**
    * Extract the remember me token from the request or returns null.
    *
    * @param req the incoming request
    * @return the token
    */
   public static String getRememberMeTokenCookie(HttpServletRequest req)
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

   private WCIController getWCIController() {
      if (wciController == null) {
         wciController = new GateinWCIController(getServletContext());
      }
      return wciController;
   }
}

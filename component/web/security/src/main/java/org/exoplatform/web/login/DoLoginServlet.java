/*
 * Copyright (C) 2011 eXo Platform SAS.
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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DoLoginServlet extends AbstractHttpServlet
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(DoLoginServlet.class);

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String initialURI = req.getParameter("initialURI");
      log.debug("Performing the do login send redirect with initialURI=" + initialURI + " and remoteUser=" + req.getRemoteUser());
 
      if (initialURI == null || initialURI.length() == 0)
      {
         initialURI = req.getContextPath();
      }

      try
      {
         URI uri = new URI(initialURI);
         if (uri.isAbsolute() && !(uri.getHost().equals(req.getServerName())))
         {
            log.warn("Cannot redirect to an URI outside of the current host when using a login redirect. Redirecting to the portal context path instead.");
            initialURI = req.getContextPath();
         }
      }
      catch (URISyntaxException e)
      {
         log.warn("Initial URI in login link is malformed. Redirecting to the portal context path instead.");
         initialURI = req.getContextPath();
      }

      // Now user is successfuly authenticated, so that we can remove credentials from temporary AuthenticationRegistry
      // and add them to ConversationState
      Credentials credentials = removeCredentialsFromRegistry(req);
      setCredentialsToConversationState(credentials);

      //
      resp.sendRedirect(resp.encodeRedirectURL(initialURI));
   }

   /**
    * Remove credentials from temporary AuthenticationRegistry because authentication of user is now finished.
    *
    * @param req
    * @return credentials,which were removed from AuthenticationRegistry
    */
   protected Credentials removeCredentialsFromRegistry(HttpServletRequest req)
   {
      AuthenticationRegistry authenticationRegistry = (AuthenticationRegistry)getContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
      return authenticationRegistry.removeCredentials(req);
   }
   
   /**
    * Add credentials to {@link ConversationState}.
    *
    * @param credentials
    */
   protected void setCredentialsToConversationState(Credentials credentials)
   {
      ConversationState currentConversationState = ConversationState.getCurrent();
      if (currentConversationState != null && credentials != null)
      {
         log.debug("Adding credentials to conversationState for user " + credentials.getUsername());
         currentConversationState.setAttribute(Credentials.CREDENTIALS, credentials);
      }
   }
}

/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.exoplatform.container.ExoContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.jboss.web.tomcat.service.sso.ClusteredSingleSignOn;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Helper valve for supporting JBoss clustered SSO Valve. Clustered SSO Valve requires reauthentication of user with same password on
 * all cluster nodes. So we need to use real password of user instead of wci ticket for updating SSO valve, which ensure that user
 * will be reauthenticated on second cluster node by SSO valve with his real password.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PortalClusteredSSOSupportValve extends ValveBase
{

   // Name of session note where credentials will be stored. Credentials are removed after update of SSO valve
   private static final String NOTE_CREDENTIALS = "org.exoplatform.web.login.PortalClusteredSSOSupportValve.NOTE_CREDENTIALS";

   // Logger
   private static final Logger log = LoggerFactory.getLogger(PortalClusteredSSOSupportValve.class);

   // JBoss clustered SSO valve
   private ClusteredSingleSignOn ssoValve = null;

   // The Context to which this Valve is attached.
   private Context context = null;


   @Override
   public void setContainer(Container container)
   {
      if (!(container instanceof Context))
      {
         throw new IllegalArgumentException(sm.getString("authenticator.notContext"));
      }
      super.setContainer(container);
      this.context = (Context) container;

      findSSOValve();
   }

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      if (ssoValve != null)
      {
         Session tomcatSession = request.getSessionInternal();

         // This means that request is going to PortalLoginController to start Login request
         if ("/login".equals(request.getServletPath()) && request.getNote(Constants.REQ_SSOID_NOTE) == null)
         {
            String password = request.getParameter("password");
            if (log.isDebugEnabled())
            {
               log.debug("Saving ccredentials into session note for SSO valve.");
            }
            tomcatSession.setNote(NOTE_CREDENTIALS, password);
         }
         else if (tomcatSession.getNote(NOTE_CREDENTIALS) != null && tomcatSession.getPrincipal() != null)
         {
            // We are just logged. SSO valve needs to be updated with "real" password of user instead of wci ticket.
            String password = (String) tomcatSession.getNote(NOTE_CREDENTIALS);
            tomcatSession.removeNote(NOTE_CREDENTIALS);
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);

            if (password != null && ssoId != null)
            {
               if (log.isDebugEnabled())
               {
                  log.debug("Update SSO valve values with real credentials of user " + request.getRemoteUser());
               }
               ssoValve.register(
                     ssoId,
                     tomcatSession.getPrincipal(),
                     tomcatSession.getAuthType(),
                     request.getRemoteUser(),
                     password);
            }
         }
      }

      // Continue with HTTP request processing
      getNext().invoke(request, response);
   }

   // finding sso valve
   private void findSSOValve()
   {
      if (!ExoContainer.getProfiles().contains("cluster"))
      {
         return;
      }

      Container parent = context.getParent();
      while ((ssoValve == null) && (parent != null))
      {
         if (!(parent instanceof Pipeline))
         {
            parent = parent.getParent();
            continue;
         }
         Valve valves[] = ((Pipeline) parent).getValves();
         for (Valve valve : ((Pipeline) parent).getValves())
         {
            if (valve instanceof ClusteredSingleSignOn)
            {
               ssoValve = (ClusteredSingleSignOn) valve;
               break;
            }
         }
         if (ssoValve == null)
         {
            parent = parent.getParent();
         }
      }

      if (ssoValve != null)
      {
         log.info("Found JBoss ClusteredSingleSignOn Valve at " + ssoValve);
      }
      else
      {
         log.info("No JBoss ClusteredSingleSignOn Valve is present");
      }
   }
}

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
import org.apache.catalina.Pipeline;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.connector.Request;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Helper filter for JBoss clustered SSO Valve. Valve requires reauthentication of user with same password on all cluster nodes.
 * So we need to use real password of user instead of wci ticket for updating SSO valve, which ensure that user will be reauthenticated
 * on second cluster node by SSO valve with his real password.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JBossClusteredSSOValveFilter extends AbstractFilter
{
   // Name of session note where credentials will be stored. Credentials are removed after update of SSO valve
   private static final String NOTE_CREDENTIALS = "org.exoplatform.web.login.JBossClusteredSSOValveFilter.NOTE_CREDENTIALS";

   // Logger
   private static final Logger log = LoggerFactory.getLogger(JBossClusteredSSOValveFilter.class);

   // JBoss clustered SSO valve
   private SingleSignOn ssoValve = null;

   // Whether attempt to find sso valve has been already performed
   private boolean ssoValveAttempted = false;

   // Helper enum, which is used to obtain instance of catalina request
   private FilterStateEnum filterState = FilterStateEnum.BEFORE_INIT;

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      SingleSignOn ssoValve = getSSOValve();

      // Simply continue with filter chain if jboss sso valve not present. Otherwise process it.
      if (ssoValve != null)
      {
         // Get Catalina request (accessible only on JBoss)
         Request tomcatRequest = getCurrentCatalinaRequest();

         if (tomcatRequest != null)
         {
            Session tomcatSession = tomcatRequest.getSessionInternal();

            // This means that request is going to PortalLoginController to start Login request
            if ("/login".equals(tomcatRequest.getServletPath()) && tomcatRequest.getNote(Constants.REQ_SSOID_NOTE) == null)
            {
               chain.doFilter(request, response);

               // Now credentials are in session and we can save them as note (notes are not replicated)
               Credentials credentials = (Credentials) tomcatSession.getSession().getAttribute(Credentials.CREDENTIALS);
               if (log.isDebugEnabled())
               {
                  log.debug("Saving credentials " + credentials + " into session note for SSO valve.");
               }
               tomcatSession.setNote(NOTE_CREDENTIALS, credentials);

               return;
            }
            else if (tomcatSession.getNote(NOTE_CREDENTIALS) != null && tomcatRequest.getRemoteUser() != null)
            {
               // We are just logged. SSO valve needs to be updated with "real" password of user instead of wci ticket.
               Credentials credentials = (Credentials) tomcatSession.getNote(NOTE_CREDENTIALS);
               tomcatSession.removeNote(NOTE_CREDENTIALS);

               if (credentials != null)
               {
                  String ssoId = (String) tomcatRequest.getNote(Constants.REQ_SSOID_NOTE);
                  if (log.isDebugEnabled())
                  {
                     log.debug("Update SSO valve values with real credentials of user " + credentials.getUsername());
                  }
                  ssoValve.register(
                        ssoId,
                        tomcatSession.getPrincipal(),
                        tomcatRequest.getAuthType(),
                        credentials.getUsername(),
                        credentials.getPassword());
               }
            }
         }
      }

      // Continue with filter chain
      chain.doFilter(request, response);
   }

   @Override
   public void destroy()
   {
      this.ssoValve = null;
   }

   // ------------------------------------------------------ Private helper methods

   private SingleSignOn getSSOValve()
   {

      // Return ssoValve if some client already tried to find it
      if (ssoValveAttempted)
      {
         return ssoValve;
      }

      // Find ssoValve
      findSSOValve();

      return ssoValve;
   }

   private synchronized void findSSOValve()
   {
      // This means that another client already tried to find sso valve
      if (ssoValveAttempted)
      {
         return;
      }

      // We need to have "cluster" profile active, and we need to be on JBoss. Otherwise SSO valve can't be used
      if (!ExoContainer.getProfiles().contains("cluster"))
      {
      }
      else if (getCurrentCatalinaRequest() == null)
      {
      }
      else
      {
         // Let's try to find SSOValve with JBossWeb api
         Request req = getCurrentCatalinaRequest();

         Wrapper wrapper = req.getWrapper();
         Container parent = wrapper.getParent();

         while ((ssoValve == null) && (parent != null))
         {
            if (!(parent instanceof Pipeline))
            {
               parent = parent.getParent();
               continue;
            }
            Valve valves[] = ((Pipeline) parent).getValves();
            for (int i = 0; i < valves.length; i++)
            {
               if (valves[i] instanceof SingleSignOn)
               {
                  ssoValve = (SingleSignOn) valves[i];
                  break;
               }
            }
            if (ssoValve == null)
            {
               parent = parent.getParent();
            }
         }
      }

      // Mark that finding has been performed
      this.ssoValveAttempted = true;

      if (ssoValve != null)
      {
         log.info("Found JBoss ClusteredSingleSignOn Valve at " + ssoValve);
      }
      else
      {
         log.info("No JBoss ClusteredSingleSignOn Valve is present");
      }
   }

   // Obtain correct instance of catalina Request. Current request is bound to ThreadLocal field, but class is different in JBoss 5 and JBoss 6.
   private Request getCurrentCatalinaRequest()
   {
      if (filterState == FilterStateEnum.SECURITY_ASSOC_VALVE)
      {
         return getCurrentTomcatRequestFromValve("org.jboss.web.tomcat.security.SecurityAssociationValve");
      }
      else if (filterState == FilterStateEnum.ACTIVE_REQUEST_VALVE)
      {
         return getCurrentTomcatRequestFromValve("org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve");
      }
      else if (filterState == FilterStateEnum.NOT_JBOSS)
      {
         return null;
      }
      else
      {
         // We need to find correct valve and obtain catalina request from it
         Request req = getCurrentTomcatRequestFromValve("org.jboss.web.tomcat.security.SecurityAssociationValve");
         if (req != null)
         {
            filterState = FilterStateEnum.SECURITY_ASSOC_VALVE;
         }
         else
         {
            req = getCurrentTomcatRequestFromValve("org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve");
            if (req != null)
            {
               filterState = FilterStateEnum.ACTIVE_REQUEST_VALVE;
            }
            else
            {
               filterState = FilterStateEnum.NOT_JBOSS;
            }
         }

         return req;
      }
   }

   private Request getCurrentTomcatRequestFromValve(String valveClassName)
   {
      try
      {
         Class<?> securityAssociationValve = Thread.currentThread().getContextClassLoader().loadClass(valveClassName);
         Field activeRequest = securityAssociationValve.getDeclaredField("activeRequest");
         ThreadLocal<Request> threadLocal = (ThreadLocal<Request>) activeRequest.get(null);
         Request req = threadLocal.get();
         if (log.isTraceEnabled())
         {
            log.trace("Current catalina Request obtained from " + valveClassName);
         }
         return req;
      }
      catch (Exception e)
      {
         log.debug("Can't obtain current catalina Request from " + valveClassName);
         return null;
      }
   }

   private enum FilterStateEnum
   {
      // We didn't try to obtain any catalina Request
      BEFORE_INIT,
      // We are not on JBoss
      NOT_JBOSS,
      // We are on JBoss 5.1 or earlier (Current catalina request can be obtained from SecurityAssociationValve threadlocal field)
      SECURITY_ASSOC_VALVE,
      // We are on JBoss 6 or EPP 5.1.1 (Current catalina request can be obtained from ActiveRequestResponseValve threadlocal field)
      ACTIVE_REQUEST_VALVE
   }

}

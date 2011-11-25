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

package org.exoplatform.web.security;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.security.jaas.AbstractLoginModule;
import org.gatein.wci.security.Credentials;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * A login module implementation that is used to handle reauthentication of client with same HTTP session on various cluster nodes.
 * After login of user on cluster node is attribute "authenticatedCredentials" added to HTTP session in method {@link #commit()}.
 * Other cluster nodes can than read these credentials in method {@link #login()}, and can reuse them to relogin.
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalLoginModule extends AbstractLoginModule
{

   /** Logger. */
   private static final Log log = ExoLogger.getLogger(PortalLoginModule.class);

   /** JACC get context method. */
   private static final Method getContextMethod;

   static
   {
      Method getContext = null;

      log.debug("About to configure PortalLoginModule");
      try
      {
         Class<?> policyContextClass = Thread.currentThread().getContextClassLoader().loadClass("javax.security.jacc.PolicyContext");
         getContext = policyContextClass.getDeclaredMethod("getContext", String.class);
      }
      catch (ClassNotFoundException ignore)
      {
         log.debug("JACC not found ignoring it", ignore);
      }
      catch (Exception e)
      {
         log.error("Could not obtain JACC get context method", e);
      }

      //
      getContextMethod = getContext;
   }

   public static final String AUTHENTICATED_CREDENTIALS = "authenticatedCredentials";

   private static final String LOGIN_ON_DIFFERENT_NODE = "PortalLoginModule.loginOnDifferentNode";

   /**
    * @see javax.security.auth.spi.LoginModule#login()
    */
   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException
   {
      if (getContextMethod != null)
      {
         Credentials authCredentials = null;

         try
         {
            HttpServletRequest request = getCurrentHttpServletRequest();

            // This can be the case with CLI login
            if (request == null)
            {
               log.debug("Unable to find HTTPServletRequest.");
               return false;
            }

            authCredentials = (Credentials)request.getSession().getAttribute(AUTHENTICATED_CREDENTIALS);

            // If authenticated credentials were presented in HTTP session, it means that we were already logged on different cluster node
            // with this HTTP session. We don't need to validate password again in this case (We don't have password anyway)
            if (authCredentials != null)
            {
               Authenticator authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);
               if (authenticator == null)
               {
                  throw new LoginException("No Authenticator component found, check your configuration");
               }

               String username = authCredentials.getUsername();
               Identity identity = authenticator.createIdentity(username);

               sharedState.put("exo.security.identity", identity);
               sharedState.put("javax.security.auth.login.name", username);

               subject.getPublicCredentials().add(new UsernameCredential(username));

               // Add empty password to subject and remove password key, so that SharedStateLoginModule won't be processed
               subject.getPrivateCredentials().add("");
               sharedState.remove("javax.security.auth.login.password");

               // Add flag that we were logged with real password on different cluster node. Not on this node.
               sharedState.put(LOGIN_ON_DIFFERENT_NODE, true);
            }
         }
         catch(Exception e)
         {
            log.error(this, e);
            LoginException le = new LoginException(e.getMessage());
            le.initCause(e);
            throw le;
         }
      }
      return true;
   }

   /**
    * @see javax.security.auth.spi.LoginModule#commit()
    */
   public boolean commit() throws LoginException
   {
      // Add authenticated credentials to session only if we were logged on this host with "real" credentials
      if (getContextMethod != null &&
         isClusteredSSO() &&
         sharedState.containsKey("javax.security.auth.login.name") &&
         sharedState.containsKey("javax.security.auth.login.password") &&
         sharedState.get(LOGIN_ON_DIFFERENT_NODE) == null)
      {
         String uid = (String)sharedState.get("javax.security.auth.login.name");

         Credentials wc = new Credentials(uid, "");

         HttpServletRequest request = null;
         try
         {
            request = getCurrentHttpServletRequest();

            // This can be the case with CLI login
            if (request == null)
            {
               log.debug("Unable to find HTTPServletRequest.");
            }
            else
            {
               request.getSession().setAttribute(AUTHENTICATED_CREDENTIALS, wc);
               handleCredentialsRemoving(request);
            }
         }
         catch(Exception e)
         {
            log.error(this,e);
            log.error("LoginModule error. Turn off session credentials checking with proper configuration option of " +
               "LoginModule set to false");
         }
      }
      return true;
   }

   /**
    * @see javax.security.auth.spi.LoginModule#abort()
    */
   public boolean abort() throws LoginException
   {
      return true;
   }

   /**
    * @see javax.security.auth.spi.LoginModule#logout()
    */
   public boolean logout() throws LoginException
   {
      return true;
   }

   @Override
   protected Log getLogger()
   {
      return log;
   }

   protected static boolean isClusteredSSO()
   {
      return ExoContainer.getProfiles().contains("cluster");
   }

   /**
    * Remove credentials of authenticated user from HTTP session.
    *
    * @param request httpRequest
    */
   protected void handleCredentialsRemoving(HttpServletRequest request)
   {
      // TODO: We can't remove credentials from HTTP session right now because WSRP-Security relies on it. See method WSSecurityCredentialHelper.handleRequest
      // request.getSession().removeAttribute(Credentials.CREDENTIALS);
   }

   private HttpServletRequest getCurrentHttpServletRequest()
   {
      HttpServletRequest request = null;
      try
      {
         if (getContextMethod != null)
         {
            request = (HttpServletRequest)getContextMethod.invoke(null, "javax.servlet.http.HttpServletRequest");
         }
      }
      catch (Exception e)
      {
         log.debug("Exception when trying to obtain HTTPServletRequest.", e);
      }

      return request;
   }
}

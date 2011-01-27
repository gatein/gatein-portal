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

package org.exoplatform.web.security;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.jaas.AbstractLoginModule;
import org.exoplatform.web.login.InitiateLoginServlet;
import org.gatein.wci.security.Credentials;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * A login module implementation that relies on the token store to check the
 * password validity. If the token store provides a valid {@link Credentials}
 * value then password stacking is used and the two entries are added in the
 * shared state map. The first entry is keyed by
 * <code>javax.security.auth.login.name</code> and contains the
 * {@link Credentials#getUsername()} value, the second entry is keyed by
 * <code>javax.security.auth.login.password</code> and contains the
 * {@link Credentials#getPassword()} ()} value.
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
      if (isClusteredSSO())
      {
         log.debug("About to configure clustered SSO");
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
      }

      //
      getContextMethod = getContext;
   }

   public static final String AUTHENTICATED_CREDENTIALS = "authenticatedCredentials";

   /**
    * @see javax.security.auth.spi.LoginModule#login()
    */
   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException
   {

      Callback[] callbacks = new Callback[2];
      callbacks[0] = new NameCallback("Username");
      callbacks[1] = new PasswordCallback("Password", false);

      try
      {
         callbackHandler.handle(callbacks);
         String password = new String(((PasswordCallback)callbacks[1]).getPassword());

         Credentials c = null;
         
         //
         // For clustered config check credentials stored and propagated in session. This won't work in tomcat because
         // of lack of JACC PolicyContext so the code must be a bit defensive
         if (getContextMethod != null && password.startsWith(InitiateLoginServlet.COOKIE_NAME))
         {
            HttpServletRequest request;
            try
            {
               request = (HttpServletRequest)getContextMethod.invoke(null, "javax.servlet.http.HttpServletRequest");
               Object o = request.getSession().getAttribute(AUTHENTICATED_CREDENTIALS);

               if (o instanceof Credentials)
               {
                 c = (Credentials) o;
               }
            }
            catch(Throwable e)
            {
               log.error(this,e);
               log.error("LoginModule error. Turn off session credentials checking with proper configuration option of " +
                  "LoginModule set to false");
            }
         }

         if (c != null)
         {
           sharedState.put("javax.security.auth.login.name", c.getUsername());
           sharedState.put("javax.security.auth.login.password", c.getPassword());
         }
         return true;
      }
      catch (Exception e)
      {
         LoginException le = new LoginException();
         le.initCause(e);
         throw le;
      }
   }

   /**
    * @see javax.security.auth.spi.LoginModule#commit()
    */
   public boolean commit() throws LoginException
   {

      if (getContextMethod != null &&
         sharedState.containsKey("javax.security.auth.login.name") &&
         sharedState.containsKey("javax.security.auth.login.password"))
      {
         String uid = (String)sharedState.get("javax.security.auth.login.name");
         String pass = (String)sharedState.get("javax.security.auth.login.password");

         Credentials wc = new Credentials(uid, pass);

         HttpServletRequest request = null;
         try
         {
            request = (HttpServletRequest)getContextMethod.invoke(null, "javax.servlet.http.HttpServletRequest");
            request.getSession().setAttribute(AUTHENTICATED_CREDENTIALS, wc);
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
}

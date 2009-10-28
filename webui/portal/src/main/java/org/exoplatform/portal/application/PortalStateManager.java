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

package org.exoplatform.portal.application;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.SessionManagerContainer;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.ConfigurationManager;
import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PortalStateManager extends StateManager
{

   protected static Log log = ExoLogger.getLogger("portal:PortalStateManager");

   /** ConcurrentMap<SessionId, HashMap<PortalName, PortalApplicationState>> **/
   private ConcurrentMap<String, HashMap<String, PortalApplicationState>> uiApplications =
      new ConcurrentHashMap<String, HashMap<String, PortalApplicationState>>();

   /**
    * This method is used to restore the UI component tree either the current request targets a portlet 
    * or the portal. 
    * 
    * In both cases, if the tree is not stored already it is created and then stored in a local Map 
    * 
    */
   @SuppressWarnings("unchecked")
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      context.setStateManager(this);
      WebuiApplication app = (WebuiApplication)context.getApplication();

      /*
       * If the request context is of type PortletRequestContext, we extract the parent context which will
       * allow to get access to the PortalApplicationState object thanks to the session id used as the key for the
       * synchronize Map uiApplications
       */
      if (context instanceof PortletRequestContext)
      {
         WebuiRequestContext preqContext = (WebuiRequestContext)context.getParentAppRequestContext();         
         PortletRequestContext pcontext = (PortletRequestContext)context;
         String key = pcontext.getApplication().getApplicationId() + "/" + pcontext.getWindowId();
         PortalApplicationState state = getApplicationState(preqContext);
         UIApplication uiApplication = state.get(key);
         if (uiApplication != null)
            return uiApplication;
         ConfigurationManager cmanager = app.getConfigurationManager();
         String uirootClass = cmanager.getApplication().getUIRootComponent();
         Class type = Thread.currentThread().getContextClassLoader().loadClass(uirootClass);
         uiApplication = (UIApplication)app.createUIComponent(type, null, null, context);
         state.put(key, uiApplication);
         return uiApplication;
      }

      PortalRequestContext pcontext = (PortalRequestContext)context;
      PortalApplicationState state = getApplicationState(pcontext);
      if (state != null)
      {
         if (!Safe.equals(pcontext.getRemoteUser(), state.getUserName()))
         {
            clearSession(pcontext.getRequest().getSession());
            state = null;
         }
      }
      if (state == null)
      {
         ConfigurationManager cmanager = app.getConfigurationManager();
         String uirootClass = cmanager.getApplication().getUIRootComponent();
         Class type = Thread.currentThread().getContextClassLoader().loadClass(uirootClass);
         UserPortalConfig config = getUserPortalConfig(pcontext);
         if (config == null)
         {
            HttpServletResponse response = pcontext.getResponse();
            //        if(pcontext.getRemoteUser() == null) {
            //          String portalName = pcontext.getPortalOwner() ;
            //          portalName = URLEncoder.encode(portalName, "UTF-8") ;
            //          String redirect = pcontext.getRequest().getContextPath() + "/private/" + portalName + "/";
            //          response.sendRedirect(redirect);
            //        }
            //        else response.sendRedirect(pcontext.getRequest().getContextPath() + "/portal-unavailable.jsp");
            response.sendRedirect(pcontext.getRequest().getContextPath() + "/portal-unavailable.jsp");
            pcontext.setResponseComplete(true);
            return null;
         }
         pcontext.setAttribute(UserPortalConfig.class, config);
         UIPortalApplication uiApplication = (UIPortalApplication)app.createUIComponent(type, null, null, context);
         state = new PortalApplicationState(uiApplication, pcontext.getRemoteUser());
         cacheApplicationState(pcontext.getSessionId(), pcontext.getPortalOwner(),state);
         SessionManagerContainer pcontainer = (SessionManagerContainer)app.getApplicationServiceContainer();
         pcontainer.createSessionContainer(context.getSessionId(), uiApplication.getOwner());
      }
      return state.getUIPortalApplication();
   }

   @SuppressWarnings("unused")
   public void storeUIRootComponent(WebuiRequestContext context)
   {
   }

   public void expire(String sessionId, WebuiApplication app)
   {
      uiApplications.remove(sessionId);
      SessionManagerContainer pcontainer = (SessionManagerContainer)app.getApplicationServiceContainer();
      pcontainer.removeSessionContainer(sessionId);
   }
   
   private PortalApplicationState getApplicationState(WebuiRequestContext context) {
     PortalRequestContext portalContext = null;
     if (context instanceof PortalRequestContext)
       portalContext = (PortalRequestContext)context;
     else 
       portalContext = (PortalRequestContext)context.getParentAppRequestContext();
     String portalName = portalContext.getPortalOwner();
     String sessionId = portalContext.getSessionId();
     
     HashMap<String, PortalApplicationState> appStates = uiApplications.get(sessionId);
     return (appStates == null) ? null : appStates.get(portalName);
   }
   
   private void cacheApplicationState(String sessionId, String portalName, PortalApplicationState state) {
      HashMap<String, PortalApplicationState> appStates = uiApplications.get(sessionId);
      if (appStates == null) {
          appStates = new HashMap<String, PortalApplicationState>();
          uiApplications.put(sessionId, appStates);
      }
      appStates.put(portalName, state);
   }

   private UserPortalConfig getUserPortalConfig(PortalRequestContext context) throws Exception
   {
      ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService service_ =
         (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      String remoteUser = context.getRemoteUser();
      String ownerUser = context.getPortalOwner();
      return service_.getUserPortalConfig(ownerUser, remoteUser);
   }

   private void clearSession(HttpSession session)
   {
      Enumeration<?> e = session.getAttributeNames();
      while (e.hasMoreElements())
      {
         String name = (String)e.nextElement();
         session.removeAttribute(name);
      }
   }

   @SuppressWarnings("serial")
   static public class PortalApplicationState extends HashMap<String, UIApplication>
   {

      private final UIPortalApplication uiPortalApplication_;

      private final String userName_;

      public PortalApplicationState(UIPortalApplication uiPortalApplication, String userName)
      {
         uiPortalApplication_ = uiPortalApplication;
         userName_ = userName;
      }

      public String getUserName()
      {
         return userName_;
      }

      public UIPortalApplication getUIPortalApplication()
      {
         return uiPortalApplication_;
      }
   }
}
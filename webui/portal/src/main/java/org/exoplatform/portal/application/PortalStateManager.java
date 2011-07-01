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
import org.exoplatform.portal.application.replication.ApplicationState;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.webui.application.ConfigurationManager;
import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PortalStateManager extends StateManager
{

   /** . */
   private static final String APPLICATION_ATTRIBUTE_PREFIX = "psm.";

   /** . */
   private static final Logger log = LoggerFactory.getLogger(PortalStateManager.class);

   @Override
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      context.setStateManager(this);

      //
      WebuiApplication app = (WebuiApplication)context.getApplication();

      //
      ApplicationState appState = null;
      HttpSession session = getSession(context);
      String key = getKey(context);
      if (session != null)
      {
         appState = (ApplicationState)session.getAttribute(APPLICATION_ATTRIBUTE_PREFIX + key);
      }

      //

      //
      UIApplication uiapp = null;
      if (appState != null)
      {
         if (Safe.equals(context.getRemoteUser(), appState.getUserName()))
         {
            uiapp = appState.getApplication();
         }
      }

      //
      if (appState != null)
      {
         log.debug("Found application " + key + " :" + appState.getApplication());
      }
      else
      {
         log.debug("Application " + key + " not found");
      }

      // Looks like some necessary hacking
      if (context instanceof PortalRequestContext)
      {
         PortalRequestContext portalRC = (PortalRequestContext)context;
         UserPortalConfig config = getUserPortalConfig(portalRC);
         if (config == null)
         {
            HttpServletResponse response = portalRC.getResponse();
            response.sendRedirect(portalRC.getRequest().getContextPath() + "/portal-unavailable.jsp");
            portalRC.setResponseComplete(true);
            return null;
         }
         portalRC.setAttribute(UserPortalConfig.class, config);
      }

      //
      if (uiapp == null)
      {
         ConfigurationManager cmanager = app.getConfigurationManager();
         String uirootClass = cmanager.getApplication().getUIRootComponent();
         Class<? extends UIApplication> type = (Class<UIApplication>) Thread.currentThread().getContextClassLoader().loadClass(uirootClass);
         uiapp = app.createUIComponent(type, null, null, context);
      }

      //
      return uiapp;
   }

   @Override
   public void storeUIRootComponent(final WebuiRequestContext context) throws Exception
   {
      UIApplication uiapp = context.getUIApplication();

      //
      if (uiapp != null)
      {
         HttpSession session = getSession(context);

         // At this point if it returns null it means that it was not possible to create a session
         // because the session might be invalidated and the response is already commited to the client.
         // That situation happens during a logout that invalidates the HttpSession
         if (session != null)
         {
            String key = getKey(context);
            log.debug("Storing application " + key);
            session.setAttribute(APPLICATION_ATTRIBUTE_PREFIX + key, new ApplicationState(uiapp, context.getRemoteUser()));
         }
      }
   }

   @Override
   public void expire(String sessionId, WebuiApplication app) throws Exception
   {
      // For now do nothing....
   }

   public static UserPortalConfig getUserPortalConfig(PortalRequestContext context) throws Exception
   {
      ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService service_ = (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      String remoteUser = context.getRemoteUser();
      String ownerUser = context.getPortalOwner();
      return service_.getUserPortalConfig(ownerUser, remoteUser, PortalRequestContext.USER_PORTAL_CONTEXT);
   }

   private String getKey(WebuiRequestContext webuiRC)
   {
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;
         return portletRC.getApplication().getApplicationId() + "/" + portletRC.getWindowId();
      }
      else
      {
         PortalRequestContext portalRC = (PortalRequestContext)webuiRC;
         String portalOwner = portalRC.getPortalOwner();
         return "portal_" + portalOwner;
      }
   }

   private HttpSession getSession(WebuiRequestContext webuiRC)
   {
      PortalRequestContext portalRC;
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;
         portalRC = (PortalRequestContext) portletRC.getParentAppRequestContext();
      }
      else
      {
         portalRC = (PortalRequestContext)webuiRC;
      }
      HttpServletRequest req = portalRC.getRequest();
      return req.getSession(false);
   }
}
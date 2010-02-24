/*
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

package org.exoplatform.portal.application.replication;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.LegacyPortalStateManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.webui.application.ConfigurationManager;
import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The basis is either {@link org.exoplatform.webui.core.UIPortletApplication} or
 * {@link org.exoplatform.portal.webui.workspace.UIPortalApplication}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReplicatingStateManager extends StateManager
{

   @Override
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      context.setStateManager(this);

      //
      WebuiApplication app = (WebuiApplication)context.getApplication();

      //
      HttpSession session = getSession(context);
      String key = getKey(context);

      //
      ApplicationState appState = (ApplicationState)session.getAttribute("bilto_" + key);

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
         System.out.println("Found application " + key + " :" + appState.getApplication());
      }
      else
      {
         System.out.println("Application " + key + " not found");
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
//         SessionManagerContainer pcontainer = (SessionManagerContainer)app.getApplicationServiceContainer();
//         pcontainer.createSessionContainer(context.getSessionId(), uiapp.getOwner());
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
      HttpSession session = getSession(context);

      //
      String key = getKey(context);

      //
      System.out.println("Storing application " + key);
      session.setAttribute("bilto_" + key, new ApplicationState(uiapp, context.getRemoteUser()));
   }

   @Override
   public void expire(String sessionId, WebuiApplication app) throws Exception
   {
      // For now do nothing....
   }

   private UserPortalConfig getUserPortalConfig(PortalRequestContext context) throws Exception
   {
      ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService service_ = (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      String remoteUser = context.getRemoteUser();
      String ownerUser = context.getPortalOwner();
      return service_.getUserPortalConfig(ownerUser, remoteUser);
   }

   private String getKey(WebuiRequestContext webuiRC)
   {
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;

         // We are temporarily not using the window id as it changes when the back end is not the same
         return portletRC.getApplication().getApplicationId()/* + "/" + portletRC.getWindowId()*/;
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
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;
         PortalRequestContext portalRC = (PortalRequestContext) portletRC.getParentAppRequestContext();
         HttpServletRequest req = portalRC.getRequest();
         return req.getSession();
      }
      else
      {
         PortalRequestContext portalRC = (PortalRequestContext)webuiRC;
         HttpServletRequest req = portalRC.getRequest();
         return req.getSession();
      }
   }
}

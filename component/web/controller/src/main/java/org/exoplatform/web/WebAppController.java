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

package org.exoplatform.web;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS
 * Mar 21, 2007  
 * 
 * The WebAppController is the entry point of the eXo web framework
 * 
 * It also stores WebRequestHandlers, Attributes and deployed Applications
 * 
 */
public class WebAppController
{

   protected static Log log = ExoLogger.getLogger("portal:WebAppController");

   private HashMap<String, Object> attributes_;

   private volatile HashMap<String, Application> applications_;

   private HashMap<String, WebRequestHandler> handlers_;

   /**
    * The WebAppControler along with the PortalRequestHandler defined in the init() method of the
    * PortalController servlet (controller.register(new PortalRequestHandler())) also add the
    * CommandHandler object that will listen for the incoming /command path in the URL
    * 
    * @throws Exception
    */
   public WebAppController() throws Exception
   {
      applications_ = new HashMap<String, Application>();
      attributes_ = new HashMap<String, Object>();
      handlers_ = new HashMap<String, WebRequestHandler>();
   }

   public Object getAttribute(String name, Object value)
   {
      return attributes_.get(name);
   }

   @SuppressWarnings("unchecked")
   public <T extends Application> T getApplication(String appId)
   {
      return (T)applications_.get(appId);
   }

   public List<Application> getApplicationByType(String type)
   {
      List<Application> applications = new ArrayList<Application>();
      for (Application app : applications_.values())
      {
         if (app.getApplicationType().equals(type))
            applications.add(app);
      }
      return applications;
   }

   public synchronized void removeApplication(String appId)
   {
      applications_.remove(appId);
   }

   /**
    *   Add application (portlet, gadget) to the global application map if and only if it has
    * not been registered yet.
    * 
    * @param <T>
    * @param app
    * @return
    */
   public <T extends Application> T addApplication(T app)
   {
      Application result = getApplication(app.getApplicationId());
      
      //Double-check block
      if(result == null)
      {
         synchronized(this)
         {
            result = getApplication(app.getApplicationId());
            if(result == null)
            {
               HashMap<String, Application> temporalApplicationsMap = new HashMap<String, Application>(applications_);
               temporalApplicationsMap.put(app.getApplicationId(), app);
               this.applications_ = temporalApplicationsMap;
               result = app;
            }
         }
      }
      
      return (T)result;
   }
   
   public void register(WebRequestHandler handler) throws Exception
   {
      for (String path : handler.getPath())
         handlers_.put(path, handler);
   }
   
   public void unregister(String[] paths)
   {
      for (String path : paths)
         handlers_.remove(path);
   }

   /**
    * This is the first method - in the eXo web framework - reached by incoming HTTP request, it acts like a
    * servlet service() method
    * 
    * According to the servlet path used the correct handler is selected and then executed.
    * 
    * The event "exo.application.portal.start-http-request" and "exo.application.portal.end-http-request" are also sent 
    * through the ListenerService and several listeners may listen to it.
    * 
    * Finally a WindowsInfosContainer object using a ThreadLocal (from the portlet-container product) is created 
    */
   public void service(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      WebRequestHandler handler = handlers_.get(req.getServletPath());
      if (log.isDebugEnabled())
      {
         log.debug("Servlet Path: " + req.getServletPath());
         log.debug("Handler used for this path: " + handler);
      }
      if (handler != null)
      {
         ExoContainer portalContainer = ExoContainerContext.getCurrentContainer();
         RequestLifeCycle.begin(portalContainer);
         try
         {
            handler.execute(this, req, res);
         }
         finally
         {
            RequestLifeCycle.end();
         }
      }
   }
}
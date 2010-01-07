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

package org.exoplatform.web.application;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.resolver.ApplicationResourceResolver;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SAS
 * May 7, 2006
 */
abstract public class Application extends BaseComponentPlugin
{

   final static public String JSR168_APPLICATION_TYPE = "jsr168Application";

   final static public String EXO_PORTLET_TYPE = "portlet";

   final static public String EXO_PORTAL_TYPE = "eXoPortal";

   final static public String EXO_GADGET_TYPE = "eXoGadget";

   public final static String WSRP_TYPE = "wsrp";

   private List<ApplicationLifecycle> lifecycleListeners_;

   private ApplicationResourceResolver resourceResolver_;

   private Hashtable<String, Object> attributes_ = new Hashtable<String, Object>();

   abstract public String getApplicationId();

   abstract public String getApplicationType();

   abstract public String getApplicationGroup();

   abstract public String getApplicationName();

   final public ApplicationResourceResolver getResourceResolver()
   {
      return resourceResolver_;
   }

   final public void setResourceResolver(ApplicationResourceResolver resolver)
   {
      resourceResolver_ = resolver;
   }

   final public Object getAttribute(String name)
   {
      return attributes_.get(name);
   }

   final public void setAttribute(String name, Object value)
   {
      attributes_.put(name, value);
   }

   abstract public ResourceBundle getResourceBundle(Locale locale) throws Exception;

   abstract public ResourceBundle getOwnerResourceBundle(String username, Locale locale) throws Exception;

   public ExoContainer getApplicationServiceContainer()
   {
      return ExoContainerContext.getCurrentContainer();
   }

   final public List<ApplicationLifecycle> getApplicationLifecycle()
   {
      return lifecycleListeners_;
   }

   final public void setApplicationLifecycle(List<ApplicationLifecycle> list)
   {
      lifecycleListeners_ = list;
   }

   public void onInit() throws Exception
   {
      for (ApplicationLifecycle lifecycle : lifecycleListeners_)
      {
         lifecycle.onInit(this);
      }
   }

   public void onDestroy() throws Exception
   {
      for (ApplicationLifecycle lifecycle : lifecycleListeners_)
         lifecycle.onDestroy(this);
   }
}
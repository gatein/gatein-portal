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

package org.exoplatform.portal.controller.resource.script;

import org.exoplatform.portal.controller.resource.ResourceRequestHandler;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Module
{

   /** . */
   protected ScriptResource resource;

   /** . */
   protected final String contextPath;

   /** . */
   protected final String name;

   /** . */
   protected int priority;

   Module(ScriptResource resource, String contextPath, String name, int priority)
   {
      this.resource = resource;
      this.contextPath = contextPath;
      this.name = name;
      this.priority = priority;
   }
   
   public static class Remote extends Module
   {

      /** . */
      private final String uri;

      Remote(ScriptResource resource, String contextPath, String name, String uri, int priority)
      {
         super(resource, contextPath, name, priority);
         
         //
         this.uri = uri;
      }

      @Override
      public boolean isRemote()
      {
         return true;
      }

      @Override
      public String getURI()
      {
         return uri;
      }
   }
   
   public static class Local extends Module
   {

      /** . */
      private final String path;

      /** . */
      private final Map<QualifiedName, String> parameters;

      Local(ScriptResource resource, String contextPath, String name, String path, int priority)
      {
         super(resource, contextPath, name, priority);

         //
         Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
         parameters.put(WebAppController.HANDLER_PARAM, "script");
         parameters.put(ResourceRequestHandler.RESOURCE_QN, resource.getId().getName());
         parameters.put(ResourceRequestHandler.SCOPE_QN, resource.getId().getScope().name());
         parameters.put(ResourceRequestHandler.MODULE_QN, name);
         
         //
         this.path = path;
         this.parameters = parameters;
      }

      public String getPath()
      {
         return path;
      }

      public Map<QualifiedName, String> getParameters()
      {
         return parameters;
      }

      @Override
      public boolean isRemote()
      {
         return false;
      }

      @Override
      public String getURI()
      {
         return contextPath + path;
      }
   }

   public ScriptResource getResource()
   {
      return resource;
   }

   public abstract boolean isRemote();

   public abstract String getURI();

   public String getContextPath()
   {
      return contextPath;
   }

   public String getName()
   {
      return name;
   }

   public int getPriority()
   {
      return priority;
   }
}

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

package org.exoplatform.web.application.javascript;

import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.script.Module;
import org.exoplatform.portal.controller.resource.script.ScriptResource;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Javascript
{

   public static Javascript create(ResourceId resource, String module, String path, String contextPath, int priority)
   {
      if (path.startsWith("http://") || path.startsWith("https://"))
      {
         return new Remote(resource, module, contextPath, path, priority);
      }
      else
      {
         return new Local(resource, module, contextPath, path, null, priority);
      }
   }

   public static Javascript create(Module module)
   {
      if (module instanceof Module.Remote)
      {
         Module.Remote remote = (Module.Remote)module;
         return new Remote(module.getResource().getId(), remote.getName(), remote.getContextPath(), remote.getURI(), remote.getPriority());
      }
      else
      {
         Module.Local local = (Module.Local)module;
         return new Local(local.getResource().getId(), local.getName(), local.getContextPath(), local.getPath(), local.getResourceBundle(), local.getPriority());
      }
   }

   /** . */
   protected final ResourceId resource;

   /** . */
   protected final String contextPath;

   /** . */
   protected final String module;

   /** . */
   protected final int priority;
   
   private Javascript(ResourceId resource, String module, String contextPath, int priority)
   {
      this.resource = resource;
      this.contextPath = contextPath;
      this.module = module;
      this.priority = priority < 0 ? Integer.MAX_VALUE : priority;
   }

   public ResourceId getResource()
   {
      return resource;
   }

   public String getModule()
   {
      return module;
   }

   public String getContextPath()
   {
      return contextPath;
   }

   public int getPriority()
   {
      return priority;
   }
   
   abstract Module addModuleTo(ScriptResource resource);

   public abstract boolean isExternalScript();

   @Override
   public String toString()
   {
      return "Javascript[scope=" + resource + ", module=" + module +"]";
   }
   
   public static class Local extends Javascript
   {

      /** . */
      protected final String path;

      /** . */
      protected final String resourceBundle;

      public Local(ResourceId resource, String module, String contextPath, String path, String resourceBundle, int priority)
      {
         super(resource, module, contextPath, priority);

         //
         this.path = path;
         this.resourceBundle = resourceBundle;
      }

      @Override
      Module addModuleTo(ScriptResource resource)
      {
         return resource.addLocalModule(contextPath, module, path, resourceBundle, priority);
      }

      public String getResourceBundle()
      {
         return resourceBundle;
      }

      @Override
      public boolean isExternalScript()
      {
         return false;
      }
   }

   public static class Remote extends Javascript
   {

      /** . */
      protected final String uri;

      public Remote(ResourceId resource, String module, String contextPath, String uri, int priority)
      {
         super(resource, module, contextPath, priority);

         //
         this.uri = uri;
      }

      @Override
      Module addModuleTo(ScriptResource resource)
      {
         return resource.addRemoteModule(contextPath, module, uri, priority);
      }

      @Override
      public boolean isExternalScript()
      {
         return true;
      }
   }
}

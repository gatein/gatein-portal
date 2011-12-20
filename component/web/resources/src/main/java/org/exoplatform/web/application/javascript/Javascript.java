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
public class Javascript
{

   public static Javascript create(ResourceId resource, String module, String path, String contextPath, int priority)
   {
      return new Javascript(resource, module, contextPath, path, priority);
   }

   public static Javascript create(Module module)
   {
      if (module instanceof Module.Remote)
      {
         Module.Remote remote = (Module.Remote)module;
         return new Javascript(module.getResource().getId(), remote.getName(), remote.getContextPath(), remote.getURI(), remote.getPriority());
      }
      else
      {
         Module.Local local = (Module.Local)module;
         return new Javascript(local.getResource().getId(), local.getName(), local.getContextPath(), local.getPath(), local.getPriority());
      }
   }

   /** . */
   protected final ResourceId resource;

   /** . */
   protected final String contextPath;

   /** . */
   protected final String path;

   /** . */
   protected final String module;

   /** . */
   protected final int priority;
   
   private Javascript(ResourceId resource, String module, String contextPath, String path, int priority)
   {
      this.resource = resource;
      this.contextPath = contextPath;
      this.path = path;
      this.module = module;
      this.priority = priority < 0 ? Integer.MAX_VALUE : priority;
   }

   public String getPath()
   {
      return isExternalScript() ? path : contextPath + path;
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
   
   Module addModuleTo(ScriptResource resource)
   {
      if (isExternalScript())
      {
         return resource.addRemoteModule(contextPath, module, path, priority);
      }
      else
      {
         return resource.addLocalModule(contextPath, module, path, priority);
      }
   }
   
   public boolean isExternalScript()
   {
      return path.startsWith("http://") || path.startsWith("https://");
   }
   
   @Override
   public String toString()
   {
      return "Javascript[scope=" + resource + ", path=" + getPath() +"]";
   }
}

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

import org.exoplatform.portal.controller.resource.Scope;
import org.exoplatform.portal.controller.resource.ScopeType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Javascript
{

   /** . */
   private final Scope scope;

   /** . */
   private final String contextPath;

   /** . */
   private final int priority;

   /** . */
   private final String path;
   
   public Javascript(Scope scope, String path, String contextPath, int priority)
   {
      this.scope = scope;
      if (path.startsWith("http://") || path.startsWith("https://"))
      {
         this.path = path;
      }
      else
      {
         this.path = contextPath + path;
      }
      this.contextPath = contextPath;
      this.priority = priority < 0 ? Integer.MAX_VALUE : priority;
   }

   public String getPath()
   {
      return this.path;
   }

   public Scope getScope()
   {
      return scope;
   }

   public String getModule()
   {
      return scope.getType() == ScopeType.MODULE ? scope.getId() : null;
   }

   public String getContextPath()
   {
      return this.contextPath;
   }

   public int getPriority()
   {
      return priority;
   }
   
   public boolean isExternalScript()
   {
      return path.startsWith("http://") || path.startsWith("https://");
   }
   
   @Override
   public String toString()
   {
      return "Javascript[scope=" + scope + ", path=" + path +"]";
   }
}

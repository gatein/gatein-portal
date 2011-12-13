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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Javascript
{

   public static Javascript create(Scope scope, String path, String contextPath, int priority)
   {
      if (path.startsWith("http://") || path.startsWith("https://"))
      {
         return new External(scope, path, contextPath, priority);
      }
      else
      {
         return new Simple(scope, path, contextPath, priority);
      }
   }

   /** . */
   private final Scope scope;

   /** . */
   private final String contextPath;

   /** . */
   private final int priority;
   
   private Javascript(Scope scope, String contextPath, int priority)
   {
      this.scope = scope;
      this.contextPath = contextPath;
      this.priority = priority < 0 ? Integer.MAX_VALUE : priority;
   }

   public abstract String getPath();

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
      return contextPath;
   }

   public int getPriority()
   {
      return priority;
   }
   
   public abstract boolean isExternalScript();
   
   @Override
   public String toString()
   {
      return "Javascript[scope=" + scope + ", path=" + getPath() +"]";
   }

   public static class External extends Javascript
   {

      /** . */
      private final String uri;

      public External(Scope scope, String uri, String contextPath, int priority)
      {
         super(scope, contextPath, priority);

         //
         this.uri = uri;
      }

      @Override
      public String getPath()
      {
         return uri;
      }

      @Override
      public boolean isExternalScript()
      {
         return true;
      }
   }

   public abstract static class Internal extends Javascript
   {

      protected Internal(Scope scope, String contextPath, int priority)
      {
         super(scope, contextPath, priority);
      }
   }

   public static class Simple extends Internal
   {

      /** . */
      private final String path;

      /** . */
      private final String uri;

      public Simple(Scope scope, String path, String contextPath, int priority)
      {
         super(scope, contextPath, priority);

         //
         this.path = path;
         this.uri = contextPath + path;
      }

      @Override
      public String getPath()
      {
         return uri;
      }

      @Override
      public boolean isExternalScript()
      {
         return false;
      }

   }

   public static class Composite extends Internal
   {

      final ArrayList<Javascript> compounds;

      public Composite(Scope scope, String contextPath, int priority)
      {
         super(scope, contextPath, priority);
         
         //
         this.compounds = new ArrayList<Javascript>();
      }

      @Override
      public String getPath()
      {
         return "/merged.js";
      }

      @Override
      public boolean isExternalScript()
      {
         return false;
      }
   }
}

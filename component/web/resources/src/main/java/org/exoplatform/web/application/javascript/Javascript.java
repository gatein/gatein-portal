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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Javascript
{
   /** . */
   private final String module;

   /** . */
   private final String contextPath;

   /** . */
   private final int priority;

   private final String path;
   
   public Javascript(String module, String path, String contextPath, int priority)
   {
      this.module = module;
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

   public String getPath() {
      return this.path;
   }

   public String getModule()
   {
      return module;
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
      return (path.startsWith("http://") || path.startsWith("https://")) ? true : false;
   }
   
   @Override
   public String toString()
   {
      return "Javascript[module=" + module + ", path=" + path +"]";
   }
   
   public static class PortalJScript extends Javascript
   {
      private final String portalName;

      public PortalJScript(String module, String path, String contextPath, int priority, String portalName)
      {
         super(module, path, contextPath, priority);
         this.portalName = portalName;
      }
      
      public String getPortalName()
      {
         return portalName;
      }
   }
   
   public static class ExtendedJScript extends Javascript
   {
      private final String script;
      
      public ExtendedJScript(String module, String path, String contextPath, String script)
      {
         super(module, path, contextPath, Integer.MAX_VALUE);
         this.script = script;
      }
      
      public String getScript()
      {
         return this.script;
      }
   }
}

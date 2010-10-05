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
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptKey
{

   /** . */
   private final String module;

   /** . */
   private final String scriptPath;

   /** . */
   private final String contextPath;

   public JavascriptKey(String module, String scriptPath, String contextPath) throws IllegalArgumentException
   {
      if (module == null || scriptPath == null || contextPath == null)
      {
         throw new IllegalArgumentException("Module and scriptPath are mandatory for JavascriptKey");
      }
      this.module = module;
      this.scriptPath = scriptPath;
      this.contextPath = contextPath;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof JavascriptKey)
      {
         JavascriptKey that = (JavascriptKey)obj;
         return module.equals(that.module) && scriptPath.equals(that.scriptPath) && contextPath.equals(that.contextPath);
      }
      return false;
   }

   public String getModule()
   {
      return module;
   }

   public String getScriptPath()
   {
      return scriptPath;
   }

   public String getContextPath()
   {
      return contextPath;
   }
   
   public boolean isExternalScript()
   {
      return (scriptPath.startsWith("http") || scriptPath.startsWith("https")) ? true : false;
   }
}

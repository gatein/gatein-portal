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

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Javascript
{

   /** . */
   private final JavascriptKey key;

   /** . */
   private final ServletContext context;

   /** . */
   private final int priority;

   public Javascript(JavascriptKey key, ServletContext context, Integer priority)
   {
      this.key = key;
      this.context = context;
      this.priority = priority != null ? priority : -1;
   }

   public String getPath() {
      if(key.isExternalScript()) 
      {
         return key.getScriptPath();
      }
      return key.getContextPath() + key.getScriptPath();
   }

   public JavascriptKey getKey()
   {
      return key;
   }

   public ServletContext getContext()
   {
      return context;
   }

   public int getPriority()
   {
      return priority;
   }

   public BufferedReader getReader()
   {
      return new BufferedReader(new InputStreamReader(context.getResourceAsStream(key.getScriptPath())));
   }
}

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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS
 * Mar 27, 2007  
 */
public class JavascriptManager
{

   /** . */
   private ArrayList<String> data = new ArrayList<String>(100);

   /** . */
   private ArrayList<String> customizedOnloadJavascript = null;

   /** . */
   private JavascriptConfigService jsSrevice_;

   public JavascriptManager()
   {
      jsSrevice_ =
         (JavascriptConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            JavascriptConfigService.class);
   }

   public void addJavascript(CharSequence s)
   {
      if (s != null)
      {
         data.add(s instanceof String ? (String)s : s.toString());
         data.add(" \n");
      }
   }

   public void importJavascript(CharSequence s)
   {
      if (s != null)
      {
         if (!jsSrevice_.isModuleLoaded(s) || PropertyManager.isDevelopping())
         {
            data.add("eXo.require('");
            data.add(s instanceof String ? (String)s : s.toString());
            data.add("'); \n");
         }
      }
   }

   public void importJavascript(String s, String location)
   {
      if (s != null && location != null)
      {
         if (!jsSrevice_.isModuleLoaded(s) || PropertyManager.isDevelopping())
         {
            data.add("eXo.require('");
            data.add(s);
            data.add("', '");
            data.add(location);
            if (!location.endsWith("/"))
            {
               data.add("/");
            }
            data.add("'); \n");
         }
      }
   }

   public void addOnLoadJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.add("eXo.core.Browser.addOnLoadCallback('mid");
         data.add(id);
         data.add("',");
         data.add(s instanceof String ? (String)s : s.toString());
         data.add("); \n");
      }
   }

   public void addOnResizeJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.add("eXo.core.Browser.addOnResizeCallback('mid");
         data.add(id);
         data.add("',");
         data.add(s instanceof String ? (String)s : s.toString());
         data.add("); \n");
      }
   }

   public void addOnScrollJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.add("eXo.core.Browser.addOnScrollCallback('mid");
         data.add(id);
         data.add("',");
         data.add(s instanceof String ? (String)s : s.toString());
         data.add("); \n");
      }
   }

   public void writeJavascript(Writer writer) throws IOException
   {
      for (int i = 0;i < data.size();i++)
      {
         String s = data.get(i);
         writer.write(s);
      }
   }

   public void addCustomizedOnLoadScript(CharSequence s)
   {
      if (s != null)
      {
         if (customizedOnloadJavascript == null)
         {
            customizedOnloadJavascript = new ArrayList<String>(30);
         }
         customizedOnloadJavascript.add(s instanceof String ? (String)s : s.toString());
         customizedOnloadJavascript.add("\n");
      }
   }

   public void writeCustomizedOnLoadScript(Writer writer) throws IOException
   {
      if (customizedOnloadJavascript != null)
      {
         for (int i = 0;i < customizedOnloadJavascript.size();i++)
         {
            String s = customizedOnloadJavascript.get(i);
            writer.write(s);
         }
      }
   }
}

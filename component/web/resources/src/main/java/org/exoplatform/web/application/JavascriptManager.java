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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by The eXo Platform SAS
 * Mar 27, 2007  
 */
public class JavascriptManager
{

   /** . */
   private TreeSet<String> onloadScripts = new TreeSet<String>();
   
   /** . */
   private StringBuilder data = new StringBuilder();

   /** . */
   private StringBuilder customizedOnloadJavascript = new StringBuilder();

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
         data.append(s instanceof String ? (String)s : s.toString());
         data.append(" \n");
      }
   }

   /**
    * This method is deprecated, please use {@link #lazyLoadJavascript(String)} instead
    */
   public void importJavascript(CharSequence s)
   {
      importJavascript(s instanceof String ? (String)s : s.toString(), null);
   }   
   
   /**
    * This method is deprecated, please use {@link #lazyLoadJavascript(String)} instead
    */
   public void importJavascript(String s, String location)
   {
      if (s != null)
      {
         if (!jsSrevice_.isModuleLoaded(s) || PropertyManager.isDevelopping())
         {
            if(location == null) location = "/eXoResources/javascript/";
            onloadScripts.add(location  + s.replaceAll("\\.", "/")  + ".js" );
         }
      }
   }
   
   /**
    * Create js code that help to load javascript file
    * @param path - path to js file, should contain context path
    */
   public void loadJavascript(String path) 
   {
      loadJavascript(Arrays.asList(path), null, null);
   }
   
   /**
    * Create js code that help to load javascript file
    * @param paths - list of path to js files, should contain context path
    * @param callback - method will be called after js file's been loaded
    * @param params - variable arguments pass to js callback method
    * @param context
    */
   public void loadJavascript(List<String> paths, String callback, String context, String ...params) 
   {
      if (paths != null && paths.size() > 0)
      {
         Iterator<String> pathItr = paths.iterator();         
         while (pathItr.hasNext()) 
         {
            String path = pathItr.next();
            if (jsSrevice_.isJavascriptLoaded(path) && !PropertyManager.isDevelopping()) 
            {
               pathItr.remove();
            }
         }
         String[] pathArray = new String[paths.size()];
         String jsPaths = buildJSArray(paths.toArray(pathArray));
                  
         if (jsPaths.length() > 2)
         {
            StringBuilder builder = new StringBuilder("eXo.loadJS(");
            builder.append(jsPaths).append(",");
            builder.append(callback).append(",");
            builder.append(context).append(",");
            builder.append(buildJSArray(params));
            builder.append("); \n");
            data.append(builder.toString());
         }
      }
   }   
   
   public void addOnLoadJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.append("eXo.core.Browser.addOnLoadCallback('mid");
         data.append(id);
         data.append("',");
         data.append(s instanceof String ? (String)s : s.toString());
         data.append("); \n");
      }
   }

   public void addOnResizeJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.append("eXo.core.Browser.addOnResizeCallback('mid");
         data.append(id);
         data.append("',");
         data.append(s instanceof String ? (String)s : s.toString());
         data.append("); \n");
      }
   }

   public void addOnScrollJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         data.append("eXo.core.Browser.addOnScrollCallback('mid");
         data.append(id);
         data.append("',");
         data.append(s instanceof String ? (String)s : s.toString());
         data.append("); \n");
      }
   }

   public void writeJavascript(Writer writer) throws IOException
   {

      if (onloadScripts.size() > 0)
      {
         Iterator<String> pathItr = onloadScripts.iterator();         
         while (pathItr.hasNext()) 
         {
            String path = pathItr.next();
            if (jsSrevice_.isJavascriptLoaded(path) && !PropertyManager.isDevelopping()) 
            {
               pathItr.remove();
            }
         }
         String[] pathArray = new String[onloadScripts.size()];
         String jsPaths = buildJSArray(onloadScripts.toArray(pathArray));
                  
         StringBuilder builder = new StringBuilder("eXo.loadJS(");
         builder.append(jsPaths).append(",");
         builder.append("function() {" + data.toString() + "eXo.core.Browser.onLoad(); " + customizedOnloadJavascript.toString() + " }");
         builder.append(");");
         writer.write(builder.toString());
      } 
      else 
      {
         writer.write(data.toString());         
         writer.write(customizedOnloadJavascript.toString());
      }
   }

   public void addCustomizedOnLoadScript(CharSequence s)
   {
      if (s != null)
      {
         customizedOnloadJavascript.append(s instanceof String ? (String)s : s.toString());
         customizedOnloadJavascript.append("\n");
      }
   }

   public void writeCustomizedOnLoadScript(Writer writer) throws IOException
   {
      if (customizedOnloadJavascript != null)
      {
         writer.write(customizedOnloadJavascript.toString());
      }
   }
   
   private String buildJSArray(String ...params)
   {
      StringBuilder pathBuilder = new StringBuilder("[");
      for (String str : params) 
      {
         pathBuilder.append("'").append(str).append("',");
      }
      pathBuilder.append("]");
      return pathBuilder.toString();
   }
}

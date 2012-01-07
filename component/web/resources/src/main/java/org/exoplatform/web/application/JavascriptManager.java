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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.FetchMode;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

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
   
   private ControllerContext context;

   public JavascriptManager(ControllerContext context)
   {
      jsSrevice_ =
         (JavascriptConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            JavascriptConfigService.class);
      this.context = context;
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
            loadJavascript(location  + s.replaceAll("\\.", "/")  + ".js");
         }
      }
   }
   
   public void loadJavascript(ResourceScope scope, String name) throws Exception 
   {
      loadJavascript(Arrays.asList(new ResourceId(scope, name)));
   }   

   public void loadJavascript(Collection<ResourceId> ids) throws Exception
   {
      if (ids == null) 
      {
         throw new IllegalArgumentException("ids can't be null");
      }
      Map<String, FetchMode> urlMap = jsSrevice_.resolveURLs(context, ids, !PropertyManager.isDevelopping(), !PropertyManager.isDevelopping());
      Set<String> urls = urlMap.keySet();
      loadJavascript(urls.toArray(new String[urls.size()]));
   }
      
   public void loadJavascript(String ...paths) 
   {
      if (paths == null)
      {
         throw new IllegalArgumentException("paths can't be null");
      }      
      for (String path : paths) 
      {
         if (!jsSrevice_.isJavascriptLoaded(path) || PropertyManager.isDevelopping())
         {
            onloadScripts.add(path);
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

   public void addCustomizedOnLoadScript(CharSequence s)
   {
      if (s != null)
      {
         customizedOnloadJavascript.append(s instanceof String ? (String)s : s.toString());
         customizedOnloadJavascript.append("\n");
      }
   }
   
   @Deprecated
   public void writeCustomizedOnLoadScript(Writer writer) throws IOException
   {
      if (customizedOnloadJavascript != null)
      {
         writer.write(customizedOnloadJavascript.toString());
      }
   }
   
   public void writeJavascript(Writer writer) throws IOException
   {
      StringBuilder callback = new StringBuilder();
      callback.append(data);
      callback.append("eXo.core.Browser.onLoad();");
      callback.append(customizedOnloadJavascript);
      
      if (onloadScripts.size() > 0)
      {
         String jsPaths = buildJSArray(onloadScripts);
                  
         StringBuilder builder = new StringBuilder("eXo.loadJS(");
         builder.append(jsPaths).append(",");
         builder.append("function() {").append(callback.toString()).append("});");
         writer.write(builder.toString());
      } 
      else 
      {
         writer.write(callback.toString());
      }
   }
   
   private String buildJSArray(Collection<String> params)
   {
      StringBuilder pathBuilder = new StringBuilder("[");
      for (String str : params) 
      {
         pathBuilder.append("'").append(str).append("',");
      }
      if (!params.isEmpty()) 
      {
         pathBuilder.deleteCharAt(pathBuilder.length() - 1);
      }
      pathBuilder.append("]");
      return pathBuilder.toString();
   }  
}

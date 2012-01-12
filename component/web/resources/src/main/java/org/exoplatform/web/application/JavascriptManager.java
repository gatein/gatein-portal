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
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.ScriptResource;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

/**
 * Created by The eXo Platform SAS
 * Mar 27, 2007  
 */
public class JavascriptManager
{

   /** . */
   private Set<String> onloadScripts = new HashSet<String>();
   
   /** . */
   private Set<ResourceId> resourceIds = new HashSet<ResourceId>();
   
   /** . */
   private StringBuilder data = new StringBuilder();

   /** . */
   private StringBuilder customizedOnloadJavascript = new StringBuilder();

   /** . */
   private JavascriptConfigService jsSrevice_;

   public JavascriptManager(ControllerContext context)
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
    * Register a Javascript Module that will be loaded in Rendering phase
    */
   public void registerJS(ResourceScope scope, String module) 
   {
      if (scope == null) 
      {
         throw new IllegalArgumentException("scope can't be null");
      }
      registerJS(Arrays.asList(new ResourceId(scope, module)));
   }   

   /**
    * Register collection of JS resources that will be loaded in Rendering phase
    */
   public void registerJS(Collection<ResourceId> ids)
   {
      if (ids == null) 
      {
         throw new IllegalArgumentException("ids can't be null");
      }
      resourceIds.addAll(ids);
   }
   
   public Set<ResourceId> getRegisteredJS()
   {
      return resourceIds;
   }

   @Deprecated
   public void importJavascript(CharSequence s)
   {
      String moduleName = s.toString();
      ScriptResource res = jsSrevice_.getResourceIncludingModule(moduleName);
      if(res != null)
      {
         try
         {
            registerJS(Arrays.asList(res.getId()));
         }
         catch (Exception ex)
         {
            //Spare me, Sonar! This importJavascript is to be deleted soon
            ex.printStackTrace();
         }
      }
      else
      {
         importJavascript(moduleName, null);
      }
   }
   
   @Deprecated
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
      
   /**
    * This method will be removed after importJavascript method is removed
    */
   private void loadJavascript(String ...paths) 
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

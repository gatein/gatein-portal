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

package org.exoplatform.web.application.javascript;

import javax.servlet.ServletContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.CompositeReader;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module;
import org.gatein.portal.controller.resource.script.ScriptGraph;
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.picocontainer.Startable;

public class JavascriptConfigService extends AbstractResourceService implements Startable
{

   /** Our logger. */
   private final Logger log = LoggerFactory.getLogger(JavascriptConfigService.class);

   /** The scripts. */
   final ScriptGraph scripts;

   /** . */
   private final WebAppListener deployer;

   /** . */
   public static final Comparator<Module> MODULE_COMPARATOR = new Comparator<Module>()
   {
      public int compare(Module o1, Module o2)
      {
         return o1.getPriority() - o2.getPriority();
      }
   };

   public JavascriptConfigService(ExoContainerContext context, ResourceCompressor compressor)
   {
      super(compressor);

      //
      this.scripts = new ScriptGraph();
      this.deployer = new JavascriptConfigDeployer(context.getPortalContainerName(), this);
   }

   public Collection<String> getAvailableScripts()
   {
      ArrayList<String> list = new ArrayList<String>();
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         list.addAll(shared.getModulesNames());
      }
      return list;
   }

   public Collection<String> getAvailableScriptsPaths()
   {
      ArrayList<Module> sharedModules = new ArrayList<Module>();
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         sharedModules.addAll(shared.getModules());
      }
      Collections.sort(sharedModules, MODULE_COMPARATOR);
      List<String> paths = new ArrayList<String>();
      for (Module module : sharedModules)
      {
         paths.add(module.getURI());
      }
      return paths;
   }

   public Reader getScript(ResourceId id, String name, Locale locale)
   {
      ScriptResource script = getResource(id);
      if (script != null && !"merged".equals(name))
      {
         return getJavascript(script, name, locale);
      }
      else
      {
         return getScript(id, locale);
      }
   }
   
   public Reader getScript(ResourceId resourceId, Locale locale)
   {
      ScriptResource resource = getResource(resourceId);
      
      if (resource != null)
      {
         List<Module> modules = new ArrayList<Module>(resource.getModules());
         Collections.sort(modules, MODULE_COMPARATOR);
         ArrayList<Reader> readers = new ArrayList<Reader>(modules.size() * 2);
         
         //
         StringBuilder buffer = new StringBuilder();
         boolean isModule = FetchMode.ON_LOAD.equals(resource.getFetchMode());
         if (isModule)
         {
            buffer.append("define('").append(resourceId).append("', ");
            buffer.append(new JSONArray(resource.getDependencies()));            
            buffer.append(", function(");
            for (ResourceId resId : resource.getDependencies()) 
            {               
               String alias = resource.getDependencyAlias(resId);
               buffer.append(alias == null ? getResource(resId).getAlias() : alias).append(",");
            }
            if (resource.getDependencies().size() > 0) 
            {
               buffer.deleteCharAt(buffer.length() - 1);
            }
            
            //                        
            buffer.append(") { var ").append(resource.getAlias()).append(" = {};");
         }
         
         //
         for (Module js : modules)
         {
            Reader jScript = getJavascript(resource, js.getName(), locale);
            if (jScript != null)
            {                                                     
               if (isModule) 
               {                   
                  buffer.append("var tmp = function() {");
               }
               buffer.append("// Begin ").append(js.getName()).append("\n");                          

               //
               readers.add(new StringReader(buffer.toString()));                  
               buffer.setLength(0);                  
               readers.add(jScript);

               //
               buffer.append("// End ").append(js.getName()).append("\n");
               if (isModule) 
               {
                  buffer.append("}();for(var prop in tmp){");
                  buffer.append(resource.getAlias()).append("[prop]=tmp[prop];").append("}");
               }
            }                                             
         }         
            
         if (isModule)
         {
            buffer.append("return ").append(resource.getAlias()).append(";});");     
         }
         readers.add(new StringReader(buffer.toString()));
         
         return new CompositeReader(readers);
      }
      else
      {
         return null;         
      }
   }
   
   public Map<String, FetchMode> resolveURLs(
      ControllerContext controllerContext,
      Map<ResourceId, FetchMode> ids,
      boolean merge,
      boolean minified,
      Locale locale) throws IOException
   {
      Map<String, FetchMode> urls = new LinkedHashMap<String, FetchMode>();
      StringBuilder buffer = new StringBuilder();
      URIWriter writer = new URIWriter(buffer);

      //
      if (ids.size() > 0)
      {
         ScriptResource resource = getResource(ids.keySet().iterator().next());
                
         //
         if (resource != null)
         {
            FetchMode mode = resource.getFetchMode();
            List<Module> modules = resource.getModules();
            if (modules.size() > 0 && modules.get(0) instanceof Module.Remote)
            {
               urls.put(((Module.Remote)modules.get(0)).getURI(), mode);
            }
            else
            {
               controllerContext.renderURL(resource.getParameters(minified, locale), writer);
               urls.put(buffer.toString(), mode);
               buffer.setLength(0);
               writer.reset(buffer);
            }            
         }
      }

      //
      return urls;
   }

   public Map<ScriptResource, FetchMode> resolveIds(Map<ResourceId, FetchMode> ids)
   {
      return scripts.resolve(ids);
   }
   
   public JSONObject getJSConfig(ControllerContext controllerContext, Locale locale) throws Exception 
   {
      JSONObject paths = new JSONObject();
      JSONObject shim = new JSONObject();      
            
      for (ScriptResource resource : getAllResources())
      {
         HashMap<ResourceId, FetchMode> ids = new HashMap<ResourceId, FetchMode>();
         ids.put(resource.getId(), null);         
         
         Map<String, FetchMode> urlMap = resolveURLs(controllerContext, ids, !PropertyManager.isDevelopping(),
            !PropertyManager.isDevelopping(), locale);         
         String url = urlMap.keySet().iterator().next();
         
         //
         String name = resource.getId().toString(); 
         paths.put(name, url.substring(0, url.length() - ".js".length()));
         
         //
         List<Module> modules = resource.getModules();         
         if (FetchMode.IMMEDIATE.equals(resource.getFetchMode()) || 
                  (modules.size() > 0 && modules.get(0) instanceof Module.Remote))
         {
            JSONArray deps = new JSONArray(resource.getDependencies());
            
            if (deps.length() > 0)
            {
               shim.put(name, new JSONObject().put("deps", deps));               
            }
         }
      }
                 
      JSONObject config = new JSONObject();      
      config.put("paths", paths);
      config.put("shim", shim);
      return config;
   }
   
   private List<ScriptResource> getAllResources()
   {
      List<ScriptResource> resources = new LinkedList<ScriptResource>();
      for (ResourceScope scope : ResourceScope.values())
      {
         resources.addAll(scripts.getResources(scope));
      }
      return resources;
   }
   
   public ScriptResource getResource(ResourceId resource)
   {
      return scripts.getResource(resource);
   }

   //TODO: This method should be removed once there is no call to importJavascript from Groovy template
   public ScriptResource getResourceIncludingModule(String moduleName)
   {
      //We accept repeated graph traversing for the moment
      for(ScriptResource sharedRes : scripts.getResources(ResourceScope.SHARED))
      {
         if(sharedRes.getModule(moduleName) != null)
         {
            return sharedRes;
         }
      }

      for(ScriptResource portletRes : scripts.getResources(ResourceScope.PORTLET))
      {
         if(portletRes.getModule(moduleName) != null)
         {
            return portletRes;
         }
      }

      for(ScriptResource portalRes : scripts.getResources(ResourceScope.PORTAL))
      {
         if(portalRes.getModule(moduleName) != null)
         {
            return portalRes;
         }
      }

      return null;
   }

   public boolean isModuleLoaded(CharSequence module)
   {
      return getAvailableScripts().contains(module.toString());
   }
   
   public boolean isJavascriptLoaded(String path)
   {
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         for (Module module : shared.getModules())
         {
            if (module.getURI().equals(path))
            {
               return true;
            }
         }
      }      
      return false;
   }

   /**
    * Start service.
    * Registry org.exoplatform.web.application.javascript.JavascriptDeployer,
    * org.exoplatform.web.application.javascript.JavascriptRemoval  into ServletContainer
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
      log.debug("Registering JavascriptConfigService for servlet container events");
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(deployer);
   }

   /**
    * Stop service.
    * Remove org.exoplatform.web.application.javascript.JavascriptDeployer,
    * org.exoplatform.web.application.javascript.JavascriptRemoval  from ServletContainer
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
      log.debug("Unregistering JavascriptConfigService for servlet container events");
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(deployer);
   }
   
   private Reader getJavascript(ScriptResource resource, String moduleName, Locale locale)
   {
      Module module = resource.getModule(moduleName);
      if (module instanceof Module.Local)
      {
         Module.Local localModule = (Module.Local)module;
         final WebApp webApp = contexts.get(localModule.getContextPath());
         if (webApp != null)
         {
            ServletContext sc = webApp.getServletContext();
            return localModule.read(locale, sc, webApp.getClassLoader());
         }
      }
      return null;
   }
}

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import org.gatein.portal.controller.resource.script.BaseScriptResource;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module;
import org.gatein.portal.controller.resource.script.ScriptGraph;
import org.gatein.portal.controller.resource.script.ScriptGroup;
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppListener;
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
   public static final List<String> RESERVED_MODULE = Arrays.asList("require", "exports", "module");

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

   public Reader getScript(ResourceId id, String name, Locale locale) throws Exception
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
   
   public Reader getScript(ResourceId resourceId, Locale locale) throws Exception
   {
      if (ResourceScope.GROUP.equals(resourceId.getScope()))
      {
         ScriptGroup loadGroup = scripts.getLoadGroup(resourceId.getName());
         if (loadGroup != null)
         {
            List<Reader> readers = new ArrayList<Reader>(loadGroup.getDependencies().size());
            for (ResourceId id : loadGroup.getDependencies())
            {
               Reader rd = getScript(id, locale);
               if (rd != null)
               {
                  readers.add(rd);
               }
            }
            return new CompositeReader(readers);
         }
         else
         {
            return null;
         }
      }
      else
      {
         ScriptResource resource = getResource(resourceId);
         
         if (resource != null)
         {  
            List<Module> modules = new ArrayList<Module>(resource.getModules());         
            
            Collections.sort(modules, MODULE_COMPARATOR);
            ArrayList<Reader> readers = new ArrayList<Reader>(modules.size() * 2);         
            StringBuilder buffer = new StringBuilder();
            
            //
            boolean isModule = FetchMode.ON_LOAD.equals(resource.getFetchMode());
            boolean isNative = modules.size() > 0 && modules.get(0) instanceof Module.Native; 
            
            if (isModule && !isNative)
            {
               buffer.append("define('").append(resourceId).append("', ");
               
               JSONArray deps = new JSONArray();               
               List<String> alias = new LinkedList<String>();
               for (ResourceId id : resource.getDependencies())
               {
                  ScriptResource dep = getResource(id);
                  if (dep != null)
                  {
                     deps.put(dep.getId());                     
                     
                     //
                     String als = resource.getDependencyAlias(id);
                     alias.add(als == null ? dep.getAlias() : als);
                  }
                  else if (RESERVED_MODULE.contains(id.getName()))
                  {
                     String reserved = id.getName();
                     deps.put(reserved);
                     alias.add(reserved);
                  }
               }
               buffer.append(deps);
               buffer.append(", function(");
               buffer.append(StringUtils.join(alias, ","));
               
               //                        
               buffer.append(") {var require = eXo.require,requirejs = require,define = eXo.define;");
               buffer.append("define.names=").append(new JSONArray(alias)).append(";");
               int idx = alias.indexOf("require");
               if (idx != -1)
               {
                  alias.set(idx, "eXo.require");
               }
               buffer.append("define.deps=[").append(StringUtils.join(alias, ",")).append("]").append(";");
               buffer.append("return ");
            }
            
            //
            for (Module js : modules)
            {            
               Reader jScript = getJavascript(resource, js.getName(), locale);
               if (jScript != null)
               {                                                     
                  readers.add(new StringReader(buffer.toString()));                  
                  buffer.setLength(0);                  
                  readers.add(new NormalizeJSReader(jScript));
               }                                             
            }                  
            
            if (isModule)
            {            
               if (!isNative)
               {
                  buffer.append("});");                                   
               }
            }
            else 
            {
               buffer.append("if (typeof define === 'function' && define.amd && !require.specified('").append(resource.getId()).append("')) {");
               buffer.append("define('").append(resource.getId()).append("');}");            
            }
            readers.add(new StringReader(buffer.toString()));            
            
            return new CompositeReader(readers);
         }
         else
         {
            return null;         
         }
      }      
   }
   
   @SuppressWarnings("unchecked")
   public String generateURL(
      ControllerContext controllerContext,
      ResourceId id,
      boolean merge,
      boolean minified,
      Locale locale) throws IOException
   {      
      @SuppressWarnings("rawtypes")
      BaseScriptResource resource = null;
      if (ResourceScope.GROUP.equals(id.getScope()))
      {
         resource = scripts.getLoadGroup(id.getName());
      }
      else
      {
         resource = getResource(id);            
      }
             
      //
      if (resource != null)
      {         
         if (resource instanceof ScriptResource)
         {
            ScriptResource rs = (ScriptResource)resource;
            
            List<Module> modules = rs.getModules();
            if (modules.size() > 0 && modules.get(0) instanceof Module.Remote)
            {
               return ((Module.Remote)modules.get(0)).getURI();
            }
         }

         StringBuilder buffer = new StringBuilder();
         URIWriter writer = new URIWriter(buffer);
         controllerContext.renderURL(resource.getParameters(minified, locale), writer);
         return buffer.toString();            
      }
      else
      {
         return null;         
      }
   }

   public Map<ScriptResource, FetchMode> resolveIds(Map<ResourceId, FetchMode> ids)
   {
      return scripts.resolve(ids);
   }
   
   public JSONObject getJSConfig(ControllerContext controllerContext, Locale locale) throws Exception 
   {
      JSONObject paths = new JSONObject();
      JSONObject shim = new JSONObject();      
            
      Map<ResourceId, String> groupURLs = new HashMap<ResourceId, String>();
      for (ScriptResource resource : getAllResources())
      {         
         if (!resource.isEmpty() || ResourceScope.SHARED.equals(resource.getId().getScope()))
         {
            String name = resource.getId().toString();
            List<Module> modules = resource.getModules();
            
            if (FetchMode.IMMEDIATE.equals(resource.getFetchMode()) || (modules.size() > 0 && (modules.get(0) instanceof Module.Remote
                     || modules.get(0) instanceof Module.Native)))
            {
               JSONArray deps = new JSONArray();
               for (ResourceId id : resource.getDependencies())
               {
                  deps.put(getResource(id).getId());
               }
               if (deps.length() > 0)
               {
                  shim.put(name, new JSONObject().put("deps", deps));                  
               }
            }
            
                   
            String url;
            ScriptGroup group = resource.getGroup();
            if (group != null)
            {
               ResourceId grpId = group.getId();
               url = groupURLs.get(grpId);
               if (url == null)
               {
                  url = buildURL(grpId, controllerContext, locale);
                  groupURLs.put(grpId, url);
               }
            }
            else
            {
               url = buildURL(resource.getId(), controllerContext, locale);                                    
            }
            paths.put(name, url);
         }         
      }
                 
      JSONObject config = new JSONObject();      
      config.put("paths", paths);
      config.put("shim", shim);
      return config;
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
      ServletContainerFactory.getServletContainer().addWebAppListener(deployer);
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
      ServletContainerFactory.getServletContainer().removeWebAppListener(deployer);
   }
   
   private Reader getJavascript(ScriptResource resource, String moduleName, Locale locale)
   {
      Module module = resource.getModule(moduleName);
      if (module instanceof Module.Local || module instanceof Module.Native)
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
   
   private String buildURL(ResourceId id, ControllerContext context, Locale locale) throws Exception
   {      
      String url = generateURL(context, id, !PropertyManager.isDevelopping(),
         !PropertyManager.isDevelopping(), locale);         
      
      if (url != null && url.endsWith(".js"))
      {            
         return url.substring(0, url.length() - ".js".length());         
      }
      else
      {
         return null;
      }
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
   
   private class NormalizeJSReader extends Reader
   {
      private boolean finished = false;         
      private boolean multiComments = false;
      private boolean singleComment = false;
      private Reader sub;

      public NormalizeJSReader(Reader sub)
      {
         this.sub = sub;
      }

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException
      {
         if (finished)
         {
            return sub.read(cbuf, off, len);
         }
         else
         {
            char[] buffer = new char[len];
            int relLen = sub.read(buffer, 0, len); 
            if (relLen == -1)
            {
               finished = true;
               return -1;
            }
            else
            {
               int r = off; 
               
               for (int i = 0; i < relLen; i++)
               {                     
                  char c = buffer[i];
                  
                  char next = 0;
                  boolean skip = false, overflow = (i + 1 == relLen);
                  if (!finished)
                  {
                     skip = true;
                     if (!singleComment && c == '/' && (next = readNext(buffer, i, overflow)) == '*')
                     {
                        multiComments = true;
                        i++;
                     }
                     else if (!singleComment && c == '*' && (next = readNext(buffer, i, overflow)) == '/')
                     {
                        multiComments = false;
                        i++;
                     }
                     else if (!multiComments && c == '/' && next == '/')
                     {
                        singleComment = true;
                        i++;
                     }
                     else if (c == '\n')
                     {
                        singleComment = false;
                     } 
                     else if (c != ' ')
                     {
                        skip = false;
                     }
                     
                     if (!skip && !multiComments && !singleComment)
                     {
                        if (next != 0 && overflow)
                        {
                           sub = new CompositeReader(new StringReader(String.valueOf(c)), sub);                        
                        }
                        cbuf[r++] = c;
                        finished = true;
                     }
                  }
                  else
                  {
                     cbuf[r++] = c;
                  }
               }
               return r - off; 
            }
         }
      }      

      private char readNext(char[] buffer, int i, boolean overflow) throws IOException
      {
         char c = 0;
         if (overflow)
         {
            int tmp = sub.read();
            if (tmp != -1)
            {
               c = (char)tmp;               
            }
         }
         else
         {
            c = buffer[i + 1];
         }
         return c;
      }

      @Override
      public void close() throws IOException
      {
         sub.close();
      }
   }
}
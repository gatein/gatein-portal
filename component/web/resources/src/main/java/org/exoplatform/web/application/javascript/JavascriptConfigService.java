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

import org.exoplatform.commons.utils.CompositeReader;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.FetchMode;
import org.exoplatform.portal.controller.resource.script.Module;
import org.exoplatform.portal.controller.resource.script.ScriptGraph;
import org.exoplatform.portal.controller.resource.script.ScriptResource;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
         for (Module js :modules)
         {
            if (!js.isRemote())
            {
               Reader jScript = getJavascript(resource, js.getName(), locale);
               if (jScript != null)
               {
                  readers.add(new StringReader("// Begin " + js.getName() + "\n"));
                  readers.add(jScript);
                  readers.add(new StringReader("// End " + js.getName() + "\n"));
               }
            }
         }
         return new CompositeReader(readers);
      }
      return null;
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
      Map<ScriptResource, FetchMode> resources = scripts.resolve(ids);

      //
      for (Map.Entry<ScriptResource, FetchMode> entry : resources.entrySet())
      {
         ScriptResource resource = entry.getKey();

         //
         if (!resource.isEmpty())
         {
            FetchMode mode = entry.getValue();
            for (Module module : resource.getModules())
            {
               if (module instanceof Module.Remote)
               {
                  urls.put(((Module.Remote)module).getURI(), mode);
               }
            }
            controllerContext.renderURL(resource.getParameters(minified, locale), writer);
            urls.put(buffer.toString(), mode);
            buffer.setLength(0);
            writer.reset(buffer);
         }
      }

      //
      return urls;
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

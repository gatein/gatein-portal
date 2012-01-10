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
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

public class JavascriptConfigService extends AbstractResourceService implements Startable
{

   /** . */
   public static final ResourceId COMMON_SHARED_RESOURCE = new ResourceId(ResourceScope.SHARED, "common");

   /** Our logger. */
   private final Logger log = LoggerFactory.getLogger(JavascriptConfigService.class);

   /** The scripts. */
   ScriptGraph scripts;

   /** . */
   private WebAppListener deployer;

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

      // todo : remove /portal ???
      ScriptGraph scripts = new ScriptGraph();

      // Not sure it's still used but that should be removed at some point
      // since we do everyting by XML now
      scripts.addResource(COMMON_SHARED_RESOURCE, FetchMode.ON_LOAD);

      //
      this.scripts = scripts;
      this.deployer = new JavascriptConfigDeployer(context.getPortalContainerName(), this);
   }

   /**
    * Return a collection list This method should return the availables scripts in the service
    *
    * @deprecated Somehow, it should use {@link #getCommonJScripts()} instead.
    * @return
    */
   @Deprecated
   public Collection<String> getAvailableScripts()
   {
      ArrayList<String> list = new ArrayList<String>();
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         list.addAll(shared.getModulesNames());
      }
      return list;
   }

   /**
    * Return a collection of all common JScripts
    *
    * @return
    */
   public Collection<Javascript> getCommonJScripts()
   {
      ArrayList<Javascript> list = new ArrayList<Javascript>();
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         list.addAll(getJavascripts(shared));
      }
      return list;
   }

   /**
    * Return a collection of all available JS paths
    *
    * @deprecated Somehow, it should use {@link #getCommonJScripts()} instead.
    *
    * @return A collection of all available JS paths
    */
   @Deprecated
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

   /**
    * Add a JScript to common javascript list and re-sort the list.
    * Then invalidate cache of all merged common JScripts to
    * ensure they will be newly merged next time.
    *
    * @param js
    */
   public void addCommonJScript(Javascript js)
   {
      ScriptResource common = scripts.getResource(COMMON_SHARED_RESOURCE);
      js.addModuleTo(common);
   }

   /**
    * Remove a JScript for this module from common javascript
    * and invalidates its cache correspondingly
    *
    * @param moduleName the module name
    */
   public void removeCommonJScript(String moduleName)
   {
      ScriptResource common = scripts.getResource(COMMON_SHARED_RESOURCE);
      common.removeModuleByName(moduleName);
   }

   /**
    * Return a collection of all PortalJScripts which belong to the specified portalName.
    *
    * @param portalName
    * @return list of JavaScript path which will be loaded by particular portal
    */
   public Collection<Javascript> getPortalJScripts(String portalName)
   {
      ScriptResource composite = scripts.getResource(ResourceScope.PORTAL, portalName);
      if (composite != null)
      {
         ArrayList<Module> modules = new ArrayList<Module>(composite.getModules());
         Collections.sort(modules, MODULE_COMPARATOR);
         ArrayList<Javascript> scripts = new ArrayList<Javascript>();
         for (Module module : modules)
         {
            scripts.add(Javascript.create(module));
         }
         return scripts;
      }
      else
      {
         return null;
      }
   }
   
   public Reader getScript(ResourceId id, String name)
   {
      ScriptResource script = getResource(id);
      if (script != null && !"merged".equals(name))
      {
         return getJavascript(script, name);
      }
      else
      {
         return getScript(id);
      }
   }

   public Reader getScript(ResourceId resourceId)
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
               Reader jScript = getJavascript(resource, js.getName());
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
      Collection<ResourceId> ids,
      boolean merge,
      boolean minified) throws IOException
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
            controllerContext.renderURL(resource.getParameters(minified), writer);
            urls.put(buffer.toString(), entry.getValue());
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

   /**
    * Add a PortalJScript which will be loaded with a specific portal.
    * <p>
    * For now, we don't persist it inside the Portal site storage but just in memory.
    * Therefore we could somehow remove all PortalJScript for a Portal by using {@link #removePortalJScripts(String)}
    * when the portal is being removed.
    *
    * @param js
    */
   public void addPortalJScript(Javascript js)
   {
      js.addModuleTo(scripts.addResource(js.getResource(), null));
   }

   /**
    * Remove portal name from a JavaScript module or remove JavaScript module if it contains only one portal name
    *
    * @param portalName portal's name which you want to remove
    */
   public void removePortalJScripts(String portalName)
   {
      ScriptResource list = scripts.removeResource(new ResourceId(ResourceScope.PORTAL, portalName));
      if (list != null)
      {
         for (Module module : list.getModules())
         {
            if (module instanceof Module.Local)
            {
               Module.Local local = (Module.Local)module;
            }
         }
      }
   }

   /**
    * Check the existence of module in Available Scripts
    * @param module
    * @return true if Available Scripts contain module, else return false
    */
   public boolean isModuleLoaded(CharSequence module)
   {
      return getAvailableScripts().contains(module.toString());
   }
   
   /**
    * Check the existence of javascript in Available Scripts
    * @param path - should contain context path
    * @return true if Available Scripts contain js with specified path
    */
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
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(deployer);
   }

   private Reader getJavascript(ScriptResource resource, String moduleName)
   {
      Module module = resource.getModule(moduleName);
      if (module instanceof Module.Local)
      {
         Module.Local local = (Module.Local)module;
         ServletContext servletContext = contexts.get(local.getContextPath());
         if (servletContext != null)
         {
            InputStream in = servletContext.getResourceAsStream(local.getPath());
            if (in != null)
            {
               return new InputStreamReader(in);
            }
         }
      }
      return null;
   }

   private List<Javascript> getJavascripts(ScriptResource resource)
   {
      ArrayList<Javascript> javascripts = new ArrayList<Javascript>();
      for (Module module : resource.getModules())
      {
         javascripts.add(Javascript.create(module));
      }
      return javascripts;
   }
}

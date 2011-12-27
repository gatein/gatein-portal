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

import org.exoplatform.commons.cache.future.FutureMap;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.Module;
import org.exoplatform.portal.controller.resource.script.ScriptGraph;
import org.exoplatform.portal.controller.resource.script.ScriptResource;
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.MainResourceResolver;
import org.exoplatform.portal.resource.ResourceResolver;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.portal.resource.compressor.ResourceType;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.controller.router.URIWriter;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
   private long lastModified = Long.MAX_VALUE;

   /** . */
   private WebAppListener deployer;

   /** . */
   private CachedJavascript mergedCommonJScripts;

   /** . */
   private final FutureMap<String, CachedJavascript, ResourceResolver> cache;

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

      Loader<String, CachedJavascript, ResourceResolver> loader = new Loader<String, CachedJavascript, ResourceResolver>()
      {
         public CachedJavascript retrieve(ResourceResolver context, String key) throws Exception
         {
            org.exoplatform.portal.resource.Resource resource = context.resolve(key);
            if (resource == null)
            {
               return null;
            }

            StringBuilder sB = new StringBuilder();
            try
            {
               BufferedReader reader = new BufferedReader(resource.read());
               String line = reader.readLine();
               try
               {
                  while (line != null)
                  {
                     sB.append(line);
                     if ((line = reader.readLine()) != null)
                     {
                        sB.append("\n");
                     }
                  }
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }
               finally
               {
                  Safe.close(reader);
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            return new CachedJavascript(sB.toString());
         }
      };
      cache = new FutureMap<String, CachedJavascript, ResourceResolver>(loader);

      // todo : remove /portal ???
      ScriptGraph scripts = new ScriptGraph();
      scripts.addResource(COMMON_SHARED_RESOURCE);

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
      invalidateMergedCommonJScripts();
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
      for (Module module : common.removeModuleByName(moduleName))
      {
         if (module instanceof Module.Local)
         {
            Module.Local local = (Module.Local)module;
            invalidateCachedJScript(local.getPath());
            invalidateMergedCommonJScripts();
         }
      }
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
   
   private String merge(ResourceId id)
   {
      ScriptResource res = getResource(id);
      List<Module> modules = new ArrayList<Module>(res.getModules());
      Collections.sort(modules, MODULE_COMPARATOR);
      StringBuilder buffer = new StringBuilder();
      for (Module js :modules)
      {
         if (!js.isRemote())
         {
            String jScript = getJScript(js.getURI());
            if (jScript != null)
            {
               buffer.append(jScript).append("\n");
            }

         }
      }
      return buffer.toString();
   }

   public InputStream getScript(ResourceId id, String name)
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

   public InputStream getScript(ResourceId resourceId)
   {
      ScriptResource resource = getResource(resourceId);
      if (resource != null)
      {
         List<Module> modules = new ArrayList<Module>(resource.getModules());
         Collections.sort(modules, MODULE_COMPARATOR);
         StringBuilder buffer = new StringBuilder();
         for (Module js :modules)
         {
            if (!js.isRemote())
            {
               String jScript = getJScript(js.getURI());
               if (jScript != null)
               {
                  buffer.append(jScript).append("\n");
               }

            }
         }
         return new ByteArrayInputStream(buffer.toString().getBytes());
      }
      return null;
   }
   
   ServletContext getContext(String contextPath)
   {
      return contexts.get(contextPath);
   }
   
   public Map<String, Boolean> resolveURLs(ControllerContext controllerContext, Collection<ResourceId> ids, boolean merge) throws IOException
   {
      Map<String, Boolean> urls = new LinkedHashMap<String, Boolean>();
      StringBuilder buffer = new StringBuilder();
      URIWriter writer = new URIWriter(buffer);

      //
      Map<ScriptResource, Boolean> resources = scripts.resolve(ids);

      //
      for (Map.Entry<ScriptResource, Boolean> entry : resources.entrySet())
      {
         ScriptResource resource = entry.getKey();

         //
         if (!resource.isEmpty())
         {
            controllerContext.renderURL(resource.getParameters(), writer);
            urls.put(buffer.toString(), entry.getValue());
            buffer.setLength(0);
            writer.reset(buffer);
         }
      }

      //
      return urls;
   }

   public List<String> resolve(ControllerContext controllerContext, boolean merge) throws IOException
   {
      return resolve(controllerContext, null, merge);
   }

   public List<String> resolve(ControllerContext controllerContext, String portalName, boolean merge) throws IOException
   {
      ArrayList<String> scripts = new ArrayList<String>();
      StringBuilder buffer = new StringBuilder();
      URIWriter writer = new URIWriter(buffer);
      
      // First shared scripts
      ScriptResource common = this.scripts.getResource(COMMON_SHARED_RESOURCE);
      if (merge)
      {
         controllerContext.renderURL(common.getParameters(), writer);
         scripts.add(buffer.toString());
         buffer.setLength(0);
         writer.reset(buffer);

         //
         for (Module module : common.getModules())
         {
            if (module.isRemote())
            {
               scripts.add(module.getURI());
            }
         }
      }
      else
      {
         for (Module module : common.getModules())
         {
            if (module instanceof Module.Local)
            {
               Module.Local local = (Module.Local)module;
               controllerContext.renderURL(local.getParameters(), writer);
               scripts.add(buffer.toString());
               buffer.setLength(0);
               writer.reset(buffer);
            }
            else
            {
               // ????
            }
         }
      }
      
      // Then portal scripts
      if (portalName != null)
      {
         ScriptResource portalScript = this.scripts.getResource(ResourceScope.PORTAL, portalName);
         if (portalScript != null)
         {
            for (Module module : portalScript.getModules())
            {
               if (module instanceof Module.Local)
               {
                  Module.Local local = (Module.Local)module;
                  controllerContext.renderURL(local.getParameters(), writer);
                  scripts.add(buffer.toString());
                  buffer.setLength(0);
                  writer.reset(buffer);
               }
            }
         }
      }
      
      //
      return scripts;
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
      js.addModuleTo(scripts.addResource(js.getResource()));
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
               invalidateCachedJScript(local.getPath());
            }
         }
      }
   }

   /**
    * unregister a {@link ServletContext} into {@link MainResourceResolver} of {@link SkinService}
    *
    * @param servletContext ServletContext will unregistered
    */
   public void unregisterServletContext(ServletContext servletContext)
   {
      super.unregisterServletContext(servletContext);
      remove(servletContext);
   }

   /**
    * Remove JavaScripts from availabe JavaScipts by ServletContext
    * @param context the webapp's {@link javax.servlet.ServletContext}
    *
    */
   public synchronized void remove(ServletContext context)
   {
      for (ScriptResource shared : scripts.getResources(ResourceScope.SHARED))
      {
         for (Module module : shared.removeModuleByContextPath(context.getContextPath()))
         {
            if (module instanceof Module.Local)
            {
               Module.Local local = (Module.Local)module;
               invalidateCachedJScript(local.getPath());
               invalidateMergedCommonJScripts();
            }
         }
      }
   }

   /**
    *  @deprecated Use {@link #invalidateMergedCommonJScripts()} instead
    */
   @Deprecated
   public void refreshMergedJavascript()
   {
      invalidateMergedCommonJScripts();
   }

   /**
    * Write the merged javascript in a provided output stream.
    *
    * @param out the output stream
    * @throws IOException any io exception
    */
   @Deprecated
   public void writeMergedJavascript(OutputStream out) throws IOException
   {
      byte[] jsBytes = getMergedJavascript();

      //
      out.write(jsBytes);
   }

   public String getJScript(String path)
   {
      CachedJavascript cachedJScript = getCachedJScript(path);
      if (cachedJScript != null)
      {
         return cachedJScript.getText();
      }
      else
      {
         return null;
      }
   }

   /**
    * Return a CachedJavascript which is lazy loaded from a {@link FutureMap} cache
    *
    * @param path
    * @return
    */
   public CachedJavascript getCachedJScript(String path)
   {
      return cache.get(mainResolver, path);
   }

   /**
    * Returns a string which is merging of all common JScripts
    *
    * @return
    */
   public CachedJavascript getMergedCommonJScripts()
   {
      if (mergedCommonJScripts == null)
      {
         ScriptResource common = scripts.getResource(COMMON_SHARED_RESOURCE);
         String jScript = merge(common.getId());

         try
         {
            jScript = compressor.compress(jScript, ResourceType.JAVASCRIPT);
         }
         catch (Exception e)
         {
         }

         mergedCommonJScripts = new CachedJavascript(jScript);
         lastModified = mergedCommonJScripts.getLastModified();
      }

      return mergedCommonJScripts;
   }

   /**
    * @deprecated Use {@link #getMergedCommonJScripts()} instead.
    * It is more clearly to see what exactly are included in the returned merging
    *
    * @return byte[]
    */
   @Deprecated
   public byte[] getMergedJavascript()
   {
      String mergedCommonJS = getMergedCommonJScripts().getText();

      return mergedCommonJS.getBytes();
   }

   /**
    * @deprecated the last modification should belong to CachedJavascript object
    * @return
    */
   @Deprecated
   public long getLastModified()
   {
      return lastModified;
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
    * Invalidate cache of merged common JScripts
    */
   public void invalidateMergedCommonJScripts()
   {
      mergedCommonJScripts = null;
   }

   /**
    * Invalidate cache for this <tt>path</tt>
    *
    * @param path
    */
   public void invalidateCachedJScript(String path)
   {
      cache.remove(path);
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

   private InputStream getJavascript(ScriptResource resource, String moduleName)
   {
      Module module = resource.getModule(moduleName);
      if (module instanceof Module.Local)
      {
         Module.Local local = (Module.Local)module;
         ServletContext servletContext = contexts.get(local.getContextPath());
         if (servletContext != null)
         {
            return servletContext.getResourceAsStream(local.getPath());
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

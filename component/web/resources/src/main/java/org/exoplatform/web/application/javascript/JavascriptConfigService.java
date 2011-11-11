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
import org.exoplatform.portal.resource.AbstractResourceService;
import org.exoplatform.portal.resource.MainResourceResolver;
import org.exoplatform.portal.resource.Resource;
import org.exoplatform.portal.resource.ResourceResolver;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.portal.resource.compressor.ResourceType;
import org.exoplatform.web.application.javascript.Javascript.ExtendedJScript;
import org.exoplatform.web.application.javascript.Javascript.PortalJScript;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

public class JavascriptConfigService extends AbstractResourceService implements Startable
{
   /** Our logger. */
   private final Logger log = LoggerFactory.getLogger(JavascriptConfigService.class);

   private List<Javascript> commonJScripts;
   
   private HashMap<String, List<PortalJScript>> portalJScripts;

   private long lastModified = Long.MAX_VALUE;

   /** . */
   private WebAppListener deployer;

   private CachedJavascript mergedCommonJScripts;
   
   private final FutureMap<String, CachedJavascript, ResourceResolver> cache;
   
   public JavascriptConfigService(ExoContainerContext context, ResourceCompressor compressor)
   {
      super(compressor);

      Loader<String, CachedJavascript, ResourceResolver> loader = new Loader<String, CachedJavascript, ResourceResolver>()
      {
         @Override
         public CachedJavascript retrieve(ResourceResolver context, String key) throws Exception
         {
            Resource resource = context.resolve(key);
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

      commonJScripts = new ArrayList<Javascript>();
      deployer = new JavascriptConfigDeployer(context.getPortalContainerName(), this);
      portalJScripts = new HashMap<String, List<PortalJScript>>();
      
      addResourceResolver(new ExtendedJScriptResourceResolver());
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
      for (Javascript js : commonJScripts)
      {
         list.add(js.getModule());
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
      return commonJScripts;
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
      ArrayList<String> list = new ArrayList<String>();
      for (Javascript js : commonJScripts)
      {
         list.add(js.getPath());
      }

      return list;
   }
   
   /**
    * Add a JScript to {@link #commonJScripts} list and re-sort the list.
    * Then invalidate cache of all merged common JScripts to
    * ensure they will be newly merged next time.
    * 
    * @param js
    */
   public void addCommonJScript(Javascript js)
   {
      commonJScripts.add(js);
      
      Collections.sort(commonJScripts, new Comparator<Javascript>()
      {
         public int compare(Javascript o1, Javascript o2)
         {
            return o1.getPriority() - o2.getPriority();
         }
      });
      
      invalidateMergedCommonJScripts();
   }

   /**
    * Remove a JScript for this module from {@link #commonJScripts}
    * and invalidates its cache correspondingly
    * 
    * @param module
    */
   public void removeCommonJScript(String module)
   {
      Iterator<Javascript> iterator = commonJScripts.iterator();
      while (iterator.hasNext())
      {
         Javascript js = iterator.next();
         if (js.getModule().equals(module))
         {
            iterator.remove();
            invalidateCachedJScript(js.getPath());
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
   public Collection<PortalJScript> getPortalJScripts(String portalName)
   {
      return portalJScripts.get(portalName);
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
   public void addPortalJScript(PortalJScript js)
   {
      List<PortalJScript> list = portalJScripts.get(js.getPortalName());
      if (list == null)
      {
         list = new ArrayList<PortalJScript>();
      }
      
      list.add(js);

      Collections.sort(list, new Comparator<Javascript>()
      {
         public int compare(Javascript o1, Javascript o2)
         {
            return o1.getPriority() - o2.getPriority();
         }
      });
      
      portalJScripts.put(js.getPortalName(), list);
   }

   /**
    * Remove portal name from a JavaScript module or remove JavaScript module if it contains only one portal name
    * 
    * @param portalName portal's name which you want to remove
    */
   public void removePortalJScripts(String portalName)
   {
      List<PortalJScript> list = portalJScripts.remove(portalName);
      for (PortalJScript js : list)
      {
         invalidateCachedJScript(js.getPath());
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
    * @param scontext
    *           the webapp's {@link javax.servlet.ServletContext}
    *          
    */
   public synchronized void remove(ServletContext context)
   {
      Iterator<Javascript> iterator = commonJScripts.iterator();
      while (iterator.hasNext())
      {
         Javascript js = iterator.next();
         if (js.getContextPath().equals(context.getContextPath()))
         {
            iterator.remove();
            invalidateCachedJScript(js.getPath());
            invalidateMergedCommonJScripts();
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
         StringBuilder sB = new StringBuilder();
         for (Javascript js : commonJScripts)
         {
            if (!js.isExternalScript())
            {
               String jScript = getJScript(js.getPath());
               if (jScript != null)
               {
                  sB.append(jScript).append("\n");
               }
               
            }
         }

         String jScript = sB.toString();

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
      for (Javascript js : commonJScripts)
      {
         if (js.getModule().equals(module))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Add an extended JavaScript to the list of common JScripts which are loaded by default
    * 
    * @deprecated This method support was not good in design so it will be unsupported soon.
    * Absolutely this usage can be replaced by using combination of {@link #addCommonJScript(Javascript)} and {@link ResourceResolver}
    * 
    * @param module
    *           module name
    * @param scriptPath
    *           URI path of JavaScript 
    * @param scontext
    *           the webapp's {@link javax.servlet.ServletContext}
    * @param scriptData
    *            Content of JavaScript that will be added into available JavaScript
    */
   @Deprecated
   public synchronized void addExtendedJavascript(String module, String scriptPath, ServletContext scontext, String scriptData)
   {
      ExtendedJScript js = new ExtendedJScript(module, scriptPath, scontext.getContextPath(), scriptData);
      commonJScripts.add(js);
      if (log.isDebugEnabled())
      {
         log.debug("Added an extended javascript " + js);
      }
   }

   /**
    * Remove an extended Javascript from the list of common JScripts
    * 
    * @deprecated This method is deprecated according to {@link #addExtendedJavascript(String, String, ServletContext, String)}.
    * Use {@link #removeCommonJScript(String)} instead.
    * @param module
    *          module will be removed
    * @param scriptPath
    *          URI of script that will be removed
    * @param scontext
    *          the webapp's {@link javax.servlet.ServletContext}
    *          
    */
   @Deprecated
   public void removeExtendedJavascript(String module, String scriptPath, ServletContext scontext)
   {
      removeCommonJScript(module);
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
   
   private class ExtendedJScriptResourceResolver implements ResourceResolver
   {
      @Override
      public Resource resolve(String path) throws NullPointerException
      {
         for (final Javascript js : commonJScripts)
         {
            if (js instanceof ExtendedJScript && js.getPath().equals(path))
            {
               final String jScript = ((ExtendedJScript)js).getScript();
               return new Resource(path)
               {
                  @Override
                  public Reader read() throws IOException
                  {
                     return new StringReader(jScript);
                  }
               };
            }
         }
         return null;
      }
   }
}

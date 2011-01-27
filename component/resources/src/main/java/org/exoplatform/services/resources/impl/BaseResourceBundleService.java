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

package org.exoplatform.services.resources.impl;

import org.exoplatform.commons.cache.future.FutureCache;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.MapResourceBundle;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.IdentityResourceBundle;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.PropertiesClassLoader;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleLoader;
import org.exoplatform.services.resources.ResourceBundleService;
import org.picocontainer.Startable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SAS Mar 9, 2007
 */
abstract public class BaseResourceBundleService implements ResourceBundleService, Startable
{

   protected Log log_;

   protected volatile List<String> classpathResources_;

   protected volatile String[] portalResourceBundleNames_;

   protected LocaleConfigService localeService_;

   protected ExoCache<String, ResourceBundle> cache_;

   private volatile FutureCache<String, ResourceBundle, ResourceBundleContext> futureCache_;
   
   private final Loader<String, ResourceBundle, ResourceBundleContext> loader_ = new Loader<String, ResourceBundle, ResourceBundleContext>()
   {
      /**
       * {@inheritDoc}
       */
      public ResourceBundle retrieve(ResourceBundleContext context, String key) throws Exception
      {
         return context.get(key);
      }
   };
   
   private volatile List<String> initResources_;

   @SuppressWarnings("unchecked")
   protected void initParams(InitParams params)
   {
      classpathResources_ = params.getValuesParam("classpath.resources").getValues();

      // resources name can use for portlets
      List prnames = params.getValuesParam("portal.resource.names").getValues();
      portalResourceBundleNames_ = new String[prnames.size()];
      for (int i = 0; i < prnames.size(); i++)
      {
         portalResourceBundleNames_[i] = (String)prnames.get(i);
      }

      initResources_ = params.getValuesParam("init.resources").getValues();
   }

   /**
    * Add new resources bundles
    */
   public synchronized void addResourceBundle(BaseResourceBundlePlugin plugin)
   {
      List<String> classpathResources = plugin.getClasspathResources();
      if (classpathResources != null && !classpathResources.isEmpty())
      {
         List<String> result = new ArrayList<String>(classpathResources);
         if (classpathResources_ != null)
         {
            result.addAll(classpathResources_);
         }
         this.classpathResources_ = Collections.unmodifiableList(result);
      }
      List<String> portalResources = plugin.getPortalResources();
      if (portalResources != null && !portalResources.isEmpty())
      {
         List<String> result = new ArrayList<String>(portalResources);
         if (portalResourceBundleNames_ != null)
         {
            result.addAll(Arrays.asList(portalResourceBundleNames_));
         }
         this.portalResourceBundleNames_ = (String[])result.toArray(new String[result.size()]);
      }
      List<String> initResources = plugin.getInitResources();
      if (initResources != null && !initResources.isEmpty())
      {
         List<String> result = new ArrayList<String>(initResources);
         if (initResources_ != null)
         {
            result.addAll(initResources_);
         }
         this.initResources_ = Collections.unmodifiableList(result);
      }
   }

   /**
    * Loads all the "init" resource bundles
    * 
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
      PageList pl = null;
      try
      {
         pl = findResourceDescriptions(new Query(null, null));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot check if a resource already exists", e);
      }
      if (pl.getAvailable() > 0)
         return;

      // init resources
      List<String> initResources = initResources_;
      for (String resource : initResources)
      {
         initResources(resource, Thread.currentThread().getContextClassLoader());
      }
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
   }

   public ResourceBundle getResourceBundle(String[] name, Locale locale)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      return getResourceBundle(name, locale, cl);
   }

   public ResourceBundle getResourceBundle(String name, Locale locale)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      return getResourceBundle(name, locale, cl);
   }

   public String[] getSharedResourceBundleNames()
   {
      return portalResourceBundleNames_;
   }

   public ResourceBundleData createResourceBundleDataInstance()
   {
      return new ResourceBundleData();
   }

   protected boolean isClasspathResource(String name)
   {
      if (classpathResources_ == null)
         return false;
      for (int i = 0; i < classpathResources_.size(); i++)
      {
         String pack = classpathResources_.get(i);
         if (name.startsWith(pack))
            return true;
      }
      return false;
   }

   protected void initResources(String baseName, ClassLoader cl)
   {
      String name = baseName.replace('.', '/');
      try
      {
         Collection<LocaleConfig> localeConfigs = localeService_.getLocalConfigs();
         // String defaultLang =
         // localeService_.getDefaultLocaleConfig().getLanguage();
         Locale defaultLocale = localeService_.getDefaultLocaleConfig().getLocale();

         for (Iterator<LocaleConfig> iter = localeConfigs.iterator(); iter.hasNext();)
         {
            LocaleConfig localeConfig = iter.next();
            // String language = localeConfig.getLanguage();
            // String content = getResourceBundleContent(name, language,
            // defaultLang, cl);
            Locale locale = localeConfig.getLocale();
            String language = locale.getLanguage();
            String country = locale.getCountry();

            String content = getResourceBundleContent(name, locale, defaultLocale, cl);
            if (content != null)
            {
               // save the content
               ResourceBundleData data = new ResourceBundleData();
               if (country != null && country.length() > 0)
               {
                  data.setId(baseName + "_" + language + "_" + country);
               }
               else
               {
                  data.setId(baseName + "_" + language);
               }
               data.setName(baseName);
               data.setLanguage(language);
               data.setData(content);
               saveResourceBundle(data);
            }
         }
      }
      catch (Exception ex)
      {
         log_.error("Error while reading the resource bundle : " + baseName, ex);
      }
   }

   protected String getResourceBundleContent(String name, String language, String defaultLang, ClassLoader cl)
      throws Exception
   {
      String fileName = null;
      try
      {
         cl = new PropertiesClassLoader(cl, true);
         fileName = name + "_" + language + ".properties";
         URL url = cl.getResource(fileName);
         if (url == null && defaultLang.equals(language))
         {
            url = cl.getResource(name + ".properties");
         }
         if (url != null)
         {
            InputStream is = url.openStream();
            try
            {
               byte[] buf = IOUtil.getStreamContentAsBytes(is);
               return new String(buf, "UTF-8");
            }
            finally
            {
               try
               {
                  is.close();
               }
               catch (IOException e)
               {
                  // Do nothing
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new Exception("Error while reading the file: " + fileName, e);
      }
      return null;
   }

   /**
    * This method is used for country support
    * 
    * @param baseName
    * @param locale
    * @param defaultLocale
    * @param cl
    * @return
    */
   protected String getResourceBundleContent(String baseName, Locale locale, Locale defaultLocale, ClassLoader cl)
      throws Exception
   {
      List<String> candidateFiles = new ArrayList<String>();

      String language = locale.getLanguage();
      String country = locale.getCountry().toUpperCase();

      String defaultLanguage = defaultLocale.getLanguage();
      String defaultCountry = defaultLocale.getCountry().toUpperCase();

      if (country != null && country.length() > 0)
      {
         candidateFiles.add(baseName + "_" + language + "_" + country + ".properties");
      }

      if (language != null && language.length() > 0)
      {
         candidateFiles.add(baseName + "_" + language + ".properties");
      }

      if (defaultCountry != null && defaultCountry.length() > 0)
      {
         candidateFiles.add(baseName + "_" + defaultLanguage + "_" + defaultCountry + ".properties");
      }

      if (defaultLanguage != null && defaultLanguage.length() > 0)
      {
         candidateFiles.add(baseName + "_" + defaultLanguage + ".properties");
      }

      candidateFiles.add(baseName + ".properties");

      cl = new PropertiesClassLoader(cl, true);
      String fileName = null;

      try
      {
         URL url = null;
         for (String candidateFile : candidateFiles)
         {
            url = cl.getResource(candidateFile);
            if (url != null)
            {
               fileName = candidateFile;
               break;
            }
         }

         if (url != null)
         {
            InputStream is = url.openStream();
            try
            {
               byte[] buf = IOUtil.getStreamContentAsBytes(is);
               return new String(buf, "UTF-8");
            }
            finally
            {
               try
               {
                  is.close();
               }
               catch (IOException e)
               {
                  // Do nothing
               }
            }
         }
      }
      catch (Exception ex)
      {
         throw new Exception("Error while reading the file: " + fileName, ex);
      }

      return null;

   }

   /**
    * Invalidate an entry in the cache at this level. Normally this is called by
    * the subclass.
    * 
    * @param name
    *           the bundle name
    */
   protected final void invalidate(String name)
   {
      cache_.remove(name);
   }

   public ResourceBundle getResourceBundle(String name, Locale locale, ClassLoader cl)
   {
      if (IdentityResourceBundle.MAGIC_LANGUAGE.equals(locale.getLanguage()))
      {
         return IdentityResourceBundle.getInstance();
      }

      String country = locale.getCountry();
      String id;
      if (country != null && country.length() > 0)
      {
         id = name + "_" + locale.getLanguage() + "_" + locale.getCountry();
      }
      else
      {
         id = name + "_" + locale.getLanguage();
      }

      boolean isClasspathResource = isClasspathResource(name);
      boolean isCacheable = !isClasspathResource || !PropertyManager.isDevelopping();
      if (isCacheable && isClasspathResource)
      {
         // Avoid naming collision
         id += "_" + cl.getClass() + "_" + System.identityHashCode(cl);
      }

      // Case 1: ResourceBundle of portlets, standard java API is used
      if (isClasspathResource)
      {
         // Cache classpath resource bundle while running portal in non-dev mode
         if (isCacheable)
         {
            ResourceBundleLoaderContext ctx = new ResourceBundleLoaderContext(name, locale, cl);
            ResourceBundle result = getFutureCache().get(ctx, id);
            if (ctx.e != null)
            {
               // Throw the RuntimeException if it occurs to remain compatible with the old behavior
               throw ctx.e;
            }
            return result;
         }
         else
         {
            return ResourceBundleLoader.load(name, locale, cl);           
         }
      }

      // Case 2: ResourceBundle of portal
      return getFutureCache().get(new GetResourceBundleFromDbContext(name, locale), id);
   }

   public ResourceBundle getResourceBundle(String[] name, Locale locale, ClassLoader cl)
   {
      if (IdentityResourceBundle.MAGIC_LANGUAGE.equals(locale.getLanguage()))
      {
         return IdentityResourceBundle.getInstance();
      }
      StringBuilder idBuf = new StringBuilder("merge:");
      for (String n : name)
         idBuf.append(n).append("_");
      idBuf.append(locale);
      String id = idBuf.toString();
      return getFutureCache().get(new GetResourceBundleContext(name, locale, cl), id);
   }

   protected FutureCache<String, ResourceBundle, ResourceBundleContext> getFutureCache()
   {
      if (futureCache_ == null)
      {
         synchronized(this)
         {
            if (futureCache_ == null)
            {
               futureCache_ = new FutureExoCache<String, ResourceBundle, ResourceBundleContext>(loader_, cache_);
            }
         }
      }
      return futureCache_;
   }
   
   abstract protected ResourceBundle getResourceBundleFromDb(String id, ResourceBundle parent, Locale locale)
      throws Exception;

   /**
    * Generic class defining a context needed to get a ResourceBundle
    */
   private static abstract class ResourceBundleContext
   {
      /**
       * Get the resource bundle corresponding to the context
       */
      abstract ResourceBundle get(String id);
   }

   /**
    * The class defining the context required to load a ResourceBundle thanks to the method 
    * <code>ResourceBundleLoader.load(String name, Locale locale, ClassLoader cl)</code>
    */
   private static class ResourceBundleLoaderContext extends ResourceBundleContext
   {
      private final String name;

      private final Locale locale;

      private final ClassLoader cl;
      
      private RuntimeException e;

      public ResourceBundleLoaderContext(String name, Locale locale, ClassLoader cl)
      {
         this.name = name;
         this.locale = locale;
         this.cl = cl;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      ResourceBundle get(String id)
      {
         try
         {
            return ResourceBundleLoader.load(name, locale, cl);
         }
         catch (RuntimeException e)
         {
            this.e = e;
         }
         return null;
      }
   }

   /**
    * The class defining the context required to load a ResourceBundle thanks to the method 
    * <code>getResourceBundleFromDb(String id, ResourceBundle parent, Locale locale)</code>
    */
   private class GetResourceBundleFromDbContext extends ResourceBundleContext
   {
      private final String name;

      private final Locale locale;

      public GetResourceBundleFromDbContext(String name, Locale locale)
      {
         this.name = name;
         this.locale = locale;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      ResourceBundle get(String id)
      {
         ResourceBundle res = null;
         try
         {
            String rootId = name + "_" + localeService_.getDefaultLocaleConfig().getLanguage();
            ResourceBundle parent = getResourceBundleFromDb(rootId, null, locale);
            if (parent != null)
            {
               res = getResourceBundleFromDb(id, parent, locale);
               if (res == null)
                  res = parent;
            }
         }
         catch (Exception ex)
         {
            log_.error("Error: " + id, ex);
         }
         return res;
      }
   }
  
   /**
    * The class defining the context required to load a ResourceBundle thanks to the method 
    * <code>getResourceBundle(String[] name, Locale locale, ClassLoader cl)</code>
    */
   private class GetResourceBundleContext extends ResourceBundleContext
   {
      private final String[] name;

      private final Locale locale;

      private final ClassLoader cl;

      public GetResourceBundleContext(String[] name, Locale locale, ClassLoader cl)
      {
         this.name = name;
         this.locale = locale;
         this.cl = cl;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      ResourceBundle get(String id)
      {
         MapResourceBundle outputBundled = null;
         try
         {
            outputBundled = new MapResourceBundle(locale);
            for (int i = 0; i < name.length; i++)
            {
               ResourceBundle temp = getResourceBundle(name[i], locale, cl);
               if (temp != null)
               {
                  outputBundled.merge(temp);
                  continue;
               }
               log_.warn("Cannot load and merge the bundle: " + name[i]);
            }
            outputBundled.resolveDependencies();
         }
         catch (Exception ex)
         {
            log_.error("Cannot load and merge the bundle: " + id, ex);
         }
         return outputBundled;
      }
   }
}
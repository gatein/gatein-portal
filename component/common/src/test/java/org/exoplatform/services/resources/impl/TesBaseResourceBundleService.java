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

import junit.framework.TestCase;

import org.exoplatform.commons.utils.MapResourceBundle;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.resources.ExoResourceBundle;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class TesBaseResourceBundleService extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      // Any language
      Locale.setDefault(new Locale("xxx", "xxx"));
   }

   public void testResourceBundleContent() throws Exception
   {
      BaseResourceBundleService service = new MyResourceBundleService(false);
      String content;
      // Simple resource bundle
      content =
         service.getResourceBundleContent("locale/test/myRB1", "en", "en", Thread.currentThread()
            .getContextClassLoader());
      assertTrue(content.contains("my.key1=My New Value In English"));
      assertTrue(content.contains("my.key3=My New Value In English"));
      content =
         service.getResourceBundleContent("locale/test/myRB1", "fr", "en", Thread.currentThread()
            .getContextClassLoader());
      assertTrue(content
         .contains("my.key1=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key2=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      content =
         service.getResourceBundleContent("locale/test/myRB2", "en", "en", Thread.currentThread()
            .getContextClassLoader());
      assertTrue(content.contains("my.key1=My New Value In English"));
      assertTrue(content.contains("my.key3=My New Value In English"));
      content =
         service.getResourceBundleContent("locale/test/myRB2", "fr", "en", Thread.currentThread()
            .getContextClassLoader());
      assertTrue(content
         .contains("my.key1=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key2=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      // Multiple resources bundles
      content = service.getResourceBundleContent("locale/test/myRB1", "en", "en", new MyClassLoader());
      assertTrue(content.contains("my.key1=My New Value In English"));
      assertTrue(content.contains("my.key2=My New Value In English v2"));
      assertTrue(content.contains("my.key3=My New Value In English v2"));
      content = service.getResourceBundleContent("locale/test/myRB1", "fr", "en", new MyClassLoader());
      assertTrue(content
         .contains("my.key1=My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key2=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key3=My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      content = service.getResourceBundleContent("locale/test/myRB2", "en", "en", new MyClassLoader());
      assertTrue(content.contains("my.key1=My New Value In English"));
      assertTrue(content.contains("my.key2=My New Value In English v2"));
      assertTrue(content.contains("my.key3=My New Value In English v2"));
      content = service.getResourceBundleContent("locale/test/myRB2", "fr", "en", new MyClassLoader());
      assertTrue(content
         .contains("my.key1=My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key2=My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
      assertTrue(content
         .contains("my.key3=My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5"));
   }

   public void testGetResourceBundle() throws Exception
   {
      BaseResourceBundleService service = new MyResourceBundleService(true);
      ResourceBundle rb;
      rb =
         service.getResourceBundle("locale.test.myRB1", Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB1", Locale.FRENCH, Thread.currentThread().getContextClassLoader());
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.JAPAN, Thread.currentThread().getContextClassLoader());
      assertEquals("My Default Value In English", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB2", Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB2", Locale.FRENCH, Thread.currentThread().getContextClassLoader());
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.JAPAN, Thread.currentThread().getContextClassLoader());
      assertEquals("My Default Value In English", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      service = new MyResourceBundleService(false);
      rb =
         service.getResourceBundle("locale.test.myRB1", Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB1", Locale.FRENCH, Thread.currentThread().getContextClassLoader());
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.JAPAN, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB2", Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb =
         service.getResourceBundle("locale.test.myRB2", Locale.FRENCH, Thread.currentThread().getContextClassLoader());
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.JAPAN, Thread.currentThread().getContextClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English", rb.getString("my.key3"));
      // Multiple resources bundles
      service = new MyResourceBundleService(new MyClassLoader(), true);
      rb = service.getResourceBundle("locale.test.myRB1", Locale.ENGLISH, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.FRENCH, new MyClassLoader());
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.JAPAN, new MyClassLoader());
      assertEquals("My Default Value In English v2", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.ENGLISH, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.FRENCH, new MyClassLoader());
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.JAPAN, new MyClassLoader());
      assertEquals("My Default Value In English v2", rb.getString("my.key1"));
      assertEquals("My Default Value In English", rb.getString("my.key2"));
      assertEquals("My Default Value In English", rb.getString("my.key3"));
      service = new MyResourceBundleService(new MyClassLoader(), false);
      rb = service.getResourceBundle("locale.test.myRB1", Locale.ENGLISH, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.FRENCH, new MyClassLoader());
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB1", Locale.JAPAN, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.ENGLISH, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.FRENCH, new MyClassLoader());
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key1"));
      assertEquals("My Value In French with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key2"));
      assertEquals("My Value In French v2 with special characters such as \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", rb
         .getString("my.key3"));
      rb = service.getResourceBundle("locale.test.myRB2", Locale.JAPAN, new MyClassLoader());
      assertEquals("My New Value In English", rb.getString("my.key1"));
      assertEquals("My New Value In English v2", rb.getString("my.key2"));
      assertEquals("My New Value In English v2", rb.getString("my.key3"));
   }

   public void testInitResources() throws Exception
   {
      MyResourceBundleService service = new MyResourceBundleService(true);
      service.initResources("locale.test.myRB1", Thread.currentThread().getContextClassLoader());
      assertEquals(2, service.getRbs().size());
      service.getRbs().clear();
      service.initResources("locale.test.myRB2", Thread.currentThread().getContextClassLoader());
      assertEquals(2, service.getRbs().size());
   }

   private static class MyResourceBundleService extends BaseResourceBundleService
   {

      private ClassLoader classLoader;

      private boolean isClasspathResource;

      private List<ResourceBundleData> rbs = new ArrayList<ResourceBundleData>();

      public MyResourceBundleService(boolean isClasspathResource) throws Exception
      {
         this(Thread.currentThread().getContextClassLoader(), isClasspathResource);
      }

      public MyResourceBundleService(ClassLoader classLoader, boolean isClasspathResource) throws Exception
      {
         this.classLoader = classLoader;
         this.isClasspathResource = isClasspathResource;
         this.localeService_ = createService();
         this.cache_ = new ExoCache()
         {

            public void addCacheListener(CacheListener arg0)
            {
            }

            public void clearCache()
            {
            }

            public Object get(Serializable arg0)
            {
               return null;
            }

            public int getCacheHit()
            {
               return 0;
            }

            public int getCacheMiss()
            {
               return 0;
            }

            public int getCacheSize()
            {
               return 0;
            }

            public List getCachedObjects()
            {
               return null;
            }

            public String getLabel()
            {
               return null;
            }

            public long getLiveTime()
            {
               return 0;
            }

            public int getMaxSize()
            {
               return 0;
            }

            public String getName()
            {
               return null;
            }

            public boolean isDistributed()
            {
               return false;
            }

            public boolean isLogEnabled()
            {
               return false;
            }

            public boolean isReplicated()
            {
               return false;
            }

            public void put(Serializable arg0, Object arg1)
            {
            }

            public void putMap(Map arg0)
            {
            }

            public Object remove(Serializable arg0)
            {
               return null;
            }

            public List removeCachedObjects()
            {
               return null;
            }

            public void select(CachedObjectSelector arg0) throws Exception
            {
            }

            public void setDistributed(boolean arg0)
            {
            }

            public void setLabel(String arg0)
            {
            }

            public void setLiveTime(long arg0)
            {
            }

            public void setLogEnabled(boolean arg0)
            {
            }

            public void setMaxSize(int arg0)
            {
            }

            public void setName(String arg0)
            {
            }

            public void setReplicated(boolean arg0)
            {
            }

         };
      }

      private LocaleConfigService createService() throws Exception
      {
         ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
         InitParams params = new InitParams();
         ValueParam param = new ValueParam();
         param.setName("locale.config.file");
         param.setValue("classpath:/resources/locales-config.xml");
         params.addParameter(param);

         //
         LocaleConfigService service = new LocaleConfigServiceImpl(params, cm);
         return service;
      }

      @Override
      protected boolean isClasspathResource(String name)
      {
         return isClasspathResource;
      }

      @Override
      protected ResourceBundle getResourceBundleFromDb(String id, ResourceBundle parent, Locale locale)
         throws Exception
      {
         String language = locale.getLanguage();
         int index = id.indexOf('_');
         if (index >= 0)
         {
            language = id.substring(index + 1);
            id = id.substring(0, index);
         }
         String content = getResourceBundleContent(id.replace('.', '/'), language, "en", classLoader);
         if (content == null)
         {
            return null;
         }
         ResourceBundle result = new ExoResourceBundle(content, parent);
         return new MapResourceBundle(result, locale);
      }

      public PageList findResourceDescriptions(Query q) throws Exception
      {
         return null;
      }

      public ResourceBundleData getResourceBundleData(String id) throws Exception
      {
         return null;
      }

      public ResourceBundleData removeResourceBundleData(String id) throws Exception
      {
         return null;
      }

      public void saveResourceBundle(ResourceBundleData data) throws Exception
      {
         rbs.add(data);
      }

      public List<ResourceBundleData> getRbs()
      {
         return rbs;
      }
   }

   private static class MyClassLoader extends ClassLoader
   {

      public MyClassLoader()
      {
         super(Thread.currentThread().getContextClassLoader());
      }

      @Override
      public Enumeration<URL> getResources(String name) throws IOException
      {
         List<URL> result = new ArrayList<URL>();
         URL url = super.getResource(name);
         if (url != null)
         {
            result.add(url);
            result.add(super.getResource(name.replace(".", "-2.")));
         }
         return Collections.enumeration(result);
      }
   }
}

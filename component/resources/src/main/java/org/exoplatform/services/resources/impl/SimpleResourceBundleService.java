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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.MapResourceBundle;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.resources.ExoResourceBundle;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by The eXo Platform SARL
 * Author : Tung Pham
 *          thanhtungty@gmail.com
 * Dec 1, 2007
 */
public class SimpleResourceBundleService extends BaseResourceBundleService
{

   private final ConcurrentMap<String, ResourceBundleData> bundles = new ConcurrentHashMap<String, ResourceBundleData>();

   public SimpleResourceBundleService(InitParams params, CacheService cService, LocaleConfigService localeService) throws Exception
   {
      log_ = ExoLogger.getLogger("org.exoplatform.services.resources");
      localeService_ = localeService;
      cache_ = cService.getCacheInstance(ResourceBundleData.class.getSimpleName());
      initParams(params);
   }

   public ResourceBundleData getResourceBundleData(String id) throws Exception
   {
      return bundles.get(id);
   }

   public ResourceBundleData removeResourceBundleData(String id) throws Exception
   {
      if (id == null)
      {
         return null;
      }
      ResourceBundleData data = bundles.remove(id);
      invalidate(id);
      return data;
   }

   public void saveResourceBundle(ResourceBundleData resourceData) throws Exception
   {
      String id = resourceData.getId();
      bundles.put(id, resourceData);
      invalidate(id);
   }

   public PageList<ResourceBundleData> findResourceDescriptions(Query q) throws Exception
   {
      final ArrayList<ResourceBundleData> list = new ArrayList<ResourceBundleData>();
      for (ResourceBundleData data : bundles.values())
      {
         boolean matches = true;
         if (q.getName() != null)
         {
            matches &= q.getName().equals(data.getName());
         }
         if (q.getLanguage() != null)
         {
            matches &= q.getLanguage().equals(data.getLanguage());
         }
         if (matches)
         {
            list.add(data);
         }
      }
      Collections.sort(list, new Comparator<ResourceBundleData>()
      {
         public int compare(ResourceBundleData o1, ResourceBundleData o2)
         {
            String l1 = o1.getLanguage();
            String l2 = o2.getLanguage();
            if (l1 == null)
            {
               return l2 == null ? 0 : 1;
            }
            else
            {
               return l1.compareTo(l2);
            }
         }
      });
      return new LazyPageList<ResourceBundleData>(new ListAccess<ResourceBundleData>()
      {
         public ResourceBundleData[] load(int index, int length) throws Exception, IllegalArgumentException
         {
            List<ResourceBundleData> sub = list.subList(index, index + length);
            return sub.toArray(new ResourceBundleData[sub.size()]);
         }

         public int getSize() throws Exception
         {
            return list.size();
         }
      }, 20);
   }

   @Override
   protected ResourceBundle getResourceBundleFromDb(String id, ResourceBundle parent, Locale locale) throws Exception
   {
      ResourceBundleData data = getResourceBundleData(id);
      if (data == null)
      {
         return null;
      }
      return new MapResourceBundle(new ExoResourceBundle(data, parent), locale);
   }
}
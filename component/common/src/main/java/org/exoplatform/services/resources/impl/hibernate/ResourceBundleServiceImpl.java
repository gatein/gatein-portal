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

package org.exoplatform.services.resources.impl.hibernate;

import org.exoplatform.commons.utils.MapResourceBundle;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.DBObjectPageList;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.resources.ExoResourceBundle;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleDescription;
import org.exoplatform.services.resources.impl.BaseResourceBundleService;
import org.hibernate.Session;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SAS. Author : Roman Pedchenko
 * roman.pedchenko@exoplatform.com.ua Apr 10, 2007
 */
public class ResourceBundleServiceImpl extends BaseResourceBundleService
{

   /**
    * Hibernate service.
    */
   private final HibernateService hService;

   /**
    * @param service hibernate service
    * @param localeService locale service
    * @param cService cache service
    * @param params init parameters
    * @throws Exception exception
    */
   public ResourceBundleServiceImpl(final HibernateService service, final LocaleConfigService localeService,
      final CacheService cService, final InitParams params) throws Exception
   {

      log_ = ExoLogger.getLogger("org.exoplatform.services.portletcontainer");
      cache_ = cService.getCacheInstance(getClass().getName());
      localeService_ = localeService;
      hService = service;

      initParams(params);
   }

   /**
    * Overridden method.
    * 
    * @param name name
    * @return data
    * @throws Exception exception
    * @see org.exoplatform.services.resources.ResourceBundleService#getResourceBundleData(java.lang.String)
    */
   public final ResourceBundleData getResourceBundleData(final String name) throws Exception
   {
      return (ResourceBundleData)hService.findOne(ResourceBundleData.class, name);
   }

   /**
    * Overridden method.
    * 
    * @param id id
    * @return data
    * @throws Exception exception
    * @see org.exoplatform.services.resources.ResourceBundleService#removeResourceBundleData(java.lang.String)
    */
   public final ResourceBundleData removeResourceBundleData(final String id) throws Exception
   {
      ResourceBundleData data = (ResourceBundleData)hService.remove(ResourceBundleData.class, id);
      cache_.remove(data.getId());
      return data;
   }

   /**
    * Overridden method.
    * 
    * @param q query
    * @return page list
    * @throws Exception exception
    * @see org.exoplatform.services.resources.ResourceBundleService#findResourceDescriptions(org.exoplatform.services.resources.Query)
    */
   public final PageList findResourceDescriptions(final Query q) throws Exception
   {
      String name = q.getName();
      if ((name == null) || (name.length() == 0))
         name = "%";
      ObjectQuery oq = new ObjectQuery(ResourceBundleDescription.class);
      oq.addLIKE("name", name);
      oq.addLIKE("language", q.getLanguage());
      oq.setDescOrderBy("name");
      return new DBObjectPageList(hService, oq);
   }

   /**
    * Overridden method.
    * 
    * @param data data
    * @throws Exception exception
    * @see org.exoplatform.services.resources.ResourceBundleService#saveResourceBundle(org.exoplatform.services.resources.ResourceBundleData)
    */
   public final void saveResourceBundle(final ResourceBundleData data) throws Exception
   {
      hService.save(data);
      cache_.remove(data.getId());
   }

   /**
    * Overridden method.
    * 
    * @param id id
    * @param parent parent
    * @param locale locale
    * @returnresource bundle
    * @throws Exception exception
    * @see org.exoplatform.services.resources.impl.BaseResourceBundleService#getResourceBundleFromDb(java.lang.String,
    *      java.util.ResourceBundle, java.util.Locale)
    */
   protected final ResourceBundle getResourceBundleFromDb(final String id, final ResourceBundle parent,
      final Locale locale) throws Exception
   {

      Session session = hService.openSession();
      ResourceBundleData data = (ResourceBundleData)session.get(ResourceBundleData.class, id);
      if (data != null)
      {
         ResourceBundle res = new ExoResourceBundle(data.getData(), parent);
         MapResourceBundle mres = new MapResourceBundle(res, locale);
         return mres;
      }
      // return null;

      ResourceBundle rB;
      try
      {
         rB = ResourceBundle.getBundle(id, locale, Thread.currentThread().getContextClassLoader());
      }
      catch (MissingResourceException e)
      {
         rB = null;
      }
      if (rB != null)
      {
         return new MapResourceBundle(rB, locale);

      }
      else
         return null;
   }
}

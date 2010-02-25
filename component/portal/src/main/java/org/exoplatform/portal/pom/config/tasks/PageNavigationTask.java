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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.data.Mapper;

import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageNavigationTask
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(PageNavigationTask.class);

   /** . */
   protected final ObjectType<? extends Site> siteType;

   /** . */
   protected final NavigationKey key;

   protected PageNavigationTask(NavigationKey key)
   {
      this.key = key;
      this.siteType = Mapper.parseSiteType(key.getType());
   }

   public static class Load extends PageNavigationTask implements CacheableDataTask<NavigationKey, NavigationData>
   {

      public Load(NavigationKey key)
      {
         super(key);
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public NavigationKey getKey()
      {
         return key;
      }

      public Class<NavigationData> getValueType()
      {
         return NavigationData.class;
      }

      public NavigationData run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, key.getId());
         if (site != null)
         {
            Navigation nav = site.getRootNavigation();
            Navigation defaultNav = nav.getChild("default");
            if (defaultNav != null)
            {
               return new Mapper(session).load(defaultNav);
            }
         }
         else
         {
            log.debug("Cannot load page navigation as the corresponding portal " + key.getId()
               + " with type " + siteType + " does not exist");
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PageNavigation.Load[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }

   public static class Save extends PageNavigationTask implements CacheableDataTask<NavigationKey, Void>
   {

      /** . */
      private final NavigationData pageNav;

      /** . */
      private final boolean overwrite;

      public Save(NavigationData pageNav, boolean overwrite)
      {
         super(pageNav.getKey());

         //
         this.pageNav = pageNav;
         this.overwrite = overwrite;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public DataAccessMode getAccessMode()
      {
         return pageNav.getStorageId() != null ? DataAccessMode.WRITE : DataAccessMode.CREATE;
      }

      public NavigationKey getKey()
      {
         return key;
      }

      public Void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, key.getId());
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot insert page navigation "
               + " as the corresponding portal " + key.getId() + " with type " + siteType + " does not exist");
         }

         // Delete node descendants first
         Navigation nav = site.getRootNavigation();

         //
         Navigation defaultNav = nav.getChild("default");
         if (defaultNav == null)
         {
            defaultNav = nav.addChild("default");
         }

         //
         new Mapper(session).save(pageNav, defaultNav);

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PageNavigation.Save[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }

   public static class Remove extends PageNavigationTask implements CacheableDataTask<NavigationKey, Void>
   {

      public Remove(NavigationData pageNav)
      {
         super(pageNav.getKey());
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.DESTROY;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public NavigationKey getKey()
      {
         return key;
      }

      public Void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, key.getId());
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot insert page navigation "
               + " as the corresponding portal " + key.getId() + " with type " + siteType + " does not exist");
         }

         // Delete descendants
         Navigation nav = site.getRootNavigation();

         //
         Navigation defaultNav = nav.getChild("default");
         if (defaultNav != null)
         {
            defaultNav.destroy();
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PageNavigation.Remove[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }
}
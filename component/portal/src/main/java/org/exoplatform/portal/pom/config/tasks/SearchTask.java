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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SearchTask<T> implements POMTask<LazyPageList<T>>
{

   /** . */
   protected final Query<T> q;

   public SearchTask(Query<T> query)
   {
      this.q = query;
   }

   public abstract static class FindSiteObject<W extends WorkspaceObject, T> extends SearchTask<T>
   {

      public FindSiteObject(Query<T> query)
      {
         super(query);
      }

      public final LazyPageList<T> run(final POMSession session) throws Exception
      {
         Iterator<W> ite;
         try
         {
            String ownerType = q.getOwnerType();
            ObjectType<? extends Site> siteType = null;
            if (ownerType != null)
            {
               siteType = Mapper.parseSiteType(ownerType.trim());
            }
            ite = findW(session, siteType, q.getOwnerId(), q.getTitle());

         }
         catch (IllegalArgumentException e)
         {
            ite = Collections.<W>emptyList().iterator();
         }

         //
         final ArrayList<String> array = new ArrayList<String>();
         while (ite.hasNext())
         {
            array.add(ite.next().getObjectId());
         }

         //
         final POMSessionManager manager = session.getManager();
         final Iterator<String> it = array.iterator();
         ListAccess<T> la = new ListAccess<T>()
         {
            public T[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               POMSession session = manager.getSession();
               T[] result = createT(length);
               for (int i = 0; i < length; i++)
               {
                  T t = loadT(session, it.next());
                  result[i] = t;
               }
               return result;
            }

            public int getSize() throws Exception
            {
               return array.size();
            }
         };

         //
         return new LazyPageList<T>(la, 10);
      }

      protected abstract Iterator<W> findW(POMSession session, ObjectType<? extends Site> siteType, String ownerId,
         String title);

      protected abstract T[] createT(int length);

      protected abstract T loadT(POMSession session, String id);

   }

   public static class FindPage extends FindSiteObject<org.gatein.mop.api.workspace.Page, PageData>
   {

      public FindPage(Query<PageData> pageQuery)
      {
         super(pageQuery);
      }

      protected Iterator<org.gatein.mop.api.workspace.Page> findW(POMSession session,
         ObjectType<? extends Site> siteType, String ownerId, String title)
      {
         return session.findObjects(ObjectType.PAGE, siteType, q.getOwnerId(), q.getTitle());
      }

      protected PageData[] createT(int length)
      {
         return new PageData[length];
      }

      protected PageData loadT(POMSession session, String id)
      {
         Page page = session.getManager().getPOMService().getModel().findObjectById(ObjectType.PAGE, id);
         return new Mapper(session).load(page);
      }
   }

   public static class FindNavigation extends FindSiteObject<Navigation, NavigationData>
   {

      public FindNavigation(Query<NavigationData> pageQuery)
      {
         super(pageQuery);
      }

      protected Iterator<Navigation> findW(POMSession session, ObjectType<? extends Site> siteType, String ownerId,
         String title)
      {
         return session.findObjects(ObjectType.NAVIGATION, siteType, q.getOwnerId(), q.getTitle());
      }

      protected NavigationData[] createT(int length)
      {
         return new NavigationData[length];
      }

      protected NavigationData loadT(POMSession session, String id)
      {
         Navigation nav = session.getManager().getPOMService().getModel().findObjectById(ObjectType.NAVIGATION, id);
         return new Mapper(session).load(nav);
      }
   }

   public static class FindPortletPreferences extends SearchTask<PortletPreferences>
   {

      public FindPortletPreferences(Query<PortletPreferences> portletPreferencesQuery)
      {
         super(portletPreferencesQuery);
      }

      public LazyPageList<PortletPreferences> run(final POMSession session) throws Exception
      {
         // We return empty on purpose at it is used when preferences are deleted by the UserPortalConfigService
         // and the prefs are deleted transitively when an entity is removed
         return new LazyPageList<PortletPreferences>(new ListAccess<PortletPreferences>()
         {
            public PortletPreferences[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               throw new AssertionError();
            }

            public int getSize() throws Exception
            {
               return 0;
            }
         }, 10);
      }
   }

   public static class FindSite extends SearchTask<PortalData>
   {

      public FindSite(Query<PortalData> siteQuery)
      {
         super(siteQuery);
      }

      public LazyPageList<PortalData> run(final POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         String ownerType = q.getOwnerType();
         ObjectType<Site> siteType = ownerType == null ? ObjectType.PORTAL_SITE : Mapper.parseSiteType(ownerType);
         final Collection<? extends Site> portals = workspace.getSites(siteType);
         final Iterator<? extends Site> iterator = portals.iterator();
         ListAccess<PortalData> la = new ListAccess<PortalData>()
         {
            public PortalData[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               //Iterator<? extends Site> iterator = portals.iterator();
               Mapper mapper = new Mapper(session);
               PortalData[] result = new PortalData[length];
               for (int i = 0; i < length; i++)
               {
                  result[i] = mapper.load(iterator.next());
               }
               return result;
            }

            public int getSize() throws Exception
            {
               return portals.size();
            }
         };
         return new LazyPageList<PortalData>(la, 10);
      }
   }

   public static class FindSiteKey extends SearchTask<PortalKey>
   {

      public FindSiteKey(Query<PortalKey> siteQuery)
      {
         super(siteQuery);
      }

      public LazyPageList<PortalKey> run(final POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         final Collection<? extends Site> portals = workspace.getSites(ObjectType.PORTAL_SITE);
         ListAccess<PortalKey> la = new ListAccess<PortalKey>()
         {
            public PortalKey[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               Iterator<? extends Site> iterator = portals.iterator();
               PortalKey[] result = new PortalKey[length];
               for (int i = 0; i < length; i++)
               {
                  Site site = iterator.next();
                  result[i] = new PortalKey("portal", site.getName());
               }
               return result;
            }

            public int getSize() throws Exception
            {
               return portals.size();
            }
         };
         return new LazyPageList<PortalKey>(la, 10);
      }
   }
}

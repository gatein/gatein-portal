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
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

      /** . */
      public static Serializable PORTAL_KEY = "FindPortalSiteKey";

      /** . */
      public static Serializable GROUP_KEY = "FindGroupSiteKey";

      /** . */
      private final ObjectType<Site> type;

      /** . */
      private final Serializable key;

      public FindSiteKey(Query<PortalKey> siteQuery)
      {
         super(siteQuery);

         //
         ObjectType<Site> type;
         Serializable key;
         if ("portal".equals(siteQuery.getOwnerType()))
         {
            type = ObjectType.PORTAL_SITE;
            key = PORTAL_KEY;
         }
         else if ("group".equals(siteQuery.getOwnerType()))
         {
            type = ObjectType.GROUP_SITE;
            key = GROUP_KEY;
         }
         else
         {
            throw new IllegalArgumentException("Invalid site type " + siteQuery.getOwnerType());
         }

         //
         this.type = type;
         this.key = key;
      }

      public ObjectType<Site> getType()
      {
         return type;
      }

      public Serializable getKey()
      {
         return key;
      }

      public LazyPageList<PortalKey> run(final POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Collection<Site> sites = workspace.getSites(type);
         final ArrayList<PortalKey> keys = new ArrayList<PortalKey>(sites.size());
         for (Site site : sites)
         {
            keys.add(new PortalKey(q.getOwnerType(), site.getName()));
         }
         ListAccess<PortalKey> la = new ListAccess<PortalKey>()
         {
            public PortalKey[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               PortalKey[] result = new PortalKey[length];
               for (int i = 0; i < length; i++)
               {
                  result[i] = keys.get(index++);
               }
               return result;
            }

            public int getSize() throws Exception
            {
               return keys.size();
            }
         };
         return new LazyPageList<PortalKey>(la, 10);
      }
   }

  @Override
  public String toString()
  {
     return getClass().getSimpleName() + "[query=" + q + "]";
  }
}

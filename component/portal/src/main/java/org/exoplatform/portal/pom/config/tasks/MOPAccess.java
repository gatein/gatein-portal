/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.chromattic.api.query.QueryResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class MOPAccess<E, I> implements ListAccess<E>
{

   /** . */
   private final POMSessionManager mgr;

   /** . */
   private final ObjectType<Site> ownerType;

   /** . */
   private final String ownerId;

   /** . */
   private final String title;

   /** . */
   private Integer size;

   MOPAccess(POMSessionManager mgr, Query<E> query)
   {
      String ownerType = query.getOwnerType();
      ObjectType<Site> siteType = null;
      if (ownerType != null)
      {
         siteType = Mapper.parseSiteType(ownerType);
      }

      //
      this.mgr = mgr;
      this.ownerType = siteType;
      this.ownerId = query.getOwnerId();
      this.title = query.getTitle();
      this.size = null;
   }

   public E[] load(final int index, final int length) throws Exception, IllegalArgumentException
   {
      return mgr.execute(new POMTask<E[]>()
      {
         public E[] run(POMSession session) throws Exception
         {
            QueryResult<I> res = findW(session, ownerType, ownerId, title, index, length);
            E[] elements = createT(length);
            int index = 0;
            while (res.hasNext() && index < length)
            {
               I internal = res.next();
               E external = convert(session, internal);
               elements[index++] = external;
            }
            return elements;
         }
         public String toString()
         {
            return MOPAccess.this.getClass().getSimpleName() + "[offset=" + index + ",limit=" + length +  "]";
         }
      });
   }

   public int getSize() throws Exception
   {
      if (size == null)
      {
         size = mgr.execute(new POMTask<Integer>()
         {
            public Integer run(POMSession session) throws Exception
            {
               QueryResult res = findW(session, ownerType, ownerId, title, 0, 1);
               return res.hits();
            }
            public String toString()
            {
               return MOPAccess.this.getClass().getSimpleName() + "[size]";
            }
         });
         if (size == null)
         {
            size = 0;
         }
      }
      return size;
   }

   protected abstract QueryResult<I> findW(
      POMSession session,
      ObjectType<Site> siteType,
      String ownerId,
      String title, int offset, int limit);

   protected abstract E[] createT(int length);

   protected abstract E convert(POMSession session, I internal);

   public static class PageAccess extends MOPAccess<PageData, Page>
   {

      public PageAccess(POMSessionManager mgr, Query<PageData> pageDataQuery)
      {
         super(mgr, pageDataQuery);
      }

      @Override
      protected QueryResult<Page> findW(POMSession session, ObjectType<Site> siteType, String ownerId, String title, int offset, int limit)
      {
         return session.findObjects(ObjectType.PAGE, siteType, ownerId, title, offset, limit);
      }

      @Override
      protected PageData convert(POMSession session, Page internal)
      {
         return new Mapper(session).load(internal);
      }

      @Override
      protected PageData[] createT(int length)
      {
         return new PageData[length];
      }
   }
}

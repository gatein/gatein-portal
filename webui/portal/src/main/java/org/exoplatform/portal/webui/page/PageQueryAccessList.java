/*
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

package org.exoplatform.portal.webui.page;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PageQueryAccessList extends PageListAccess<PageModel, Query<Page>>
{

   public PageQueryAccessList(Query<Page> state, int pageSize)
   {
      super(state, pageSize);
   }

   @Override
   protected ListAccess<PageModel> create(Query<Page> state) throws Exception
   {
      ExoContainer container = PortalContainer.getInstance();
      final PageService pageService = (PageService) container.getComponentInstance(PageService.class);
      final SiteType siteType = SiteType.valueOf(state.getOwnerType().toUpperCase());
      final String siteName = state.getOwnerId();
      final String name = state.getName();
      final String title = state.getTitle();
      final QueryResult<PageContext> total = pageService.findPages(0, 1, siteType, null, null, null);
      
      //
      ListAccess<PageModel> result = new ListAccess<PageModel>()
      {
         @Override
         public PageModel[] load(int index, int length) throws Exception
         {
            Iterator<PageContext> iterator = pageService.findPages(index, length, siteType, siteName, name, title).iterator();
            ArrayList<PageModel> copy = new ArrayList<PageModel>();
            while(iterator.hasNext())
            {
               copy.add(new PageModel(iterator.next()));
            }
            return copy.toArray(new PageModel[copy.size()]);
         }

         @Override
         public int getSize() throws Exception
         {
            return total.getHits();
         }
      };
      
      //
      return result;
   }
}

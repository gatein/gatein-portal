/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */
public class PageIterator implements Serializable, Iterator<List<?>> {
    private final String ownerType;

    private final String ownerId;

    private final String name;

    private final String title;

    private int pageSize = 10;

    private int currentIndex = 0;

    private boolean hasNext = true;

    public PageIterator(String ownerType, String ownerId, String name, String title, int pageSize) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.name = name;
        this.title = title;
        this.pageSize = pageSize;
    }

    public boolean hasNext() {
        if (hasNext && currentIndex == 0) {
            ExoContainer container = PortalContainer.getInstance();
            PageService pageService = (PageService) container.getComponentInstance(PageService.class);
            QueryResult<PageContext> result = pageService.findPages(0, 1, SiteType.valueOf(ownerType.toUpperCase()), ownerId,
                    name, title);

            hasNext = result.getSize() > 0;
        }

        //
        return hasNext;
    }

    /**
     *
     * @return Returns a list of pages or returns null if has not any page.
     */
    public List<PageModel> next() {
        ExoContainer container = PortalContainer.getInstance();
        PageService pageService = (PageService) container.getComponentInstance(PageService.class);
        QueryResult<PageContext> result = pageService.findPages(currentIndex, pageSize + 1,
                SiteType.valueOf(ownerType.toUpperCase()), ownerId, name, title);

        int size = result.getSize();
        hasNext = size > pageSize;

        //
        if (size > 0) {
            int hsize = hasNext ? pageSize : size;
            List<PageModel> holder = new ArrayList<PageModel>(hsize);

            Iterator<PageContext> iterator = result.iterator();
            while (holder.size() < hsize) {
                holder.add(new PageModel(iterator.next()));
            }

            currentIndex += holder.size();
            return holder;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

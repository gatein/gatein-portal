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

package org.exoplatform.portal.mop.importer;

import java.util.Arrays;

import org.exoplatform.portal.config.model.Page;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.page.PageState;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerAdapter;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.PageData;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class PageImporter {

    /** . */
    private final Page src;

    /** . */
    private final LayoutService layoutService;

    /** . */
    private final PageService pageService;

    /** . */
    private final ImportMode mode;

    public PageImporter(ImportMode importMode, Page page, LayoutService layoutService, PageService pageService) {
        this.mode = importMode;
        this.src = page;
        this.layoutService = layoutService;
        this.pageService = pageService;
    }

    public void perform() throws Exception {
        PageContext existingPage = pageService.loadPage(src.getPageKey());
        Page dst;

        //
        switch (mode) {
            case CONSERVE:
                dst = null;
                break;
            case INSERT:
                if (existingPage == null) {
                    dst = src;
                } else {
                    dst = null;
                }
                break;
            case MERGE:
            case OVERWRITE:
                dst = src;
                break;
            default:
                throw new AssertionError();
        }

        if (dst != null) {
            PageState dstState = new PageState(dst.getTitle(), dst.getDescription(), dst.isShowMaxWindow(), dst.getFactoryId(),
                    dst.getAccessPermissions() != null ? Arrays.asList(dst.getAccessPermissions()) : null,
                    dst.getEditPermissions() != null ? Arrays.asList(dst.getEditPermissions()) : null);

            //
            PageContext page = new PageContext(src.getPageKey(), dstState);

            // First save page as an object
            pageService.savePage(page);

            // We cheat a bit with this cast
            // but well it's easier to do this way
            NodeContext<ComponentData, ElementState> context = (NodeContext<ComponentData, ElementState>) layoutService.loadLayout(ElementState.model(), page.getLayoutId(), null);

            //
            PageData data = src.build();

            //
            ContainerData container = new ContainerData(
                    page.getLayoutId(),
                    data.getStorageName(),
                    data.getId(),
                    data.getName(),
                    data.getIcon(),
                    data.getTemplate(),
                    data.getFactoryId(),
                    data.getTitle(),
                    data.getDescription(),
                    data.getWidth(),
                    data.getHeight(),
                    data.getAccessPermissions(),
                    data.getChildren());

            // Remove children
            while (true) {
                NodeContext<ComponentData, ElementState> first = context.getFirst();
                if (first != null) {
                    first.removeNode();
                } else {
                    break;
                }
            }

            // Then save page layout
            layoutService.saveLayout(new ContainerAdapter(container), container, context, null);
        }
    }
}

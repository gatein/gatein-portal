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

import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.exoplatform.portal.pom.data.ComponentData;
import org.gatein.portal.mop.hierarchy.HierarchyError;
import org.gatein.portal.mop.hierarchy.HierarchyException;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author Julien Viet
 */
public class TestPageImporter extends AbstractMopServiceTest {

    public void testInsert() throws Exception {
        createSite(SiteType.PORTAL, "bilto");
        sync(true);
        Page page = new Page();
        page.setOwnerId("bilto");
        page.setOwnerType("portal");
        page.setName("the_page");
        Container container = new Container();
        container.setStorageName("foo");
        page.getChildren().add(container);
        PageImporter importer = new PageImporter(ImportMode.INSERT, page, context.getLayoutService(), getPageService(), getSecurityService());
        importer.perform();
        PageContext pageContext = getPageService().loadPage(PageKey.parse("portal::bilto::the_page"));
        NodeContext<ComponentData, ElementState> ret = (NodeContext<ComponentData, ElementState>)context.getLayoutService().loadLayout(ElementState.model(), pageContext.getLayoutId(), null);
        assertEquals(1, ret.getSize());
        assertNotNull(ret.get("foo"));
    }

    public void testDuplicateChildName() throws Exception {
        createSite(SiteType.PORTAL, "duplicate");
        sync(true);
        Page page = new Page();
        page.setOwnerId("duplicate");
        page.setOwnerType("portal");
        page.setName("the_page");
        page.getChildren().add(new Container().setStorageName("foo"));
        page.getChildren().add(new Container().setStorageName("foo"));
        PageImporter importer = new PageImporter(ImportMode.INSERT, page, context.getLayoutService(), getPageService(), getSecurityService());
        try {
            importer.perform();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.ADD_CONCURRENTLY_ADDED_NODE, e.getError());
        }
    }

    public void testOverwrite() throws Exception {
        createSite(SiteType.PORTAL, "overwrite");
        Page page = new Page();
        page.setOwnerId("overwrite");
        page.setOwnerType("portal");
        page.setName("the_page");
        page.getChildren().add(new Container().setStorageName("foo"));
        page.getChildren().add(new Container().setStorageName("bar"));
        PageImporter importer = new PageImporter(ImportMode.OVERWRITE, page, context.getLayoutService(), context.getPageService(), getSecurityService());
        importer.perform();
        sync(true);
        importer = new PageImporter(ImportMode.OVERWRITE, page, context.getLayoutService(), context.getPageService(), context.getSecurityService());
        importer.perform();
    }
}

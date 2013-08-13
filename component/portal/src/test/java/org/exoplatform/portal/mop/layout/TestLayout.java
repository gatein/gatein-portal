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

package org.exoplatform.portal.mop.layout;

import java.util.Arrays;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.mop.core.util.Tools;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.layout.Element;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestLayout extends AbstractMopServiceTest {

    /** . */
    private LayoutService layoutService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        this.layoutService = context.getLayoutService();
    }

    public void testSite() {
        SiteData site = createSite(SiteType.PORTAL, "test_layout_site");
        createElements(site, Element.portlet("app/foo").title("foo"), Element.portlet("app/bar").title("bar"));
        testAll(site.layoutId);
    }

    public void testPage() {
        PageData page = createPage(createSite(SiteType.PORTAL, "test_layout_page"), "page", new PageState.Builder().build());
        createElements(page, Element.portlet("app/foo").title("foo"), Element.portlet("app/bar").title("bar"));
        testAll(page.layoutId);
    }

    /**
     * One single test now that do multiple things : shorcut
     */
    private void testAll(String layoutId) {

        //
        NodeContext<Element, ElementState> context = layoutService.loadLayout(Element.MODEL, layoutId, null);
        assertEquals(2, context.getNodeSize());
        Element foo = context.getNode(0);
        Element bar = context.getNode(1);
        assertEquals("foo", ((ElementState.Window) foo.getState()).properties.get(ElementState.Window.TITLE));
        assertEquals("bar", ((ElementState.Window) bar.getState()).properties.get(ElementState.Window.TITLE));

        // Add a new portlet in the background
        createElements(layoutId, Element.portlet("app/juu").title("juu"));

        // Save with no changes but we get the concurrent change
        layoutService.saveLayout(context, null);
        assertEquals(3, context.getNodeSize());
        foo = context.getNode(0);
        bar = context.getNode(1);
        Element juu = context.getNode(2);
        assertEquals("foo", ((ElementState.Window) foo.getState()).properties.get(ElementState.Window.TITLE));
        assertEquals("bar", ((ElementState.Window) bar.getState()).properties.get(ElementState.Window.TITLE));
        assertEquals("juu", ((ElementState.Window) juu.getState()).properties.get(ElementState.Window.TITLE));

        // Test move
        context.add(1, context.get(2));
        layoutService.saveLayout(context, null);
        assertEquals(3, context.getNodeSize());
        foo = context.getNode(0);
        juu = context.getNode(1);
        bar = context.getNode(2);
        assertEquals("foo", ((ElementState.Window) foo.getState()).properties.get(ElementState.Window.TITLE));
        assertEquals("juu", ((ElementState.Window) juu.getState()).properties.get(ElementState.Window.TITLE));
        assertEquals("bar", ((ElementState.Window) bar.getState()).properties.get(ElementState.Window.TITLE));

        //
        NodeData<ElementState> root = getElement(layoutId, layoutId);
        assertEquals(Arrays.asList(context.get(0).getId(), context.get(1).getId(), context.get(2).getId()), Tools.list(root.iterator()));

        // Test update
        context.getNode(0).setState(((ElementState.WindowBuilder)context.getNode(0).getState().builder()).description("foodesc").build());
        layoutService.saveLayout(context, null);

        //
        assertEquals("foodesc", ((ElementState.Window)getElement(layoutId, context.getNode(0).getId()).getState()).properties.get(ElementState.Window.DESCRIPTION));

        // Test destroy
        assertTrue(context.get(0).removeNode());
        layoutService.saveLayout(context, null);

        //
        root = getElement(layoutId, layoutId);
        assertEquals(Arrays.asList(context.get(0).getId(), context.get(1).getId()), Tools.list(root.iterator()));
    }
}

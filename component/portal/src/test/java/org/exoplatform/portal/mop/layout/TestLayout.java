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

    /**
     * One single test now that do multiple things : shorcut
     */
    public void testAll() {
        SiteData site = createSite(SiteType.PORTAL, "test_layout");
        createElements(site, Element.portlet("app/foo").title("foo"), Element.portlet("app/bar").title("bar"));

        //
        NodeContext<Element, ElementState> context = layoutService.loadLayout(Element.MODEL, site.layoutId, null);
        assertEquals(2, context.getNodeSize());
        Element foo = context.getNode(0);
        Element bar = context.getNode(1);
        assertEquals("foo", ((ElementState.Window) foo.getState()).title);
        assertEquals("bar", ((ElementState.Window) bar.getState()).title);

        // Add a new portlet in the background
        createElements(site, Element.portlet("app/juu").title("juu"));

        // Save with no changes but we get the concurrent change
        layoutService.saveLayout(context, null);
        assertEquals(3, context.getNodeSize());
        foo = context.getNode(0);
        bar = context.getNode(1);
        Element juu = context.getNode(2);
        assertEquals("foo", ((ElementState.Window) foo.getState()).title);
        assertEquals("bar", ((ElementState.Window) bar.getState()).title);
        assertEquals("juu", ((ElementState.Window) juu.getState()).title);

        // Test move
        context.add(1, context.get(2));
        layoutService.saveLayout(context, null);
        assertEquals(3, context.getNodeSize());
        foo = context.getNode(0);
        juu = context.getNode(1);
        bar = context.getNode(2);
        assertEquals("foo", ((ElementState.Window) foo.getState()).title);
        assertEquals("juu", ((ElementState.Window) juu.getState()).title);
        assertEquals("bar", ((ElementState.Window) bar.getState()).title);

        //
        NodeData<ElementState> root = getElement(site);
        assertEquals(Arrays.asList(context.get(0).getId(), context.get(1).getId(), context.get(2).getId()), Tools.list(root.iterator()));

        // Test update
        context.getNode(0).setState(((ElementState.WindowBuilder)context.getNode(0).getState().builder()).description("foodesc").build());
        layoutService.saveLayout(context, null);

        //
        assertEquals("foodesc", ((ElementState.Window)getElement(context.getNode(0).getId()).getState()).description);

        // Test destroy
        assertTrue(context.get(0).removeNode());
        layoutService.saveLayout(context, null);

        //
        root = getElement(site);
        assertEquals(Arrays.asList(context.get(0).getId(), context.get(1).getId()), Tools.list(root.iterator()));
    }
}

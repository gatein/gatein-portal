/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.api.navigation;

import static org.gatein.api.navigation.ApiNodeTest.assertIterator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.gatein.api.AbstractApiTest;
import org.gatein.api.common.Filter;
import org.gatein.api.navigation.Visibility.Status;
import org.gatein.api.page.PageId;
import org.gatein.api.security.User;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ApiFilteredNodeTest extends AbstractApiTest {
    private Filter<Node> filter;
    private FilteredNode filtered;
    private Node root;
    private Navigation navigation;

    @Test
    public void addChild() {
        filtered.addChild("child5");

        assertIterator(filtered.iterator(), "child0", "child2", "child4", "child5");
        assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4", "child5");
    }

    @Test
    public void addChild_IndexFirst() {
        filtered.addChild(0, "child5");

        assertIterator(filtered.iterator(), "child5", "child0", "child2", "child4");
        assertIterator(root.iterator(), "child5", "child0", "child1", "child2", "child3", "child4");
    }

    @Test
    public void addChild_IndexLast() {
        filtered.addChild(3, "child5");

        assertIterator(filtered.iterator(), "child0", "child2", "child4", "child5");
        assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4", "child5");
    }

    @Test
    public void addChild_IndexMiddle() {
        filtered.addChild(1, "child5");

        assertIterator(filtered.iterator(), "child0", "child5", "child2", "child4");
        assertIterator(root.iterator(), "child0", "child1", "child5", "child2", "child3", "child4");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addChild_IndexOutOfBounds() {
        filtered.addChild(4, "child5");
    }

    @Before
    public void before() throws Exception {
        super.before();

        createSite(defaultSiteId);

        navigation = portal.getNavigation(defaultSiteId);

        root = navigation.getRootNode(Nodes.visitAll());
        root.addChild("child0");
        root.addChild("child1");
        root.addChild("child2");
        root.addChild("child3");
        root.addChild("child4");

        filter = new Filter<Node>() {
            @Override
            public boolean accept(Node object) {
                return !(object.getName().equals("child1") || object.getName().equals("child3"));
            }
        };

        filtered = root.filter().show(filter);
    }

    @Test
    public void getChild() {
        assertNotNull(filtered.getChild("child0"));
        assertTrue(filtered.getChild("child0") instanceof ApiFilteredNode);
        assertNull(filtered.getChild("child1"));
    }

    @Test
    public void getChild_Index() {
        assertEquals("child0", filtered.getChild(0).getName());
        assertTrue(filtered.getChild(0) instanceof ApiFilteredNode);
        assertEquals("child2", filtered.getChild(1).getName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getChild_IndexOutOfBounds() {
        filtered.getChild(4);
    }

    @Test
    public void getChildCount() {
        assertEquals(3, filtered.getChildCount());
    }

    @Test
    public void getDescendant() {
        root.getChild("child0").addChild("child0-0");
        root.getChild("child1").addChild("child1-0");

        assertNotNull(filtered.getNode(NodePath.path("child0", "child0-0")));
        assertTrue(filtered.getNode(NodePath.path("child0", "child0-0")) instanceof ApiFilteredNode);
        assertNull(filtered.getNode(NodePath.path("child1")));
        assertNull(filtered.getNode(NodePath.path("child1", "child0-0")));
    }

    @Test
    public void hasChild() {
        assertTrue(filtered.hasChild("child0"));
        assertFalse(filtered.hasChild("child1"));
        assertFalse(filtered.hasChild("nosuch"));
    }

    @Test
    public void indexOf() {
        assertEquals(0, filtered.indexOf("child0"));
        assertEquals(1, filtered.indexOf("child2"));
    }

    @Test
    public void iterator() {
        assertIterator(filtered.iterator(), "child0", "child2", "child4");
        assertTrue(filtered.iterator().next() instanceof ApiFilteredNode);
    }

    @Test
    public void moveTo_First() {
        filtered.getChild("child4").moveTo(0);

        assertIterator(filtered.iterator(), "child4", "child0", "child2");
        assertIterator(root.iterator(), "child4", "child0", "child1", "child2", "child3");
    }

    @Test
    public void moveTo_Last() {
        filtered.getChild("child4").moveTo(2);

        assertIterator(filtered.iterator(), "child0", "child2", "child4");
        assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4");
    }

    @Test
    public void moveTo_Middle() {
        filtered.getChild("child4").moveTo(1);

        assertIterator(filtered.iterator(), "child0", "child4", "child2");
        assertIterator(root.iterator(), "child0", "child1", "child4", "child2", "child3");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void moveTo_OutOfBounds() {
        filtered.getChild("child4").moveTo(3);
    }

    @Test
    public void moveTo_Parent() {
        Node parent0 = root.addChild("parent0");
        parent0.addChild("child0");
        parent0.addChild("child1");
        parent0.addChild("child2");

        filtered.addChild("parent1").addChild("child5").moveTo(1, parent0);

        assertIterator(filtered.getChild("parent0").iterator(), "child0", "child5", "child2");
        assertIterator(root.getChild("parent0").iterator(), "child0", "child1", "child5", "child2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void show_NullFilter() {
        root.filter().show(null);
    }

    @Test
    public void showAll() {
        assertIterator(filtered.iterator(), "child0", "child2", "child4");
        filtered = filtered.showAll();
        assertIterator(filtered.iterator(), "child0", "child1", "child2", "child3", "child4");
    }

    @Test
    public void showDefault() {
        createPage(defaultSiteId, "page1");
        setPermission(new PageId(defaultSiteId, "page1"), "Everyone", "*:/platform/administrators");

        root.getChild("child1").setVisibility(false);
        root.getChild("child2").setPageId(new PageId(defaultSiteId, "page1"));

        assertIterator(root.filter().showDefault().iterator(), "child0", "child3", "child4");
    }

    @Test
    public void showVisible() {
        root.getChild("child1").setVisibility(false);
        root.getChild("child2").setVisibility(new Visibility(Status.SYSTEM));
        root.getChild("child3").setVisibility(PublicationDate.endingOn(new Date(System.currentTimeMillis() - 1000)));

        filtered = root.filter().showVisible();

        assertIterator(filtered.iterator(), "child0", "child4");
    }

    @Test
    public void showHasAccess() {
        createPage(defaultSiteId, "page1");
        setPermission(new PageId(defaultSiteId, "page1"), "Everyone", "*:/platform/administrators");

        createPage(defaultSiteId, "page2");
        setPermission(new PageId(defaultSiteId, "page2"), "Everyone", "Everyone");

        root.getChild("child1").setPageId(new PageId(defaultSiteId, "page1"));
        root.getChild("child2").setPageId(new PageId(defaultSiteId, "page2"));

        assertIterator(root.filter().showHasAccess(new User("a")).iterator(), "child0", "child2", "child3", "child4");
        assertIterator(root.filter().showHasAccess(new User("root")).iterator(), "child0", "child1", "child2", "child3",
                "child4");
    }

    @Test
    public void showHasEdit() {
        createPage(defaultSiteId, "page1");
        setPermission(new PageId(defaultSiteId, "page1"), "*:/platform/administrators", "Everyone");

        createPage(defaultSiteId, "page2");
        setPermission(new PageId(defaultSiteId, "page2"), "Everyone", "Everyone");

        root.getChild("child1").setPageId(new PageId(defaultSiteId, "page1"));
        root.getChild("child2").setPageId(new PageId(defaultSiteId, "page2"));

        assertIterator(root.filter().showHasEdit(new User("a")).iterator(), "child0", "child2", "child3", "child4");
        assertIterator(root.filter().showHasEdit(new User("root")).iterator(), "child0", "child1", "child2", "child3",
                "child4");
    }

    @Test
    public void sourceNotChanged() {
        root.filter().show(filter);

        assertEquals(5, root.getChildCount());
        assertNotNull(root.getChild("child1"));
    }
}

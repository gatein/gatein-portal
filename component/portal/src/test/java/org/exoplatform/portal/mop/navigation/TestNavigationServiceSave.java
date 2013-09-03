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

package org.exoplatform.portal.mop.navigation;

import java.util.Iterator;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.hierarchy.HierarchyError;
import org.gatein.portal.mop.hierarchy.HierarchyException;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NavigationNode;
import org.gatein.portal.mop.site.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.portal.mop.hierarchy.NodeChange;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationServiceSave extends AbstractMopServiceTest {

    public void testNonExistingSite() throws Exception {
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("non_existing")));
    }

    public void testSaveNavigation() throws Exception {
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav);

        //
        createSite(SiteType.PORTAL, "save_navigation");

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav);

        //
        nav = new NavigationContext(SiteKey.portal("save_navigation"), new NavigationState(5));
        assertNull(nav.getData());
        assertNotNull(nav.getState());
        getNavigationService().saveNavigation(nav);
        assertNotNull(nav.getData());
        assertNull(nav.getState(true));

        //
        nav.setState(new NavigationState(5));
        getNavigationService().saveNavigation(nav);
        nav = getNavigationService().loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav.getState(true));
        assertNotNull(nav.getData().state);
        assertEquals(5, nav.getData().state.getPriority().intValue());

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("save_navigation"));
        assertNotNull(nav);
        assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
        NavigationState state = nav.getData().state;
        Integer p = state.getPriority();
        assertEquals(5, (int) p);
        assertNotNull(nav.getData().rootId);
    }

    public void testDestroyNavigation() throws Exception {
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);

        //
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "destroy_navigation"));
        createNodeChild(node, "a");

        //
        sync(true);
        getNavigationService().clearCache();

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNotNull(nav);

        //
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();

        //
        assertTrue(getNavigationService().destroyNavigation(nav));
        assertNull(nav.getState());
        assertNull(nav.getData());

        //
        try {
            getNavigationService().destroyNavigation(nav);
        } catch (IllegalArgumentException e) {
        }

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);
    }

    public void testAddChild() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "add_child"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("add_child"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertEquals(0, root1.getNodeCount());

        // Test what happens when null is added
        try {
            root1.addChild((String) null);
            fail();
        } catch (NullPointerException ignore) {
        }

        // Test what happens when an illegal index is added
        try {
            root1.addChild(-1, "foo");
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            root1.addChild(1, "foo");
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }

        //
        NavigationNode foo = root1.addChild("foo");
        assertNull(foo.getId());
        assertEquals("foo", foo.getName());
        assertSame(foo, root1.getChild("foo"));
        assertEquals(1, root1.getNodeCount());
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        NavigationNode foo2 = root2.getChild("foo");
        assertNotNull(foo2);
        assertEquals(1, root2.getNodeCount());
        assertEquals("foo", foo2.getName());

        //
        root1.assertEquals(root2);
    }

    public void testRemoveChild() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "remove_child"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("remove_child"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();

        //
        try {
            root1.removeChild(null);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            root1.removeChild("bar");
            fail();
        } catch (IllegalArgumentException e) {
        }

        //
        NavigationNode foo1 = root1.getChild("foo");
        assertNotNull(foo1.getId());
        assertEquals("foo", foo1.getName());
        assertSame(foo1, root1.getChild("foo"));

        //
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.getChild("foo"));
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        NavigationNode foo2 = root2.getChild("foo");
        assertNull(foo2);

        //
        root1.assertEquals(root2);
    }

    public void testRemoveTransientChild() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "remove_transient_child"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("remove_transient_child"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        NavigationNode foo1 = root1.addChild("foo");
        assertNull(foo1.getId());
        assertEquals("foo", foo1.getName());
        assertSame(foo1, root1.getChild("foo"));

        //
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.getChild("foo"));
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        NavigationNode foo2 = root2.getChild("foo");
        assertNull(foo2);

        //
        root1.assertEquals(root2);
    }

    public void testRename() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "rename"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("rename"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.GRANDCHILDREN, null).getNode();
        try {
            root1.setName("something");
            fail();
        } catch (IllegalStateException e) {
        }

        //
        NavigationNode a1 = root1.getChild("a");
        assertEquals(0, a1.getContext().getIndex());
        try {
            a1.setName(null);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            a1.setName("b");
            fail();
        } catch (IllegalArgumentException e) {
        }

        //
        a1.setName("c");
        assertEquals("c", a1.getName());
        assertEquals(0, a1.getContext().getIndex());
        getNavigationService().saveNode(a1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("rename"));
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        NavigationNode a2 = root2.getChild("c");
        assertNotNull(a2);
        // assertEquals(0, a2.getContext().getIndex());

        // Does not pass randomly because of JCR bugs
        // root1.assertEquals(root2);
    }

    public void testReorderChild() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "reorder_child"));
        createNodeChild(node, "foo", "bar", "juu");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("reorder_child"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        Iterator<NavigationNode> i = root1.getChildren().iterator();
        NavigationNode foo1 = i.next();
        assertEquals("foo", foo1.getName());
        NavigationNode bar1 = i.next();
        assertEquals("bar", bar1.getName());
        NavigationNode juu1 = i.next();
        assertEquals("juu", juu1.getName());
        assertFalse(i.hasNext());

        // Test what happens when null is added
        try {
            root1.addChild(1, (NavigationNode) null);
            fail();
        } catch (NullPointerException expected) {
        }

        // Test what happens when an illegal index is added
        try {
            root1.addChild(-1, juu1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            root1.addChild(4, juu1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        //
        root1.addChild(1, juu1);
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        i = root2.getChildren().iterator();
        NavigationNode foo2 = i.next();
        assertEquals("foo", foo2.getName());
        NavigationNode juu2 = i.next();
        assertEquals("juu", juu2.getName());
        NavigationNode bar2 = i.next();
        assertEquals("bar", bar2.getName());
        assertFalse(i.hasNext());

        //
        root1.assertEquals(root2);

        //
        root2.addChild(0, bar2);

        //
        getNavigationService().saveNode(root2.getContext(), null);

        //
        root2.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root3 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        i = root3.getChildren().iterator();
        NavigationNode bar3 = i.next();
        assertEquals("bar", bar3.getName());
        NavigationNode foo3 = i.next();
        assertEquals("foo", foo3.getName());
        NavigationNode juu3 = i.next();
        assertEquals("juu", juu3.getName());
        assertFalse(i.hasNext());

        //
        root2.assertEquals(root3);
    }

    public void _testReorderChild2() {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "reorder_child_2"));
        createNodeChild(node, "foo", "bar", "juu");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("reorder_child_2"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertEquals("bar", root.getChild(1).getName());
        assertTrue(root.removeChild("bar"));
        getNavigationService().saveNode(root.getContext(), null);

        //
        sync(true);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("daa");
        NavigationNode tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());
        getNavigationService().saveNode(root.getContext(), null);

        //
        sync(true);

        //
//        root = new NavigationServiceImpl(new MopPersistenceFactory(mgr)).loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        for (NavigationNode child : root.getChildren()) {
            System.out.println("child : " + child.getId());
        }
        tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());

        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        for (NavigationNode child : root.getChildren()) {
            System.out.println("child : " + child.getId());
        }
        tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());
    }

    public void testMoveChild() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "move_child"));
        createNodeChild(createNodeChild(node, "foo", "bar")[0], "juu");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("move_child"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode foo1 = root1.getChild("foo");
        NavigationNode bar1 = root1.getChild("bar");
        NavigationNode juu1 = foo1.getChild("juu");
        bar1.addChild(juu1);
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode foo2 = root2.getChild("foo");
        NavigationNode juu2 = foo2.getChild("juu");
        assertNull(juu2);
        NavigationNode bar2 = root2.getChild("bar");
        juu2 = bar2.getChild("juu");
        assertNotNull(juu2);

        //
        root1.assertEquals(root2);
    }

    public void testMoveAfter1() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_move_after_1"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_move_after_1"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode a = root.getChild("a");
        NavigationNode b = root.getChild("b");
        NavigationNode c = root.getChild("c");
        root.addChild(1, a);
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));
        getNavigationService().saveNode(root.getContext(), null);

        //
        root.assertConsistent();
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));

        //
        sync(true);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        a = root.getChild("a");
        b = root.getChild("b");
        c = root.getChild("c");
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));
    }

    public void testMoveAfter2() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_move_after_2"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_move_after_2"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode a = root.getChild("a");
        NavigationNode b = root.getChild("b");
        NavigationNode c = root.getChild("c");
        root.addChild(2, a);
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));
        getNavigationService().saveNode(root.getContext(), null);

        //
        root.assertConsistent();
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));

        //
        sync(true);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        a = root.getChild("a");
        b = root.getChild("b");
        c = root.getChild("c");
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));
    }

    public void testRenameNode() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "rename_node"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("rename_node"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode foo1 = root1.getChild("foo");
        foo1.setName("foo");
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("rename_node"));
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();

        //
        root1.assertEquals(root2);

        //
        NavigationNode foo2 = root2.getChild("foo");
        foo2.setName("bar");
        assertEquals("bar", foo2.getName());
        assertSame(foo2, root2.getChild("bar"));
        getNavigationService().saveNode(root2.getContext(), null);
        assertEquals("bar", foo2.getName());
        assertSame(foo2, root2.getChild("bar"));

        //
        root2.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root3 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode bar3 = root3.getChild("bar");
        assertNotNull(bar3);
        assertSame(bar3, root3.getChild("bar"));

        //
        root2.assertEquals(root3);

        //
        root3.addChild("foo");
        try {
            bar3.setName("foo");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testSaveChildren() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_children"));
        createNodeChild(node, "1", "2", "3", "4", "5");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_children"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        root1.removeChild("5");
        root1.removeChild("2");
        root1.addChild(0, root1.getChild("3"));
        root1.addChild(1, root1.addChild("."));
        getNavigationService().saveNode(root1.getContext(), null);
        Iterator<NavigationNode> i = root1.getChildren().iterator();
        assertEquals("3", i.next().getName());
        assertEquals(".", i.next().getName());
        assertEquals("1", i.next().getName());
        assertEquals("4", i.next().getName());
        assertFalse(i.hasNext());

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        i = root2.getChildren().iterator();
        assertEquals("3", i.next().getName());
        assertEquals(".", i.next().getName());
        assertEquals("1", i.next().getName());
        assertEquals("4", i.next().getName());
        assertFalse(i.hasNext());

        //
        root1.assertEquals(root2);
    }

    public void testSaveRecursive() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_recursive"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_recursive"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode foo1 = root1.getChild("foo");
        NavigationNode bar1 = foo1.addChild("bar");
        bar1.addChild("juu");
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode foo2 = root2.getChild("foo");
        NavigationNode bar2 = foo2.getChild("bar");
        assertNotNull(bar2.getId());
        NavigationNode juu2 = bar2.getChild("juu");
        assertNotNull(juu2.getId());

        //
        root1.assertEquals(root2);
    }

    public void testSaveState() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_state"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_state"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.SINGLE, null).getNode();
        NodeState state = root1.getState();
        assertNull(state.getLabel());
        assertEquals(-1, state.getStartPublicationTime());
        assertEquals(-1, state.getEndPublicationTime());
        long now = System.currentTimeMillis();
        root1.setState(new NodeState.Builder().endPublicationTime(now).label("bar").build());
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        state = root2.getState();
        assertEquals("bar", state.getLabel());
        assertEquals(-1, state.getStartPublicationTime());
        assertEquals(now, state.getEndPublicationTime());
        assertEquals(Visibility.DISPLAYED, state.getVisibility());

        //
        root1.assertEquals(root2);
    }

    public void _testSaveStateOverwrite() {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_state_overwrite"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_state_overwrite"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("foo");
        getNavigationService().saveNode(root.getContext(), null);

        //
        sync(true);

        //
        root.addChild("bar");
        getNavigationService().saveNode(root.getContext(), null);

        //
        sync(true);

        //
        nav = getNavigationService().loadNavigation(SiteKey.portal("save_state_overwrite"));
        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertEquals(2, root.getChildren().size());
    }

    public void testRecreateNode() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "recreate_node"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("recreate_node"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        String fooId = root1.getChild("foo").getId();
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.addChild("foo").getId());
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertNotNull(root2.getChild("foo").getId());
        assertNotSame(fooId, root2.getChild("foo").getId());

        //
        root1.assertEquals(root2);
    }

    public void testMoveToAdded() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "move_to_added"));
        createNodeChild(createNodeChild(node, "a")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("move_to_added"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        NavigationNode a1 = root1.getChild("a");
        NavigationNode b1 = a1.getChild("b");
        NavigationNode c1 = root1.addChild("c");
        c1.addChild(b1);
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        navigation = getNavigationService().loadNavigation(SiteKey.portal("move_to_added"));
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        NavigationNode a2 = root2.getChild("a");
        assertNotNull(a2);
        NavigationNode c2 = root2.getChild("c");
        assertNotNull(c2);
        NavigationNode b2 = c2.getChild("b");
        assertNotNull(b2);

        //
        root1.assertEquals(root2);
    }

    public void testMoveFromRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "moved_from_removed"));
        createNodeChild(createNodeChild(node, "a", "b")[0], "c");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("moved_from_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        NavigationNode a1 = root1.getChild("a");
        NavigationNode b1 = root1.getChild("b");
        NavigationNode c1 = a1.getChild("c");
        b1.addChild(c1);
        root1.removeChild("a");
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        navigation = getNavigationService().loadNavigation(SiteKey.portal("moved_from_removed"));
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertNull(root2.getChild("a"));
        NavigationNode b2 = root2.getChild("b");
        assertNotNull(b2);
        NavigationNode c2 = b2.getChild("c");
        assertNotNull(c2);

        //
        root1.assertEquals(root2);
    }

    public void testRemoveAdded() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "remove_added"));

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("remove_added"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        root.addChild("foo");
        root.removeChild("foo");
        getNavigationService().saveNode(root.getContext(), null);

        //
        root.assertConsistent();

        //
        sync(true);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertEquals(0, root.getChildren().size());
    }

    public void testTransitiveRemoveTransient() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "transitive_remove_transient"));

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("transitive_remove_transient"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        root.addChild("foo").addChild("bar");
        root.removeChild("foo");
        getNavigationService().saveNode(root.getContext(), null);

        //
        root.assertConsistent();

        //
        sync(true);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertEquals(0, root.getChildren().size());
    }

    public void testRenameCreatedNode() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_rename_created"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("save_rename_created"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.ALL, null).getNode();
        NavigationNode temp = root.addChild("temp");
        temp.setName("bar");
        Iterator<NodeChange<NavigationNode, NodeState>> changes = root.save(getNavigationService());
        assertFalse(changes.hasNext());
    }

    public void testConcurrentAddToRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "add_to_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("add_to_removed"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root.getChild("a").addChild("b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
        }
    }

    public void testConcurrentMerge() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_merge"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("save_merge"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.CHILDREN, null).getNode();

        //
        sync();

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.CHILDREN, null).getNode();
        root2.addChild(1, root2.addChild("2"));
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        getNavigationService().saveNode(root1.getContext(), null);
        root1.assertConsistent();

        //
        root1.addChild(1, root1.addChild("1"));
        getNavigationService().saveNode(root1.getContext(), null);
        root1.assertConsistent();
    }

    public void testConcurrentRemoveRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "remove_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("remove_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.removeChild("a");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        getNavigationService().saveNode(root1.getContext(), null);

        //
        root1.assertEquals(root2);
    }

    public void testConcurrentMoveRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "move_removed"));
        createNodeChild(createNodeChild(node, "a")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("move_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(root1.getChild("a").getChild("b"));

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.getChild("a").removeChild("b");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE, e.getError());
        }
    }

    public void testConcurrentMoveToRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "move_to_removed"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("move_to_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("b").addChild(root1.getChild("a"));

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("b");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.MOVE_CONCURRENTLY_REMOVED_DST_NODE, e.getError());
        }
    }

    public void testConcurrentMoveMoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "move_moved"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("move_moved"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("b").addChild(root1.getChild("a"));

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.getChild("c").addChild(root2.getChild("a"));
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE, e.getError());
        }
    }

    public void testConcurrentAddDuplicate() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_add_duplicate"));

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_add_duplicate"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        root1.addChild("a");
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.ADD_CONCURRENTLY_ADDED_NODE, e.getError());
        }
    }

    public void testConcurrentAddAfterRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_add_after_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_add_after_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(1, "b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
        }
    }

    public void testConcurrentMoveAfterRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_move_after_removed"));
        createNodeChild(createNodeChild(node, "a", "c")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_move_after_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(2, root1.getChild("a").getChild("b"));

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("c");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
        }
    }

    public void testConcurrentMoveFromRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_move_from_removed"));
        createNodeChild(createNodeChild(node, "a", "c")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_move_from_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("c").addChild(root1.getChild("a").getChild("b"));

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE, e.getError());
        }
    }

    public void testConcurrentRenameRemoved() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_rename_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_rename_removed"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setName("b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.RENAME_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testConcurrentDuplicateRename() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_duplicate_rename"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_duplicate_rename"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setName("b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("b");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.RENAME_CONCURRENTLY_DUPLICATE_NAME, e.getError());
        }
    }

    public void testSavePhantomNode() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "concurrent_save"));

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("concurrent_save"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild("a");
        getNavigationService().saveNode(root1.getContext(), null);

        //
        sync(true);

        // Reload the root node and modify it
        root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setState(root1.getState().builder().label("foo").build());

        //
        sync(true);

        // Edit navigation in another browser
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        // Now click Save button in the first browser
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertEquals(HierarchyError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testConcurrentRemovalDoesNotPreventSave() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "removal_does_not_prevent_save"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("removal_does_not_prevent_save"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        getNavigationService().saveNode(root1.getContext(), null);
    }

    public void testConcurrentRename() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_concurrent_rename"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("save_concurrent_rename"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        NavigationNode a = root1.getChild("a");
        a.setName("b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        NavigationNode a2 = root2.getChild("a");
        a2.setName("c");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        Iterator<NodeChange<NavigationNode, NodeState>> changes = root1.save(getNavigationService());
        assertFalse(changes.hasNext());
    }

    public void testRemovedNavigation() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_removed_navigation"));

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("save_removed_navigation"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        getNavigationService().destroyNavigation(navigation);

        //
        sync(true);

        //
        try {
            getNavigationService().saveNode(root.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertSame(HierarchyError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testPendingChangesBypassCache() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "pending_changes_bypass_cache"));

        //
        sync(true);

        //
        NavigationContext nav = getNavigationService().loadNavigation(SiteKey.portal("pending_changes_bypass_cache"));
        NavigationNode root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("foo");
        getNavigationService().saveNode(root.getContext(), null);

        //
        root = getNavigationService().loadNode(NavigationNode.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertNotNull(root.getChild("foo"));
    }

    public void testAtomic() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_atomic"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("save_atomic"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").addChild("c");
        root1.getChild("b").addChild("d");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("b");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        assertSessionNotModified();

        //
        try {
            getNavigationService().saveNode(root1.getContext(), null);
            fail();
        } catch (HierarchyException e) {
            assertSame(HierarchyError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
        }

        //
        assertSessionNotModified();
    }

    public void testRebase() throws Exception {
        NodeData node = createNavigation(createSite(SiteType.PORTAL, "save_rebase"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = getNavigationService().loadNavigation(SiteKey.portal("save_rebase"));
        NavigationNode root1 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        NavigationNode a = root1.getChild("a");
        NavigationNode b = root1.addChild("b");

        //
        NavigationNode root2 = getNavigationService().loadNode(NavigationNode.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("c");
        getNavigationService().saveNode(root2.getContext(), null);

        //
        Iterator<NodeChange<NavigationNode, NodeState>> changes = root1.save(getNavigationService());
        NodeChange.Added<NavigationNode, NodeState> added = (NodeChange.Added<NavigationNode, NodeState>) changes.next();
        NavigationNode c = added.getTarget();
        assertEquals("c", c.getName());
        assertFalse(changes.hasNext());
        assertSame(a, root1.getChild(0));
        assertSame(b, root1.getChild(1));
        assertSame(c, root1.getChild(2));
    }
}

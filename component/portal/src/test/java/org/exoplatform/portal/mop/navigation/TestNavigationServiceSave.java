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

import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.site.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.portal.mop.hierarchy.Node;
import org.gatein.portal.mop.hierarchy.NodeChange;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationError;
import org.gatein.portal.mop.navigation.NavigationServiceException;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNavigationServiceSave extends AbstractTestNavigationService {

    public void testNonExistingSite() throws Exception {
        assertNull(service.loadNavigation(SiteKey.portal("non_existing")));
    }

    public void testSaveNavigation() throws Exception {
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav);

        //
        createSite(SiteType.PORTAL, "save_navigation");

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav);

        //
        nav = new NavigationContext(SiteKey.portal("save_navigation"), new NavigationState(5));
        assertNull(nav.getData());
        assertNotNull(nav.getState());
        service.saveNavigation(nav);
        assertNotNull(nav.getData());
        assertNull(nav.getState(true));

        //
        nav.setState(new NavigationState(5));
        service.saveNavigation(nav);
        nav = service.loadNavigation(SiteKey.portal("save_navigation"));
        assertNull(nav.getState(true));
        assertNotNull(nav.getData().state);
        assertEquals(5, nav.getData().state.getPriority().intValue());

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("save_navigation"));
        assertNotNull(nav);
        assertEquals(SiteKey.portal("save_navigation"), nav.getKey());
        NavigationState state = nav.getData().state;
        Integer p = state.getPriority();
        assertEquals(5, (int) p);
        assertNotNull(nav.getData().rootId);
    }

    public void testDestroyNavigation() throws Exception {
        NavigationContext nav = service.loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);

        //
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "destroy_navigation"));
        createNodeChild(node, "a");

        //
        sync(true);
        service.clearCache();

        //
        nav = service.loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNotNull(nav);

        //
        Node root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();

        //
        assertTrue(service.destroyNavigation(nav));
        assertNull(nav.getState());
        assertNull(nav.getData());

        //
        try {
            service.destroyNavigation(nav);
        } catch (IllegalArgumentException e) {
        }

        //
        nav = service.loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("destroy_navigation"));
        assertNull(nav);
    }

    public void testAddChild() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "add_child"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("add_child"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
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
        Node foo = root1.addChild("foo");
        assertNull(foo.getId());
        assertEquals("foo", foo.getName());
        assertSame(foo, root1.getChild("foo"));
        assertEquals(1, root1.getNodeCount());
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Node foo2 = root2.getChild("foo");
        assertNotNull(foo2);
        assertEquals(1, root2.getNodeCount());
        assertEquals("foo", foo2.getName());

        //
        root1.assertEquals(root2);
    }

    public void testRemoveChild() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "remove_child"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("remove_child"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();

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
        Node foo1 = root1.getChild("foo");
        assertNotNull(foo1.getId());
        assertEquals("foo", foo1.getName());
        assertSame(foo1, root1.getChild("foo"));

        //
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.getChild("foo"));
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Node foo2 = root2.getChild("foo");
        assertNull(foo2);

        //
        root1.assertEquals(root2);
    }

    public void testRemoveTransientChild() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "remove_transient_child"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("remove_transient_child"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Node foo1 = root1.addChild("foo");
        assertNull(foo1.getId());
        assertEquals("foo", foo1.getName());
        assertSame(foo1, root1.getChild("foo"));

        //
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.getChild("foo"));
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Node foo2 = root2.getChild("foo");
        assertNull(foo2);

        //
        root1.assertEquals(root2);
    }

    public void testRename() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "rename"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("rename"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.GRANDCHILDREN, null).getNode();
        try {
            root1.setName("something");
            fail();
        } catch (IllegalStateException e) {
        }

        //
        Node a1 = root1.getChild("a");
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
        service.saveNode(a1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("rename"));
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Node a2 = root2.getChild("c");
        assertNotNull(a2);
        // assertEquals(0, a2.getContext().getIndex());

        // Does not pass randomly because of JCR bugs
        // root1.assertEquals(root2);
    }

    public void testReorderChild() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "reorder_child"));
        createNodeChild(node, "foo", "bar", "juu");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("reorder_child"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        Iterator<Node> i = root1.getChildren().iterator();
        Node foo1 = i.next();
        assertEquals("foo", foo1.getName());
        Node bar1 = i.next();
        assertEquals("bar", bar1.getName());
        Node juu1 = i.next();
        assertEquals("juu", juu1.getName());
        assertFalse(i.hasNext());

        // Test what happens when null is added
        try {
            root1.addChild(1, (Node) null);
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
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        i = root2.getChildren().iterator();
        Node foo2 = i.next();
        assertEquals("foo", foo2.getName());
        Node juu2 = i.next();
        assertEquals("juu", juu2.getName());
        Node bar2 = i.next();
        assertEquals("bar", bar2.getName());
        assertFalse(i.hasNext());

        //
        root1.assertEquals(root2);

        //
        root2.addChild(0, bar2);

        //
        service.saveNode(root2.getContext(), null);

        //
        root2.assertConsistent();

        //
        sync(true);

        //
        Node root3 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        i = root3.getChildren().iterator();
        Node bar3 = i.next();
        assertEquals("bar", bar3.getName());
        Node foo3 = i.next();
        assertEquals("foo", foo3.getName());
        Node juu3 = i.next();
        assertEquals("juu", juu3.getName());
        assertFalse(i.hasNext());

        //
        root2.assertEquals(root3);
    }

    public void _testReorderChild2() {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "reorder_child_2"));
        createNodeChild(node, "foo", "bar", "juu");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("reorder_child_2"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertEquals("bar", root.getChild(1).getName());
        assertTrue(root.removeChild("bar"));
        service.saveNode(root.getContext(), null);

        //
        sync(true);

        //
        root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("daa");
        Node tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());
        service.saveNode(root.getContext(), null);

        //
        sync(true);

        //
//        root = new NavigationServiceImpl(new MopPersistenceFactory(mgr)).loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        for (Node child : root.getChildren()) {
            System.out.println("child : " + child.getId());
        }
        tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());

        root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        for (Node child : root.getChildren()) {
            System.out.println("child : " + child.getId());
        }
        tab3 = root.getChild(2);
        assertEquals("daa", tab3.getName());
    }

    public void testMoveChild() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "move_child"));
        createNodeChild(createNodeChild(node, "foo", "bar")[0], "juu");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("move_child"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node foo1 = root1.getChild("foo");
        Node bar1 = root1.getChild("bar");
        Node juu1 = foo1.getChild("juu");
        bar1.addChild(juu1);
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node foo2 = root2.getChild("foo");
        Node juu2 = foo2.getChild("juu");
        assertNull(juu2);
        Node bar2 = root2.getChild("bar");
        juu2 = bar2.getChild("juu");
        assertNotNull(juu2);

        //
        root1.assertEquals(root2);
    }

    public void testMoveAfter1() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_move_after_1"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_move_after_1"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node a = root.getChild("a");
        Node b = root.getChild("b");
        Node c = root.getChild("c");
        root.addChild(1, a);
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));
        service.saveNode(root.getContext(), null);

        //
        root.assertConsistent();
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));

        //
        sync(true);

        //
        root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        a = root.getChild("a");
        b = root.getChild("b");
        c = root.getChild("c");
        assertSame(a, root.getChild(0));
        assertSame(b, root.getChild(1));
        assertSame(c, root.getChild(2));
    }

    public void testMoveAfter2() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_move_after_2"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_move_after_2"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node a = root.getChild("a");
        Node b = root.getChild("b");
        Node c = root.getChild("c");
        root.addChild(2, a);
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));
        service.saveNode(root.getContext(), null);

        //
        root.assertConsistent();
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));

        //
        sync(true);

        //
        root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        a = root.getChild("a");
        b = root.getChild("b");
        c = root.getChild("c");
        assertSame(b, root.getChild(0));
        assertSame(a, root.getChild(1));
        assertSame(c, root.getChild(2));
    }

    public void testRenameNode() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "rename_node"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("rename_node"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node foo1 = root1.getChild("foo");
        foo1.setName("foo");
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("rename_node"));
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();

        //
        root1.assertEquals(root2);

        //
        Node foo2 = root2.getChild("foo");
        foo2.setName("bar");
        assertEquals("bar", foo2.getName());
        assertSame(foo2, root2.getChild("bar"));
        service.saveNode(root2.getContext(), null);
        assertEquals("bar", foo2.getName());
        assertSame(foo2, root2.getChild("bar"));

        //
        root2.assertConsistent();

        //
        sync(true);

        //
        Node root3 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node bar3 = root3.getChild("bar");
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
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_children"));
        createNodeChild(node, "1", "2", "3", "4", "5");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_children"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        root1.removeChild("5");
        root1.removeChild("2");
        root1.addChild(0, root1.getChild("3"));
        root1.addChild(1, root1.addChild("."));
        service.saveNode(root1.getContext(), null);
        Iterator<Node> i = root1.getChildren().iterator();
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
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
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
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_recursive"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_recursive"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node foo1 = root1.getChild("foo");
        Node bar1 = foo1.addChild("bar");
        bar1.addChild("juu");
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node foo2 = root2.getChild("foo");
        Node bar2 = foo2.getChild("bar");
        assertNotNull(bar2.getId());
        Node juu2 = bar2.getChild("juu");
        assertNotNull(juu2.getId());

        //
        root1.assertEquals(root2);
    }

    public void testSaveState() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_state"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_state"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.SINGLE, null).getNode();
        NodeState state = root1.getState();
        assertNull(state.getLabel());
        assertEquals(-1, state.getStartPublicationTime());
        assertEquals(-1, state.getEndPublicationTime());
        long now = System.currentTimeMillis();
        root1.setState(new NodeState.Builder().endPublicationTime(now).label("bar").build());
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        state = root2.getState();
        assertEquals("bar", state.getLabel());
        assertEquals(-1, state.getStartPublicationTime());
        assertEquals(now, state.getEndPublicationTime());
        assertEquals(Visibility.DISPLAYED, state.getVisibility());

        //
        root1.assertEquals(root2);
    }

    public void _testSaveStateOverwrite() {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_state_overwrite"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("foo");
        service.saveNode(root.getContext(), null);

        //
        sync(true);

        //
        root.addChild("bar");
        service.saveNode(root.getContext(), null);

        //
        sync(true);

        //
        nav = service.loadNavigation(SiteKey.portal("save_state_overwrite"));
        root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertEquals(2, root.getChildren().size());
    }

    public void testRecreateNode() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "recreate_node"));
        createNodeChild(node, "foo");

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("recreate_node"));
        Node root1 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        String fooId = root1.getChild("foo").getId();
        assertTrue(root1.removeChild("foo"));
        assertNull(root1.addChild("foo").getId());
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        Node root2 = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertNotNull(root2.getChild("foo").getId());
        assertNotSame(fooId, root2.getChild("foo").getId());

        //
        root1.assertEquals(root2);
    }

    public void testMoveToAdded() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "move_to_added"));
        createNodeChild(createNodeChild(node, "a")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        Node a1 = root1.getChild("a");
        Node b1 = a1.getChild("b");
        Node c1 = root1.addChild("c");
        c1.addChild(b1);
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        navigation = service.loadNavigation(SiteKey.portal("move_to_added"));
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        Node a2 = root2.getChild("a");
        assertNotNull(a2);
        Node c2 = root2.getChild("c");
        assertNotNull(c2);
        Node b2 = c2.getChild("b");
        assertNotNull(b2);

        //
        root1.assertEquals(root2);
    }

    public void testMoveFromRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "moved_from_removed"));
        createNodeChild(createNodeChild(node, "a", "b")[0], "c");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        Node a1 = root1.getChild("a");
        Node b1 = root1.getChild("b");
        Node c1 = a1.getChild("c");
        b1.addChild(c1);
        root1.removeChild("a");
        service.saveNode(root1.getContext(), null);

        //
        root1.assertConsistent();

        //
        sync(true);

        //
        navigation = service.loadNavigation(SiteKey.portal("moved_from_removed"));
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertNull(root2.getChild("a"));
        Node b2 = root2.getChild("b");
        assertNotNull(b2);
        Node c2 = b2.getChild("c");
        assertNotNull(c2);

        //
        root1.assertEquals(root2);
    }

    public void testRemoveAdded() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "remove_added"));

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("remove_added"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        root.addChild("foo");
        root.removeChild("foo");
        service.saveNode(root.getContext(), null);

        //
        root.assertConsistent();

        //
        sync(true);

        //
        root = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertEquals(0, root.getChildren().size());
    }

    public void testTransitiveRemoveTransient() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "transitive_remove_transient"));

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("transitive_remove_transient"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        root.addChild("foo").addChild("bar");
        root.removeChild("foo");
        service.saveNode(root.getContext(), null);

        //
        root.assertConsistent();

        //
        sync(true);

        //
        root = service.loadNode(Node.MODEL, navigation, Scope.GRANDCHILDREN, null).getNode();
        assertEquals(0, root.getChildren().size());
    }

    public void testRenameCreatedNode() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_rename_created"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("save_rename_created"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.ALL, null).getNode();
        Node temp = root.addChild("temp");
        temp.setName("bar");
        Iterator<NodeChange<Node, NodeState>> changes = root.save(service);
        assertFalse(changes.hasNext());
    }

    public void testConcurrentAddToRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "add_to_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("add_to_removed"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.getChild("a").addChild("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
        }
    }

    public void testConcurrentMerge() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_merge"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_merge"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();

        //
        sync();

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
        root2.addChild(1, root2.addChild("2"));
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        service.saveNode(root1.getContext(), null);
        root1.assertConsistent();

        //
        root1.addChild(1, root1.addChild("1"));
        service.saveNode(root1.getContext(), null);
        root1.assertConsistent();
    }

    public void testConcurrentRemoveRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "remove_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("remove_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.removeChild("a");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        service.saveNode(root1.getContext(), null);

        //
        root1.assertEquals(root2);
    }

    public void testConcurrentMoveRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "move_removed"));
        createNodeChild(createNodeChild(node, "a")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(root1.getChild("a").getChild("b"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.getChild("a").removeChild("b");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_MOVED_NODE, e.getError());
        }
    }

    public void testConcurrentMoveToRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "move_to_removed"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_to_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("b").addChild(root1.getChild("a"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("b");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_DST_NODE, e.getError());
        }
    }

    public void testConcurrentMoveMoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "move_moved"));
        createNodeChild(node, "a", "b", "c");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("move_moved"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("b").addChild(root1.getChild("a"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.getChild("c").addChild(root2.getChild("a"));
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_CHANGED_SRC_NODE, e.getError());
        }
    }

    public void testConcurrentAddDuplicate() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_add_duplicate"));

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_add_duplicate"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        root1.addChild("a");
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.ADD_CONCURRENTLY_ADDED_NODE, e.getError());
        }
    }

    public void testConcurrentAddAfterRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_add_after_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_add_after_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(1, "b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
        }
    }

    public void testConcurrentMoveAfterRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_move_after_removed"));
        createNodeChild(createNodeChild(node, "a", "c")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_move_after_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild(2, root1.getChild("a").getChild("b"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("c");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_PREVIOUS_NODE, e.getError());
        }
    }

    public void testConcurrentMoveFromRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_move_from_removed"));
        createNodeChild(createNodeChild(node, "a", "c")[0], "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_move_from_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("c").addChild(root1.getChild("a").getChild("b"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_REMOVED_SRC_NODE, e.getError());
        }
    }

    public void testConcurrentRenameRemoved() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_rename_removed"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_rename_removed"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setName("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.RENAME_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testConcurrentDuplicateRename() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_duplicate_rename"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_duplicate_rename"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setName("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("b");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME, e.getError());
        }
    }

    public void testSavePhantomNode() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "concurrent_save"));

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("concurrent_save"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.addChild("a");
        service.saveNode(root1.getContext(), null);

        //
        sync(true);

        // Reload the root node and modify it
        root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").setState(root1.getState().builder().label("foo").build());

        //
        sync(true);

        // Edit navigation in another browser
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        // Now click Save button in the first browser
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testConcurrentRemovalDoesNotPreventSave() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "removal_does_not_prevent_save"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("removal_does_not_prevent_save"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        service.saveNode(root1.getContext(), null);
    }

    public void testConcurrentRename() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_concurrent_rename"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_concurrent_rename"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node a = root1.getChild("a");
        a.setName("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node a2 = root2.getChild("a");
        a2.setName("c");
        service.saveNode(root2.getContext(), null);

        //
        Iterator<NodeChange<Node, NodeState>> changes = root1.save(service);
        assertFalse(changes.hasNext());
    }

    public void testRemovedNavigation() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_removed_navigation"));

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_removed_navigation"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        service.destroyNavigation(navigation);

        //
        sync(true);

        //
        try {
            service.saveNode(root.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertSame(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testPendingChangesBypassCache() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "pending_changes_bypass_cache"));

        //
        sync(true);

        //
        NavigationContext nav = service.loadNavigation(SiteKey.portal("pending_changes_bypass_cache"));
        Node root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        root.addChild("foo");
        service.saveNode(root.getContext(), null);

        //
        root = service.loadNode(Node.MODEL, nav, Scope.CHILDREN, null).getNode();
        assertNotNull(root.getChild("foo"));
    }

    public void testAtomic() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_atomic"));
        createNodeChild(node, "a", "b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_atomic"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root1.getChild("a").addChild("c");
        root1.getChild("b").addChild("d");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("b");
        service.saveNode(root2.getContext(), null);

        //
        sync(true);

        //
        assertSessionNotModified();

        //
        try {
            service.saveNode(root1.getContext(), null);
            fail();
        } catch (NavigationServiceException e) {
            assertSame(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
        }

        //
        assertSessionNotModified();
    }

    public void testRebase() throws Exception {
        NodeData node = createNavigatation(createSite(SiteType.PORTAL, "save_rebase"));
        createNodeChild(node, "a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("save_rebase"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node a = root1.getChild("a");
        Node b = root1.addChild("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("c");
        service.saveNode(root2.getContext(), null);

        //
        Iterator<NodeChange<Node, NodeState>> changes = root1.save(service);
        NodeChange.Added<Node, NodeState> added = (NodeChange.Added<Node, NodeState>) changes.next();
        Node c = added.getTarget();
        assertEquals("c", c.getName());
        assertFalse(changes.hasNext());
        assertSame(a, root1.getChild(0));
        assertSame(b, root1.getChild(1));
        assertSame(c, root1.getChild(2));
    }
}

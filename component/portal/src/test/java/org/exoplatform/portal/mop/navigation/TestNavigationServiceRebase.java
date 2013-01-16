/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.hierarchy.Node;
import org.gatein.portal.mop.hierarchy.NodeChange;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationError;
import org.gatein.portal.mop.navigation.NavigationServiceException;
import org.gatein.portal.mop.navigation.NodeState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationServiceRebase extends AbstractTestNavigationService {

    public void testRebase1() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase1");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");
        def.addChild("d");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase1"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node a = root1.getChild("a");
        Node d = root1.getChild("d");
        Node b = root1.addChild(1, "b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node c2 = root2.addChild(1, "c");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        service.rebaseNode(root1.getContext(), null, null);
        assertEquals(4, root1.getNodeCount());
        assertSame(a, root1.getChild(0));
        assertSame(b, root1.getChild(1));
        Node c1 = root1.getChild(2);
        assertEquals("c", c1.getName());
        assertEquals(c2.getId(), c1.getId());
        assertSame(d, root1.getChild(3));

    }

    public void testRebase2() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase2");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");
        def.addChild("b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase2"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        Node a = root1.getChild("a");
        Node b = root1.getChild("b");
        Node c = a.addChild("c");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.getChild("b").addChild(root2.getChild("a"));
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        service.rebaseNode(root1.getContext(), null, null);
        assertEquals(null, root1.getChild("a"));
        assertSame(b, root1.getChild("b"));
        assertEquals(root1, b.getParent());
        assertSame(a, b.getChild("a"));
        assertEquals(b, a.getParent());
        assertSame(c, a.getChild("c"));
        assertEquals(a, c.getParent());
    }

    public void testRebase3() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase3");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");
        def.addChild("b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase3"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.getChild("a").addChild("foo");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.removeChild("a");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        try {
            service.rebaseNode(root.getContext(), null, null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
        }

    }

    /**
     * This test is quite important as it ensures that the copy tree during the rebase operation is rebuild from the initial
     * state. Indeed the move / destroy operations would fail otherwise as the move operation would not find its source.
     */
    public void testRebase4() {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase4");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a").addChild("b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase4"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.addChild(root.getChild("a").getChild("b"));
        root.removeChild("a");

        //
        service.rebaseNode(root.getContext(), null, null);
    }

    public void testRebaseAddDuplicate() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_add_duplicate");
        Navigation def = portal.getRootNavigation().addChild("default");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_add_duplicate"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.addChild("a");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("a");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        try {
            service.rebaseNode(root.getContext(), null, null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.ADD_CONCURRENTLY_ADDED_NODE, e.getError());
        }

    }

    public void testRebaseMoveDuplicate() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_move_duplicate");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a").addChild("b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_move_duplicate"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.addChild(root.getChild("a").getChild("b"));

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("b");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        try {
            service.rebaseNode(root.getContext(), null, null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.MOVE_CONCURRENTLY_DUPLICATE_NAME, e.getError());
        }

    }

    public void testRebaseRenameDuplicate() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_rename_duplicate");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_rename_duplicate"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root.getChild("a").setName("b");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("b");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        try {
            service.rebaseNode(root.getContext(), null, null);
            fail();
        } catch (NavigationServiceException e) {
            assertEquals(NavigationError.RENAME_CONCURRENTLY_DUPLICATE_NAME, e.getError());
        }
    }

    public void testFederation() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_federation");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a").addChild("b");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_federation"));
        Node root1 = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
        final Node a = root1.getChild("a");
        final Node c = root1.addChild("c");

        //
        Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        root2.addChild("d").addChild("e");
        service.saveNode(root2.getContext(), null);
        sync(true);

        //
        Iterator<NodeChange<Node, NodeState>> changes = a.rebase(service, Scope.CHILDREN);
        Iterator<Node> children = root1.getChildren().iterator();
        assertSame(a, children.next());
        assertSame(c, children.next());
        Node d = children.next();
        assertEquals("d", d.getName());
        assertFalse(children.hasNext());
        assertFalse(d.getContext().isExpanded());
        children = a.getChildren().iterator();
        Node b = children.next();
        assertEquals("b", b.getName());
        assertFalse(children.hasNext());
        assertFalse(b.getContext().isExpanded());
        NodeChange.Added<Node, NodeState> added1 = (NodeChange.Added<Node, NodeState>) changes.next();
        assertSame(b, added1.getTarget());
        assertSame(null, added1.getPrevious());
        assertSame(a, added1.getParent());
        NodeChange.Added<Node, NodeState> added2 = (NodeChange.Added<Node, NodeState>) changes.next();
        assertSame(d, added2.getTarget());
        assertSame(c, added2.getPrevious());
        assertSame(root1, added2.getParent());
        assertFalse(changes.hasNext());
    }

    public void testTransientParent() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_transient_parent");
        portal.getRootNavigation().addChild("default");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_transient_parent"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.CHILDREN, null).getNode();
        Node a = root.addChild("a");
        Node b = root.addChild("b"); // It is only failed if we add more than one transient node

        //
        service.rebaseNode(a.getContext(), Scope.CHILDREN, null);
    }

    public void testRemovedNavigation() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_removed_navigation");
        portal.getRootNavigation().addChild("default");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_removed_navigation"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();
        service.destroyNavigation(navigation);

        //
        sync(true);

        //
        try {
            service.rebaseNode(root.getContext(), null, null);
        } catch (NavigationServiceException e) {
            assertSame(NavigationError.UPDATE_CONCURRENTLY_REMOVED_NODE, e.getError());
        }
    }

    public void testStateRebase() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_state");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_state"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();

        NodeState state = new NodeState.Builder().label("foo").build();
        root.getChild("a").setState(state);
        assertSame(state, root.getChild("a").getState());

        //
        sync(true);

        //
        Iterator<NodeChange<Node, NodeState>> changes = root.rebase(service, null);
        assertFalse(changes.hasNext());
        assertSame(state, root.getChild("a").getState());
    }

    public void testNameRebase() throws Exception {
        MOPService mop = mgr.getPOMService();
        Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase_name");
        Navigation def = portal.getRootNavigation().addChild("default");
        def.addChild("a");

        //
        sync(true);

        //
        NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase_name"));
        Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).getNode();

        Node a = root.getChild("a");
        a.setName("b");
        assertSame("b", a.getName());

        //
        sync(true);

        //
        Iterator<NodeChange<Node, NodeState>> changes = root.rebase(service, null);
        assertFalse(changes.hasNext());
        assertSame("b", a.getName());
    }
}

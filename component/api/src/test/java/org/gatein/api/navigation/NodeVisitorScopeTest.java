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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope.Visitor;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.portal.mop.page.PageKey;
import org.junit.Test;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodeVisitorScopeTest {
    @Test
    public void nodePathScope() {
        NodeState nodeState = new NodeState("label", "icon", -1, -1, Visibility.DISPLAYED, new PageKey(new SiteKey(
                SiteType.PORTAL, "site"), "page"), false);
        NodeVisitorMock mock = new NodeVisitorMock();
        Visitor visitor = new NodeVisitorScope(mock).get();

        mock.instrument(true);
        assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(0, "id", "default", nodeState));
        assertNull(mock.name);
        assertNull(mock.details);

        mock.instrument(true);
        assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(1, "id", "1", nodeState));
        assertNotNull(mock.name);
        assertEquals(1, mock.depth);
        assertEquals(NodePath.path("1"), mock.details.getNodePath());

        mock.instrument(false);
        assertEquals(VisitMode.NO_CHILDREN, visitor.enter(2, "id", "1-1", nodeState));
        assertNotNull(mock.name);
        assertEquals(2, mock.depth);
        assertEquals(NodePath.path("1", "1-1"), mock.details.getNodePath());

        visitor.leave(2, "id", "1-1", nodeState);
        visitor.leave(1, "id", "1", nodeState);
        visitor.leave(0, "id", "default", null);

        mock.instrument(false);
    }

    class NodeVisitorMock implements NodeVisitor {
        private int depth;

        private boolean visit;

        private String name;

        private NodeDetails details;

        @Override
        public boolean visit(int depth, String name, NodeDetails details) {
            this.depth = depth;
            this.name = name;
            this.details = details;
            return visit;
        }

        public void instrument(boolean visit) {
            this.visit = visit;
            name = null;
            depth = -1;
        }
    }
}

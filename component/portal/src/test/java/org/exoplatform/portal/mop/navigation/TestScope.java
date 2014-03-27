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

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.page.PageKey;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestScope extends AbstractGateInTest {

    public void testBranchShape() {
        NodeState nodeState = new NodeState("", null, -1, -1, Visibility.DISPLAYED,
                PageKey.parse("portal::classic::home"), false, AttributesState.EMPTY);

        //
        Scope scope0 = GenericScope.branchShape(new String[0], Scope.CHILDREN);
        Scope.Visitor visitor0 = scope0.get();
        assertEquals(VisitMode.ALL_CHILDREN, visitor0.enter(0, "0", "", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor0.enter(1, "1", "a", nodeState));

        //
        scope0 = GenericScope.branchShape(new String[0], Scope.SINGLE);
        visitor0 = scope0.get();
        assertEquals(VisitMode.NO_CHILDREN, visitor0.enter(0, "0", "", nodeState));

        //
        Scope scope1 = GenericScope.branchShape(new String[] { "a" }, Scope.CHILDREN);
        Scope.Visitor visitor1 = scope1.get();
        assertEquals(VisitMode.ALL_CHILDREN, visitor1.enter(0, "0", "", nodeState));
        assertEquals(VisitMode.ALL_CHILDREN, visitor1.enter(1, "1", "a", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor1.enter(1, "2", "b", nodeState));

        //
        scope1 = GenericScope.branchShape(new String[] { "a" }, Scope.SINGLE);
        visitor1 = scope1.get();
        assertEquals(VisitMode.ALL_CHILDREN, visitor1.enter(0, "0", "", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor1.enter(1, "1", "a", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor1.enter(1, "2", "b", nodeState));

        //
        Scope scope2 = GenericScope.branchShape(new String[] { "a", "c" }, Scope.SINGLE);
        Scope.Visitor visitor2 = scope2.get();
        assertEquals(VisitMode.ALL_CHILDREN, visitor2.enter(0, "0", "", nodeState));
        assertEquals(VisitMode.ALL_CHILDREN, visitor2.enter(1, "1", "a", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor2.enter(1, "2", "b", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor2.enter(2, "3", "c", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor2.enter(2, "4", "d", nodeState));

        //
        scope2 = GenericScope.branchShape(new String[] { "a" }, GenericScope.branchShape(new String[] { "b" }, Scope.CHILDREN));
        visitor2 = scope2.get();
        assertEquals(VisitMode.ALL_CHILDREN, visitor2.enter(0, "0", "", nodeState));
        assertEquals(VisitMode.ALL_CHILDREN, visitor2.enter(1, "1", "a", nodeState));
        assertEquals(VisitMode.ALL_CHILDREN, visitor2.enter(2, "2", "b", nodeState));
        assertEquals(VisitMode.NO_CHILDREN, visitor2.enter(2, "3", "c", nodeState));
    }
}

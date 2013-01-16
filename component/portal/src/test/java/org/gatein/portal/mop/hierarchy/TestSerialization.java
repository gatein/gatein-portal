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

package org.gatein.portal.mop.hierarchy;

import java.util.Arrays;

import org.exoplatform.component.test.AbstractGateInTest;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.common.io.IOTools;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestSerialization extends AbstractGateInTest {

    public void testNodeDataSerialization() throws Exception {
        NodeData<NodeState> data = new NodeData<NodeState>("parent", "this", "foo", new NodeState.Builder()
                .pageRef(SiteKey.portal("classic").page("bar")).icon("bar").build(), new String[] { "child" });
        NodeData<NodeState> copy = IOTools.clone(data);
        assertEquals(copy.parentId, data.parentId);
        assertEquals(copy.id, data.id);
        assertEquals(copy.name, data.name);
        assertEquals(Arrays.asList(copy.children), Arrays.asList(data.children));
        assertEquals(copy.state, data.state);
    }
}

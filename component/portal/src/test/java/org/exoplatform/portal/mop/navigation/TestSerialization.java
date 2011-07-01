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

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.mop.SiteKey;
import org.gatein.common.io.IOTools;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestSerialization extends AbstractGateInTest
{

   public void testNodeStateSerialization() throws Exception
   {
      NodeState state = new NodeState.Builder().pageRef("foo").icon("bar").build();
      NodeState copy = IOTools.clone(state);
      assertEquals(state, copy);
   }

   public void testNodeDataSerialization() throws Exception
   {
      NodeData data = new NodeData("parent", "this", "foo", new NodeState.Builder().pageRef("bar").icon("bar").build(), new String[]{"child"});
      NodeData copy = IOTools.clone(data);
      assertEquals(copy.parentId, data.parentId);
      assertEquals(copy.id, data.id);
      assertEquals(copy.name, data.name);
      assertEquals(Arrays.asList(copy.children), Arrays.asList(data.children));
      assertEquals(copy.state, data.state);
   }

   public void testNavigationStateSerialization() throws Exception
   {
      NavigationState state = new NavigationState(5);
      NavigationState copy = IOTools.clone(state);
      assertEquals(state.getPriority(), copy.getPriority());
   }

   public void testNavigationDataSerialization() throws Exception
   {
      NavigationData data = new NavigationData(SiteKey.portal("classic"), new NavigationState(3), "root");
      NavigationData copy = IOTools.clone(data);
      assertEquals(data.rootId, copy.rootId);
      assertEquals(data.state.getPriority(), copy.state.getPriority());
      assertEquals(data.key, copy.key);
   }
}

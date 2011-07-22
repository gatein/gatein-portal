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

package org.exoplatform.portal.config;

import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.navigation.NodeContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestImportNavigationInsert extends AbstractImportNavigationTest
{

   @Override
   protected ImportMode getMode()
   {
      return ImportMode.INSERT;
   }

   @Override
   protected void assertState(NodeContext<?> root)
   {
      assertEquals(3, root.getNodeCount());
      NodeContext<?> foo = root.get("foo");
      assertNotNull(foo);
      assertEquals("foo_icon_1", foo.getState().getIcon());
      assertEquals(1, foo.getNodeCount());
      NodeContext<?> juu = foo.get("juu");
      assertNotNull(juu);
      assertEquals("juu_icon", juu.getState().getIcon());
      assertEquals(0, juu.getNodeCount());
      NodeContext<?> bar = root.get("bar");
      assertNotNull(bar);
      assertEquals("bar_icon", bar.getState().getIcon());
      assertEquals(0, bar.getNodeCount());
      NodeContext<?> daa = root.get("daa");
      assertNotNull(daa);
      assertEquals("daa_icon", daa.getState().getIcon());
      assertEquals(0, daa.getNodeCount());
   }
}

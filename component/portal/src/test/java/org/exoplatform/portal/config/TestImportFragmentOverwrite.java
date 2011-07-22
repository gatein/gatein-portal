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
public class TestImportFragmentOverwrite extends AbstractImportFragmentTest
{

   @Override
   protected ImportMode getMode()
   {
      return ImportMode.OVERWRITE;
   }

   @Override
   protected void assertState(NodeContext<?> root)
   {
      assertEquals(1, root.getNodeSize());
      NodeContext<?> foo = root.get("foo");
      assertNotNull(foo);
      assertEquals("foo_icon", foo.getState().getIcon());
      assertEquals(1, foo.getNodeSize());
      NodeContext<?> bar = foo.get("bar");
      assertNotNull(bar);
      assertEquals("bar_icon", bar.getState().getIcon());
      assertEquals(0, bar.getNodeSize());
   }
}

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

package org.exoplatform.portal.mop.user;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestParsePath extends TestCase
{

   public void testFoo()
   {
      assertPath("a", "a");
      assertPath("a/", "a");
      assertPath("a//", "a");
      assertPath("a/b", "a", "b");
      assertPath("a/b/", "a", "b");
      assertPath("a/b//", "a", "b");
      assertPath("a//b", "a", "", "b");
      assertPath("a//b/", "a", "", "b");
      assertPath("a//b//", "a", "", "b");
      assertPath("/", (String[])null);
      assertPath("//");
      assertPath("///");
      assertPath("/a", "a");
      assertPath("//a", "", "a");
      assertPath("/a/", "a");
      assertPath("/a//", "a");
      assertPath("/a////", "a");
   }

   private void assertPath(String path, String... expected)
   {
      // Previous behavior
      assertEquals(expected, legacy(path));
      assertEquals(expected, Utils.parsePath(path));
   }

   private void assertEquals(String[] expected, String[] actual)
   {
      if (actual == null)
      {
         assertNull(expected);
      }
      else
      {
         assertNotNull(expected);
         assertEquals(Arrays.asList(expected), Arrays.asList(actual));
      }
   }

   private String[] legacy(String path)
   {
      //  Remove any leading /
      if (path.length() > 0 && path.charAt(0) == '/')
      {
         path = path.substring(1);
      }

      // Find the first navigation available or return null
      if (path.length() == 0)
      {
         return null;
      }

      // Split into segments
      return path.split("/");
   }
}

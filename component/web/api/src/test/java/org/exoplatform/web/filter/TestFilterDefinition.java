/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.filter;

import org.exoplatform.component.test.AbstractGateInTest;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class TestFilterDefinition extends AbstractGateInTest
{

   public void testMatch()
   {
      FilterDefinition def = new FilterDefinition(null, null);
      try
      {
         def.getMapping();
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
      }
      def = new FilterDefinition(null, new ArrayList<String>());
      try
      {
         def.getMapping();
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
      }
      def = new FilterDefinition(null, Arrays.asList(".*\\.gif"));
      assertTrue(def.getMapping().match("/foo/foo.gif"));
      assertFalse(def.getMapping().match("/foo/foo.giff"));
      def = new FilterDefinition(null, Arrays.asList(".*\\.gif", ".*\\.giff"));
      assertTrue(def.getMapping().match("/foo/foo.gif"));
      assertTrue(def.getMapping().match("/foo/foo.giff"));
      assertFalse(def.getMapping().match("/foo/foo.giffo"));
   }
}

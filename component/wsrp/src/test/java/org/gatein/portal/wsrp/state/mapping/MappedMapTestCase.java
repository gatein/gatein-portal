/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.mapping;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MappedMapTestCase extends TestCase
{
   private MappedMap<String, String> mapped;

   public void testInitFrom()
   {
      Map<String, String> external = new HashMap<String, String>();
      external.put("bar", "barvalue");

      Map<String, Object> internal = new HashMap<String, Object>();
      internal.put("foo", "foovalue");
      internal.put("baz", "bazvalue");

      mapped.initFrom(external, internal);

      assertEquals("foovalue", internal.get("foo"));
      assertEquals("barvalue", internal.get("bar"));
      assertNull("baz isn't in the external map we initialize from so it shouldn't be present anymore after", internal.get("baz"));
   }

   public void testToExternalMap()
   {
      Map<String, Object> internal = new HashMap<String, Object>();
      internal.put("foo", "foovalue");
      internal.put("baz", "bazvalue");

      Map<String, String> external = mapped.toExternalMap(internal);

      assertNull(external.get("foo"));
      assertEquals("bazvalue", external.get("baz"));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      mapped = new MappedMap<String, String>(new MappedMap.Converter<String, String>()
      {

         public String fromInternal(String s)
         {
            return s;
         }

         public String toInternal(String s)
         {
            return s;
         }
      }, new MappedMap.Converter<Object, String>()
      {
         public String fromInternal(Object o)
         {
            return o.toString();
         }

         public Object toInternal(String s)
         {
            return s;
         }
      }, "foo");
   }
}

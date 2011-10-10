/*
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

package org.exoplatform.commons.cache.future;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GetTestCase extends TestCase
{
   public void testGet()
   {
      FutureMap<String, String, Callable<String>> futureCache = new FutureMap<String, String, Callable<String>>(new StringLoader());
      Assert.assertEquals("foo_value", futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            return "foo_value";
         }
      }, "foo"));
      Assert.assertEquals("foo_value", futureCache.data.get("foo"));
   }

   public void testNullValue()
   {
      FutureMap<String, String, Callable<String>> futureCache = new FutureMap<String, String, Callable<String>>(new StringLoader());
      Assert.assertEquals(null, futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            return null;
         }
      }, "foo"));
      Assert.assertFalse(futureCache.data.containsKey("foo"));
   }

   public void testThrowException()
   {
      FutureMap<String, String, Callable<String>> futureCache = new FutureMap<String, String, Callable<String>>(new StringLoader());
      Assert.assertEquals(null, futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            throw new Exception("DON'T FREAK OUT");
         }
      }, "foo"));
      Assert.assertFalse(futureCache.data.containsKey("foo"));
   }

   public void testReentrancy()
   {
      final FutureMap<String, String, Callable<String>> futureCache = new FutureMap<String, String, Callable<String>>(new StringLoader());
      String res = futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            try
            {
               futureCache.get(new Callable<String>()
               {
                  public String call() throws Exception
                  {
                     // Should not go there
                     throw new AssertionError();
                  }
               }, "foo");
               return "fail";
            }
            catch (IllegalStateException expected)
            {
               return "pass";
            }
         }
      }, "foo");
      assertEquals("pass", res);
   }
}

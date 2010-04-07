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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.gatein.common.util.Tools;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConcurrentGetWhenPutTestCase extends TestCase
{

   /** . */
   private AssertionFailedError failure;

   /** . */
   private List<String> events = Collections.synchronizedList(new LinkedList<String>());

   FutureCache<String, String, Callable<String>> futureCache = new FutureCache<String, String, Callable<String>>(new StringLoader()) {


      @Override
      protected String get(String key)
      {
         if (key == key1)
         {
            if (Thread.currentThread() != thread1)
            {
               failure = new AssertionFailedError();
            }
            events.add("get/key1");
         }
         else if (key == key2)
         {
            if (Thread.currentThread() != thread2)
            {
               failure = new AssertionFailedError();
            }
            events.add("get/key2");
         }
         else
         {
            failure = new AssertionFailedError();
         }
         return null;
      }

      @Override
      protected void put(String key, String value)
      {
         if (key == key1)
         {
            if (Thread.currentThread() == thread1)
            {
               events.add("begin_put/key1/" + value);

               //
               thread2.start();

               //
               while (thread2.getState() != Thread.State.WAITING)
               {
                  // Wait until thread 2 is blocked
               }

               //
               events.add("end_put/key1");
            }
            else
            {
               failure = new AssertionFailedError();
            }
         }
         else
         {
            failure = new AssertionFailedError();
         }
      }
   };

   /** . */
   private final String key1 = new String("foo");

   /** . */
   private final String key2 = new String("foo");

   Thread thread1 = new Thread()
   {
      @Override
      public void run()
      {
         String v = futureCache.get(new Callable<String>()
         {
            public String call() throws Exception
            {
               events.add("call/key1");
               return "foo_value_1";
            }
         }, key1);
         events.add("retrieved/key1/" + v);
      }
   };

   Thread thread2 = new Thread()
   {
      @Override
      public void run()
      {
         String v = futureCache.get(new Callable<String>()
         {
            public String call() throws Exception
            {
               failure = new AssertionFailedError();
               return "foo_value_2";
            }
         }, key2);
         events.add("retrieved/key2/" + v);
      }
   };

   public void testMain() throws Exception
   {
      thread1.start();

      //
      thread1.join();
      thread2.join();

      //
      if (failure != null)
      {
         throw failure;
      }

      //
      List<String> expectedEvents = Arrays.asList(
         "get/key1",
         "call/key1",
         "begin_put/key1/foo_value_1",
         "get/key2",
         "end_put/key1"
      );

      //
      assertEquals(expectedEvents, events.subList(0, expectedEvents.size()));

      //
      Set<String> expectedEndEvents = Tools.toSet("retrieved/key1/foo_value_1", "retrieved/key2/foo_value_1");
      assertEquals(expectedEndEvents, new HashSet<String>(events.subList(expectedEvents.size(), events.size())));
   }

}
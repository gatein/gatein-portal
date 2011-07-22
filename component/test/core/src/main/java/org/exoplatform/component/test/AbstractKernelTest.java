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

package org.exoplatform.component.test;

import junit.framework.TestSuite;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An abstract test that takes care of running the unit tests with the semantic described by the
 * {#link GateInTestClassLoader}.
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AbstractKernelTest extends AbstractGateInTest
{

   /** . */
   private static KernelBootstrap bootstrap;

   /** . */
   private static final Map<Class<?>, AtomicLong> counters = new HashMap<Class<?>, AtomicLong>();

   protected AbstractKernelTest()
   {
      super();
   }

   protected AbstractKernelTest(String name)
   {
      super(name);
   }

   public PortalContainer getContainer()
   {
      return bootstrap != null ? bootstrap.getContainer() : null;
   }

   protected void begin()
   {
      RequestLifeCycle.begin(getContainer());
   }

   protected void end()
   {
      RequestLifeCycle.end();
   }

   @Override
   protected void beforeRunBare() throws Exception
   {
      Class<?> key = getClass();

      //
      if (!counters.containsKey(key))
      {
         counters.put(key, new AtomicLong(new TestSuite(getClass()).testCount()));

         //
         bootstrap = new KernelBootstrap(Thread.currentThread().getContextClassLoader());

         // Configure ourselves
         bootstrap.addConfiguration(getClass());

         //
         bootstrap.boot();
      }

      //
//      List<Throwable> failures = new ArrayList<Throwable>();
   }

   @Override
   protected void afterRunBare()
   {
      Class<?> key = getClass();

      //
      if (counters.get(key).decrementAndGet() == 0)
      {
         bootstrap.dispose();

         //
         bootstrap = null;
      }

/*
      if (failures.size() > 0)
      {
         Throwable failure = failures.get(0);
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(failure);
         throw afe;
      }
*/
   }
}

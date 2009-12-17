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

import junit.framework.TestCase;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract test that takes care of running the unit tests with the semantic described by the
 * {#link GateInTestClassLoader}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractGateInTest extends TestCase
{

   /** . */
   private PortalContainer container;

   protected AbstractGateInTest()
   {
      super();
   }

   protected AbstractGateInTest(String name)
   {
      super(name);
   }

   public PortalContainer getContainer()
   {
      return container;
   }

   protected void begin()
   {
      RequestLifeCycle.begin(container);
   }

   protected void end()
   {
      RequestLifeCycle.end();
   }

   @Override
   public void runBare() throws Throwable
   {
      ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();

      //
      Set<String> rootConfigPaths = new HashSet<String>();
      rootConfigPaths.add("conf/root-configuration.xml");

      //
      Set<String> portalConfigPaths = new HashSet<String>();
      portalConfigPaths.add("conf/portal-configuration.xml");

      //
      EnumMap<ContainerScope, Set<String>> configs = new EnumMap<ContainerScope, Set<String>>(ContainerScope.class);
      configs.put(ContainerScope.ROOT, rootConfigPaths);
      configs.put(ContainerScope.PORTAL, portalConfigPaths);

      //
      ConfiguredBy cfBy = getClass().getAnnotation(ConfiguredBy.class);
      if (cfBy != null)
      {
         for (ConfigurationUnit src : cfBy.value())
         {
            configs.get(src.scope()).add(src.path());
         }
      }

      //
//      List<Throwable> failures = new ArrayList<Throwable>();

      //
      try
      {
         ClassLoader testClassLoader = new GateInTestClassLoader(realClassLoader, rootConfigPaths, portalConfigPaths);
         Thread.currentThread().setContextClassLoader(testClassLoader);

         // Boot the container
         container = PortalContainer.getInstance();

         // Execute test
         super.runBare();
      }
      finally
      {
         container = null;

         //
         Thread.currentThread().setContextClassLoader(realClassLoader);
      }

      //
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

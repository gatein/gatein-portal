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

import junit.framework.AssertionFailedError;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import java.io.File;
import java.io.FilenameFilter;
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
public class AbstractKernelTest extends AbstractGateInTest
{

   /** The system property for gatein tmp dir. */
   private static final String TMP_DIR = "gatein.test.tmp.dir";

   /** . */
   private PortalContainer container;

   /** . */
   private ClassLoader realClassLoader;

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
   protected void beforeRunBare() throws Exception
   {
      realClassLoader = Thread.currentThread().getContextClassLoader();

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

      // Take care of creating tmp directory for unit test
      if (System.getProperty(TMP_DIR) == null)
      {
         // Get base dir set by maven or die
         File targetDir = new File(new File(System.getProperty("basedir")), "target");
         if (!targetDir.exists())
         {
            throw new AssertionFailedError("Target dir for unit test does not exist");
         }
         if (!targetDir.isDirectory())
         {
            throw new AssertionFailedError("Target dir is not a directory");
         }
         if (!targetDir.canWrite())
         {
            throw new AssertionFailedError("Target dir is not writable");
         }

         //
         Set<String> fileNames = new HashSet<String>();
         for (File child : targetDir.listFiles(new FilenameFilter()
         {
            public boolean accept(File dir, String name)
            {
               return name.startsWith("gateintest-");
            }
         }))
         {
            fileNames.add(child.getName());
         }

         //
         String fileName;
         int count = 0;
         while (true)
         {
            fileName = "gateintest-" + count;
            if (!fileNames.contains(fileName)) {
               break;
            }
            count++;
         }

         //
         File tmp = new File(targetDir, fileName);
         if (!tmp.mkdirs())
         {
            throw new AssertionFailedError("Could not create directory " + tmp.getCanonicalPath());
         }

         //
         System.setProperty(TMP_DIR, tmp.getCanonicalPath());
      }

      //
      ClassLoader testClassLoader = new GateInTestClassLoader(realClassLoader, rootConfigPaths, portalConfigPaths);
      Thread.currentThread().setContextClassLoader(testClassLoader);

      // Boot the container
      container = PortalContainer.getInstance();

      //
//      List<Throwable> failures = new ArrayList<Throwable>();
   }

   @Override
   protected void afterRunBare()
   {
      container = null;

      //
      Thread.currentThread().setContextClassLoader(realClassLoader);

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

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

package org.exoplatform.component.test;

import junit.framework.TestCase;
import org.exoplatform.container.PortalContainer;

import java.io.File;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class KernelBootstrapTestCase extends TestCase
{

   public void testReboot()
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.boot();
      PortalContainer container1 = bootstrap.getContainer();
      bootstrap.dispose();
      bootstrap.boot();
      PortalContainer container2 = bootstrap.getContainer();
      assertNotSame(container1, container2);
   }

   public void testGetTmpDir()
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      assertEquals(bootstrap.getTargetDir(), bootstrap.getTmpDir().getParentFile());
   }

   public void testSetTmpDir()
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      try
      {
         bootstrap.setTmpDir(new File("a"));
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         bootstrap.setTmpDir(bootstrap.getTargetDir());
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         bootstrap.setTmpDir(new File(bootstrap.getTargetDir(), "a"));
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testTmpDirLifeCycle()
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      try
      {
         File tmp = bootstrap.getTmpDir();
         assertFalse(tmp.exists());
         tmp = bootstrap.getTmpDir();
         bootstrap.boot();
         assertTrue(tmp.exists());
      }
      finally
      {
         bootstrap.dispose();
      }
   }

   public void testUseExistingTmpDir()
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      File tmp;
      try
      {
         bootstrap.boot();
         tmp = bootstrap.getTmpDir();
      }
      finally
      {
         bootstrap.dispose();
      }
      try
      {
         bootstrap = new KernelBootstrap();
         bootstrap.setTmpDir(tmp);
         bootstrap.boot();
      }
      finally
      {
         bootstrap.dispose();
      }
   }
}

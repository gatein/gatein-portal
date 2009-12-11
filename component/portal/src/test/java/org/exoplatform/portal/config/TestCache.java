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
package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.test.BasicTestCase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestCache extends AbstractPortalTest
{

   /** . */
   DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public void setUp() throws Exception
   {
      super.setUp();
      if (storage_ != null)
         return;
      PortalContainer container = PortalContainer.getInstance();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
   }

   public void testDirtyWrite() throws Exception
   {
      // Read
      Page page = storage_.getPage("portal::test::test4");
      assertEquals(null, page.getTitle());

      // Update
      page.setTitle("foo");
      storage_.save(page);

      //
      final AtomicBoolean go = new AtomicBoolean(false);

      // Force a cache update with the entry that will be modified
      // when the main session is closed
     
      new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               mgr.openSession();
               storage_.getPage("portal::test::test4");
               session.close();
            }
            catch (Exception e)
            {
               throw new Error(e);
            }
            finally
            {
               go.set(true);
            }
         }
      }.start();

      //
      while (!go.get())
      {
         Thread.sleep(1);
      }

      // Reopen session with no modifications that use the cache
      session.close(true);
      mgr.openSession();

      //
      page = storage_.getPage("portal::test::test4");
      assertEquals("foo", page.getTitle());
   }
}

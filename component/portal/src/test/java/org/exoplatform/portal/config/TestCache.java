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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestCache extends AbstractConfigTest
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
      PortalContainer container = getContainer();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
   }

   public void testGetNullInvalidation() throws Exception
   {
      begin();
      session = mgr.openSession();
      assertNull(storage_.getPage("portal::test::nonexisting"));
      Page page = new Page();
      page.setPageId("portal::test::nonexisting");
      storage_.create(page);
      end(true);
      begin();
      session = mgr.openSession();
      assertNotNull(storage_.getPage("portal::test::nonexisting"));
      end();
   }

   public void _testDirtyWrite() throws Exception
   {
      begin();
      session = mgr.openSession();

      // Read
      Page page = storage_.getPage("portal::test::test4");
      assertEquals(null, page.getTitle());

      // Update and save
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
               begin();
               mgr.openSession();
               storage_.getPage("portal::test::test4");
               session.close();
               end();
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

      // Save the cache should be invalidated
      session.close();
      end(true);

      // Reopen session with no modifications that use the cache
      begin();
      mgr.openSession();

      //
      page = storage_.getPage("portal::test::test4");
      assertEquals("foo", page.getTitle());

      // Restore to orginal value
      page.setTitle(null);
      storage_.save(page);
      
      //
      end(true);
   }

   public void testGetPageFromRemovedPortal() throws Exception
   {
      // Create what we need for the test
      begin();
      session = mgr.openSession();
      PortalConfig portalConfig = new PortalConfig("portal", "testGetPageFromRemovedPortal");
      storage_.create(portalConfig);
      storage_.create(new Page("portal", "testGetPageFromRemovedPortal", "home"));
      end(true);

      // Clear cache
      mgr.clearCache();

      // The first transaction
      begin();
      session = mgr.openSession();

      // Get page from JCR and it should be stored in cache
      Page page = storage_.getPage("portal::testGetPageFromRemovedPortal::home");
      assertNotNull(page);

      // Now remove the portal
      PortalConfig portal = storage_.getPortalConfig("portal", "testGetPageFromRemovedPortal");
      storage_.remove(portal);

      // Terminate the first transaction
      end(true);

      // The second transaction
      begin();
      session = mgr.openSession();

      // The portal should be null
      portal = storage_.getPortalConfig("portal", "testGetPageFromRemovedPortal");
      assertNull(portal);

      // The portal home page should also be null
      page = storage_.getPage("portal::testGetPageFromRemovedPortal::home");
      assertNull(page);

      // End second transaction
      end(true);
   }
}

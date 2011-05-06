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

package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSearch extends AbstractPortalTest
{

   /** . */
   private DataStorage storage;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = PortalContainer.getInstance();
      storage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   private void assertFound(String searchTitle, String expectedPage) throws Exception
   {
      Query<Page> q = new Query<Page>(null, null, null, searchTitle, Page.class);
      List<Page> res = storage.find(q).getAll();
      assertEquals(1, res.size());
      assertEquals(expectedPage, res.get(0).getPageId());
   }

   private void assertNotFound(String searchTitle) throws Exception
   {
      Query<Page> q = new Query<Page>(null, null, null, searchTitle, Page.class);
      List<Page> res = storage.find(q).getAll();
      assertEquals(0, res.size());
   }

   public void testFoo() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::searchedpage");
      page.setTitle("Juuu Ziii");
      storage.create(page);
      session.save();

      //
      assertFound("Juuu Ziii", "portal::test::searchedpage");
      assertFound("Juuu", "portal::test::searchedpage");
      assertFound("Ziii", "portal::test::searchedpage");
      assertFound("juuu ziii", "portal::test::searchedpage");
      assertFound("juuu", "portal::test::searchedpage");
      assertFound("ziii", "portal::test::searchedpage");
      assertFound("juu", "portal::test::searchedpage");
      assertFound("zii", "portal::test::searchedpage");
      assertFound("ju", "portal::test::searchedpage");
      assertFound("zi", "portal::test::searchedpage");

      assertNotFound("foo");
      assertNotFound("foo bar");
   }
   
   public void testSearchPageByOwnerID() throws Exception
   {
      Query<Page> q = new Query<Page>(null, "foo", Page.class);
      List<Page> res = storage.find(q).getAll();
      assertEquals(0, res.size());
      
      q.setOwnerId("test");
      res = storage.find(q).getAll();
      int pageNum = res.size();
      assertTrue(pageNum > 0);
      
      //Test trim ownerID
      q.setOwnerId("   test   ");
      res = storage.find(q).getAll();
      assertEquals(pageNum, res.size());
      
      //This should returns all pages
      q.setOwnerId(null);
      res = storage.find(q).getAll();
      assertTrue(res.size() > 0);
   }
}

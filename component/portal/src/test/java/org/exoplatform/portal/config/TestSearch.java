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
public class TestSearch extends AbstractConfigTest
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

   private void assertPageFound(Query<Page> q, String expectedPage) throws Exception
   {
      List<Page> res = storage.find(q).getAll();
      assertEquals(1, res.size());
      assertEquals(expectedPage, res.get(0).getPageId());
   }

   private void assertPageNotFound(Query<Page> q) throws Exception
   {
      List<Page> res = storage.find(q).getAll();
      assertEquals(0, res.size());
   }

   public void testSearchPage() throws Exception
   {
      Page page = new Page();
      page.setPageId("portal::test::searchedpage");
      page.setTitle("Juuu Ziii");
      storage.create(page);
      session.save();

      //
      assertPageFound(new Query<Page>(null, null, null, "Juuu Ziii", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "Juuu", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "Ziii", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "juuu ziii", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "juuu", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "ziii", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "juu", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "zii", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "ju", Page.class), "portal::test::searchedpage");
      assertPageFound(new Query<Page>(null, null, null, "zi", Page.class), "portal::test::searchedpage");

      assertPageNotFound(new Query<Page>(null, null, null, "foo", Page.class));
      assertPageNotFound(new Query<Page>(null, null, null, "foo bar", Page.class));
      assertPageNotFound(new Query<Page>("test", null, null, null, Page.class));
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

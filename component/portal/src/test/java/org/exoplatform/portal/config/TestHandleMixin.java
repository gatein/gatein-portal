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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Aug 25, 2010
 */

public class TestHandleMixin extends AbstractPortalTest
{

   private DataStorage dataStorage;
   
   private POMSessionManager pomMgr;
   
   private POMSession session;
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      begin();
      
      PortalContainer container = PortalContainer.getInstance();
      dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      POMSessionManager pomMgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = pomMgr.getSession();
   }
   
   private void createPage() throws Exception
   {
      
      Page page = new Page();
      page.setTitle("MyTitle");
      page.setOwnerType(PortalConfig.PORTAL_TYPE);
      page.setOwnerId("test");
      page.setName("foo");

      dataStorage.create(page);
   }

   public void testAccessMixin() throws Exception
   {
      createPage();
      Page page = dataStorage.getPage("portal::test::foo");
      
      assertNotNull(page);
      assertEquals("MyTitle", page.getTitle());
      assertEquals("test", page.getOwnerId());
      assertEquals("foo", page.getName());
      
      SampleMixin sampleMixin = dataStorage.adapt(page, SampleMixin.class);
      //Check the default value of sampleProperty property
      assertEquals("SampleProperty", sampleMixin.getSampleProperty());
   }
      
   public void testModifyMixin() throws Exception
   {
      createPage();
      Page page = dataStorage.getPage("portal::test::foo");
      
      assertNotNull(page);
      assertEquals("MyTitle", page.getTitle());
      assertEquals("test", page.getOwnerId());
      assertEquals("foo", page.getName());
      
      SampleMixin sampleMixin = dataStorage.adapt(page, SampleMixin.class);
      sampleMixin.setSampleProperty("FYM!");
      
      Page page2 = dataStorage.getPage("portal::test::foo");
      assertNotNull(page2);
      SampleMixin sampleMixin2 = dataStorage.adapt(page2, SampleMixin.class);
      assertEquals("FYM!", sampleMixin2.getSampleProperty());
      
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }
}

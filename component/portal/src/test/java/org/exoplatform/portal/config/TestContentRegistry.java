/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.registry.CategoryDefinition;
import org.exoplatform.portal.pom.registry.ContentDefinition;
import org.exoplatform.portal.pom.registry.ContentRegistry;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.test.BasicTestCase;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestContentRegistry extends BasicTestCase
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

      //
      PortalContainer container = PortalContainer.getInstance();
      storage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close(false);
      session = null;
      storage = null;
   }

   public void testGetContentRegistry()
   {
      POMSession session = mgr.getSession();
      ContentRegistry registry = session.getContentRegistry();
      assertNotNull(registry);
   }

   public void testCreateCategory()
   {
      POMSession session = mgr.getSession();
      ContentRegistry registry = session.getContentRegistry();
      CategoryDefinition category = registry.createCategory("foo");
      assertNotNull(category);
      assertSame(category, registry.getCategory("foo"));
   }

   public void testCreateContent()
   {
      POMSession session = mgr.getSession();
      ContentRegistry registry = session.getContentRegistry();
      CategoryDefinition category = registry.createCategory("foo");
      ContentDefinition content = category.createContent("bar", Portlet.CONTENT_TYPE, "myportlet");
      assertNotNull(content);

      // Test that we do have a customization shared at the workspace level
      Workspace workspace = session.getWorkspace();
      assertNotNull(workspace.getCustomization("bar"));
   }

}

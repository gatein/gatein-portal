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
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestContentRegistry extends AbstractConfigTest
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
      session.close(false);
      end();
      super.tearDown();
   }

   public void testFoo()
   {
      // Make junit happy
   }

/*
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

*/
}

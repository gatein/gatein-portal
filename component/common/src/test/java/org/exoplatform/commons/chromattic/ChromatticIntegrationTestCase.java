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
package org.exoplatform.commons.chromattic;

import junit.framework.TestCase;
import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.PortalContainer;

import javax.jcr.Session;
import javax.jcr.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ChromatticIntegrationTestCase extends TestCase
{

   /** . */
   private ChromatticLifeCycle configurator;

   /** . */
   private ChromatticManager chromatticManager;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager)container.getComponent(ChromatticManager.class);
      configurator = chromatticManager.getConfigurator("test");
   }

   @Override
   protected void tearDown() throws Exception
   {
      
   }

   public void testConfiguratorInitialized() throws Exception
   {
      assertNotNull(configurator);
      assertEquals("test", configurator.getWorkspaceName());
      assertNotNull(configurator.getChromattic());
      assertSame(chromatticManager, configurator.getManager());
   }

   public void testCannotInitiateMoreThanOneRequest()
   {
      chromatticManager.beginRequest();

      //
      try
      {
         chromatticManager.beginRequest();
         fail();
      }
      catch (IllegalStateException e)
      {
      }

      //
      chromatticManager.endRequest(false);
   }

   public void testCannotEndNonExistingRequest()
   {
      try
      {
         chromatticManager.endRequest(false);
         fail();
      }
      catch (IllegalStateException e)
      {
      }
   }


   public void testWrapperFailsWhenNoGlobalRequest() throws Exception
   {
      try
      {
         configurator.getChromattic().openSession();
         fail();
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void testLocalRequest() throws Exception
   {
      Session jcrSession;

      //
      SessionContext context = configurator.openContext();
      try
      {
         ChromatticSession session = configurator.getChromattic().openSession();
         FooEntity foo = session.create(FooEntity.class);
         assertEquals("test", foo.getWorkspace());
         jcrSession = session.getJCRSession();
         assertTrue(jcrSession.isLive());
         Workspace workspace = jcrSession.getWorkspace();
         assertEquals("test", workspace.getName());

         session.close();
         assertTrue(jcrSession.isLive());
      }
      finally
      {
         configurator.closeContext(context, false);
      }

      // Assert JCR session was properly closed
      assertFalse(jcrSession.isLive());
   }

   public void testLocalRequestNoSessionAccess()
   {
      SessionContext context = configurator.openContext();
      configurator.closeContext(context, false);
   }

   public void testGlobalRequest() throws Exception
   {
      Session jcrSession;

      //
      chromatticManager.beginRequest();
      try
      {
         Chromattic chromattic = configurator.getChromattic();

         // Opens a session with the provided Chromattic
         ChromatticSession session = chromattic.openSession();

         // Check how chromattic see the session
         FooEntity foo = session.create(FooEntity.class);
         assertEquals("test", foo.getWorkspace());

         // Check related JCR session
         jcrSession = session.getJCRSession();
         assertTrue(jcrSession.isLive());
         Workspace workspace = jcrSession.getWorkspace();
         assertEquals("test", workspace.getName());

         // Closing chromattic session should not close the underlying JCR session
         session.close();
         assertTrue(jcrSession.isLive());
      }
      finally
      {
         chromatticManager.endRequest(false);
      }

      // Assert JCR session was properly closed
      assertFalse(jcrSession.isLive());
   }
}

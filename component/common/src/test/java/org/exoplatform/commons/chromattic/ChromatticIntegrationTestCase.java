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
   private ChromatticLifeCycle testLF;

   /** . */
   private ChromatticManager chromatticManager;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager)container.getComponent(ChromatticManager.class);
      testLF = chromatticManager.getLifeCycle("test");
   }

   @Override
   protected void tearDown() throws Exception
   {
      
   }

   public void testConfiguratorInitialized() throws Exception
   {
      assertNotNull(testLF);
      assertEquals("test", testLF.getWorkspaceName());
      assertNotNull(testLF.getChromattic());
      assertSame(chromatticManager, testLF.getManager());
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
         testLF.getChromattic().openSession();
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
      SessionContext context = testLF.openContext();
      try
      {
         ChromatticSession session = testLF.getChromattic().openSession();
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
         testLF.closeContext(context, false);
      }

      // Assert JCR session was properly closed
      assertFalse(jcrSession.isLive());
   }

   public void testLocalRequestNoSessionAccess()
   {
      SessionContext context = testLF.openContext();
      testLF.closeContext(context, false);
   }

   public void testGlobalSession() throws Exception
   {
      Session jcrSession;

      //
      chromatticManager.beginRequest();
      try
      {
         Chromattic chromattic = testLF.getChromattic();

         // No context should be open
         assertNull(testLF.getContext(true));

         // Opens a session with the provided Chromattic
         ChromatticSession session = chromattic.openSession();

         // Now we should have a context
         assertNotNull(testLF.getContext(true));

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

   public void testGlobalSessionContext() throws Exception
   {
      chromatticManager.beginRequest();
      try
      {
         SessionContext context = testLF.getContext(true);
         assertNull(context);

         //
         context = testLF.getContext(false);
         assertNotNull(context);
      }
      finally
      {
         chromatticManager.endRequest(false);
      }
   }
}

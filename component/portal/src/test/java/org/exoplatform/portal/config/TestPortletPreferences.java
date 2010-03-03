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
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortletPreferences extends AbstractPortalTest
{

   public TestPortletPreferences(String name)
   {
      super(name);
   }

   /** . */
   private DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = getContainer();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   public void testSiteScopedPreferences() throws Exception
   {
      Page page = storage_.getPage("portal::test::test4");
      Application<Portlet> app = (Application<Portlet>)page.getChildren().get(0);
      PersistentApplicationState<Portlet> state = (PersistentApplicationState)app.getState();

      //
      Portlet prefs = storage_.load(state, ApplicationType.PORTLET);
      assertEquals(new PortletBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         .build(), prefs);

      //
      prefs.setValue("template", "someanothervalue");
      storage_.save(state, prefs);

      //
      prefs = storage_.load(state, ApplicationType.PORTLET);
      assertNotNull(prefs);
      assertEquals(new PortletBuilder().add("template", "someanothervalue").build(), prefs);
   }

   public void testNullPreferenceValue() throws Exception
   {
      Page page = storage_.getPage("portal::test::test4");
      Application<Portlet> app = (Application<Portlet>)page.getChildren().get(0);
      PersistentApplicationState<Portlet> state = (PersistentApplicationState)app.getState();

      //
      Portlet prefs = storage_.load(state, ApplicationType.PORTLET);

      //
      prefs.setValue("template", null);
      storage_.save(state, prefs);

      //
      prefs = storage_.load(state, ApplicationType.PORTLET);
      assertNotNull(prefs);
      assertEquals(new PortletBuilder().add("template", "").build(), prefs);
   }
}

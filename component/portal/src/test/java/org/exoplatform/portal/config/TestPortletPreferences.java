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
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.portlet.PortletApplication;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.portlet.Preferences;
import org.exoplatform.portal.pom.spi.portlet.PreferencesBuilder;
import org.exoplatform.test.BasicTestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortletPreferences extends BasicTestCase
{

   public TestPortletPreferences(String name)
   {
      super(name);
   }

   /** . */
   private DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   public void setUp() throws Exception
   {
      super.setUp();
      if (storage_ != null)
         return;
      PortalContainer container = PortalContainer.getInstance();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      mgr.closeSession(false);
   }

   public void testSiteScopedPreferences() throws Exception
   {
      Page page = storage_.getPage("portal::test::test4");
      PortletApplication app = (PortletApplication)page.getChildren().get(0);
      PersistentApplicationState<Preferences> state = (PersistentApplicationState)app.getState();

      //
      Preferences prefs = storage_.load(state);
      assertEquals(new PreferencesBuilder().add("template", "par:/groovy/groovy/webui/component/UIBannerPortlet.gtmpl")
         .build(), prefs);

      //
      prefs.setValue("template", "someanothervalue");
      storage_.save(state, prefs);

      //
      prefs = storage_.load(state);
      assertNotNull(prefs);
      assertEquals(new PreferencesBuilder().add("template", "someanothervalue").build(), prefs);
   }
}

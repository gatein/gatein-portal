/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.description;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.AbstractPortalTest;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.i18n.I18Nized;
import org.exoplatform.portal.mop.navigation.NavigationServiceImpl;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.common.util.Tools;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml")
})
public class TestDescriptionService extends AbstractPortalTest
{

   /** . */
   protected POMSessionManager mgr;

   /** . */
   protected NavigationServiceImpl service;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = PortalContainer.getInstance();
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      service = new NavigationServiceImpl(mgr);
//      dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);

      // Clear the cache for each test
      service.clearCache();

      //
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }

   public void testResolveNoDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      String id = nav.getObjectId();

      //
      assertEquals(null, svc.resolveDescription(id, null, Locale.ENGLISH));
   }

   public void testResolveDefaultDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      Described described = nav.adapt(Described.class);
      described.setName("foo_name");
      String id = nav.getObjectId();

      //
      assertEquals(null, svc.resolveDescription(id, null, Locale.ENGLISH));
   }

   public void testResolveLocalizedDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      I18Nized i18nized = nav.adapt(I18Nized.class);
      Described described = i18nized.getMixin(Described.class, Locale.ENGLISH, true);
      described.setName("name_en");
      described = i18nized.getMixin(Described.class, Locale.UK, true);
      described.setName("name_en_GB");
      String id = nav.getObjectId();

      //
      assertEquals(null, svc.resolveDescription(id, null, Locale.GERMAN));
      assertEquals(null, svc.resolveDescription(id, null, new Locale("", "GB")));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, Locale.ENGLISH, Locale.GERMAN));
      assertEquals(new Described.State("name_en_GB", null), svc.resolveDescription(id, Locale.UK, Locale.GERMAN));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, Locale.US, Locale.GERMAN));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, null, Locale.ENGLISH));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, null, Locale.US));
      assertEquals(new Described.State("name_en_GB", null), svc.resolveDescription(id, null, Locale.UK));
   }

   public void testResolveDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      Described described = nav.adapt(Described.class);
      described.setName("name");
      I18Nized i18nized = nav.adapt(I18Nized.class);
      described = i18nized.getMixin(Described.class, Locale.ENGLISH, true);
      described.setName("name_en");
      described = i18nized.getMixin(Described.class, Locale.UK, true);
      described.setName("name_en_GB");
      String id = nav.getObjectId();

      //
      assertEquals(null, svc.resolveDescription(id, null, Locale.GERMAN));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, Locale.ENGLISH, Locale.GERMAN));
      assertEquals(new Described.State("name_en_GB", null), svc.resolveDescription(id, Locale.UK, Locale.GERMAN));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, Locale.US, Locale.GERMAN));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, null, Locale.ENGLISH));
      assertEquals(new Described.State("name_en", null), svc.resolveDescription(id, null, Locale.US));
      assertEquals(new Described.State("name_en_GB", null), svc.resolveDescription(id, null, Locale.UK));
   }

   public void testGetDefaultDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      String id = nav.getObjectId();
      Described described = nav.adapt(Described.class);
      described.setName("foo_name");

      //
      assertEquals(new Described.State("foo_name", null), svc.getDescription(id));
   }

   public void testSetDefaultDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      String id = nav.getObjectId();

      //
      assertNull(svc.getDescription(id));

      //
      svc.setDescription(id, new Described.State("foo_name", null));

      //
      assertTrue(nav.isAdapted(Described.class));
      Described described = nav.adapt(Described.class);
      assertEquals("foo_name", described.getName());
   }

   public void testRemoveDefaultDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      String id = nav.getObjectId();
      Described described = nav.adapt(Described.class);
      described.setName("foo_name");

      //
      svc.setDescription(id, null);
   }

   public void testSetLocalizedDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");

      //
      svc.setDescription(nav.getObjectId(), Locale.ENGLISH, new Described.State("foo_english", null));

      //
      assertTrue(nav.isAdapted(I18Nized.class));
      I18Nized ized = nav.adapt(I18Nized.class);
      Described desc = ized.getMixin(Described.class, Locale.ENGLISH, false);
      assertNotNull(desc);
      assertEquals("foo_english", desc.getName());
   }

   public void testSetInvalidLocaleDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");

      //
      try
      {
         svc.setDescription(nav.getObjectId(), new Locale("", "GB"), new Described.State("foo_invalid", null));
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }

      //
      try
      {
         svc.setDescription(nav.getObjectId(), new Locale("en", "GB", "variant"), new Described.State("foo_invalid", null));
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testAddLocalizedDescription() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");
      I18Nized i18nized = nav.adapt(I18Nized.class);
      Described desc = i18nized.getMixin(Described.class, Locale.ENGLISH, true);
      desc.setName("add_english");

      //
      svc.setDescription(nav.getObjectId(), Locale.FRENCH, new Described.State("add_french", null));

      //
      assertTrue(nav.isAdapted(I18Nized.class));
      I18Nized ized = nav.adapt(I18Nized.class);
      desc = ized.getMixin(Described.class, Locale.ENGLISH, false);
      assertNotNull(desc);
      assertEquals("add_english", desc.getName());
      desc = ized.getMixin(Described.class, Locale.FRENCH, false);
      assertNotNull(desc);
      assertEquals("add_french", desc.getName());
   }

   public void testGetDescriptions() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");

      //
      assertNull(svc.getDescriptions(nav.getObjectId()));

      //
      I18Nized i18nized = nav.adapt(I18Nized.class);
      Described described = i18nized.getMixin(Described.class, Locale.ENGLISH, true);
      described.setName("foo_english");
      described = i18nized.getMixin(Described.class, Locale.FRENCH, true);
      described.setName("foo_french");

      //
      Map<Locale, Described.State> description = svc.getDescriptions(nav.getObjectId());
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), description.keySet());
      assertEquals(new Described.State("foo_english", null), description.get(Locale.ENGLISH));
      assertEquals(new Described.State("foo_french", null), description.get(Locale.FRENCH));
   }

   public void testSetDescriptions() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");

      //
      assertNull(svc.getDescriptions(nav.getObjectId()));

      //
      Map<Locale, Described.State> description = new HashMap<Locale, Described.State>();
      description.put(Locale.ENGLISH, new Described.State("bar_english", null));
      description.put(Locale.FRENCH, new Described.State("bar_french", null));
      svc.setDescriptions(nav.getObjectId(), description);

      //
      description = svc.getDescriptions(nav.getObjectId());
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), description.keySet());
      assertEquals(new Described.State("bar_english", null), description.get(Locale.ENGLISH));
      assertEquals(new Described.State("bar_french", null), description.get(Locale.FRENCH));

      //
      description = new HashMap<Locale, Described.State>();
      description.put(Locale.ENGLISH, new Described.State("bar_english_2", null));
      svc.setDescriptions(nav.getObjectId(), description);

      //
      description = svc.getDescriptions(nav.getObjectId());
      assertEquals(Tools.toSet(Locale.ENGLISH), description.keySet());
      assertEquals(new Described.State("bar_english_2", null), description.get(Locale.ENGLISH));
   }

   public void testSetInvalidLocaleDescriptions() throws Exception
   {
      DescriptionService svc = new DescriptionServiceImpl(mgr);
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "foo");
      Navigation nav = portal.getRootNavigation().addChild("default");

      //
      try
      {
         svc.setDescriptions(nav.getObjectId(), Collections.singletonMap(new Locale("", "GB"), new Described.State("bar_invalid", null)));
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         svc.setDescriptions(nav.getObjectId(), Collections.singletonMap(new Locale("en", "GB", "variant"), new Described.State("bar_invalid", null)));
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }
}

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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.*;
import org.gatein.common.util.Tools;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.core.api.MOPService;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static org.exoplatform.portal.mop.importer.Builder.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationImporter extends AbstractTestNavigationService
{

   public void testInsertCreateNavigation()
   {
      testCreate(ImportMode.INSERT);
   }

   public void testConserveCreateNavigation()
   {
      testCreate(ImportMode.CONSERVE);
   }

   public void testOverwriteCreateNavigation()
   {
      testCreate(ImportMode.OVERWRITE);
   }

   private void testCreate(ImportMode mode)
   {
      String name = mode.name() + "_create";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));
      PageNavigation src = new PageNavigation("portal", name);
      src.setPriority(2);
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, mode, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal(name));
      assertEquals(2, (int)ctx.getState().getPriority());
   }

   public void testInsertNavigation()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "insert_navigation");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("insert_navigation")));

      //
      FragmentBuilder builder = fragment().add(node("a"));

      //
      PageNavigation src = new PageNavigation("portal", "insert_navigation").addFragment(builder.build());
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("insert_navigation"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      NodeContext<?> a = node.get("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals("a", a.getState().getLabel());
      assertEquals(0, a.getNodeCount());
   }

   public void testInsertFragment()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "insert_fragment");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("insert_fragment")));

      //
      FragmentBuilder builder = fragment().add(node("a").add(node("b")));

      //
      PageNavigation src = new PageNavigation("portal", "insert_fragment").addFragment(builder.build());
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("insert_fragment"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      NodeContext<?> a = node.get("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals("a", a.getState().getLabel());
      assertEquals(1, a.getNodeCount());
   }

   public void testCreateMerge()
   {
      testMerge(ImportMode.CONSERVE);
   }

   public void testInsertMerge()
   {
      testMerge(ImportMode.INSERT);
   }

   public void testOverwriteMerge()
   {
      testMerge(ImportMode.OVERWRITE);
   }

   private void testMerge(ImportMode importMode)
   {
      String name = importMode.name() + "_merge";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));

      //
      FragmentBuilder builder = fragment().add(node("a").add(node("b")));

      //
      PageNavigation src = new PageNavigation("portal", name).addFragment(builder.build());
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.CONSERVE, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal(name));
      Node node = service.loadNode(Node.MODEL, ctx, Scope.ALL, null).getNode();
      Node a = node.getChild("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals(1, a.getNodeCount());
      Node b = a.getChild("b");
      assertNotNull(b);
      assertEquals("b", b.getName());
      assertEquals(0, b.getNodeCount());

      //
      builder = fragment().add(node("a").add(node("d"))).add(node("c"));
      src = new PageNavigation("portal", name).addFragment(builder.build());
      importer = new NavigationImporter(Locale.ENGLISH, importMode, src, service, descriptionService);
      importer.perform();

      //
      ctx = service.loadNavigation(SiteKey.portal(name));
      node = service.loadNode(Node.MODEL, ctx, Scope.ALL, null).getNode();
      switch (importMode)
      {
         case INSERT:
         {
            assertEquals(2, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals("a", a.getState().getLabel());
            assertEquals(2, a.getNodeCount());
            b = a.getChild("b");
            assertNotNull(b);
            assertEquals("b", b.getState().getLabel());
            assertEquals(0, b.getNodeCount());
            Node c = node.getChild("c");
            assertNotNull(c);
            assertEquals("c", c.getState().getLabel());
            assertEquals(0, c.getNodeCount());
            Node d = a.getChild("d");
            assertNotNull(d);
            assertEquals("d", d.getName());
            assertEquals(0, d.getNodeCount());
            break;
         }
         case CONSERVE:
         {
            assertEquals(1, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals(1, a.getNodeCount());
            assertNotNull(b);
            assertEquals("b", b.getState().getLabel());
            assertEquals(0, b.getNodeCount());
            break;
         }
         case OVERWRITE:
         {
            assertEquals(2, node.getNodeCount());
            a = node.getChild("a");
            assertNotNull(a);
            assertEquals("a", a.getState().getLabel());
            assertEquals(1, a.getNodeCount());
            Node c = node.getChild("c");
            assertNotNull(c);
            assertEquals("c", c.getState().getLabel());
            assertEquals(0, c.getNodeCount());
            Node d = a.getChild("d");
            assertNotNull(d);
            assertEquals("d", d.getName());
            assertEquals(0, d.getNodeCount());
            break;
         }
      }
   }

   public void testOrder()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "order");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("order")));

      //
      PageNavigation src = new PageNavigation("portal", "order").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("order"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(3, node.getNodeCount());
      assertEquals("a", node.get(0).getName());
      assertEquals("b", node.get(1).getName());
      assertEquals("c", node.get(2).getName());

      //
      src.getFragment().getNodes().add(0, node("d").build());
      importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(4, node.getNodeCount());
      assertEquals("d", node.get(0).getName());
      assertEquals("a", node.get(1).getName());
      assertEquals("b", node.get(2).getName());
      assertEquals("c", node.get(3).getName());

      //
      src.getFragment().getNodes().add(node("e").build());
      importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(5, node.getNodeCount());
      assertEquals("d", node.get(0).getName());
      assertEquals("a", node.get(1).getName());
      assertEquals("b", node.get(2).getName());
      assertEquals("c", node.get(3).getName());
      assertEquals("e", node.get(4).getName());
   }

   public void testExtendedLabel()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "extended_label");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("extended_label")));

      //
      PageNavigation src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      NavigationFragment fragment = src.getFragment();
      fragment.getNode("a").setLabels(new I18NString(new LocalizedString("a_en", Locale.ENGLISH), new LocalizedString("a_fr", Locale.FRENCH)));
      fragment.getNode("b").setLabels(new I18NString(new LocalizedString("b_en"), new LocalizedString("b_fr", Locale.FRENCH)));
      fragment.getNode("c").setLabels(new I18NString(new LocalizedString("c_en")));
      src.setOwnerId("extended_label");
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.OVERWRITE, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("extended_label"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();

      // The fully explicit case
      NodeContext<?> a = (NodeContext<?>)node.getNode("a");
      Map<Locale, Described.State> aDesc = descriptionService.getDescriptions(a.getId());
      assertNotNull(aDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), aDesc.keySet());
      assertEquals(new Described.State("a_en", null), aDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("a_fr", null), aDesc.get(Locale.FRENCH));
      assertNull(a.getState().getLabel());

      // No explicit language means to use the portal locale
      NodeContext<?> b = (NodeContext<?>)node.getNode("b");
      Map<Locale, Described.State> bDesc = descriptionService.getDescriptions(b.getId());
      assertNotNull(bDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), bDesc.keySet());
      assertEquals(new Described.State("b_en", null), bDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("b_fr", null), bDesc.get(Locale.FRENCH));
      assertNull(b.getState().getLabel());

      // The simple use case : one single label without the xml:lang attribute
      NodeContext<?> c = (NodeContext<?>)node.getNode("c");
      Map<Locale, Described.State> cDesc = descriptionService.getDescriptions(c.getId());
      assertNull(cDesc);
      assertEquals("c_en", c.getState().getLabel());

      //----------------- Now test extended labels merge -----------------//
      src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      fragment = src.getFragment();
      fragment.getNode("a").setLabels(new I18NString(new LocalizedString("a_it", Locale.ITALIAN), new LocalizedString("a_de", Locale.GERMAN)));
      fragment.getNode("b").setLabels(new I18NString(new LocalizedString("foo_b_en"), new LocalizedString("b_it", Locale.ITALIAN)));
      fragment.getNode("c").setLabels(new I18NString(new LocalizedString("foo_c_en")));
      src.setOwnerId("extended_label");

      importer = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, service, descriptionService);
      importer.perform();

      //
      ctx = service.loadNavigation(SiteKey.portal("extended_label"));
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();

      // The fully explicit case
      a = (NodeContext<?>)node.getNode("a");
      aDesc = descriptionService.getDescriptions(a.getId());
      assertNotNull(aDesc);
      assertEquals(Tools.toSet(Locale.ITALIAN, Locale.GERMAN), aDesc.keySet());
      assertEquals(new Described.State("a_it", null), aDesc.get(Locale.ITALIAN));
      assertEquals(new Described.State("a_de", null), aDesc.get(Locale.GERMAN));
      assertNull(a.getState().getLabel());

      // No explicit language means to use the portal locale
      b = (NodeContext<?>)node.getNode("b");
      bDesc = descriptionService.getDescriptions(b.getId());
      assertNotNull(bDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.ITALIAN), bDesc.keySet());
      assertEquals(new Described.State("foo_b_en", null), bDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("b_it", null), bDesc.get(Locale.ITALIAN));
      assertNull(b.getState().getLabel());

      // The simple use case : one single label without the xml:lang attribute
      c = (NodeContext<?>)node.getNode("c");
      cDesc = descriptionService.getDescriptions(c.getId());
      assertNull(cDesc);
      assertEquals("foo_c_en", c.getState().getLabel());

      //----------------- Now test extended labels overwrite -----------------//
      src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"), node("c")).build());
      fragment = src.getFragment();
      fragment.getNode("a").setLabels(new I18NString(new LocalizedString("bar_a_en", Locale.ENGLISH), new LocalizedString("bar_a_fr", Locale.FRENCH)));
      fragment.getNode("b").setLabels(new I18NString(new LocalizedString("bar_b_en"), new LocalizedString("bar_b_fr", Locale.FRENCH)));
      fragment.getNode("c").setLabels(new I18NString(new LocalizedString("bar_c_en")));
      src.setOwnerId("extended_label");

      importer = new NavigationImporter(Locale.ENGLISH, ImportMode.OVERWRITE, src, service, descriptionService);
      importer.perform();

      //
      ctx = service.loadNavigation(SiteKey.portal("extended_label"));
      node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();

      // The fully explicit case
      a = (NodeContext<?>)node.getNode("a");
      aDesc = descriptionService.getDescriptions(a.getId());
      assertNotNull(aDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), aDesc.keySet());
      assertEquals(new Described.State("bar_a_en", null), aDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("bar_a_fr", null), aDesc.get(Locale.FRENCH));
      assertNull(a.getState().getLabel());

      // No explicit language means to use the portal locale
      b = (NodeContext<?>)node.getNode("b");
      bDesc = descriptionService.getDescriptions(b.getId());
      assertNotNull(bDesc);
      assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), bDesc.keySet());
      assertEquals(new Described.State("bar_b_en", null), bDesc.get(Locale.ENGLISH));
      assertEquals(new Described.State("bar_b_fr", null), bDesc.get(Locale.FRENCH));
      assertNull(b.getState().getLabel());

      // The simple use case : one single label without the xml:lang attribute
      c = (NodeContext<?>)node.getNode("c");
      cDesc = descriptionService.getDescriptions(c.getId());
      assertNull(cDesc);
      assertEquals("bar_c_en", c.getState().getLabel());
   }

   public void testFullNavigation()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "full_navigation");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("full_navigation")));

      //
      PageNavigation src = new PageNavigation("portal", "full_navigation").addFragment(fragment().add(node("a")).build());
      src.addFragment(fragment().add(node("b"), node("c")).build());
      src.addFragment(fragment("a").add(node("d")).build());

      //
      NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, service, descriptionService);
      importer.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("full_navigation"));
      NodeContext<NodeContext<?>> root = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null);
      assertEquals(3, root.getNodeSize());
      Iterator<NodeContext<?>> i = root.iterator();
      NodeContext<?> a = i.next();
      assertEquals("a", a.getName());
      assertEquals(1, a.getNodeSize());
      NodeContext<?> d = a.get("d");
      assertNotNull(d);
      assertEquals(0, d.getNodeSize());
      NodeContext<?> b = i.next();
      assertEquals("b", b.getName());
      assertEquals(0, b.getNodeSize());
      NodeContext<?> c = i.next();
      assertEquals("c", c.getName());
      assertEquals(0, c.getNodeSize());
      assertFalse(i.hasNext());
   }
}

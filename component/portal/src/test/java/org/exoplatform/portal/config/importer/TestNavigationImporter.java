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

package org.exoplatform.portal.config.importer;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.*;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.core.api.MOPService;

import static org.exoplatform.portal.config.importer.Builder.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationImporter extends AbstractTestNavigationService
{

   public void testMergeCreateNavigation()
   {
      testCreate(ImportMode.MERGE);
   }

   public void testConserveCreateNavigation()
   {
      testCreate(ImportMode.CONSERVE);
   }

   public void testReimportCreateNavigation()
   {
      testCreate(ImportMode.REIMPORT);
   }

   private void testCreate(ImportMode mode)
   {
      String name = mode.name() + "_create_navigation";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));
      PageNavigation src = navigation(name).build();
      src.setPriority(2);
      src.setOwnerId(name);
      NavigationImporter merge = new NavigationImporter(mode, src, service);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal(name));
      assertEquals(2, (int)ctx.getState().getPriority());
   }

   public void testMergeCreate()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_create");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_create")));

      //
      Builder builder = navigation("merge_create").add(node("a"));

      //
      PageNavigation src = builder.build();
      src.setOwnerId("merge_create");
      NavigationImporter merge = new NavigationImporter(ImportMode.MERGE, src, service);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_create"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      NodeContext<?> a = node.get("a");
      assertNotNull(a);
      assertEquals("a", a.getName());
      assertEquals("a", a.getState().getLabel());
      assertEquals(0, a.getNodeCount());
   }

   public void testMergeNested()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_nested");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_nested")));

      //
      Builder builder = navigation("merge_nested").add(node("a").add(node("b")));

      //
      PageNavigation src = builder.build();
      src.setOwnerId("merge_nested");
      NavigationImporter merge = new NavigationImporter(ImportMode.MERGE, src, service);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_nested"));
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

   public void testMergeMerge()
   {
      testMerge(ImportMode.MERGE);
   }

   public void testReimportMerge()
   {
      testMerge(ImportMode.REIMPORT);
   }

   private void testMerge(ImportMode importMode)
   {
      String name = importMode.name() + "_merge_merge";

      //
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, name);
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal(name)));

      //
      Builder builder = navigation(name).add(node("a").add(node("b")));

      //
      PageNavigation src = builder.build();
      src.setOwnerId(name);
      NavigationImporter merge = new NavigationImporter(ImportMode.CONSERVE, src, service);
      merge.perform();

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
      builder = navigation(name).add(node("a").add(node("d"))).add(node("c"));
      src = builder.build();
      src.setOwnerId(name);
      merge = new NavigationImporter(importMode, src, service);
      merge.perform();

      //
      ctx = service.loadNavigation(SiteKey.portal(name));
      node = service.loadNode(Node.MODEL, ctx, Scope.ALL, null).getNode();
      switch (importMode)
      {
         case MERGE:
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
         case REIMPORT:
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

   public void testMergeOrder()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "merge_order");
      sync(true);

      //
      assertNull(service.loadNavigation(SiteKey.portal("merge_order")));

      //
      PageNavigation src = navigation("merge_order").add(node("a"), node("b"), node("c")).build();
      src.setOwnerId("merge_order");
      NavigationImporter merge = new NavigationImporter(ImportMode.MERGE, src, service);
      merge.perform();

      //
      NavigationContext ctx = service.loadNavigation(SiteKey.portal("merge_order"));
      NodeContext<?> node = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null).getNode();
      assertEquals(3, node.getNodeCount());
      assertEquals("a", node.get(0).getName());
      assertEquals("b", node.get(1).getName());
      assertEquals("c", node.get(2).getName());
   }
}

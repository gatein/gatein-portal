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

import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.AbstractTestNavigationService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.core.api.MOPService;

import java.util.Locale;

import static org.exoplatform.portal.mop.importer.Builder.fragment;
import static org.exoplatform.portal.mop.importer.Builder.node;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationFragmentImporter extends AbstractTestNavigationService
{

   public void testRemoveOrphan()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "remove_orphan");
      sync(true);

      //
      NavigationContext ctx = new NavigationContext(SiteKey.portal("remove_orphan"), new NavigationState(1));
      service.saveNavigation(ctx);
      NodeContext root = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null);
      root.add(0, "foo").add(0, "bar");
      service.saveNode(root, null);

      //
      NavigationFragment imported = fragment("foo").build();

      //
      NavigationFragmentImporter importer = new NavigationFragmentImporter(
         new String[0],
         service,
         SiteKey.portal("remove_orphan"),
         Locale.ENGLISH,
         descriptionService,
         imported,
         new ImportConfig(true, false, false));
      NodeContext node = importer.perform();
      assertEquals(0, node.getNodeSize());
   }

   public void testCreateMissingPath()
   {
      MOPService mop = mgr.getPOMService();
      mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "create_missing_path");
      sync(true);

      //
      NavigationContext ctx = new NavigationContext(SiteKey.portal("create_missing_path"), new NavigationState(1));
      service.saveNavigation(ctx);
      NodeContext root = service.loadNode(NodeModel.SELF_MODEL, ctx, Scope.ALL, null);
      root.add(0, "foo").add(0, "bar");
      service.saveNode(root, null);

      //
      NavigationFragment imported = fragment("foo").add(node("juu")).build();

      //
      NavigationFragmentImporter importer = new NavigationFragmentImporter(
         new String[]{"foo","bar"},
         service,
         SiteKey.portal("create_missing_path"),
         Locale.ENGLISH,
         descriptionService,
         imported,
         ImportMode.INSERT.config);
      NodeContext node = importer.perform();
      assertNotNull(node);
      assertEquals("bar", node.getName());
      assertNotNull(node.get("juu"));

      //
      importer = new NavigationFragmentImporter(
         new String[]{"foo","bar","daa"},
         service,
         SiteKey.portal("create_missing_path"),
         Locale.ENGLISH,
         descriptionService,
         imported,
         ImportMode.INSERT.config);
      node = importer.perform();
      assertNotNull(node);
      assertEquals("daa", node.getName());
      assertNotNull(node.get("juu"));

      //
      importer = new NavigationFragmentImporter(
         new String[]{"foo"},
         service,
         SiteKey.portal("create_missing_path"),
         Locale.ENGLISH,
         descriptionService,
         imported,
         ImportMode.INSERT.config);
      node = importer.perform();
      assertEquals("foo", node.getName());
      assertNotNull(node.get("juu"));
   }
}

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

import static org.exoplatform.portal.mop.importer.Builder.fragment;
import static org.exoplatform.portal.mop.importer.Builder.node;

import java.util.Locale;

import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationNodeImporter extends AbstractMopServiceTest {

    public void testRemoveOrphan() {
        createSite(SiteType.PORTAL, "remove_orphan");
        sync(true);

        //
        NavigationContext ctx = new NavigationContext(SiteKey.portal("remove_orphan"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        NodeContext root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(0, "foo", NodeState.INITIAL).add(0, "bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        //
        NavigationFragment imported = fragment("foo").build();

        //
        NavigationNodeImporter importer = new NavigationNodeImporter(new String[0], getNavigationService(),
                SiteKey.portal("remove_orphan"), Locale.ENGLISH, getDescriptionService(), imported, new ImportConfig(true, false,
                        false));
        NodeContext node = importer.perform();
        assertEquals(0, node.getNodeSize());
    }

    public void testCreateMissingPath() {
        createSite(SiteType.PORTAL, "create_missing_path");
        sync(true);

        //
        NavigationContext ctx = new NavigationContext(SiteKey.portal("create_missing_path"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        NodeContext root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(0, "foo", NodeState.INITIAL).add(0, "bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        //
        NavigationFragment imported = fragment("foo").add(node("juu")).build();

        //
        NavigationNodeImporter importer = new NavigationNodeImporter(new String[] { "foo", "bar" }, getNavigationService(),
                SiteKey.portal("create_missing_path"), Locale.ENGLISH, getDescriptionService(), imported, ImportMode.INSERT.config);
        NodeContext node = importer.perform();
        assertNotNull(node);
        assertEquals("bar", node.getName());
        assertNotNull(node.get("juu"));

        //
        importer = new NavigationNodeImporter(new String[] { "foo", "bar", "daa" }, getNavigationService(),
                SiteKey.portal("create_missing_path"), Locale.ENGLISH, getDescriptionService(), imported, ImportMode.INSERT.config);
        node = importer.perform();
        assertNotNull(node);
        assertEquals("daa", node.getName());
        assertNotNull(node.get("juu"));

        //
        importer = new NavigationNodeImporter(new String[] { "foo" }, getNavigationService(), SiteKey.portal("create_missing_path"),
                Locale.ENGLISH, getDescriptionService(), imported, ImportMode.INSERT.config);
        node = importer.perform();
        assertEquals("foo", node.getName());
        assertNotNull(node.get("juu"));
    }
}

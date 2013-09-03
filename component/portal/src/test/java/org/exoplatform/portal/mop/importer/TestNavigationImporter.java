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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.navigation.NavigationNode;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.common.util.Tools;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationImporter extends AbstractMopServiceTest {

    public void testInsertCreateNavigation() {
        testCreate(ImportMode.INSERT);
    }

    public void testConserveCreateNavigation() {
        testCreate(ImportMode.CONSERVE);
    }

    public void testOverwriteCreateNavigation() {
        testCreate(ImportMode.OVERWRITE);
    }

    private void testCreate(ImportMode mode) {
        String name = mode.name() + "_create";

        //
        createSite(SiteType.PORTAL, name);
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal(name)));
        PageNavigation src = new PageNavigation("portal", name);
        src.setPriority(2);
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, mode, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal(name));
        assertEquals(2, (int) ctx.getState().getPriority());
    }

    public void testInsertNavigation() {
        createSite(SiteType.PORTAL, "insert_navigation");
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("insert_navigation")));

        //
        FragmentBuilder builder = fragment().add(node("a"));

        //
        PageNavigation src = new PageNavigation("portal", "insert_navigation").addFragment(builder.build());
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("insert_navigation"));
        NodeContext<?, NodeState> node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        NodeContext<?, NodeState> a = node.get("a");
        assertNotNull(a);
        assertEquals("a", a.getName());
        assertEquals("a", a.getState().getLabel());
        assertEquals(0, a.getNodeCount());
    }

    public void testInsertFragment() {
        createSite(SiteType.PORTAL, "insert_fragment");
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("insert_fragment")));

        //
        FragmentBuilder builder = fragment().add(node("a").add(node("b")));

        //
        PageNavigation src = new PageNavigation("portal", "insert_fragment").addFragment(builder.build());
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("insert_fragment"));
        NodeContext<?, NodeState> node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        NodeContext<?, NodeState> a = node.get("a");
        assertNotNull(a);
        assertEquals("a", a.getName());
        assertEquals("a", a.getState().getLabel());
        assertEquals(1, a.getNodeCount());
    }

    public void testCreateMerge() {
        testMerge(ImportMode.CONSERVE);
    }

    public void testInsertMerge() {
        testMerge(ImportMode.INSERT);
    }

    public void testOverwriteMerge() {
        testMerge(ImportMode.OVERWRITE);
    }

    private void testMerge(ImportMode importMode) {
        String name = importMode.name() + "_merge";

        //
        createSite(SiteType.PORTAL, name);
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal(name)));

        //
        FragmentBuilder builder = fragment().add(node("a").add(node("b")));

        //
        PageNavigation src = new PageNavigation("portal", name).addFragment(builder.build());
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.CONSERVE, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal(name));
        NavigationNode node = getNavigationService().loadNode(NavigationNode.MODEL, ctx, Scope.ALL, null).getNode();
        NavigationNode a = node.getChild("a");
        assertNotNull(a);
        assertEquals("a", a.getName());
        assertEquals(1, a.getNodeCount());
        NavigationNode b = a.getChild("b");
        assertNotNull(b);
        assertEquals("b", b.getName());
        assertEquals(0, b.getNodeCount());

        //
        builder = fragment().add(node("a").add(node("d"))).add(node("c"));
        src = new PageNavigation("portal", name).addFragment(builder.build());
        importer = new NavigationImporter(Locale.ENGLISH, importMode, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        ctx = getNavigationService().loadNavigation(SiteKey.portal(name));
        node = getNavigationService().loadNode(NavigationNode.MODEL, ctx, Scope.ALL, null).getNode();
        switch (importMode) {
            case INSERT: {
                assertEquals(2, node.getNodeCount());
                a = node.getChild("a");
                assertNotNull(a);
                assertEquals("a", a.getState().getLabel());
                assertEquals(2, a.getNodeCount());
                b = a.getChild("b");
                assertNotNull(b);
                assertEquals("b", b.getState().getLabel());
                assertEquals(0, b.getNodeCount());
                NavigationNode c = node.getChild("c");
                assertNotNull(c);
                assertEquals("c", c.getState().getLabel());
                assertEquals(0, c.getNodeCount());
                NavigationNode d = a.getChild("d");
                assertNotNull(d);
                assertEquals("d", d.getName());
                assertEquals(0, d.getNodeCount());
                break;
            }
            case CONSERVE: {
                assertEquals(1, node.getNodeCount());
                a = node.getChild("a");
                assertNotNull(a);
                assertEquals(1, a.getNodeCount());
                assertNotNull(b);
                assertEquals("b", b.getState().getLabel());
                assertEquals(0, b.getNodeCount());
                break;
            }
            case OVERWRITE: {
                assertEquals(2, node.getNodeCount());
                a = node.getChild("a");
                assertNotNull(a);
                assertEquals("a", a.getState().getLabel());
                assertEquals(1, a.getNodeCount());
                NavigationNode c = node.getChild("c");
                assertNotNull(c);
                assertEquals("c", c.getState().getLabel());
                assertEquals(0, c.getNodeCount());
                NavigationNode d = a.getChild("d");
                assertNotNull(d);
                assertEquals("d", d.getName());
                assertEquals(0, d.getNodeCount());
                break;
            }
        }
    }

    public void testOrder() {
        createSite(SiteType.PORTAL, "order");
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("order")));

        //
        PageNavigation src = new PageNavigation("portal", "order").addFragment(fragment().add(node("a"), node("b"), node("c"))
                .build());
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("order"));
        NodeContext<?, NodeState> node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertEquals(3, node.getNodeCount());
        assertEquals("a", node.get(0).getName());
        assertEquals("b", node.get(1).getName());
        assertEquals("c", node.get(2).getName());

        //
        src.getFragment().getNodes().add(0, node("d").build());
        importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertEquals(4, node.getNodeCount());
        assertEquals("d", node.get(0).getName());
        assertEquals("a", node.get(1).getName());
        assertEquals("b", node.get(2).getName());
        assertEquals("c", node.get(3).getName());

        //
        src.getFragment().getNodes().add(node("e").build());
        importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertEquals(5, node.getNodeCount());
        assertEquals("d", node.get(0).getName());
        assertEquals("a", node.get(1).getName());
        assertEquals("b", node.get(2).getName());
        assertEquals("c", node.get(3).getName());
        assertEquals("e", node.get(4).getName());
    }

    public void testExtendedLabel() {
        createSite(SiteType.PORTAL, "extended_label");
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("extended_label")));

        //
        PageNavigation src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"),
                node("c")).build());
        NavigationFragment fragment = src.getFragment();
        fragment.getNode("a").setLabels(
                new I18NString(new LocalizedString("a_en", Locale.ENGLISH), new LocalizedString("a_fr", Locale.FRENCH)));
        fragment.getNode("b")
                .setLabels(new I18NString(new LocalizedString("b_en"), new LocalizedString("b_fr", Locale.FRENCH)));
        fragment.getNode("c").setLabels(new I18NString(new LocalizedString("c_en")));
        src.setOwnerId("extended_label");
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.OVERWRITE, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("extended_label"));
        NodeContext<?, NodeState> node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);

        // The fully explicit case
        NodeContext<?, NodeState> a = node.get("a");
        Map<Locale, DescriptionState> aDesc = getDescriptionService().loadDescriptions(a.getId());
        assertNotNull(aDesc);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), aDesc.keySet());
        assertEquals(new DescriptionState("a_en", null), aDesc.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("a_fr", null), aDesc.get(Locale.FRENCH));
        assertNull(a.getState().getLabel());

        // No explicit language means to use the portal locale
        NodeContext<?, NodeState> b = node.get("b");
        Map<Locale, DescriptionState> bDesc = getDescriptionService().loadDescriptions(b.getId());
        assertNotNull(bDesc);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), bDesc.keySet());
        assertEquals(new DescriptionState("b_en", null), bDesc.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("b_fr", null), bDesc.get(Locale.FRENCH));
        assertNull(b.getState().getLabel());

        // The simple use case : one single label without the xml:lang attribute
        NodeContext<?, NodeState> c = node.get("c");
        Map<Locale, DescriptionState> cDesc = getDescriptionService().loadDescriptions(c.getId());
        assertNull(cDesc);
        assertEquals("c_en", c.getState().getLabel());

        // ----------------- Now test extended labels merge -----------------//
        src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"), node("c"))
                .build());
        fragment = src.getFragment();
        fragment.getNode("a").setLabels(
                new I18NString(new LocalizedString("a_it", Locale.ITALIAN), new LocalizedString("a_de", Locale.GERMAN)));
        fragment.getNode("b").setLabels(
                new I18NString(new LocalizedString("foo_b_en"), new LocalizedString("b_it", Locale.ITALIAN)));
        fragment.getNode("c").setLabels(new I18NString(new LocalizedString("foo_c_en")));
        src.setOwnerId("extended_label");

        importer = new NavigationImporter(Locale.ENGLISH, ImportMode.MERGE, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        ctx = getNavigationService().loadNavigation(SiteKey.portal("extended_label"));
        node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);

        // The fully explicit case
        a = node.get("a");
        aDesc = getDescriptionService().loadDescriptions(a.getId());
        assertNotNull(aDesc);
        assertEquals(Tools.toSet(Locale.ITALIAN, Locale.GERMAN), aDesc.keySet());
        assertEquals(new DescriptionState("a_it", null), aDesc.get(Locale.ITALIAN));
        assertEquals(new DescriptionState("a_de", null), aDesc.get(Locale.GERMAN));
        assertNull(a.getState().getLabel());

        // No explicit language means to use the portal locale
        b = node.get("b");
        bDesc = getDescriptionService().loadDescriptions(b.getId());
        assertNotNull(bDesc);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.ITALIAN), bDesc.keySet());
        assertEquals(new DescriptionState("foo_b_en", null), bDesc.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("b_it", null), bDesc.get(Locale.ITALIAN));
        assertNull(b.getState().getLabel());

        // The simple use case : one single label without the xml:lang attribute
        c = node.get("c");
        cDesc = getDescriptionService().loadDescriptions(c.getId());
        assertNull(cDesc);
        assertEquals("foo_c_en", c.getState().getLabel());

        // ----------------- Now test extended labels overwrite -----------------//
        src = new PageNavigation("portal", "extended_label").addFragment(fragment().add(node("a"), node("b"), node("c"))
                .build());
        fragment = src.getFragment();
        fragment.getNode("a")
                .setLabels(
                        new I18NString(new LocalizedString("bar_a_en", Locale.ENGLISH), new LocalizedString("bar_a_fr",
                                Locale.FRENCH)));
        fragment.getNode("b").setLabels(
                new I18NString(new LocalizedString("bar_b_en"), new LocalizedString("bar_b_fr", Locale.FRENCH)));
        fragment.getNode("c").setLabels(new I18NString(new LocalizedString("bar_c_en")));
        src.setOwnerId("extended_label");

        importer = new NavigationImporter(Locale.ENGLISH, ImportMode.OVERWRITE, src, getNavigationService(), getDescriptionService());
        importer.perform();

        //
        ctx = getNavigationService().loadNavigation(SiteKey.portal("extended_label"));
        node = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);

        // The fully explicit case
        a = node.get("a");
        aDesc = getDescriptionService().loadDescriptions(a.getId());
        assertNotNull(aDesc);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), aDesc.keySet());
        assertEquals(new DescriptionState("bar_a_en", null), aDesc.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("bar_a_fr", null), aDesc.get(Locale.FRENCH));
        assertNull(a.getState().getLabel());

        // No explicit language means to use the portal locale
        b = node.get("b");
        bDesc = getDescriptionService().loadDescriptions(b.getId());
        assertNotNull(bDesc);
        assertEquals(Tools.toSet(Locale.ENGLISH, Locale.FRENCH), bDesc.keySet());
        assertEquals(new DescriptionState("bar_b_en", null), bDesc.get(Locale.ENGLISH));
        assertEquals(new DescriptionState("bar_b_fr", null), bDesc.get(Locale.FRENCH));
        assertNull(b.getState().getLabel());

        // The simple use case : one single label without the xml:lang attribute
        c = node.get("c");
        cDesc = getDescriptionService().loadDescriptions(c.getId());
        assertNull(cDesc);
        assertEquals("bar_c_en", c.getState().getLabel());
    }

    public void testFullNavigation() {
        createSite(SiteType.PORTAL, "full_navigation");
        sync(true);

        //
        assertNull(getNavigationService().loadNavigation(SiteKey.portal("full_navigation")));

        //
        PageNavigation src = new PageNavigation("portal", "full_navigation").addFragment(fragment().add(node("a")).build());
        src.addFragment(fragment().add(node("b"), node("c")).build());
        src.addFragment(fragment("a").add(node("d")).build());

        //
        NavigationImporter importer = new NavigationImporter(Locale.ENGLISH, ImportMode.INSERT, src, getNavigationService(),
                getDescriptionService());
        importer.perform();

        //
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("full_navigation"));
        NodeContext<?, NodeState> root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertEquals(3, root.getNodeSize());
        Iterator<? extends NodeContext<?, NodeState>> i = root.iterator();
        NodeContext<?, NodeState> a = i.next();
        assertEquals("a", a.getName());
        assertEquals(1, a.getNodeSize());
        NodeContext<?, NodeState> d = a.get("d");
        assertNotNull(d);
        assertEquals(0, d.getNodeSize());
        NodeContext<?, NodeState> b = i.next();
        assertEquals("b", b.getName());
        assertEquals(0, b.getNodeSize());
        NodeContext<?, NodeState> c = i.next();
        assertEquals("c", c.getName());
        assertEquals(0, c.getNodeSize());
        assertFalse(i.hasNext());
    }
}

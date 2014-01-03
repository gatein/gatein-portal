/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.gatein.portal.importer;

import java.util.Arrays;
import java.util.Date;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;
import org.junit.Test;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/25/13
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-mop-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-reimport-configuration.xml")
})
public class TestModelImporter extends AbstractMopServiceTest {

    private MopModelImporter mopModelImporter;

    private static volatile boolean dataInjected = false;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mopModelImporter = (MopModelImporter) getContainer().getComponentInstanceOfType(MopModelImporter.class);
        mopModelImporter.wireServices(context);
    }

    @Test
    public void testDataImport() {
        injectData();

        mopModelImporter.doImport();

        //Check conserve import
        NavigationContext ctx = getNavigationService().loadNavigation(SiteKey.portal("conserveImport"));
        assertNotNull(ctx);
        NodeContext root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertNotNull(root.get("conserve_foo"));
        assertNotNull(root.get("conserve_foo").get("conserve_bar"));
        assertNull(root.get("home"));

        //Check insert import
        ctx = getNavigationService().loadNavigation(SiteKey.portal("insertImport"));
        assertNotNull(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertNotNull(root.get("insert_foo"));
        assertNotNull(root.get("insert_foo").get("insert_bar"));
        assertNotNull(root.get("home"));

        //Check merge import
        ctx = getNavigationService().loadNavigation(SiteKey.portal("mergeImport"));
        assertNotNull(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertNotNull(root.get("merge_foo"));
        assertNotNull(root.get("merge_foo").get("merge_bar"));
        assertNotNull(root.get("home"));
        assertNotNull(root.get("webexplorer"));

        //Check overwrite import
        ctx = getNavigationService().loadNavigation(SiteKey.portal("overwriteImport"));
        assertNotNull(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        assertNull(root.get("overwrite_foo"));
        assertNotNull(root.get("home"));
        assertNotNull(root.get("webexplorer"));
        //TODO: Check page data
    }

    private void injectData() {
        if (!dataInjected) {
            createSiteData();
            createPageData();
            createNavigationData();
            createImportedMixin();

            dataInjected = true;
        }
    }

    private void createSiteData() {
        createSite(SiteType.PORTAL, "conserveImport");
        createSite(SiteType.PORTAL, "insertImport");
        createSite(SiteType.PORTAL, "mergeImport");
        createSite(SiteType.PORTAL, "overwriteImport");
        sync(true);
    }

    private void createNavigationData() {
        NavigationContext ctx = new NavigationContext(SiteKey.portal("conserveImport"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        NodeContext root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(null, "conserve_foo", NodeState.INITIAL).add(null, "conserve_bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        ctx = new NavigationContext(SiteKey.portal("insertImport"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(null, "insert_foo", NodeState.INITIAL).add(null, "insert_bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        ctx = new NavigationContext(SiteKey.portal("mergeImport"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(null, "merge_foo", NodeState.INITIAL).add(null, "merge_bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        ctx = new NavigationContext(SiteKey.portal("overwriteImport"), new NavigationState(1));
        getNavigationService().saveNavigation(ctx);
        root = getNavigationService().loadNode(NodeState.model(), ctx, Scope.ALL, null);
        root.add(null, "overwrite_foo", NodeState.INITIAL).add(null, "overwrite_bar", NodeState.INITIAL);
        getNavigationService().saveNode(root, null);

        sync(true);
    }

    private void createPageData(String importMode) {
        PageState fooState = new PageState(
                importMode + "_foo",
                importMode + "_foo_description",
                true,
                importMode + "_foo_factory_id",
                Arrays.asList(importMode + "_foo_access_permission"),
                Arrays.asList(importMode + "_foo_edit_permission"));
        PageState homeState = new PageState(
                "Home Page",
                "Home Page descripton",
                true,
                "Default",
                Arrays.asList("Everyone"),
                Arrays.asList("*:/platform/administrators")
        );
        PageState webexState = new PageState(
                "Web Explorer Page",
                "Web Explorer Page descripton",
                true,
                "Default",
                Arrays.asList("Everyone"),
                Arrays.asList("*:/platform/administrators")
        );
        getPageService().savePage(new PageContext(new PageKey(SiteKey.portal(importMode + "Import"), importMode + "_foo"), fooState));
        getPageService().savePage(new PageContext(new PageKey(SiteKey.portal(importMode + "Import"), importMode + "_home"), homeState));
        getPageService().savePage(new PageContext(new PageKey(SiteKey.portal(importMode + "Import"), importMode + "_webexplorer"), webexState));
    }

    private void createPageData() {
        for (String importMode : new String[]{"conserve", "insert", "merge", "overwrite"}) {
            createPageData(importMode);
        }
    }

    private void createImportedMixin()
    {
        POMSession session = mopModelImporter.getPOMSessionManager().getSession();
        Workspace workspace = session.getWorkspace();
        Imported imported = workspace.adapt(Imported.class);
        imported.setCreationDate(new Date());
        imported.setStatus(Imported.Status.WANT_REIMPORT.status());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}

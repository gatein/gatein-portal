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

package org.exoplatform.portal.config;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedContainer;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.RestrictAccess;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov 13, 2007
 */
public class TestMOP extends AbstractConfigTest {

    /** . */
    private DataStorage storage;

    /** . */
    private PageService pageService;

    /** . */
    private POMSessionManager mgr;

    /** . */
    private POMSession session;

    /** . */
    private NavigationService navService;

    public TestMOP(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        begin();
        PortalContainer container = getContainer();
        storage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
        pageService = (PageService) container.getComponentInstanceOfType(PageService.class);
        mgr = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
        navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
        session = mgr.openSession();
    }

    protected void tearDown() throws Exception {
        session.close();
        end();
        super.tearDown();
    }

    public void testLoadLegacyGroupWithNormalizedName() throws Exception {
        SiteKey key = SiteKey.group("/test/legacy");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);
        NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
        NodeContext<?> node = root.get(0);
        assertEquals(SiteKey.group("/test/legacy").page("register"), node.getState().getPageRef());

        Page page = storage.getPage("group::/test/legacy::register");
        assertNotNull(page);
        assertEquals("group::/test/legacy::register", page.getPageId());
        assertEquals("/test/legacy", page.getOwnerId());
    }

    public void testLoadGroupWithNormalizedName() throws Exception {
        SiteKey key = SiteKey.group("/test/normalized");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);
        NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
        NodeContext<?> node = root.get(0);
        assertEquals(SiteKey.group("/test/normalized").page("register"), node.getState().getPageRef());

        Page page = storage.getPage("group::/test/normalized::register");
        assertNotNull(page);
        assertEquals("group::/test/normalized::register", page.getPageId());
        assertEquals("/test/normalized", page.getOwnerId());
    }

    public void testLoadNavigation() throws Exception {
        SiteKey key = SiteKey.portal("test");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);

        //
        assertEquals(1, (int) nav.getState().getPriority());

        //
        NodeContext<?> root = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
        assertEquals(5, root.getNodeCount());

        //
        NodeContext<?> nodeNavigation = root.get(0);
        assertEquals(0, nodeNavigation.getNodeCount());
        assertEquals("node_name", nodeNavigation.getName());
        assertEquals("node_label", nodeNavigation.getState().getLabel());
        assertEquals("node_icon", nodeNavigation.getState().getIcon());
        GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
        start.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(start.getTime().getTime(), nodeNavigation.getState().getStartPublicationTime());
        GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
        end.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(end.getTime().getTime(), nodeNavigation.getState().getEndPublicationTime());
        assertEquals(Visibility.TEMPORAL, nodeNavigation.getState().getVisibility());
    }

    public void testLoadPortal() throws Exception {
        PortalConfig testPortal = storage.getPortalConfig("test");
        assertNotNull(testPortal);

        assertEquals("test", testPortal.getName());
        assertEquals("en", testPortal.getLocale());
        assertArrayEquals(new String[] { "test_portal_access_permissions" }, testPortal.getAccessPermissions());
        assertEquals("test_edit_permission", testPortal.getEditPermission());
        assertEquals("test_skin", testPortal.getSkin());
        assertEquals("test_prop_value", testPortal.getProperty("prop_key"));
        assertNull(testPortal.getLabel());
        assertNull(testPortal.getDescription());


    }

    public void testLoadPageWithoutPageId() throws Exception {
        Page page = storage.getPage("portal::test::test2");
        assertNotNull(page);
        assertEquals("portal::test::test2", page.getPageId());
        assertEquals("test", page.getOwnerId());
        assertEquals("portal", page.getOwnerType());
        assertEquals("test2", page.getName());
    }

    public void testLoadPage() throws Exception {
        Page page = storage.getPage("portal::test::test1");
        assertNotNull(page);

        PageContext pageContext = pageService.loadPage(page.getPageKey());
        assertNotNull(pageContext);

        //
        assertEquals("test_title", pageContext.getState().getDisplayName());
        assertEquals("test_factory_id", pageContext.getState().getFactoryId());
        assertEquals(Arrays.<String> asList("test_access_permissions"), pageContext.getState().getAccessPermissions());
        assertEquals("test_edit_permission", pageContext.getState().getEditPermission());
        assertEquals(true, pageContext.getState().getShowMaxWindow());

        //
        List<ModelObject> children = page.getChildren();
        assertEquals(2, children.size());

        //
        Container container1 = (Container) children.get(0);
        assertEquals("container_1", container1.getName());
        assertEquals("container_1_title", container1.getTitle());
        assertEquals("container_1_icon", container1.getIcon());
        assertEquals("container_1_template", container1.getTemplate());
        assertArrayEquals(new String[] { "container_1_access_permissions" }, container1.getAccessPermissions());
        assertEquals("container_1_factory_id", container1.getFactoryId());
        assertEquals("container_1_description", container1.getDescription());
        assertEquals("container_1_width", container1.getWidth());
        assertEquals("container_1_height", container1.getHeight());

        //
        Application<?> application1 = (Application<?>) children.get(1);
        assertEquals("application_1_theme", application1.getTheme());
        assertEquals("application_1_title", application1.getTitle());
        assertArrayEquals(new String[] { "application_1_access_permissions" }, application1.getAccessPermissions());
        assertEquals(true, application1.getShowInfoBar());
        assertEquals(true, application1.getShowApplicationState());
        assertEquals(true, application1.getShowApplicationMode());
        assertEquals("application_1_description", application1.getDescription());
        assertEquals("application_1_icon", application1.getIcon());
        assertEquals("application_1_width", application1.getWidth());
        assertEquals("application_1_height", application1.getHeight());
        // assertEquals("portal#test:/web/BannerPortlet/banner", application1.getInstanceState().getWeakReference());
    }

    public void testSaveNavigation() throws Exception {
        Site portal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        assertNotNull(portal);

        //
        Navigation rootNavigation = portal.getRootNavigation();
        assertNotNull(rootNavigation);

        //
        Navigation defaultNav = rootNavigation.getChild("default");

        //
        Attributes defaultAttrs = defaultNav.getAttributes();
        assertEquals(1, (int) defaultAttrs.getInteger("priority"));

        //
        Collection<? extends Navigation> childrenNavigations = defaultNav.getChildren();
        assertNotNull(childrenNavigations);
        assertEquals(5, childrenNavigations.size());
        Iterator<? extends Navigation> i = childrenNavigations.iterator();

        //
        assertTrue(i.hasNext());
        Navigation nodeNavigation = i.next();
        assertNotNull(nodeNavigation);
        assertEquals(0, nodeNavigation.getChildren().size());
        assertEquals("node_name", nodeNavigation.getName());
        Described nodeDescribed = nodeNavigation.adapt(Described.class);
        assertEquals("node_label", nodeDescribed.getName());
        Attributes nodeAttrs = nodeNavigation.getAttributes();
        assertEquals("node_icon", nodeAttrs.getString("icon"));

        //
        assertTrue(nodeNavigation.isAdapted(Visible.class));
        assertTrue(nodeNavigation.isAdapted(RestrictAccess.class));
        RestrictAccess visible = nodeNavigation.adapt(RestrictAccess.class);
        GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
        start.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(start.getTime(), visible.getStartPublicationDate());
        GregorianCalendar end = new GregorianCalendar(2009, 2, 21, 1, 33, 0);
        end.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(end.getTime(), visible.getEndPublicationDate());
        assertEquals(Visibility.TEMPORAL, visible.getVisibility());

        //
        Link link = nodeNavigation.getLink();
        assertNotNull(link);
    }

    public void testSavePortal() throws Exception {
        Site testSite = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        assertNotNull(testSite);

        //
        assertTrue(testSite.isAdapted(ProtectedResource.class));
        ProtectedResource pr = testSite.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("test_portal_access_permissions"), pr.getAccessPermissions());
        assertEquals("test_edit_permission", pr.getEditPermission());

        //
        assertEquals("test", testSite.getName());
        Attributes attrs = testSite.getAttributes();
        assertEquals("en", attrs.getString("locale"));
        assertEquals("test_skin", attrs.getString("skin"));
        assertEquals("test_prop_value", attrs.getString("prop_key"));

        //
        org.gatein.mop.api.workspace.Page layout = testSite.getRootNavigation().getTemplatized().getTemplate();
        assertNotNull(layout);
        assertSame(testSite.getRootPage().getChild("templates").getChild("default"), layout);
    }

    public void testSavePageWithoutPageId() throws Exception {
        Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        org.gatein.mop.api.workspace.Page testRootPage = testPortal.getRootPage();
        org.gatein.mop.api.workspace.Page pages = testRootPage.getChild("pages");
        org.gatein.mop.api.workspace.Page testPage = pages.getChild("test2");
        assertNotNull(testPage);
    }

    public void testSavePage() throws Exception {
        Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        org.gatein.mop.api.workspace.Page testRootPage = testPortal.getRootPage();
        org.gatein.mop.api.workspace.Page pages = testRootPage.getChild("pages");
        org.gatein.mop.api.workspace.Page testPage = pages.getChild("test1");
        assertNotNull(testPage);

        //
        assertTrue(testPage.isAdapted(ProtectedResource.class));
        ProtectedResource pr = testPage.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("test_access_permissions"), pr.getAccessPermissions());
        assertEquals("test_edit_permission", pr.getEditPermission());

        //
        Described testPageDescribed = testPage.adapt(Described.class);
        assertEquals("test_title", testPageDescribed.getName());
        assertEquals(null, testPageDescribed.getDescription());

        //
        Attributes testPageAttrs = testPage.getAttributes();
        assertEquals("test_factory_id", testPageAttrs.getString("factory-id"));
        assertEquals(true, (boolean) testPageAttrs.getBoolean("show-max-window"));

        //
        UIContainer c = testPage.getRootComponent();
        assertNotNull(c);
        assertEquals(2, c.getComponents().size());
        Iterator<? extends UIComponent> it = c.getComponents().iterator();

        //
        UIContainer container1 = (UIContainer) it.next();
        assertTrue(container1.isAdapted(ProtectedResource.class));
        ProtectedResource container1PR = container1.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("container_1_access_permissions"), container1PR.getAccessPermissions());
        Described container1Described = container1.adapt(Described.class);
        assertEquals("container_1_title", container1Described.getName());
        assertEquals("container_1_description", container1Described.getDescription());
        Attributes container1Attrs = container1.getAttributes();
        assertEquals("container_1", container1Attrs.getString("name"));
        assertEquals("container_1_icon", container1Attrs.getString("icon"));
        assertEquals("container_1_template", container1Attrs.getString("template"));
        assertEquals("container_1_factory_id", container1Attrs.getString("factory-id"));
        assertEquals("container_1_width", container1Attrs.getString("width"));
        assertEquals("container_1_height", container1Attrs.getString("height"));

        //
        UIWindow application1 = (UIWindow) it.next();
        assertTrue(application1.isAdapted(ProtectedResource.class));
        ProtectedResource application1PR = application1.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("application_1_access_permissions"), application1PR.getAccessPermissions());
        Described application1Described = application1.adapt(Described.class);
        assertEquals("application_1_title", application1Described.getName());
        assertEquals("application_1_description", application1Described.getDescription());
        Attributes application1Attrs = application1.getAttributes();
        assertEquals("application_1_theme", application1Attrs.getString("theme"));
        assertEquals(true, (boolean) application1Attrs.getBoolean("showinfobar"));
        assertEquals(true, (boolean) application1Attrs.getBoolean("showmode"));
        assertEquals(true, (boolean) application1Attrs.getBoolean("showwindowstate"));
        assertEquals("application_1_icon", application1Attrs.getString("icon"));
        assertEquals("application_1_width", application1Attrs.getString("width"));
        assertEquals("application_1_height", application1Attrs.getString("height"));

        //
        Customization<?> customization = application1.getCustomization();
        assertNotNull(customization);
        assertEquals("application/portlet", customization.getType().getMimeType());
        assertEquals("web/BannerPortlet", customization.getContentId());
        // assertEquals("banner", customization.getName());
    }

    public void testRestrictedPortal() throws Exception {
        /* Portal */
        PortalConfig testPortal = storage.getPortalConfig("test");
        assertNotNull(testPortal);

        Container testLayout = testPortal.getPortalLayout();
        /* There are no move-apps-permissions or move-containers-permissions in the underlying portal.xml file
         * therefore the defaults defined in binding.xml should made effective on import */
        assertArrayEquals(new String[] {UserACL.EVERYONE}, testLayout.getMoveAppsPermissions());
        assertArrayEquals(new String[] {UserACL.EVERYONE}, testLayout.getMoveContainersPermissions());

        /* In classic/portal.xml, we have set explicit <move-apps-permissions>
         * and <move-containers-permissions> */
        PortalConfig classicPortal = storage.getPortalConfig("classic");
        assertNotNull(classicPortal);
        Container classicLayout = classicPortal.getPortalLayout();
        assertArrayEquals(new String[] {"classic-portal-move-apps-permissions"}, classicLayout.getMoveAppsPermissions());
        assertArrayEquals(new String[] {"classic-portal-move-containers-permissions"}, classicLayout.getMoveContainersPermissions());


        Site testSite = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        assertNotNull(testSite);
        org.gatein.mop.api.workspace.Page layout = testSite.getRootNavigation().getTemplatized().getTemplate();
        /* layoutRoot corresponds to <portal-layout> in test/portal.xml */
        UIContainer layoutRoot = layout.getRootComponent();
        /* There are no move-apps-permissions or move-containers-permissions in the underlying portal.xml file
         * therefore the defaults defined in binding.xml should made effective on import */
        assertTrue(layoutRoot.isAdapted(ProtectedContainer.class));
        ProtectedContainer pc = layoutRoot.adapt(ProtectedContainer.class);
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pc.getMoveAppsPermissions());
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pc.getMoveContainersPermissions());

        /* In classic/portal.xml, we have set explicit <move-apps-permissions>
         * and <move-containers-permissions> */
        Site classicSite = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "classic");
        assertNotNull(classicSite);
        UIContainer classicLayoutRoot = classicSite.getRootNavigation().getTemplatized().getTemplate().getRootComponent();
        assertTrue(classicLayoutRoot.isAdapted(ProtectedContainer.class));
        pc = classicLayoutRoot.adapt(ProtectedContainer.class);
        assertEquals(Collections.singletonList("classic-portal-move-apps-permissions"), pc.getMoveAppsPermissions());
        assertEquals(Collections.singletonList("classic-portal-move-containers-permissions"), pc.getMoveContainersPermissions());

    }

    public void testRestrictedPage() throws Exception {
        Page page = storage.getPage("portal::test::test1");
        assertNotNull(page);

        PageContext pageContext = pageService.loadPage(page.getPageKey());
        assertNotNull(pageContext);

        /* There are no move-apps-permissions or move-containers-permissions in the underlying pages.xml file
         * for test1 page. Therefore, the defaults defined in binding.xml should made effective on import */
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pageContext.getState().getMoveAppsPermissions());
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pageContext.getState().getMoveContainersPermissions());

        /* In classic/pages.xml, we have set explicit <move-apps-permissions>
         * and <move-containers-permissions> for add-component-test-page and some of its subcomponents */
        Page addComponentTestPage = storage.getPage("portal::classic::add-component-test-page");
        assertNotNull(addComponentTestPage);
        PageContext addComponentTestPageContext = pageService.loadPage(addComponentTestPage.getPageKey());
        assertEquals(
                Arrays.asList("*:/platform/page-move-apps-permissions-1", "*:/platform/page-move-apps-permissions-2"),
                addComponentTestPageContext.getState().getMoveAppsPermissions());
        assertEquals(
                Arrays.asList("*:/platform/page-move-containers-permissions-1", "*:/platform/page-move-containers-permissions-2"),
                addComponentTestPageContext.getState().getMoveContainersPermissions());

        ModelObject addComponentTestContainer = addComponentTestPage.getChildren().get(0);

        assertTrue(addComponentTestContainer instanceof Container);
        assertArrayEquals(
                new String[] {"*:/platform/container-move-apps-permissions-1", "*:/platform/container-move-apps-permissions-2"},
                ((Container) addComponentTestContainer).getMoveAppsPermissions());
        assertArrayEquals(
                new String[] {"*:/platform/container-move-containers-permissions-1", "*:/platform/container-move-containers-permissions-2"},
                ((Container) addComponentTestContainer).getMoveContainersPermissions());

        Site testPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "test");
        org.gatein.mop.api.workspace.Page testRootPage = testPortal.getRootPage();
        org.gatein.mop.api.workspace.Page pages = testRootPage.getChild("pages");
        org.gatein.mop.api.workspace.Page testPage = pages.getChild("test1");
        assertNotNull(testPage);

        //
        assertTrue(testPage.isAdapted(ProtectedResource.class));
        ProtectedResource pr = testPage.adapt(ProtectedResource.class);
        assertEquals(Collections.singletonList("test_access_permissions"), pr.getAccessPermissions());
        assertEquals("test_edit_permission", pr.getEditPermission());

        assertTrue(testPage.isAdapted(ProtectedContainer.class));
        ProtectedContainer pc = testPage.adapt(ProtectedContainer.class);
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pc.getMoveAppsPermissions());
        assertEquals(Collections.singletonList(UserACL.EVERYONE), pc.getMoveContainersPermissions());


        Site classicPortal = session.getWorkspace().getSite(ObjectType.PORTAL_SITE, "classic");
        org.gatein.mop.api.workspace.Page addComponentTestPageMop = classicPortal.getRootPage().getChild("pages").getChild("add-component-test-page");
        assertNotNull(addComponentTestPageMop);

        assertTrue(addComponentTestPageMop.isAdapted(ProtectedContainer.class));
        pc = addComponentTestPageMop.adapt(ProtectedContainer.class);
        assertEquals(Arrays.asList("*:/platform/page-move-apps-permissions-1",
                "*:/platform/page-move-apps-permissions-2"), pc.getMoveAppsPermissions());
        assertEquals(Arrays.asList("*:/platform/page-move-containers-permissions-1",
                "*:/platform/page-move-containers-permissions-2"), pc.getMoveContainersPermissions());

        UIComponent addComponentTestContainerMop = addComponentTestPageMop.getRootComponent().getComponents().get(0);
        assertTrue(addComponentTestContainerMop.isAdapted(ProtectedContainer.class));
        pc = addComponentTestContainerMop.adapt(ProtectedContainer.class);
        assertEquals(Arrays.asList("*:/platform/container-move-apps-permissions-1",
                "*:/platform/container-move-apps-permissions-2"), pc.getMoveAppsPermissions());
        assertEquals(Arrays.asList("*:/platform/container-move-containers-permissions-1",
                "*:/platform/container-move-containers-permissions-2"), pc.getMoveContainersPermissions());

    }
}

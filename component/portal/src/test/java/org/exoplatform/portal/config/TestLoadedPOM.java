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

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.portal.mop.site.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * Created by The eXo Platform SARL Author : Tung Pham thanhtungty@gmail.com Nov 13, 2007
 */
public class TestLoadedPOM extends AbstractConfigTest {

    /** . */
    private UserPortalConfigService portalConfigService;

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

    public TestLoadedPOM(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        begin();
        PortalContainer container = getContainer();
        portalConfigService = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
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

    public void testLegacyGroupWithNormalizedName() throws Exception {
        SiteKey key = SiteKey.group("/test/legacy");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);
        NodeContext<?, NodeState> root = navService.loadNode(NodeState.model(), nav, Scope.ALL, null);
        NodeContext<?, NodeState> node = root.get(0);
        assertEquals(SiteKey.group("/test/legacy").page("register"), node.getState().getPageRef());

        Page page = storage.getPage("group::/test/legacy::register");
        assertNotNull(page);
        assertEquals("group::/test/legacy::register", page.getPageId());
        assertEquals("/test/legacy", page.getOwnerId());
    }

    public void testGroupWithNormalizedName() throws Exception {
        SiteKey key = SiteKey.group("/test/normalized");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);
        NodeContext<?, NodeState> root = navService.loadNode(NodeState.model(), nav, Scope.ALL, null);
        NodeContext<?, NodeState> node = root.get(0);
        assertEquals(SiteKey.group("/test/normalized").page("register"), node.getState().getPageRef());

        Page page = storage.getPage("group::/test/normalized::register");
        assertNotNull(page);
        assertEquals("group::/test/normalized::register", page.getPageId());
        assertEquals("/test/normalized", page.getOwnerId());
    }

    public void testNavigation() throws Exception {
        SiteKey key = SiteKey.portal("test");
        NavigationContext nav = navService.loadNavigation(key);
        assertNotNull(nav);

        //
        assertEquals(1, (int) nav.getState().getPriority());

        //
        NodeContext<?, NodeState> root = navService.loadNode(NodeState.model(), nav, Scope.ALL, null);
        assertEquals(5, root.getNodeCount());

        //
        NodeContext<?, NodeState> nodeNavigation = root.get(0);
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

    public void testPortal() throws Exception {
        PortalConfig portal = storage.getPortalConfig("test");
        assertNotNull(portal);

        assertEquals("test", portal.getName());
        assertEquals("en", portal.getLocale());
        assertTrue(Arrays.equals(new String[] { "test_access_permissions" }, portal.getAccessPermissions()));
        assertTrue(Arrays.equals(new String[] {"test_edit_permission"}, portal.getEditPermissions()));
        assertEquals("test_skin", portal.getSkin());
        assertEquals("test_prop_value", portal.getProperty("prop_key"));
    }

    public void testPageWithoutPageId() throws Exception {
        Page page = storage.getPage("portal::test::test2");
        assertNotNull(page);
        assertEquals("portal::test::test2", page.getPageId());
        assertEquals("test", page.getOwnerId());
        assertEquals("portal", page.getOwnerType());
        assertEquals("test2", page.getName());
    }

    public void testPage() throws Exception {
        Page page = storage.getPage("portal::test::test1");
        assertNotNull(page);

        PageContext pageContext = pageService.loadPage(page.getPageKey());
        assertNotNull(pageContext);

        //
        assertEquals("test_title", pageContext.getState().getDisplayName());
        assertEquals("test_factory_id", pageContext.getState().getFactoryId());
        assertEquals(Arrays.<String> asList("test_access_permissions"), pageContext.getState().getAccessPermissions());
        assertEquals(Arrays.<String>asList("test_edit_permission"), pageContext.getState().getEditPermissions());
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
        assertTrue(Arrays.equals(new String[] { "container_1_access_permissions" }, container1.getAccessPermissions()));
        assertEquals("container_1_factory_id", container1.getFactoryId());
        assertEquals("container_1_description", container1.getDescription());
        assertEquals("container_1_width", container1.getWidth());
        assertEquals("container_1_height", container1.getHeight());

        //
        Application application1 = (Application) children.get(1);
        assertEquals("application_1_theme", application1.getTheme());
        assertEquals("application_1_title", application1.getTitle());
        assertTrue(Arrays.equals(new String[] { "application_1_access_permissions" }, application1.getAccessPermissions()));
        assertEquals(true, application1.getShowInfoBar());
        assertEquals(true, application1.getShowApplicationState());
        assertEquals(true, application1.getShowApplicationMode());
        assertEquals("application_1_description", application1.getDescription());
        assertEquals("application_1_icon", application1.getIcon());
        assertEquals("application_1_width", application1.getWidth());
        assertEquals("application_1_height", application1.getHeight());
        // assertEquals("portal#test:/web/BannerPortlet/banner", application1.getInstanceState().getWeakReference());
    }
}

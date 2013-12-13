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

package org.exoplatform.portal.mop.user;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.AssertionFailedError;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.AbstractPortalTest;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationServiceImpl;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMDataStorage;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.gatein.common.i18n.MapResourceBundle;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/portal/mop/user/configuration.xml") })
public class TestUserPortal extends AbstractPortalTest {

    /** . */
    private UserPortalConfigService userPortalConfigSer_;

    /** . */
    private OrganizationService orgService_;

    /** . */
    private DataStorage storage_;

    /** . */
    private POMSessionManager mgr;

    /** . */
    private Authenticator authenticator;

    /** . */
    private ListenerService listenerService;

    /** . */
    private LinkedList<Event> events;

    /** . */
    private boolean registered;

    /** . */
    private POMDataStorage mopStorage;

    public TestUserPortal(String name) {
        super(name);

        //
        registered = false;
    }

    @Override
    protected void setUp() throws Exception {
        Listener listener = new Listener() {
            @Override
            public void onEvent(Event event) throws Exception {
                events.add(event);
            }
        };

        PortalContainer container = getContainer();
        userPortalConfigSer_ = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
        orgService_ = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        mgr = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
        authenticator = (Authenticator) container.getComponentInstanceOfType(Authenticator.class);
        listenerService = (ListenerService) container.getComponentInstanceOfType(ListenerService.class);
        events = new LinkedList<Event>();
        storage_ = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
        mopStorage = (POMDataStorage) container.getComponentInstanceOfType(POMDataStorage.class);

        // Register only once for all unit tests
        if (!registered) {
            // I'm using this due to crappy design of
            // org.exoplatform.services.listener.ListenerService
            listenerService.addListener(DataStorage.PAGE_CREATED, listener);
            listenerService.addListener(DataStorage.PAGE_REMOVED, listener);
            listenerService.addListener(DataStorage.PAGE_UPDATED, listener);
        }
    }

    private static Map<SiteKey, UserNavigation> toMap(UserPortalConfig cfg) {
        return toMap(cfg.getUserPortal().getNavigations());
    }

    private static Map<SiteKey, UserNavigation> toMap(List<UserNavigation> navigations) {
        Map<SiteKey, UserNavigation> map = new HashMap<SiteKey, UserNavigation>();
        for (UserNavigation nav : navigations) {
            map.put(nav.getKey(), nav);
        }
        return map;
    }

    public void testRootGetUserPortalConfig() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                Map<SiteKey, UserNavigation> navigations = toMap(userPortalCfg);
                assertEquals(5, navigations.size());
                assertTrue(navigations.containsKey(SiteKey.portal("classic")));
                assertTrue(navigations.containsKey(SiteKey.user("root")));
                assertTrue(navigations.containsKey(SiteKey.group("/platform/administrators")));
                assertTrue(navigations.containsKey(SiteKey.group("/organization/management/executive-board")));
                assertTrue(navigations.containsKey(SiteKey.group("/organization/management/executive-board")));
                assertTrue(navigations.containsKey(SiteKey.group("/platform/users")));

                // Now try with the specific api
                UserNavigation rootNav = userPortalCfg.getUserPortal().getNavigation(SiteKey.user("root"));
                assertNotNull(rootNav);
                assertEquals(SiteKey.user("root"), rootNav.getKey());
            }
        }.execute("root");
    }

    public void testFilter() {
        UnitTest test = new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();
                UserNavigation nav = portal.getNavigation(SiteKey.portal("classic"));

                //
                UserNode root = portal.getNode(nav, Scope.ALL, UserNodeFilterConfig.builder().build(), null);
                assertNotNull(root.getChild("home"));
                assertNotNull(root.getChild("webexplorer"));
            }
        };

        //
        test.execute("root");
        test.execute();
    }

    public void testRefreshNavigations() {
        UnitTest test = new UnitTest() {
            public void doExecute() throws Exception {

                //
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();

                //
                NavigationServiceImpl service = new NavigationServiceImpl(mgr);
                SiteKey navKey = SiteKey.group("/organization/management");
                NavigationContext nav = new NavigationContext(navKey, new NavigationState(1));

                //
                NavigationContext got = service.loadNavigation(navKey);
                assertEquals(null, got);
                assertEquals(null, portal.getNavigation(navKey));

                //
                service.saveNavigation(nav);
                assertEquals(null, portal.getNavigation(navKey));
                portal.refresh();
                assertNotNull(portal.getNavigation(navKey));

            }
        };

        //
        test.execute("root");
    }

    public void testFilterWithVisibility() {
        class Test extends UnitTest {

            final int authorizationMode;
            final boolean guest;
            final boolean displayedGuest;
            final boolean systemGuest;
            final boolean systemUsers;
            final Visibility[] visibilities;

            Test(int authorizationMode, boolean guest, boolean displayedGuest, boolean systemGuest, boolean systemUsers) {
                this(authorizationMode, guest, displayedGuest, systemGuest, systemUsers, (Visibility[]) null);
            }

            Test(int authorizationMode, boolean guest, boolean displayedGuest, boolean systemGuest, boolean systemUsers,
                    Visibility... visibilities) {
                this.authorizationMode = authorizationMode;
                this.guest = guest;
                this.displayedGuest = displayedGuest;
                this.systemGuest = systemGuest;
                this.systemUsers = systemUsers;
                this.visibilities = visibilities;
            }

            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("system", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();
                UserNavigation nav = portal.getNavigation(SiteKey.portal("system"));

                //
                UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder().withAuthMode(authorizationMode);
                if (visibilities != null) {
                    builder.withVisibility(visibilities);
                }
                UserNodeFilterConfig config = builder.build();

                //
                UserNode root = portal.getNode(nav, Scope.ALL, config, null);
                assertEquals(guest, root.getChild("guest") != null);
                assertEquals(displayedGuest, root.getChild("displayed_guest") != null);
                assertEquals(systemGuest, root.getChild("system_guest") != null);
                assertEquals(systemUsers, root.getChild("system_users") != null);

                //
                assertEquals(guest, portal.resolvePath(nav, config, "guest") != null);
                assertEquals(displayedGuest, portal.resolvePath(nav, config, "displayed_guest") != null);
                assertEquals(systemGuest, portal.resolvePath(nav, config, "system_guest") != null);
                assertEquals(systemUsers, portal.resolvePath(nav, config, "system_users") != null);
            }
        }

        //
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true).execute();
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true).execute("root");
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, false, false, Visibility.DISPLAYED).execute();
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, false, false, Visibility.DISPLAYED).execute("root");
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, false, false, Visibility.DISPLAYED).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM).execute();
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("root");
        new Test(UserNodeFilterConfig.AUTH_NO_CHECK, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("demo");

        //
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, false).execute();
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, true).execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, true).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, false, false, Visibility.DISPLAYED).execute();
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, false, false, Visibility.DISPLAYED).execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, false, false, Visibility.DISPLAYED).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, false, Visibility.DISPLAYED, Visibility.SYSTEM).execute();
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("demo");

        //
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false).execute();
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, true, true).execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false, Visibility.DISPLAYED).execute();
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false, Visibility.DISPLAYED).execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false, Visibility.DISPLAYED).execute("demo");
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute();
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, true, true, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("root");
        new Test(UserNodeFilterConfig.AUTH_READ_WRITE, true, true, false, false, Visibility.DISPLAYED, Visibility.SYSTEM)
                .execute("demo");
    }

    public void testFilterWithAuthorizationCheck() {
        class Check extends UnitTest {

            /** . */
            boolean pass = true;

            @Override
            protected void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();
                UserNavigation nav = portal.getNavigation(SiteKey.group("/platform/administrators"));

                //
                UserNode root = portal.getNode(nav, Scope.ALL, UserNodeFilterConfig.builder().withReadWriteCheck().build(),
                        null);
                pass &= root.getChild("administration") != null;
                pass &= root.getChild("administration").getChild("communityManagement") != null;
            }
        }

        //
        Check root = new Check();
        root.execute("root");
        assertTrue(root.pass);

        //
        Check anon = new Check();
        anon.execute("john");
        assertFalse(anon.pass);
    }

    public void testFilterPropagation() {
        UnitTest test = new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("system", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();
                UserNavigation nav = portal.getNavigation(SiteKey.portal("system"));

                //
                UserNode root = portal.getNode(nav, Scope.SINGLE,
                        UserNodeFilterConfig.builder().withVisibility(Visibility.DISPLAYED).build(), null);
                assertFalse(root.hasChildrenRelationship());

                //
                portal.updateNode(root, Scope.ALL, null);
                assertTrue(root.hasChildrenRelationship());
                assertNotNull(root.getChild("guest"));
                assertNotNull(root.getChild("displayed_guest"));
                assertNull(root.getChild("system_users"));
            }
        };

        //
        test.execute("root");
    }

    public void testNavigationOrder() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                List<UserNavigation> navigations = userPortal.getNavigations();
                assertEquals("expected to have 5 navigations instead of " + navigations, 5, navigations.size());

                UserNavigation nav = navigations.get(0);
                assertEquals(1, nav.getPriority());
                assertEquals(SiteKey.portal("classic"), nav.getKey()); // 1

                nav = navigations.get(1);
                assertEquals(2, nav.getPriority());
                assertEquals(SiteKey.group("/platform/administrators"), nav.getKey()); // 2

                nav = navigations.get(2);
                assertEquals(5, nav.getPriority());
                assertEquals(SiteKey.user("root"), nav.getKey()); // 3

                //2 last navigations have the same priority -1, they always be at last of the list
                //and we don't control their order
                List<SiteKey> lastNav = new LinkedList<SiteKey>();
                lastNav.add(SiteKey.group("/organization/management/executive-board"));
                lastNav.add(SiteKey.group("/platform/users"));
                for (int i = 3; i < navigations.size(); i++) {
                    nav = navigations.get(i);
                    assertEquals(PageNavigation.UNDEFINED_PRIORITY, nav.getPriority());
                    lastNav.remove(nav.getKey());
                }
                assertTrue(lastNav.isEmpty());
            }
        }.execute("root");
    }

    public void testPathResolution() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();

                //
                UserNode nav = userPortal.resolvePath(null, "/");
                assertEquals(SiteKey.portal("classic"), nav.getNavigation().getKey());
                assertEquals("home", nav.getName());
                assertEquals("default", nav.getParent().getName());
                assertNull(nav.getParent().getParent());

                //
                nav = userPortal.resolvePath(null, "/foo");
                assertEquals(SiteKey.portal("classic"), nav.getNavigation().getKey());
                assertEquals("home", nav.getName());
                assertEquals("default", nav.getParent().getName());
                assertNull(nav.getParent().getParent());

                //
                nav = userPortal.resolvePath(null, "/home");
                assertEquals(SiteKey.portal("classic"), nav.getNavigation().getKey());
                assertEquals("home", nav.getName());
                assertEquals("default", nav.getParent().getName());
                assertNull(nav.getParent().getParent());

                //
                nav = userPortal.resolvePath(null, "/administration/communityManagement");
                assertEquals(SiteKey.group("/platform/administrators"), nav.getNavigation().getKey());
                assertEquals("communityManagement", nav.getName());
                assertEquals("administration", nav.getParent().getName());
                assertEquals("default", nav.getParent().getParent().getName());
                assertNull(nav.getParent().getParent().getParent());
            }
        }.execute("root");
    }

    public void testFindBestAvailablePath() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("limited", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();

                // Without authentication
                UserNode nav = userPortal.resolvePath(null, "/");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("foo", nav.getName());

                nav = userPortal.resolvePath(null, "/foo");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("foo", nav.getName());

                // With read auth
                UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
                builder.withReadCheck();
                UserNodeFilterConfig filterConfig = builder.build();

                nav = userPortal.resolvePath(filterConfig, "/");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("bar", nav.getName());

                nav = userPortal.resolvePath(filterConfig, "/foo");
                assertNull(nav);

                nav = userPortal.resolvePath(filterConfig, "/bit");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("bit", nav.getName());

                // With read and write auth
                builder = UserNodeFilterConfig.builder();
                builder.withReadWriteCheck();
                filterConfig = builder.build();

                nav = userPortal.resolvePath(filterConfig, "/");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("bit", nav.getName());

                nav = userPortal.resolvePath(filterConfig, "/foo");
                assertNull(nav);

                nav = userPortal.resolvePath(filterConfig, "/bar");
                assertNull(nav);

                nav = userPortal.resolvePath(filterConfig, "/bit");
                assertEquals(SiteKey.portal("limited"), nav.getNavigation().getKey());
                assertEquals("bit", nav.getName());
            }
        }.execute("demo");
    }

    public void testPathResolutionPerNavigation() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.group("/platform/administrators"));

                //
                UserNode path = userPortal.resolvePath(navigation, null, "/");
                assertNull(path);

                //
                path = userPortal.resolvePath(navigation, null, "/foo");
                assertNull(path);

                //
                path = userPortal.resolvePath(navigation, null, "/administration");
                assertNotNull(path);
                assertEquals("administration", path.getName());

                //
                path = userPortal.resolvePath(navigation, null, "/administration/communityManagement");
                assertNotNull(path);
                assertEquals("communityManagement", path.getName());
            }
        }.execute("root");
    }

    public void testDefaultNodeResolution() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("classic"));

                UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
                builder.withReadCheck();
                UserNodeFilterConfig filterConfig = builder.build();

                UserNode defaultNode = userPortal.getDefaultPath(navigation, filterConfig);
                assertEquals("home", defaultNode.getName());
            }
        }.execute("root");
    }

    public void testLabel() {
        new UnitTest() {
            public void doExecute() throws Exception {
                SimpleUserPortalContext ctx = new SimpleUserPortalContext(Locale.ENGLISH);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("portal.classic.home", "foo");
                map.put("portal.classic.emoh", "bar");
                ctx.add(SiteKey.portal("classic"), new MapResourceBundle(map));
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId(), ctx);
                UserPortal userPortal = userPortalCfg.getUserPortal();

                //
                UserNode path = userPortal.resolvePath(null, "/home");
                assertEquals("#{portal.classic.home}", path.getLabel());
                assertEquals("foo", path.getResolvedLabel());

                // Note that we don't save otherwise that may affect other tests
                // this is fine for this test I think
                path.setLabel("#{portal.classic.emoh}");
                assertEquals("bar", path.getResolvedLabel());
            }
        }.execute("root");
    }

    public void testExtendedLabel() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalContext ctx = new SimpleUserPortalContext(Locale.ENGLISH);
                UserPortal portal = userPortalConfigSer_.getUserPortalConfig("extended", getUserId(), ctx).getUserPortal();
                UserNode path = portal.resolvePath(null, "/bar");
                assertEquals(null, path.getLabel());
                assertEquals("bar_label_en", path.getResolvedLabel());

                // Now test transient node
                UserNode juu = path.addChild("juu");
                assertEquals(null, juu.getLabel());
                assertEquals("juu", juu.getResolvedLabel());

                //
                ctx = new SimpleUserPortalContext(Locale.FRENCH);
                portal = userPortalConfigSer_.getUserPortalConfig("extended", getUserId(), ctx).getUserPortal();
                path = portal.resolvePath(null, "/bar");
                assertEquals(null, path.getLabel());
                assertEquals("bar_label_fr", path.getResolvedLabel());
            }
        }.execute("root");
    }

    public void testLoadNode() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.group("/platform/administrators"));

                //
                UserNode root = userPortal.getNode(navigation, Scope.SINGLE, null, null);
                assertEquals("default", root.getName());
                assertEquals(1, root.getChildrenCount());
                assertEquals(0, root.getChildren().size());
                assertFalse(root.hasChildrenRelationship());

                //
                root = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                assertEquals("default", root.getName());
                assertEquals(1, root.getChildrenCount());
                assertEquals(1, root.getChildren().size());
                assertTrue(root.hasChildrenRelationship());
                Iterator<UserNode> children = root.getChildren().iterator();
                UserNode administration = children.next();
                assertEquals("administration", administration.getName());
                assertEquals(5, administration.getChildrenCount());
                assertEquals(0, administration.getChildren().size());
                assertFalse(administration.hasChildrenRelationship());

                //
                userPortal.updateNode(administration, Scope.CHILDREN, null);
                assertEquals("administration", administration.getName());
                assertEquals(5, administration.getChildrenCount());
                assertEquals(5, administration.getChildren().size());
                assertTrue(administration.hasChildrenRelationship());

                //
                UserNode registry = administration.getChildren().iterator().next();
                assertEquals("registry", registry.getName());
                assertEquals(0, registry.getChildrenCount());
                assertEquals(0, registry.getChildren().size());
                assertFalse(registry.hasChildrenRelationship());

                // I'm too lazy to check the remaining nodes...
            }
        }.execute("root");
    }

    public void testPublicationTime() {
        new UnitTest() {
            public void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("test", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("test"));

                UserNode root = userPortal.getNode(navigation, Scope.ALL, UserNodeFilterConfig.builder().withTemporalCheck()
                        .build(), null);
                GregorianCalendar start = new GregorianCalendar(2000, 2, 21, 1, 33, 0);
                start.setTimeZone(TimeZone.getTimeZone("UTC"));
                GregorianCalendar end = new GregorianCalendar(2050, 2, 21, 1, 33, 0);
                end.setTimeZone(TimeZone.getTimeZone("UTC"));

                assertEquals(3, root.getChildrenCount());

                UserNode node1 = root.getChild("node_name1");
                assertNotNull(node1);
                assertEquals(start.getTimeInMillis(), node1.getStartPublicationTime());
                assertEquals(end.getTimeInMillis(), node1.getEndPublicationTime());

                UserNode node2 = root.getChild("node_name3");
                assertNotNull(node2);
                assertEquals(-1, node2.getStartPublicationTime());
                assertEquals(end.getTimeInMillis(), node2.getEndPublicationTime());

                UserNode node3 = root.getChild("node_name4");
                assertNotNull(node3);
                assertEquals(-1, node3.getStartPublicationTime());
                assertEquals(-1, node3.getEndPublicationTime());
            }
        }.execute("root");
    }

    public void testSave() {
        new UnitTest() {
            public void doExecute() throws Exception {
                storage_.create(new PortalConfig("portal", "usernode_recursive"));
                end(true);

                //
                begin();
                Site site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "usernode_recursive");
                site.getRootNavigation().addChild("default");
                end(true);

                //
                begin();
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("usernode_recursive", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("usernode_recursive"));
                UserNode root = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                root.addChild("foo");
                userPortal.saveNode(root, null);
                end(true);

                //
                begin();
                root = userPortal.getNode(navigation, Scope.ALL, null, null);
                root.addChild("bar");
                root.getChild("foo").addChild("juu");
                userPortal.saveNode(root, null);
                end(true);

                //
                begin();
                userPortalCfg = userPortalConfigSer_.getUserPortalConfig("usernode_recursive", getUserId());
                userPortal = userPortalCfg.getUserPortal();
                navigation = userPortal.getNavigation(SiteKey.portal("usernode_recursive"));
                root = userPortal.getNode(navigation, Scope.ALL, null, null);
                assertNotNull(root.getChild("bar"));
                UserNode foo = root.getChild("foo");
                assertNotNull(foo.getChild("juu"));

                root.removeChild("foo");
                root.addChild("foo");
                userPortal.saveNode(root, null);
                end(true);

                begin();
                root = userPortal.getNode(navigation, Scope.ALL, null, null);
                foo = root.getChild("foo");
                assertNull(foo.getChild("juu"));
            }
        }.execute("root");
    }

    public void testInvalidateState() {
        new UnitTest() {
            public void doExecute() throws Exception {
                storage_.create(new PortalConfig("portal", "usernode_invalidate_uri"));
                end(true);

                //
                begin();
                Site site = mgr.getPOMService().getModel().getWorkspace()
                        .getSite(ObjectType.PORTAL_SITE, "usernode_invalidate_uri");
                site.getRootNavigation().addChild("default");
                end(true);

                //
                begin();
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("usernode_invalidate_uri",
                        getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("usernode_invalidate_uri"));
                UserNode root = userPortal.getNode(navigation, Scope.ALL, null, null);
                UserNode foo = root.addChild("foo");
                UserNode bar = root.addChild("bar");
                assertEquals("foo", foo.getURI());
                assertEquals("bar", bar.getURI());
                userPortal.saveNode(root, null);
                end(true);

                begin();
                // Move node --> change URI
                foo.addChild(bar);
                assertEquals("foo/bar", bar.getURI());

                // Rename node --> URI should be changed too
                bar.setName("bar2");
                assertEquals("foo/bar2", bar.getURI());

                userPortal.saveNode(bar, null);
                end(true);

                begin();
                UserNode root2 = userPortal.getNode(navigation, Scope.ALL, null, null);
                UserNode foo2 = root2.getChild("foo");
                foo2.setName("foo2");

                UserNode bar2 = foo2.getChild("bar2");
                root2.addChild(bar2);

                userPortal.saveNode(bar2, null);
                end(true);

                begin();

                // Changes from other session : foo has been renamed, and bar has been moved
                userPortal.updateNode(root, Scope.ALL, null);
                assertEquals("foo2", foo.getURI());
                assertEquals("bar2", bar.getURI());
            }
        }.execute("root");
    }

    public void testNodeExtension() {
        new UnitTest() {
            public void doExecute() throws Exception {
                storage_.create(new PortalConfig("portal", "node_extension"));
                end(true);

                //
                begin();
                Site site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "node_extension");
                site.getRootNavigation().addChild("default");
                end(true);

                begin();
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("node_extension", getUserId());
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("node_extension"));
                UserNode root1 = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                end(true);

                begin();
                UserNode root2 = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                UserNode foo2 = root2.addChild("foo");
                userPortal.saveNode(root2, null);
                end(true);

                begin();
                UserNode foo1 = root1.getChild("foo");
                assertNull(foo1);
                userPortal.updateNode(root1, Scope.GRANDCHILDREN, null);
                foo1 = root1.getChild("foo");
                assertNotNull(foo1);
                foo1.addChild("bar");
                userPortal.saveNode(root1, null);
                end(true);

                begin();
                UserNode bar2 = foo2.getChild("bar");
                assertNull(foo2.getChild("bar"));
                userPortal.updateNode(foo2, Scope.GRANDCHILDREN, null);
                bar2 = foo2.getChild("bar");
                assertNotNull(bar2);
                bar2.addChild("foo_bar");
                userPortal.saveNode(root2, null);
                end(true);

                begin();
                root1 = userPortal.getNode(navigation, Scope.ALL, null, null);
                UserNode bar1 = root1.getChild("foo").getChild("bar");
                assertNotNull(bar1);
                assertNotNull(bar1.getChild("foo_bar"));
            }
        }.execute("root");
    }

    public void testCacheInvalidation() {

        new UnitTest() {
            public void doExecute() throws Exception {
                storage_.create(new PortalConfig("portal", "cache_invalidation"));
                end(true);

                //
                begin();
                Site site = mgr.getPOMService().getModel().getWorkspace().getSite(ObjectType.PORTAL_SITE, "cache_invalidation");
                site.getRootNavigation().addChild("default");
                end(true);

                begin();
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("cache_invalidation", "root");
                UserPortal userPortal = userPortalCfg.getUserPortal();
                UserNavigation navigation = userPortal.getNavigation(SiteKey.portal("cache_invalidation"));
                UserNode root = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                root.addChild("foo");

                userPortal.saveNode(root, null);
                root = userPortal.getNode(navigation, Scope.CHILDREN, null, null);
                assertNotNull(root.getChild("foo")); // should Cache be invalidated right after save()
            }
        }.execute("root");
    }

    public void testInfiniteLoop() {
        new UnitTest() {
            @Override
            protected void doExecute() throws Exception {
                UserPortalConfig userPortalCfg = userPortalConfigSer_.getUserPortalConfig("classic", getUserId());
                UserPortal portal = userPortalCfg.getUserPortal();
                UserNavigation nav = portal.getNavigation(SiteKey.group("/platform/administrators"));

                //
                UserNode root = portal.getNode(nav, Scope.GRANDCHILDREN, null, null);
                portal.updateNode(root, Scope.GRANDCHILDREN, null); // Re-update the root node
                Collection<UserNode> children = root.getChildren();
                int level = 0;
                for (UserNode child : children) {
                    println(child, level);
                }
            }

            private void println(UserNode node, int level) {
                Collection<UserNode> children = node.getChildren();
                UserNode temp = null;
                Iterator<UserNode> it = children.iterator();
                while (it.hasNext()) {
                    UserNode child = it.next();
                    if (child == temp) {
                        child = it.next();
                        fail("There is infinite loop");
                    }
                    temp = child;
                    println(child, level + 1);
                }
            }
        }.execute("root");
    }

    private abstract class UnitTest {

        /** . */
        private String userId;

        protected final void execute() {
            execute(null);
        }

        protected final void execute(String userId) {
            Throwable failure = null;

            //
            begin();

            //
            ConversationState conversationState = null;
            if (userId != null) {
                try {
                    conversationState = new ConversationState(authenticator.createIdentity(userId));
                } catch (Exception e) {
                    failure = e;
                }
            }

            //
            if (failure == null) {
                // Clear cache for test
                mgr.clearCache();

                //
                this.userId = userId;
                ConversationState.setCurrent(conversationState);
                try {
                    doExecute();
                } catch (Exception e) {
                    failure = e;
                    log.error("Test failed", e);
                } finally {
                    this.userId = null;
                    ConversationState.setCurrent(null);
                    end();
                }
            }

            // Report error as a junit assertion failure
            if (failure != null) {
                AssertionFailedError err = new AssertionFailedError();
                err.initCause(failure);
                throw err;
            }
        }

        public final String getUserId() {
            return userId;
        }

        protected abstract void doExecute() throws Exception;

    }
}

package org.exoplatform.portal.mop.page;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.AbstractMOPTest;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.mop.api.workspace.ObjectType;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/portal/mop/page/configuration.xml") })
public class TestPageServiceWrapper extends AbstractMOPTest {

    /** . */
    private ListenerService listenerService;

    /** . */
    private POMSessionManager mgr;

    /** . */
    protected PageService serviceWrapper;

    @Override
    protected void setUp() throws Exception {
        PortalContainer container = getContainer();

        //
        serviceWrapper = (PageService) container.getComponentInstanceOfType(PageService.class);
        listenerService = (ListenerService) container.getComponentInstanceOfType(ListenerService.class);
        mgr = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);

        //
        super.setUp();
    }

    public void testInitialization() {
        PageContext page = serviceWrapper.loadPage(SiteKey.portal("classic").page("homepage"));
        assertNotNull(page);

        PageState state = page.getState();
        assertEquals("Home Page", state.getDisplayName());
        assertEquals(Arrays.asList("Everyone"), state.getAccessPermissions());
        assertEquals("*:/platform/administrators", state.getEditPermission());
        assertNull(state.getFactoryId());
        assertFalse(state.getShowMaxWindow());
    }

    public void testNotification() {
        class ListenerImpl extends Listener<PageService, PageKey> {

            /** . */
            private final LinkedList<Event<PageService, PageKey>> events = new LinkedList<Event<PageService, PageKey>>();

            @Override
            public void onEvent(Event<PageService, PageKey> event) throws Exception {
                events.addLast(event);
            }
        }

        //
        ListenerImpl createListener = new ListenerImpl();
        ListenerImpl updateListener = new ListenerImpl();
        ListenerImpl destroyListener = new ListenerImpl();

        //
        listenerService.addListener(EventType.PAGE_CREATED, createListener);
        listenerService.addListener(EventType.PAGE_UPDATED, updateListener);
        listenerService.addListener(EventType.PAGE_DESTROYED, destroyListener);

        //
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "notification").getRootPage()
                .addChild("pages");
        sync(true);

        //
        PageKey key = SiteKey.portal("notification").page("home");

        // Create
        PageContext page = new PageContext(key, new PageState("home", "description", true, null,
                Collections.singletonList("foo"), "bar", Collections.singletonList("moveAppsPermissions"),
                Collections.singletonList("moveContainersPermissions")));
        assertTrue(serviceWrapper.savePage(page));
        assertEquals(1, createListener.events.size());
        assertEquals(0, updateListener.events.size());
        assertEquals(0, destroyListener.events.size());

        // Update
        page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), "bar", Collections
                .singletonList("moveAppsPermissions"), Collections.singletonList("moveContainersPermissions")));
        assertFalse(serviceWrapper.savePage(page));
        assertEquals(1, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        assertEquals(0, destroyListener.events.size());

        // Destroy
        page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), "bar", Collections
                .singletonList("moveAppsPermissions"), Collections.singletonList("moveContainersPermissions")));
        assertTrue(serviceWrapper.destroyPage(key));
        assertEquals(1, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        assertEquals(1, destroyListener.events.size());
    }

    public void testDataStorageSynchronization() throws Exception {
        // Create a page *foo*
        mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "datastorage_sync").getRootPage()
                .addChild("pages").addChild("foo");
        sync(true);

        PageKey fooKey = SiteKey.portal("datastorage_sync").page("foo");

        // Check the existence of the page and its layout
        DataStorage storage = (DataStorage) getContainer().getComponentInstanceOfType(DataStorage.class);
        Page page = storage.getPage(fooKey.format());
        assertNotNull(page);
        assertEquals("foo", page.getName());
        assertEquals(Collections.EMPTY_LIST, page.getChildren());
        assertNotNull(serviceWrapper.loadPage(fooKey));

        // Delete page
        assertTrue(serviceWrapper.destroyPage(fooKey));
        assertNull(storage.getPage(fooKey.format()));
        assertNull(serviceWrapper.loadPage(fooKey));
        sync(true);

        // Check for subsequence actions
        assertNull(storage.getPage(fooKey.format()));
    }
}

package org.exoplatform.portal.mop.page;

import java.util.Collections;
import java.util.LinkedList;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.mop.PersistenceContext;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.portal.mop.site.SiteType;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPageServiceWrapper extends AbstractMopServiceTest {

    /** . */
    private ListenerService listenerService;

    /** . */
    protected PageService serviceWrapper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        PortalContainer container = getContainer();

        //
        POMSessionManager mgr = ((PersistenceContext.JCR)context).getManager();

        //
        listenerService = new ListenerService(container.getContext());
        serviceWrapper = new PageServiceWrapper(
                mgr.getRepositoryService(),
                mgr,
                listenerService);
    }

    public void testNotification() {
        class ListenerImpl extends Listener<PageService, PageKey> {

            /** . */
            private final LinkedList<Event> events = new LinkedList<Event>();

            @Override
            public void onEvent(Event event) throws Exception {
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
        createSite(SiteType.PORTAL, "notification");
        sync(true);

        //
        PageKey key = SiteKey.portal("notification").page("home");

        // Create
        PageContext page = new PageContext(key, new PageState("home", "description", true, null,
                Collections.singletonList("foo"), Collections.singletonList("bar")));
        assertTrue(serviceWrapper.savePage(page));
        assertEquals(1, createListener.events.size());
        assertEquals(0, updateListener.events.size());
        assertEquals(0, destroyListener.events.size());

        // Update
        page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), Collections.singletonList("bar")));
        assertFalse(serviceWrapper.savePage(page));
        assertEquals(1, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        assertEquals(0, destroyListener.events.size());

        // Destroy
        page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), Collections.singletonList("bar")));
        assertTrue(serviceWrapper.destroyPage(key));
        assertEquals(1, createListener.events.size());
        assertEquals(1, updateListener.events.size());
        assertEquals(1, destroyListener.events.size());
    }
}

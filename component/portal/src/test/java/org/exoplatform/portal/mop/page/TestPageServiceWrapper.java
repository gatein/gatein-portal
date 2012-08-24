package org.exoplatform.portal.mop.page;

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

import java.util.Collections;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/portal/mop/page/configuration.xml")
})
public class TestPageServiceWrapper extends AbstractMOPTest
{

   /** . */
   private ListenerService listenerService;

   /** . */
   private POMSessionManager mgr;

   /** . */
   protected PageService serviceWrapper;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer container = getContainer();

      //
      serviceWrapper = (PageService)container.getComponentInstanceOfType(PageService.class);
      listenerService = (ListenerService)container.getComponentInstanceOfType(ListenerService.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);

      //
      super.setUp();
   }

   public void testNotification()
   {
      class ListenerImpl extends Listener<PageService, PageKey>
      {

         /** . */
         private final LinkedList<Event> events = new LinkedList<Event>();

         @Override
         public void onEvent(Event event) throws Exception
         {
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
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "notification").getRootPage().addChild("pages");
      sync(true);

      //
      PageKey key = SiteKey.portal("notification").page("home");

      // Create
      PageContext page = new PageContext(key, new PageState(
         "home",
         "description",
         true,
         null,
         Collections.singletonList("foo"),
         "bar"
      ));
      assertTrue(serviceWrapper.savePage(page));
      assertEquals(1, createListener.events.size());
      assertEquals(0, updateListener.events.size());
      assertEquals(0, destroyListener.events.size());

      // Update
      page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), "bar"));
      assertFalse(serviceWrapper.savePage(page));
      assertEquals(1, createListener.events.size());
      assertEquals(1, updateListener.events.size());
      assertEquals(0, destroyListener.events.size());

      // Destroy
      page.setState(new PageState("home2", "description2", false, null, Collections.singletonList("foo"), "bar"));
      assertTrue(serviceWrapper.destroyPage(key));
      assertEquals(1, createListener.events.size());
      assertEquals(1, updateListener.events.size());
      assertEquals(1, destroyListener.events.size());
   }

   public void testDataStorageSynchronization() throws Exception
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "datastorage_sync").getRootPage().addChild("pages");
      sync(true);

      //
      DataStorage storage = (DataStorage)getContainer().getComponentInstanceOfType(DataStorage.class);
      Page fooPage = new Page("portal", "datastorage_sync", "foo");
      fooPage.setTitle("foo_name");
      storage.save(fooPage);
      sync(true);

      // Force cache loading
      fooPage = storage.getPage("portal::datastorage_sync::foo");
      assertEquals("foo_name", fooPage.getTitle());

      // Save
      PageKey fooKey = SiteKey.portal("datastorage_sync").page("foo");
      PageContext foo = serviceWrapper.loadPage(fooKey);
      PageState fooState = foo.getState();
      assertEquals("foo_name", fooState.getName());
      foo.setState(fooState.builder().name("foo_name_2").build());
      assertFalse(serviceWrapper.savePage(foo));

      // Check cache was invalidated
      fooPage = storage.getPage("portal::datastorage_sync::foo");
      assertEquals("foo_name_2", fooPage.getTitle());
      sync(true);

      // Check cache remains invalidated after synchronization
      fooPage = storage.getPage("portal::datastorage_sync::foo");
      assertEquals("foo_name_2", fooPage.getTitle());

      // Delete
      assertTrue(serviceWrapper.destroyPage(fooKey));
      fooPage = storage.getPage("portal::datastorage_sync::foo");
      assertNull(fooPage);
      sync(true);

      // Check cache remains invalidated after synchronization
      fooPage = storage.getPage("portal::datastorage_sync::foo");
      assertNull(fooPage);
   }
}

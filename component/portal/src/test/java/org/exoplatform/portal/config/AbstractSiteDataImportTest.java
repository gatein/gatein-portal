package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public abstract class AbstractSiteDataImportTest extends AbstractDataImportTest
{

   @Override
   protected final String getConfig1()
   {
      return "site1";
   }
   
   @Override
   protected final String getConfig2()
   {
      return "site2";
   }
   
   @Override
   protected final void afterOneBootWithExtention(PortalContainer container) throws Exception
   {
      RequestLifeCycle.begin(container);
      
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      
      // Test portal
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      PortalConfig portal = dataStorage.getPortalConfig("classic");
      Container layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      Application<Portlet> layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site2/layout", dataStorage.getId(layoutPortlet.getState()));
      
      Page page = dataStorage.getPage("portal::classic::home");
      assertNotNull(page);
      assertEquals("site 1", page.getTitle());
      
      page = dataStorage.getPage("portal::classic::page1");
      assertNotNull(page);
      assertEquals("site 2", page.getTitle());
      
      page = dataStorage.getPage("portal::classic::page2");
      assertNotNull(page);
      assertEquals("site 2", page.getTitle());

      // Test group
      portal = dataStorage.getPortalConfig(SiteType.GROUP.getName(), "/platform/administrators");
      layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site1/layout", dataStorage.getId(layoutPortlet.getState()));
      
      page = dataStorage.getPage("group::/platform/administrators::page1");
      assertNotNull(page);
      assertEquals("site 2", page.getTitle());
      
      // Test user
      Page dashboard1 = dataStorage.getPage("user::root::dashboard1");
      assertNotNull(dashboard1);
      assertEquals("site 2", dashboard1.getTitle());
      
      RequestLifeCycle.end();
   }
   
   @Override
   protected final void afterFirstBoot(PortalContainer container) throws Exception
   {
      RequestLifeCycle.begin(container);

      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      
      // Test portal
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      PortalConfig portal = dataStorage.getPortalConfig("classic");
      Container layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      Application<Portlet> layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site1/layout", dataStorage.getId(layoutPortlet.getState()));
      
      Page page = dataStorage.getPage("portal::classic::home");
      assertNotNull(page);
      assertEquals("site 1", page.getTitle());
      
      page = dataStorage.getPage("portal::classic::page1");
      assertNotNull(page);
      assertEquals("site 1", page.getTitle());
      
      page = dataStorage.getPage("portal::classic::page2");
      assertNull(page);

      // Test group
      portal = dataStorage.getPortalConfig(SiteType.GROUP.getName(), "/platform/administrators");
      layout = portal.getPortalLayout();
      assertEquals(1, layout.getChildren().size());
      layoutPortlet = (Application<Portlet>)layout.getChildren().get(0);
      assertEquals("site1/layout", dataStorage.getId(layoutPortlet.getState()));
      
      page = dataStorage.getPage("group::/platform/administrators::page1");
      assertNull(page);
      
      // Test user
      Page dashboard1 = dataStorage.getPage("user::root::dashboard1");
      assertNull(dashboard1);
      
      RequestLifeCycle.end();
   }
   
   @Override
   protected final void afterSecondBoot(PortalContainer container) throws Exception
   {
      afterFirstBoot(container);
   }
   
   @Override
   protected void afterSecondBootWithWantReimport(PortalContainer container) throws Exception
   {
      afterSecondBootWithOverride(container);
   }
   
   @Override
   protected final void afterSecondBootWithNoMixin(PortalContainer container) throws Exception
   {
      afterSecondBoot(container);
   }
}

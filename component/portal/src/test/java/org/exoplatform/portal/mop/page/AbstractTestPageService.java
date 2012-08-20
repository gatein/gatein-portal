package org.exoplatform.portal.mop.page;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.AbstractMOPTest;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-mop-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/portal/mop/page/configuration.xml")
})
public class AbstractTestPageService extends AbstractMOPTest
{

   /** . */
   static final SiteKey CLASSIC = SiteKey.portal("classic");

   /** . */
   static final PageKey CLASSIC_HOMEPAGE = CLASSIC.page("homepage");

   /** . */
   static final PageKey CLASSIC_FOO = CLASSIC.page("foo");

   /** . */
   protected POMSessionManager mgr;

   /** . */
   protected PageServiceImpl service;

   /** . */
   protected DataStorage dataStorage;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = PortalContainer.getInstance();
      mgr = new POMSessionManager(
         (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class),
         (ChromatticManager)container.getComponentInstanceOfType(ChromatticManager.class),
         (CacheService)container.getComponentInstanceOfType(CacheService.class)
      );
      mgr.start();

      //
      service = new PageServiceImpl(mgr);

      //
      begin();
   }
}

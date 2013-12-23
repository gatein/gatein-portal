/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.description.SimpleDataCache;
import org.exoplatform.portal.mop.site.MopStore;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.gatein.portal.impl.mop.mongo.MongoStore;
import org.gatein.portal.impl.mop.ram.RamCustomizationStore;
import org.gatein.portal.impl.mop.ram.RamDescriptionStore;
import org.gatein.portal.impl.mop.ram.RamLayoutStore;
import org.gatein.portal.impl.mop.ram.RamNavigationStore;
import org.gatein.portal.impl.mop.ram.RamPageStore;
import org.gatein.portal.impl.mop.ram.RamSecurityStore;
import org.gatein.portal.impl.mop.ram.RamSiteStore;
import org.gatein.portal.impl.mop.ram.RamStore;
import org.gatein.portal.impl.mop.ram.Tx;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.customization.CustomizationServiceImpl;
import org.gatein.portal.mop.customization.CustomizationStore;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.description.DescriptionServiceImpl;
import org.gatein.portal.mop.description.DescriptionStore;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.layout.LayoutServiceImpl;
import org.gatein.portal.mop.layout.LayoutStore;
import org.gatein.portal.mop.navigation.NavigationServiceImpl;
import org.gatein.portal.mop.navigation.NavigationStore;
import org.gatein.portal.mop.page.PageServiceImpl;
import org.gatein.portal.mop.page.PageStore;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.mop.permission.SecurityServiceImpl;
import org.gatein.portal.mop.permission.SecurityStore;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.mop.site.SiteServiceImpl;
import org.gatein.portal.mop.site.SiteStore;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class PersistenceContext {

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public abstract boolean isSessionModified();

    public abstract CustomizationService getCustomizationService();

    public abstract NavigationServiceImpl getNavigationService();

    public abstract DescriptionService getDescriptionService();
    
    public abstract SecurityService getSecurityService();

    public abstract PageServiceImpl getPageService();

    public abstract LayoutService getLayoutService();

    public abstract SiteService getSiteService();

    public abstract NavigationStore getNavigationStore();

    public abstract DescriptionStore getDescriptionStore();
    
    public abstract SecurityStore getSecurityStore();

    public abstract PageStore getPageStore();

    public abstract SiteStore getSiteStore();

    public abstract LayoutStore getLayoutStore();

    public abstract void begin();

    public abstract void end(boolean save);

    public abstract boolean assertSessionNotModified();

    public abstract boolean assertSessionModified();

    public static class JCR extends PersistenceContext {

        /** . */
        private org.exoplatform.portal.mop.customization.MopStore customizationStore;

        /** . */
        private CustomizationService customizationService;

        /** . */
        private org.exoplatform.portal.mop.navigation.MopStore navigationStore;

        /** . */
        private NavigationServiceImpl navigationService;

        /** . */
        private PageServiceImpl pageService;

        /** . */
        private PageStore pageStore;

        /** . */
        private LayoutService layoutService;

        /** . */
        private POMSessionManager mgr;

        /** . */
        private DescriptionService descriptionService;
        
        /** . */
        private SecurityService securityService;

        /** . */
        private SiteStore siteStore;

        /** . */
        private SiteService siteService;

        /** . */
        private LayoutStore layoutStore;

        /** . */
        private org.exoplatform.portal.mop.description.MopStore descriptionStore;
        
        /** . */
        private SecurityStore securityStore;

        @Override
        protected void setUp() {
            PortalContainer container = PortalContainer.getInstance();

            //
            mgr = new POMSessionManager(
                    (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class),
                    (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class),
                    (CacheService) container.getComponentInstanceOfType(CacheService.class));
            mgr.start();

            //
            customizationStore = new org.exoplatform.portal.mop.customization.MopStore(mgr);
            customizationService = new CustomizationServiceImpl(customizationStore);
            navigationStore = new org.exoplatform.portal.mop.navigation.MopStore(mgr);
            navigationService = new NavigationServiceImpl(navigationStore);
            descriptionStore = new org.exoplatform.portal.mop.description.MopStore(mgr, new SimpleDataCache());
            descriptionService = new DescriptionServiceImpl(descriptionStore);
            securityStore = new org.exoplatform.portal.mop.permission.MopStore(mgr);
            securityService = new SecurityServiceImpl(securityStore);
            siteStore = new MopStore(mgr, new org.exoplatform.portal.mop.site.SimpleDataCache());
            siteService = new SiteServiceImpl(siteStore);
            pageStore = new org.exoplatform.portal.mop.page.MopStore(mgr, new org.exoplatform.portal.mop.page.SimpleDataCache());
            pageService = new PageServiceImpl(pageStore);
            layoutStore = new org.exoplatform.portal.mop.layout.MopStore(mgr);
            layoutService = new LayoutServiceImpl(layoutStore);

            // Clear the cache for each test
            navigationService.clearCache();
        }

        public POMSessionManager getManager() {
            return mgr;
        }

        @Override
        public CustomizationService getCustomizationService() {
            return customizationService;
        }

        @Override
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PageStore getPageStore() {
            return pageStore;
        }

        @Override
        public DescriptionStore getDescriptionStore() {
            return descriptionStore;
        }
        
        @Override
        public SecurityStore getSecurityStore() {
            return securityStore;
        }

        @Override
        public NavigationServiceImpl getNavigationService() {
            return navigationService;
        }

        @Override
        public DescriptionService getDescriptionService() {
            return descriptionService;
        }
        
        @Override
        public SecurityService getSecurityService() {
            return securityService;
        }

        @Override
        public LayoutService getLayoutService() {
            return layoutService;
        }

        @Override
        public SiteService getSiteService() {
            return siteService;
        }

        @Override
        public NavigationStore getNavigationStore() {
            return navigationStore;
        }

        @Override
        public SiteStore getSiteStore() {
            return siteStore;
        }

        @Override
        public LayoutStore getLayoutStore() {
            return layoutStore;
        }

        @Override
        public boolean isSessionModified() {
            return mgr.getSession().isModified();
        }

        @Override
        public void begin() {
        }

        @Override
        public void end(boolean save) {
        }

        @Override
        public boolean assertSessionNotModified() {
            return !mgr.getSession().isModified();
        }

        @Override
        public boolean assertSessionModified() {
            return mgr.getSession().isModified();
        }
    }

    public static class Ram extends PersistenceContext {

        /** . */
        CustomizationServiceImpl customizationService;

        /** . */
        NavigationServiceImpl navigationService;

        /** . */
        PageServiceImpl pageService;

        /** . */
        RamStore store;

        /** . */
        DescriptionServiceImpl descriptionService;
        
        /** . */
        SecurityServiceImpl securityService;

        /** . */
        LayoutServiceImpl layoutService;

        /** . */
        SiteServiceImpl siteService;

        /** . */
        CustomizationStore customizationStore;

        /** . */
        LayoutStore layoutStore;

        /** . */
        RamDescriptionStore descriptionStore;
        
        /** . */
        RamSecurityStore securityStore;

        /** . */
        RamPageStore pageStore;

        /** . */
        RamSiteStore siteStore;

        /** . */
        RamNavigationStore navigationStore;

        @Override
        protected void setUp() {
            store = new RamStore();
            navigationService = new NavigationServiceImpl(navigationStore = new RamNavigationStore(store));
            pageService = new PageServiceImpl(pageStore = new RamPageStore(store));
            descriptionService = new DescriptionServiceImpl(descriptionStore = new RamDescriptionStore(store));
            securityService = new SecurityServiceImpl(securityStore = new RamSecurityStore(store));
            layoutService = new LayoutServiceImpl(layoutStore = new RamLayoutStore(store));
            customizationService = new CustomizationServiceImpl(customizationStore = new RamCustomizationStore(store));
            siteService = new SiteServiceImpl(siteStore = new RamSiteStore(store));
        }

        @Override
        public void begin() {
            Tx.begin();
        }

        @Override
        public void end(boolean save) {
            Tx.end(save);
        }

        @Override
        public boolean isSessionModified() {
            return true;
        }

        @Override
        public CustomizationService getCustomizationService() {
            return customizationService;
        }

        @Override
        public NavigationServiceImpl getNavigationService() {
            return navigationService;
        }

        @Override
        public LayoutStore getLayoutStore() {
            return new RamLayoutStore(store);
        }

        @Override
        public LayoutService getLayoutService() {
            return layoutService;
        }

        @Override
        public DescriptionService getDescriptionService() {
            return descriptionService;
        }

        @Override
        public DescriptionStore getDescriptionStore() {
            return descriptionStore;
        }
        
        @Override
        public SecurityService getSecurityService() {
            return securityService;
        }

        @Override
        public SecurityStore getSecurityStore() {
            return securityStore;
        }

        @Override
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PageStore getPageStore() {
            return pageStore;
        }

        @Override
        public NavigationStore getNavigationStore() {
            return navigationStore;
        }

        @Override
        public SiteStore getSiteStore() {
            return siteStore;
        }

        @Override
        public SiteService getSiteService() {
            return siteService;
        }

        @Override
        public boolean assertSessionNotModified() {
            return true;
        }

        @Override
        public boolean assertSessionModified() {
            return true;
        }
    }

    public static class Mongo extends PersistenceContext {

        /** . */
        private MongodExecutable mongodExe;

        /** . */
        private MongodProcess mongod;

        /** . */
        private MongoStore store;

        /** . */
        private SiteStore siteStore;

        /** . */
        private SiteService siteService;

        /** . */
        private PageStore pageStore;

        /** . */
        private PageServiceImpl pageService;

        /** . */
        private NavigationStore navigationStore;

        /** . */
        private NavigationServiceImpl navigationService;

        /** . */
        private LayoutService layoutService;

        /** . */
        private LayoutStore layoutStore;

        /** . */
        private DescriptionService descriptionService;

        /** . */
        private DescriptionStore descriptionStore;
        
        /** . */
        private SecurityService securityService;

        /** . */
        private SecurityStore securityStore;

        /** . */
        private CustomizationService customizationService;

        @Override
        protected void setUp() throws Exception {
            mongodExe = MongodStarter.getDefaultInstance().prepare(new MongodConfig(Version.V2_0_5, 27777, Network.localhostIsIPv6()));
            mongod = mongodExe.start();
            store = new MongoStore("localhost", 27777);
            store.start();
            siteService = new SiteServiceImpl(siteStore = store.getSiteStore());
            pageService = new PageServiceImpl(pageStore = store.getPageStore());
            navigationService = new NavigationServiceImpl(navigationStore = store.getNavigationStore());
            layoutService = new LayoutServiceImpl(layoutStore = store.getLayoutStore());
            descriptionService = new DescriptionServiceImpl(descriptionStore = store.getDescriptionStore());
            securityService = new SecurityServiceImpl(securityStore = store.getSecurityStore());
            customizationService = new CustomizationServiceImpl(store.getCustomizationStore());
        }

        @Override
        protected void tearDown() throws Exception {
            if (mongodExe != null) {
                if (mongod != null) {
                    MongoClient mongo = new MongoClient("localhost", 27777);
                    DB db = mongo.getDB("gatein");
                    db.dropDatabase();
                    mongo.close();
                    mongod.stop();
                }
                mongodExe.stop();
            }
            store.stop();
        }

        @Override
        public void begin() {
        }

        @Override
        public void end(boolean save) {
        }

        @Override
        public boolean assertSessionNotModified() {
            return true;
        }

        @Override
        public boolean assertSessionModified() {
            return true;
        }

        @Override
        public boolean isSessionModified() {
            return false;
        }

        @Override
        public SiteStore getSiteStore() {
            return siteStore;
        }

        @Override
        public SiteService getSiteService() {
            return siteService;
        }

        @Override
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PageStore getPageStore() {
            return pageStore;
        }

        @Override
        public NavigationServiceImpl getNavigationService() {
            return navigationService;
        }

        @Override
        public NavigationStore getNavigationStore() {
            return navigationStore;
        }

        @Override
        public LayoutStore getLayoutStore() {
            return layoutStore;
        }

        @Override
        public LayoutService getLayoutService() {
            return layoutService;
        }

        @Override
        public DescriptionService getDescriptionService() {
            return descriptionService;
        }

        @Override
        public DescriptionStore getDescriptionStore() {
            return descriptionStore;
        }
        
        @Override
        public SecurityService getSecurityService() {
            return securityService;
        }

        @Override
        public SecurityStore getSecurityStore() {
            return securityStore;
        }

        @Override
        public CustomizationService getCustomizationService() {
            return customizationService;
        }
    }
}

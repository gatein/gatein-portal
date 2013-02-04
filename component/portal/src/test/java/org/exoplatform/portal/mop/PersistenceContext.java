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
import org.gatein.portal.impl.mop.ram.RamDescriptionStore;
import org.gatein.portal.impl.mop.ram.RamLayoutStore;
import org.gatein.portal.impl.mop.ram.RamNavigationStore;
import org.gatein.portal.impl.mop.ram.RamPageStore;
import org.gatein.portal.impl.mop.ram.RamStore;
import org.gatein.portal.impl.mop.ram.RamSiteStore;
import org.gatein.portal.impl.mop.ram.Tx;
import org.gatein.portal.mop.description.DescriptionStore;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.description.DescriptionServiceImpl;
import org.gatein.portal.mop.layout.LayoutStore;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.layout.LayoutServiceImpl;
import org.gatein.portal.mop.navigation.NavigationStore;
import org.gatein.portal.mop.navigation.NavigationServiceImpl;
import org.gatein.portal.mop.page.PageStore;
import org.gatein.portal.mop.page.PageServiceImpl;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.mop.site.SiteServiceImpl;
import org.gatein.portal.mop.site.SiteStore;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class PersistenceContext {

    abstract void setUp();

    public abstract boolean isSessionModified();

    public abstract NavigationServiceImpl getNavigationService();

    public abstract DescriptionService getDescriptionService();

    public abstract PageServiceImpl getPageService();

    public abstract LayoutService getLayoutService();

    public abstract SiteService getSiteService();

    public abstract NavigationStore getNavigationPersistence();

    public abstract DescriptionStore getDescriptionPersistence();

    public abstract PageStore getPageStore();

    public abstract SiteStore getSiteStore();

    public abstract LayoutStore getLayoutStore();

    public abstract void begin();

    public abstract void end(boolean save);

    public abstract boolean assertSessionNotModified();

    public abstract boolean assertSessionModified();

    public static class JCR extends PersistenceContext {

        /** . */
        private org.exoplatform.portal.mop.navigation.MopStore navigationPersistenceFactory;

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
        private SiteStore siteStore;

        /** . */
        private LayoutStore layoutStore;

        /** . */
        private org.exoplatform.portal.mop.description.MopStore descriptionPersistence;

        private SiteService siteService;

        @Override
        void setUp() {
            PortalContainer container = PortalContainer.getInstance();

            //
            mgr = new POMSessionManager(
                    (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class),
                    (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class),
                    (CacheService) container.getComponentInstanceOfType(CacheService.class));
            mgr.start();

            //
            navigationPersistenceFactory = new org.exoplatform.portal.mop.navigation.MopStore(mgr);
            navigationService = new NavigationServiceImpl(navigationPersistenceFactory);
            descriptionPersistence = new org.exoplatform.portal.mop.description.MopStore(mgr, new SimpleDataCache());
            descriptionService = new DescriptionServiceImpl(descriptionPersistence);
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
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PageStore getPageStore() {
            return pageStore;
        }

        @Override
        public DescriptionStore getDescriptionPersistence() {
            return descriptionPersistence;
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
        public LayoutService getLayoutService() {
            return layoutService;
        }

        @Override
        public NavigationStore getNavigationPersistence() {
            return navigationPersistenceFactory;
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
        NavigationServiceImpl navigationService;

        /** . */
        PageServiceImpl pageService;

        /** . */
        RamStore persistence;

        /** . */
        DescriptionServiceImpl descriptionService;

        /** . */
        LayoutServiceImpl layoutService;

        /** . */
        LayoutStore layoutStore;

        /** . */
        RamDescriptionStore descriptionPersistence;

        /** . */
        RamPageStore pagePersistence;

        /** . */
        RamSiteStore sitePersistence;

        /** . */
        RamNavigationStore navigationPersistence;

        SiteService siteService;

        @Override
        void setUp() {
            persistence = new RamStore();
            navigationPersistence = new RamNavigationStore(persistence);
            navigationService = new NavigationServiceImpl(navigationPersistence);
            pagePersistence = new RamPageStore(persistence);
            pageService = new PageServiceImpl(pagePersistence);
            descriptionPersistence = new RamDescriptionStore(persistence);
            descriptionService = new DescriptionServiceImpl(descriptionPersistence);
            layoutStore = new RamLayoutStore(persistence);
            layoutService = new LayoutServiceImpl(layoutStore);
            sitePersistence = new RamSiteStore(persistence);
            siteService = new SiteServiceImpl(sitePersistence);
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
        public NavigationServiceImpl getNavigationService() {
            return navigationService;
        }

        @Override
        public LayoutStore getLayoutStore() {
            return new RamLayoutStore(persistence);
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
        public DescriptionStore getDescriptionPersistence() {
            return descriptionPersistence;
        }

        @Override
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PageStore getPageStore() {
            return pagePersistence;
        }

        @Override
        public NavigationStore getNavigationPersistence() {
            return navigationPersistence;
        }

        @Override
        public SiteStore getSiteStore() {
            return sitePersistence;
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
}

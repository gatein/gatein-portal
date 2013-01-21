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

import javax.inject.Provider;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.description.SimpleDataCache;
import org.exoplatform.portal.mop.navigation.MopPersistenceFactory;
import org.exoplatform.portal.mop.site.MopPersistence;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.gatein.portal.impl.mop.ram.RamPersistence;
import org.gatein.portal.impl.mop.ram.Tx;
import org.gatein.portal.mop.description.DescriptionPersistence;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.description.DescriptionServiceImpl;
import org.gatein.portal.mop.navigation.NavigationPersistence;
import org.gatein.portal.mop.navigation.NavigationServiceImpl;
import org.gatein.portal.mop.page.PagePersistence;
import org.gatein.portal.mop.page.PageServiceImpl;
import org.gatein.portal.mop.site.SitePersistence;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class PersistenceContext {

    abstract void setUp();

    public abstract boolean isSessionModified();

    public abstract NavigationServiceImpl getNavigationService();

    public abstract DescriptionService getDescriptionService();

    public abstract DescriptionPersistence getDescriptionPersistence();

    public abstract PageServiceImpl getPageService();

    public abstract NavigationPersistence getNavigationPersistence();

    public abstract PagePersistence getPagePersistence();

    public abstract SitePersistence getSitePersistence();

    public abstract void begin();

    public abstract void end(boolean save);

    public abstract boolean assertSessionNotModified();

    public abstract boolean assertSessionModified();

    public static class JCR extends PersistenceContext {

        /** . */
        private MopPersistenceFactory navigationPersistenceFactory;

        /** . */
        private NavigationServiceImpl navigationService;

        /** . */
        private PageServiceImpl pageService;

        /** . */
        private PagePersistence pagePersistence;

        /** . */
        private POMSessionManager mgr;

        /** . */
        private DescriptionService descriptionService;

        /** . */
        private SitePersistence sitePersistence;

        /** . */
        private org.exoplatform.portal.mop.description.MopPersistence descriptionPersistence;

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
            navigationPersistenceFactory = new MopPersistenceFactory(mgr);
            navigationService = new NavigationServiceImpl(navigationPersistenceFactory);
            descriptionPersistence = new org.exoplatform.portal.mop.description.MopPersistence(mgr, new SimpleDataCache());
            descriptionService = new DescriptionServiceImpl(descriptionPersistence);
            sitePersistence = new MopPersistence(mgr, new org.exoplatform.portal.mop.site.SimpleDataCache());
            pagePersistence = new org.exoplatform.portal.mop.page.MopPersistence(mgr, new org.exoplatform.portal.mop.page.SimpleDataCache());
            pageService = new PageServiceImpl(pagePersistence);

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
        public PagePersistence getPagePersistence() {
            return pagePersistence;
        }

        @Override
        public DescriptionPersistence getDescriptionPersistence() {
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
        public NavigationPersistence getNavigationPersistence() {
            return navigationPersistenceFactory.get();
        }

        @Override
        public SitePersistence getSitePersistence() {
            return sitePersistence;
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
        RamPersistence persistence;

        @Override
        void setUp() {
            persistence = new RamPersistence();
            navigationService = new NavigationServiceImpl(new Provider<NavigationPersistence>() {
                @Override
                public NavigationPersistence get() {
                    return persistence;
                }
            });
            pageService = new PageServiceImpl(persistence.getPagePersistence());
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
        public DescriptionService getDescriptionService() {
            return null;
        }

        @Override
        public DescriptionPersistence getDescriptionPersistence() {
            return null;
        }

        @Override
        public PageServiceImpl getPageService() {
            return pageService;
        }

        @Override
        public PagePersistence getPagePersistence() {
            return persistence.getPagePersistence();
        }

        @Override
        public NavigationPersistence getNavigationPersistence() {
            return persistence;
        }

        @Override
        public SitePersistence getSitePersistence() {
            return persistence;
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

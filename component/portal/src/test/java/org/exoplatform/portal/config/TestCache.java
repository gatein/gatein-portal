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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestCache extends AbstractConfigTest {

    /** . */
    private DataStorage storage_;

    /** . */
    private PageService pageService;

    /** . */
    private POMSessionManager mgr;

    /** . */
    private POMSession session;

    public void setUp() throws Exception {
        super.setUp();
        PortalContainer container = getContainer();
        storage_ = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
        pageService = (PageService) container.getComponentInstanceOfType(PageService.class);
        mgr = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
    }

    public void testGetPageFromRemovedPortal() throws Exception {
        // Create what we need for the test
        begin();
        session = mgr.openSession();
        PortalConfig portalConfig = new PortalConfig("portal", "testGetPageFromRemovedPortal");
        storage_.create(portalConfig);
        pageService.savePage(new PageContext(PageKey.parse("portal::testGetPageFromRemovedPortal::home"), null));
        end(true);

        // Clear cache
        mgr.clearCache();

        // The first transaction
        begin();
        session = mgr.openSession();

        // Get page from JCR and it should be stored in cache
        Page page = storage_.getPage("portal::testGetPageFromRemovedPortal::home");
        assertNotNull(page);

        // Now remove the portal
        PortalConfig portal = storage_.getPortalConfig("portal", "testGetPageFromRemovedPortal");
        storage_.remove(portal);

        // Terminate the first transaction
        end(true);

        // The second transaction
        begin();
        session = mgr.openSession();

        // The portal should be null
        portal = storage_.getPortalConfig("portal", "testGetPageFromRemovedPortal");
        assertNull(portal);

        // The portal home page should also be null
        page = storage_.getPage("portal::testGetPageFromRemovedPortal::home");
        assertNull(page);

        // End second transaction
        end(true);
    }
}

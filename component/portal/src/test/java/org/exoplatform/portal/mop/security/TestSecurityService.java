/*
 * Copyright (C) 2013 eXo Platform SAS.
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

package org.exoplatform.portal.mop.security;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.permission.SecurityState;
import org.gatein.portal.mop.permission.SecurityStore;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

public class TestSecurityService extends AbstractMopServiceTest {

    /** . */
    protected SecurityStore persistence;
    
    private String id;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        persistence = context.getSecurityStore();
        id = createNavigation();
    }

    @Override
    protected void tearDown() throws Exception {
        destroyNavigation();
        super.tearDown();
    }


    public void testLifeCycle() throws Exception {
        //
        assertNull(getSecurityService().loadPermission(id));
        getSecurityService().savePermission(id, new SecurityState(new String[] {"foo"}, "bar"));
        assertNotNull(getSecurityService().loadPermission(id));

        //
        destroyNavigation();
        assertNull(getSecurityService().loadPermission(id));
    }

    public void testSavePermission() throws Exception {
        //
        getSecurityService().savePermission(id, new SecurityState(new String[] {"foo"}, "bar"));

        //
        SecurityState state = persistence.loadPermission(id);
        assertEquals("foo", state.getAccessPermission()[0]);
        assertEquals("bar", state.getEditPermission());
    }

    public void testUpdatePermission() throws Exception {
        assertNull(getSecurityService().loadPermission(id));
        
        SecurityState state = new SecurityState(new String[] {"foo"}, "bar");
        getSecurityService().savePermission(id, state);
        assertEquals(state, getSecurityService().loadPermission(id));

        //
        getSecurityService().savePermission(id, new SecurityState(null, null));
        SecurityState update = getSecurityService().loadPermission(id);
        assertNull(update.getAccessPermission());
        assertNull(update.getAccessPermission());
        
        //
        getSecurityService().savePermission(id, new SecurityState(new String[] {"foo2"}, "bar2"));
        update = getSecurityService().loadPermission(id);
        assertEquals("foo2", update.getAccessPermission()[0]);
        assertEquals("bar2", update.getEditPermission());
    }

    public void testRemovePermission() throws Exception {
        getSecurityService().savePermission(id, new SecurityState(new String[] {"foo"}, "bar"));

        //
        assertNotNull(getSecurityService().loadPermission(id));

        getSecurityService().savePermission(id, null);
        assertNull(getSecurityService().loadPermission(id));
    }

    private String createNavigation() {
         SiteData siteData = createSite(SiteType.PORTAL, "foo");
         return createNavigation(siteData).id;
    }
    
    private void destroyNavigation() {
        NavigationContext nav = getNavigationService().loadNavigation(new SiteKey(SiteType.PORTAL, "foo"));
        if (nav != null) {
            getNavigationService().destroyNavigation(nav);            
        }
    }
}

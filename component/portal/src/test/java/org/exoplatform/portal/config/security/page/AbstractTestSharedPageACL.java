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

package org.exoplatform.portal.config.security.page;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.security.AbstractTestUserACL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractTestSharedPageACL extends AbstractTestUserACL {

    protected abstract String getOwnerType();

    public void testPage() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[0]);

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertFalse(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertFalse(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertFalse(administrator.hasEditPermission(page));
        assertFalse(manager.hasEditPermission(page));
        assertFalse(user.hasEditPermission(page));
        assertFalse(guest.hasEditPermission(page));
    }

    public void testPageAccessibleByEveryone() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[] { "Everyone" });

        //
        assertTrue(root.hasPermission(page));
        assertTrue(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertTrue(user.hasPermission(page));
        assertTrue(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertFalse(administrator.hasEditPermission(page));
        assertFalse(manager.hasEditPermission(page));
        assertFalse(user.hasEditPermission(page));
        assertFalse(guest.hasEditPermission(page));
    }

    public void testPageEditableByEveryone() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[0]);
        page.setEditPermissions(new String[]{"Everyone"});

        //
        assertTrue(root.hasPermission(page));
        assertTrue(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertTrue(user.hasPermission(page));
        assertTrue(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertTrue(administrator.hasEditPermission(page));
        assertTrue(manager.hasEditPermission(page));
        assertTrue(user.hasEditPermission(page));
        assertTrue(guest.hasEditPermission(page));
    }

    public void testPageAccessibleByGuests() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[] { "whatever:/platform/guests" });

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertFalse(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertTrue(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertFalse(administrator.hasEditPermission(page));
        assertFalse(manager.hasEditPermission(page));
        assertFalse(user.hasEditPermission(page));
        assertFalse(guest.hasEditPermission(page));
    }

    public void testPageEditableByGuests() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[0]);
        page.setEditPermissions(new String[]{"whatever:/platform/guests"});

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertFalse(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertTrue(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertFalse(administrator.hasEditPermission(page));
        assertFalse(manager.hasEditPermission(page));
        assertFalse(user.hasEditPermission(page));
        assertTrue(guest.hasEditPermission(page));
    }

    public void testPageAccessibleByEveryOneAndGuests() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[] { "Everyone", "whatever:/platform/guests" });

        //
        assertTrue(root.hasPermission(page));
        assertTrue(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertTrue(user.hasPermission(page));
        assertTrue(guest.hasPermission(page));

        //
        assertTrue(root.hasEditPermission(page));
        assertFalse(administrator.hasEditPermission(page));
        assertFalse(manager.hasEditPermission(page));
        assertFalse(user.hasEditPermission(page));
        assertFalse(guest.hasEditPermission(page));
    }

    public void testPageWithAccessPermission() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[] { "manager:/manageable" });

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertFalse(guest.hasPermission(page));

        //
        page.setAccessPermissions(new String[] { "*:/manageable" });

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertFalse(guest.hasPermission(page));
    }

    public void testPageWithEditPermission() {
        Page page = new Page();
        page.setOwnerType(getOwnerType());
        page.setOwnerId("foo");
        page.setAccessPermissions(new String[0]);
        page.setEditPermissions(new String[]{"manager:/manageable"});

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertFalse(guest.hasPermission(page));

        //
        page.setEditPermissions(new String[]{"*:/manageable"});

        //
        assertTrue(root.hasPermission(page));
        assertFalse(administrator.hasPermission(page));
        assertTrue(manager.hasPermission(page));
        assertFalse(user.hasPermission(page));
        assertFalse(guest.hasPermission(page));
    }
}

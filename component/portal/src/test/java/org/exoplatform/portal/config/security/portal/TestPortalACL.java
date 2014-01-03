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

package org.exoplatform.portal.config.security.portal;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.security.AbstractTestUserACL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortalACL extends AbstractTestUserACL {

    public void testFoo() {
        PortalConfig portal = new PortalConfig();
        portal.setAccessPermissions(new String[0]);

        //
        assertTrue(root.hasEditPermission(portal));
        assertFalse(administrator.hasEditPermission(portal));
        assertFalse(manager.hasEditPermission(portal));
        assertFalse(user.hasEditPermission(portal));
        assertFalse(guest.hasEditPermission(portal));

        //
        assertTrue(root.hasPermission(portal));
        assertFalse(administrator.hasPermission(portal));
        assertFalse(manager.hasPermission(portal));
        assertFalse(user.hasPermission(portal));
        assertFalse(guest.hasPermission(portal));
    }

    public void testPortalAccessible() {
        PortalConfig portal = new PortalConfig();
        portal.setAccessPermissions(new String[] { "manager:/manageable" });

        //
        assertTrue(root.hasEditPermission(portal));
        assertFalse(administrator.hasEditPermission(portal));
        assertFalse(manager.hasEditPermission(portal));
        assertFalse(user.hasEditPermission(portal));
        assertFalse(guest.hasEditPermission(portal));

        //
        assertTrue(root.hasPermission(portal));
        assertFalse(administrator.hasPermission(portal));
        assertTrue(manager.hasPermission(portal));
        assertFalse(user.hasPermission(portal));
        assertFalse(guest.hasPermission(portal));
    }

    public void testPortalEditable() {
        PortalConfig portal = new PortalConfig();
        portal.setAccessPermissions(new String[0]);
        portal.setEditPermissions(new String[] {"manager:/manageable"});

        //
        assertTrue(root.hasEditPermission(portal));
        assertFalse(administrator.hasEditPermission(portal));
        assertTrue(manager.hasEditPermission(portal));
        assertFalse(user.hasEditPermission(portal));
        assertFalse(guest.hasEditPermission(portal));

        //
        assertTrue(root.hasPermission(portal));
        assertFalse(administrator.hasPermission(portal));
        assertTrue(manager.hasPermission(portal));
        assertFalse(user.hasPermission(portal));
        assertFalse(guest.hasPermission(portal));
    }

    public void testPortalAccessibleAndEditable() {
        PortalConfig portal = new PortalConfig();
        portal.setAccessPermissions(new String[] { "manager:/manageable" });
        portal.setEditPermissions(new String[] {"manager:/manageable"});

        //
        assertTrue(root.hasEditPermission(portal));
        assertFalse(administrator.hasEditPermission(portal));
        assertTrue(manager.hasEditPermission(portal));
        assertFalse(user.hasEditPermission(portal));
        assertFalse(guest.hasEditPermission(portal));

        //
        assertTrue(root.hasPermission(portal));
        assertFalse(administrator.hasPermission(portal));
        assertTrue(manager.hasPermission(portal));
        assertFalse(user.hasPermission(portal));
        assertFalse(guest.hasPermission(portal));
    }

}

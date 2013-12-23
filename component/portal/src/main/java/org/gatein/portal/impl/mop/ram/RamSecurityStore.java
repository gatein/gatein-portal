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

package org.gatein.portal.impl.mop.ram;

import org.gatein.portal.mop.permission.SecurityState;
import org.gatein.portal.mop.permission.SecurityStore;

public class RamSecurityStore implements SecurityStore {

    public static String SECURITY_STATE = "permissions";
    
    /** . */
    private final Store store;

    public RamSecurityStore(RamStore persistence) {
        this.store = persistence.store;
    }

    @Override
    public SecurityState loadPermission(String id) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String state = current.getChild(id, SECURITY_STATE);
        
        if (state != null) {
            Node stateNode = current.getNode(state);
            return (SecurityState)stateNode.getState();
        }
        
        return null;
    }

    @Override
    public void savePermission(String id, SecurityState state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();

        if (current.getNode(id) != null) {
            String secID = current.getChild(id, SECURITY_STATE);
            if (secID == null) {
                if (state != null) {
                    current.addChild(id, SECURITY_STATE, state);                    
                }
            } else if (state != null) {
                current.update(secID, state);
            } else {
                current.remove(secID);
            }
        }
    }
}
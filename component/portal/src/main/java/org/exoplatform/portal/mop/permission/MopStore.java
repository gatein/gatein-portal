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

package org.exoplatform.portal.mop.permission;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.portal.mop.permission.SecurityState;
import org.gatein.portal.mop.permission.SecurityStore;

public class MopStore implements SecurityStore {

    /** . */
    private final POMSessionManager manager;

    public MopStore(POMSessionManager manager) {
        this.manager = manager;
    }

    @Override
    public SecurityState loadPermission(String id) {
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        if (obj != null) {
            if (obj.isAdapted(ProtectedResource.class)) {
                ProtectedResource res = obj.adapt(ProtectedResource.class);
                
                List<String> aList = res.getAccessPermissions();
                String[] accessPermission = aList == null ? null : aList.toArray(new String[aList.size()]);
                return new SecurityState(accessPermission, res.getEditPermission());
            }
        }
        
        return null;
    }

    @Override
    public void savePermission(String id, SecurityState state) {
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        if (obj != null) {
            if (state != null) {
                ProtectedResource res = obj.adapt(ProtectedResource.class);
                List<String> accessPer = state.getAccessPermission() == null ? null : Arrays.asList(state.getAccessPermission()); 
                res.setAccessPermissions(accessPer);
                res.setEditPermission(state.getEditPermission());                
            } else {
                obj.removeAdapter(ProtectedResource.class);
            }
        }
    }
}
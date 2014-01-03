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
  
package org.gatein.portal.mop.permission;

import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings("serial")
public class SecurityState implements Serializable {
    private String[] accessPermission;
    private String[] editPermission;

    public SecurityState(String[] accessPermission, String[] editPermission) {
        this.accessPermission = accessPermission;
        this.editPermission = editPermission;
    }

    public String[] getAccessPermission() {
        if (accessPermission != null) {
            return Arrays.copyOf(accessPermission, accessPermission.length);            
        } else {
            return null;
        }
    }

    public String[] getEditPermissions() {
        if(editPermission != null) {
            return Arrays.copyOf(editPermission, editPermission.length);
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessPermission == null) ? 0 : Arrays.hashCode(accessPermission));
        result = prime * result + ((editPermission == null) ? 0 : Arrays.hashCode(editPermission));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecurityState other = (SecurityState) obj;
        if (!Arrays.equals(accessPermission, other.accessPermission)) {
            return false;
        }
        if(!Arrays.equals(editPermission, other.editPermission)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "SecurityState [accessPermission=" + accessPermission + ", editPermission=" + editPermission + "]";
    }

}

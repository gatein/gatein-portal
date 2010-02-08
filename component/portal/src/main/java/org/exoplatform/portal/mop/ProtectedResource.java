/*
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

package org.exoplatform.portal.mop;

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;

import java.util.Collections;
import java.util.List;

/**
 * Describe an object that can be secured
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@MixinType(name = "mop:protectedresource")
public abstract class ProtectedResource
{

   @Property(name = "mop:access-permissions")
   public abstract List<String> getAccessPermissions();

   public abstract void setAccessPermissions(List<String> accessPermissions);

   @Property(name = "mop:edit-permissions")
   public abstract String getEditPermission();

   public abstract void setEditPermission(String editPermission);

/*
   public String getEditPermission()
   {
      List<String> editPermissions = getEditPermissions();
      return editPermissions.isEmpty() ? null : editPermissions.get(0);
   }

   public void setEditPermission(String editPermission)
   {
      setEditPermission(Collections.singletonList(editPermission));
   }

   @Property(name = "mop:edit-permissions")
   public abstract List<String> getEditPermissions();

   public abstract void setEditPermission(List<String> editPermissions);
*/

}

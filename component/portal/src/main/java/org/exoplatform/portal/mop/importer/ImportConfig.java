/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.importer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ImportConfig
{

   /** . */
   final boolean destroyOrphan;

   /** . */
   final boolean updatedSame;

   /** . */
   final boolean createMissing;

   public ImportConfig(boolean destroyOrphan, boolean updatedSame, boolean createMissing)
   {
      this.destroyOrphan = destroyOrphan;
      this.updatedSame = updatedSame;
      this.createMissing = createMissing;
   }

   /**
    * Returns true when an orphan node should be destroyed.
    *
    * @return the destroy orphan value
    */
   public boolean getDestroyOrphan()
   {
      return destroyOrphan;
   }

   public boolean getUpdatedSame()
   {
      return updatedSame;
   }

   public boolean getCreateMissing()
   {
      return createMissing;
   }
}

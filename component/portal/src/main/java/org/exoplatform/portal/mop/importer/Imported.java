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

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;

import java.util.Date;

/**
 * This mixin denotes the import performed.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@MixinType(name = "gtn:imported")
public abstract class Imported
{
   public enum Status {

      UNKNOWN(-1),

      FAILED(0),

      DONE(1),

      WANT_REIMPORT(2);

      private final int status;

      Status(int status)
      {
         this.status = status;
      }

      public int status()
      {
         return this.status;
      }

      public static Status getStatus(int status)
      {
         for (Status type : Status.values())
         {
            if (type.status() == status)
            {
               return type;
            }
         }

         return UNKNOWN;
      }
   }
   
   @Property(name = "gtn:status")
   public abstract Integer getStatus();

   public abstract void setStatus(Integer status);
   
   @Property(name = "gtn:creationdate")
   public abstract Date getCreationDate();

   public abstract void setCreationDate(Date creationDate);

   @Property(name = "gtn:lastmodificationdate")
   public abstract Date getLastModificationDate();

   public abstract void setLastModificationDate(Date lastModificationDate);

}

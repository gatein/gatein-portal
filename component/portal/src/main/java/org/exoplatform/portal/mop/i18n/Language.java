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
package org.exoplatform.portal.mop.i18n;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.PrimaryType;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 15, 2011
 */

/**
 * A nake node type that describe a language entry
 * This node is able to support data through addition of mixins
 */
@PrimaryType(name = "gtn:language")
public abstract class Language
{

   /** . */
   public ChromatticSession session;

   @Name
   abstract String getName();

   <M> M getMixin(Class<M> mixinType, boolean create)
   {
      M mixin = session.getEmbedded(this, mixinType);
      if (mixin == null && create)
      {
         mixin = session.create(mixinType);
         session.setEmbedded(this, mixinType, mixin);
      }
      return mixin;
   }

   <M> boolean removeMixin(Class<M> mixinType)
   {
      M mixin = session.getEmbedded(this, mixinType);
      if (mixin != null)
      {
         session.setEmbedded(this, mixinType, null);
         return true;
      }
      else
      {
         return false;
      }
   }
}

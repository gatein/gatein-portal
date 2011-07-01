/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.portal.config.model.PortalConfig;

import java.io.Serializable;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
public final class SiteKey implements Serializable
{

   public static SiteKey portal(String name)
   {
      return new SiteKey(SiteType.PORTAL, name);
   }

   public static SiteKey group(String name)
   {
      return new SiteKey(SiteType.GROUP, name);
   }

   public static SiteKey user(String name)
   {
      return new SiteKey(SiteType.USER, name);
   }

   /** . */
   private final SiteType type;

   /** . */
   private final String name;

   public SiteKey(SiteType type, String name)
   {
      if (type == null)
      {
         throw new NullPointerException("No null type can be provided");
      }
      if (name == null)
      {
         throw new NullPointerException("No null name can be provided");
      }

      //
      this.type = type;
      this.name = name;
   }
   
   // This will be used for transition in usage from PortalKey and SiteKey
   public SiteKey(String type, String name)
   {
      if (PortalConfig.PORTAL_TYPE.equals(type))
      {
         this.type = SiteType.PORTAL;
      }
      else if (PortalConfig.GROUP_TYPE.equals(type))
      {
         this.type = SiteType.GROUP;
      }
      else if (PortalConfig.USER_TYPE.equals(type))
      {
         this.type = SiteType.USER;
      }
      else
      {
         throw new NullPointerException("No null name can be provided");
      }
      
      this.name = name;
   }

   public SiteType getType()
   {
      return type;
   }
   
   public String getTypeName()
   {
      return type.getName();
   }

   public String getName()
   {
      return name;
   }

   @Override
   public int hashCode()
   {
      return name.hashCode() ^ type.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }

      // We need to use class equality here
      if (obj != null && getClass().equals(obj.getClass()))
      {
         SiteKey that = (SiteKey)obj;
         return type.equals(that.type) && name.equals(that.name);
      }

      //
      return false;
   }

   @Override
   public String toString()
   {
      return "SiteKey[type=" + type.toString() + ",name=" + name + "]";
   }
}

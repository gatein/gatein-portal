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

package org.exoplatform.portal.config;

import java.io.Serializable;

/**
 * Created by The eXo Platform SARL        .
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Date: Jun 14, 2003
 * Time: 1:12:22 PM
 */
public class Query<T> implements Serializable
{

   private String ownerType_;

   private String ownerId_;

   private String name_;

   private String title_;

   private Class<T> classType_;

   public Query(String ownerType, String ownerId, Class<T> clazz)
   {
      ownerType_ = ownerType;
      ownerId_ = ownerId;
      classType_ = clazz;
   }

   public Query(String ownerType, String ownerId, String name, String title, Class<T> clazz)
   {
      ownerType_ = ownerType;
      ownerId_ = ownerId;
      classType_ = clazz;
      name_ = name;
      title_ = title;
   }

   public Query(Query<?> that, Class<T> clazz)
   {
      ownerType_ = that.ownerType_;
      ownerId_ = that.ownerId_;
      classType_ = clazz;
      name_ = that.name_;
      title_ = that.title_;
   }

   public String getOwnerType()
   {
      return ownerType_;
   }

   public void setOwnerType(String s)
   {
      ownerType_ = s;
   }

   public String getOwnerId()
   {
      return ownerId_;
   }

   public void setOwnerId(String s)
   {
      ownerId_ = s;
   }

   public Class<T> getClassType()
   {
      return classType_;
   }

   public void setClassType(Class<T> clazz)
   {
      classType_ = clazz;
   }

   public String getName()
   {
      return name_;
   }

   public void setName(String name_)
   {
      this.name_ = name_;
   }

   public String getTitle()
   {
      return title_;
   }

   public void setTitle(String title_)
   {
      this.title_ = title_;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder("Query[");
      if (classType_ != null)
      {
         builder.append("class=").append(classType_.getSimpleName()).append(",");
      }
      if (ownerType_ != null)
      {
         builder.append("ownerType=").append(ownerType_).append(",");
      }
      if (ownerId_ != null)
      {
         builder.append("ownerId=").append(ownerId_).append(",");
      }
      if (name_ != null)
      {
         builder.append("name=").append(name_).append(",");
      }
      if (title_ != null)
      {
         builder.append("title=").append(title_).append(",");
      }
      if (builder.charAt(builder.length() - 1) == ',')
      {
         builder.setLength(builder.length() - 1);
      }
      builder.append(']');
      return builder.toString();
   }
}

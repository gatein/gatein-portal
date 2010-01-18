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

package org.exoplatform.application.registry;

import org.exoplatform.portal.config.model.ApplicationType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 15 juin 2004
 *
 */
public class Application implements Serializable
{

   private String categoryName;

   private String displayName;

   private String description;

   private Date createdDate;

   private Date modifiedDate;

   private ArrayList<String> accessPermissions;

   private String applicationName;

   private String type;

   /** . */
   private String storageId;

   /** . */
   private String id;

   /** . */
   private String iconURL;

   /** . */
   private String contentId;

   public String getContentId()
   {
      return contentId;
   }

   public void setContentId(String contentId)
   {
      this.contentId = contentId;
   }

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getDisplayName()
   {
      return displayName;
   }

   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }

   public String getDescription()
   {
      return description == null ? "" : description;
   }

   public void setDescription(String s)
   {
      description = s;
   }

   public Date getCreatedDate()
   {
      return createdDate;
   }

   public void setCreatedDate(Date d)
   {
      createdDate = d;
   }

   public Date getModifiedDate()
   {
      return modifiedDate;
   }

   public void setModifiedDate(Date d)
   {
      modifiedDate = d;
   }

   public String getCategoryName()
   {
      return categoryName;
   }

   public void setCategoryName(String s)
   {
      this.categoryName = s;
   }

   public void setAccessPermissions(ArrayList<String> accessPerms)
   {
      accessPermissions = accessPerms;

   }

   public ArrayList<String> getAccessPermissions()
   {
      if (accessPermissions == null)
         accessPermissions = new ArrayList<String>();
      return accessPermissions;
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public void setApplicationName(String applicationName)
   {
      this.applicationName = applicationName;
   }

   public ApplicationType getType()
   {
      return type != null ? ApplicationType.getType(type) : null;
   }

   public void setType(ApplicationType type)
   {
      this.type = type != null ? type.getName() : null;
   }

   public String getIconURL()
   {
      return iconURL;
   }

   public void setIconURL(String iconURL)
   {
      this.iconURL = iconURL;
   }
}
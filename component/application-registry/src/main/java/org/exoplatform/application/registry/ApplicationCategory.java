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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 15 juin 2004
 */
public class ApplicationCategory implements Serializable
{

   private String name;

   private String displayName;

   private String description;

   private Date createdDate;

   private Date modifiedDate;

   private List<Application> applications = new ArrayList<Application>();

   private List<String> accessPermissions = new ArrayList<String>();

   public String getName()
   {
      return name;
   }

   public void setName(String id)
   {
      this.name = id;
   }

   public String getDisplayName()
   {
      return getDisplayName(false);
   }
   
   /**
    * Return display name <br/>
    * If it's null or empty and checkEmpty is true, return name instead 
    * @param checkEmpty
    */
   public String getDisplayName(boolean checkEmpty)
   {
      if (checkEmpty && (displayName == null || displayName.trim().length() == 0))
      {
         return getName();
      }
      return displayName;
   }

   public void setDisplayName(String s)
   {
      displayName = s;
   }

   public String getDescription()
   {
      return description;
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

   public List<Application> getApplications()
   {
      return applications;
   }

   public void setApplications(List<Application> applications)
   {
      this.applications = applications;
   }

   public void setAccessPermissions(List<String> accessPerms)
   {
      accessPermissions = accessPerms;
   }

   public List<String> getAccessPermissions()
   {
      return accessPermissions;
   }

}
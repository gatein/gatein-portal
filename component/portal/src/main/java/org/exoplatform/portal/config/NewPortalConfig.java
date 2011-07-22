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

import java.util.HashSet;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 23, 2006
 */
public final class NewPortalConfig
{

   private HashSet<String> predefinedOwner = new HashSet<String>(5);

   private String ownerType;

   private String templateName;

   private String location;
   
   private String label;
   
   private String description;

   private String importMode;

   /**
    * @deprecated use the location instead
    */
   @Deprecated
   private String templateLocation;

   public NewPortalConfig()
   {
   }

   public NewPortalConfig(NewPortalConfig cfg)
   {
      this.ownerType = cfg.ownerType;
      this.templateLocation = cfg.templateLocation;
      this.location = cfg.location;
      this.label = cfg.label;
      this.description = cfg.description;
      this.templateName = cfg.templateName;
      this.predefinedOwner = new HashSet<String>(cfg.predefinedOwner);
      this.importMode = cfg.importMode;
   }
   
   public NewPortalConfig(String path)
   {
      this.location = path;
   }

   public HashSet<String> getPredefinedOwner()
   {
      return predefinedOwner;
   }

   public void setPredefinedOwner(HashSet<String> s)
   {
      this.predefinedOwner = s;
   }

   /**
    * @return the location
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * @param location the location to set
    */
   public void setLocation(String location)
   {
      this.location = location;
   }
   
   public String getTemplateLocation()
   {
      if (location != null)
         return location;
      else
         return templateLocation;
   }

   public void setTemplateLocation(String s)
   {
      this.location = s;
      this.templateLocation = s;
   }

   public String getTemplateName()
   {
      return templateName;
   }

   public void setTemplateName(String s)
   {
      this.templateName = s;
   }

   public boolean isPredefinedOwner(String user)
   {
      return predefinedOwner.contains(user);
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getImportMode()
   {
      return importMode;
   }

   public void setImportMode(String importMode)
   {
      this.importMode = importMode;
   }

   @Override
   public String toString()
   {
      return "PortalConfig[predefinedOwner=" + predefinedOwner + ",ownerType=" + ownerType + ",templateName="
         + templateName + ",label=" + label + ",description=" + description + ",location=" + location + "]";
   }
}

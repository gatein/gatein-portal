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

package org.exoplatform.services.resources;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS . Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: May 14, 2004 Time: 1:12:22 PM
 */
@SuppressWarnings("serial")
public class ResourceBundleDescription implements Serializable
{

   protected String id_;

   protected String name_;

   protected String language_;

   protected String country_;

   protected String variant_;

   protected String resourceType_;

   public ResourceBundleDescription()
   {
   }

   /**
    * @hibernate.id generator-class="assigned"
    **/
   public String getId()
   {
      if (id_ == null)
      {
         StringBuffer b = new StringBuffer();
         b.append(name_);
         if (language_ != null)
            b.append('_').append(language_);
         if(country_ != null) b.append('_').append(country_);
         if(variant_ != null) b.append('_').append(variant_);
         id_ = b.toString();
      }
      return id_;
   }

   public void setId(String id)
   {
      id_ = id;
   }

   /**
    * @hibernate.property
    **/
   public String getName()
   {
      return name_;
   }

   public void setName(String name)
   {
      name_ = name;
   }

   /**
    * @hibernate.property
    **/
   public String getLanguage()
   {
      return language_;
   }

   public void setLanguage(String s)
   {
      language_ = s;
   }

   /**
    * @hibernate.property
    **/
   public String getCountry()
   {
      return country_;
   }

   public void setCountry(String s)
   {
      country_ = s;
   }

   /**
    * @hibernate.property
    **/
   public String getVariant()
   {
      return variant_;
   }

   public void setVariant(String s)
   {
      variant_ = s;
   }

   /**
    * @hibernate.property
    **/
   public String getResourceType()
   {
      return resourceType_;
   }

   public void setResourceType(String s)
   {
      resourceType_ = s;
   }

   final static public String DEFAULT_LANGUAGE = "en";

}

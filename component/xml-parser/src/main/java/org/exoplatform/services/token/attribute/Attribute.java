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

package org.exoplatform.services.token.attribute;

/**
 *
 * @author nhuthuan
 * Email: nhudinhthuan@yahoo.com
 */
public class Attribute
{

   private String name;

   private String value;

   public Attribute(String n, String v)
   {
      name = n;
      value = v;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String n)
   {
      name = n;
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String v)
   {
      value = v;
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj instanceof Attribute)
      {
         return ((Attribute)obj).getName().equalsIgnoreCase(name);
      }
      if (obj instanceof String)
      {
         return name.equalsIgnoreCase((String)obj);
      }
      return super.equals(obj);
   }

}

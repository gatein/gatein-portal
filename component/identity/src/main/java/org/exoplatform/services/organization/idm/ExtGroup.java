/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.services.organization.idm;

import org.exoplatform.services.organization.Group;

import java.io.Serializable;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class ExtGroup implements Group, Serializable, Comparable
{
   private String id;

   private String parentId;

   private String groupName;

   private String label;

   private String desc;

   public ExtGroup()
   {

   }

   public ExtGroup(String name)
   {
      groupName = name;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getParentId()
   {
      return parentId;
   }

   public void setParentId(String parentId)
   {
      this.parentId = parentId;
   }

   public String getGroupName()
   {
      return groupName;
   }

   public void setGroupName(String name)
   {
      this.groupName = name;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
   }

   public String getDescription()
   {
      return desc;
   }

   public void setDescription(String s)
   {
      desc = s;
   }


   public String toString()
   {
      return "Group[" + id + "|" + groupName + "]";
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (!(o instanceof ExtGroup))
      {
         return false;
      }

      ExtGroup extGroup = (ExtGroup)o;

      if (desc != null ? !desc.equals(extGroup.desc) : extGroup.desc != null)
      {
         return false;
      }
      if (groupName != null ? !groupName.equals(extGroup.groupName) : extGroup.groupName != null)
      {
         return false;
      }
      if (id != null ? !id.equals(extGroup.id) : extGroup.id != null)
      {
         return false;
      }
      if (label != null ? !label.equals(extGroup.label) : extGroup.label != null)
      {
         return false;
      }
      if (parentId != null ? !parentId.equals(extGroup.parentId) : extGroup.parentId != null)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = id != null ? id.hashCode() : 0;
      result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
      result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
      result = 31 * result + (label != null ? label.hashCode() : 0);
      result = 31 * result + (desc != null ? desc.hashCode() : 0);
      return result;
   }

   public int compareTo(Object o)
   {
      if (!(o instanceof Group))
      {
         return 0;
      }

      Group group = (Group)o;

      return groupName.compareTo(group.getGroupName());

   }
}

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

package org.exoplatform.services.organization.idm;

import org.exoplatform.services.organization.Membership;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class MembershipImpl implements Membership, Comparable
{
   private String membershipType = "member";

   private String userName = null;

   private String groupId = null;

   public MembershipImpl()
   {
   }

   public MembershipImpl(String id)
   {
      String[] fields = id.split(":");

      // Id can be pure "//" in some cases
      if (fields[0] != null)
      {
         membershipType = fields[0];
      }
      if (fields[1] != null)
      {
         userName = fields[1];
      }
      if (fields[2] != null)
      {
         groupId = fields[2];
      }
   }

   public String getId()
   {
      StringBuffer id = new StringBuffer();

      if (membershipType != null)
      {
         id.append(membershipType);
      }
      id.append(":");
      if (userName != null)
      {
         id.append(userName);
      }
      id.append(":");
      if (groupId != null)
      {
         id.append(groupId);
      }

      return id.toString();
   }

   public String getMembershipType()
   {
      return membershipType;
   }

   public void setMembershipType(String membershipType)
   {
      this.membershipType = membershipType;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String groupId)
   {
      this.groupId = groupId;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (!(o instanceof MembershipImpl))
      {
         return false;
      }

      MembershipImpl that = (MembershipImpl)o;

      if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null)
      {
         return false;
      }
      if (membershipType != null ? !membershipType.equals(that.membershipType) : that.membershipType != null)
      {
         return false;
      }
      if (userName != null ? !userName.equals(that.userName) : that.userName != null)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = membershipType != null ? membershipType.hashCode() : 0;
      result = 31 * result + (userName != null ? userName.hashCode() : 0);
      result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
      return result;
   }

   public int compareTo(Object o)
   {
      if (!(o instanceof Membership) || userName == null)
      {
         return 0;
      }

      Membership m = (Membership)o;

      return userName.compareTo(m.getUserName());  
   }

   @Override
   public String toString()
   {
      return "MembershipImpl{" +
         "membershipType='" + membershipType + '\'' +
         ", userName='" + userName + '\'' +
         ", groupId='" + groupId + '\'' +
         '}';
   }
}

package org.exoplatform.services.organization.idm;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class Config
{

   private Map<String, String> groupTypeMappings = new HashMap<String, String>();

   private boolean useParentIdAsGroupType = false;

   private boolean passwordAsAttribute = false;

   private String defaultGroupType = "GTN_GROUP_TYPE";

   private String rootGroupName = "GTN_ROOT_GROUP";

   private String pathSeparator = ".";

   private String slashReplacement = "@_@_@";

   private boolean forceMembershipOfMappedTypes = false;

   private String associationMembershipType;

   private boolean ignoreMappedMembershipType = true;

   private boolean useJTA = false;

   private boolean sortGroups = true;

   private boolean sortMemberships = true;

   public Config()
   {
   }


   public String getGroupType(String parentId)
   {


      if (parentId == null || parentId.length() == 0)
      {
         parentId = "/";
      }    



      if (!useParentIdAsGroupType)
      {
         String type =_getGroupType(parentId, true, true, true);
         if (type != null)
         {
            return type;
         }
         return getDefaultGroupType();
      }


      // Search for exact match in mappings
      String type = _getGroupType(parentId, false, true, true);

      // If not then check for inherited type
      if (type == null)
      {
         type = _getGroupType(parentId, true, false, true);
      }

      // If not then prepare type from this id
      if (type == null)
      {
         type = convertType(parentId);
      }

      return type;

   }

   private String _getGroupType(String parentId, boolean checkParents, boolean matchExact, boolean matchInherited)
   {


      if (matchExact && getGroupTypeMappings().keySet().contains(parentId))
      {
         return getGroupTypeMappings().get(parentId);
      }

      String id = !parentId.equals("/") ? parentId + "/*" : "/*";

      if (matchInherited && getGroupTypeMappings().keySet().contains(id))
      {
         return getGroupTypeMappings().get(id);
      }


      if (checkParents && !parentId.equals("/") && parentId.contains("/"))
      {
         // Check if any mapping that contains '/*' match this id
         for (String key : groupTypeMappings.keySet())
         {
            id = key;
            if (id.endsWith("/*"))
            {
               id = id.substring(0, id.length() - 2);
            }
            else
            {
               continue;
            }

            if (parentId.startsWith(id))
            {
               return groupTypeMappings.get(key);
            }
         }
      }

      return null;
   }

   public String getParentId(String type)
   {
      for (Map.Entry<String, String> entry : groupTypeMappings.entrySet())
      {
         if (entry.getValue().equals(type))
         {
            return entry.getKey();
         }
      }

      return null;
   }

   public String getPLIDMGroupName(String gtnGroupName)
   {
      return gtnGroupName.replaceAll(getSlashReplacement(), "/");
   }

   public String getGtnGroupName(String plidmGroupName)
   {
      return plidmGroupName.replaceAll("/", getSlashReplacement());
   }

   Set<String> getTypes(String id)
   {
      HashSet<String> types = new HashSet<String>();

      for (String key : groupTypeMappings.keySet())
      {
         if (key.equals(id) || key.equals(id + "/*"))
         {
            types.add(groupTypeMappings.get(key));
         }
      }

      return types;
   }

   Set<String> getAllTypes()
   {
      HashSet<String> types = new HashSet<String>(groupTypeMappings.values());

      return types;

   }

   private String convertType(String type)
   {

      return type.replaceAll("/", pathSeparator);
   }


   public boolean isUseParentIdAsGroupType()
   {
      return useParentIdAsGroupType;
   }

   public void setUseParentIdAsGroupType(boolean useParentIdAsGroupType)
   {
      this.useParentIdAsGroupType = useParentIdAsGroupType;
   }

   public String getDefaultGroupType()
   {
      return defaultGroupType;
   }

   public void setDefaultGroupType(String defaultGroupType)
   {
      this.defaultGroupType = defaultGroupType;
   }

   public String getRootGroupName()
   {
      return rootGroupName;
   }

   public void setRootGroupName(String rootGroupName)
   {
      this.rootGroupName = rootGroupName;
   }

   public void setGroupTypeMappings(Map<String, String> groupTypeMappings)
   {
      this.groupTypeMappings = groupTypeMappings;
   }

   public Map<String, String> getGroupTypeMappings()
   {
      return groupTypeMappings;
   }

   public boolean isPasswordAsAttribute()
   {
      return passwordAsAttribute;
   }

   public void setPasswordAsAttribute(boolean passwordAsAttribute)
   {
      this.passwordAsAttribute = passwordAsAttribute;
   }

   public String getPathSeparator()
   {
      return pathSeparator;
   }

   public void setPathSeparator(String pathSeparator)
   {
      this.pathSeparator = pathSeparator;
   }

   public boolean isForceMembershipOfMappedTypes()
   {
      return forceMembershipOfMappedTypes;
   }

   public void setForceMembershipOfMappedTypes(boolean forceMembershipOfMappedTypes)
   {
      this.forceMembershipOfMappedTypes = forceMembershipOfMappedTypes;
   }

   public String getAssociationMembershipType()
   {
      return associationMembershipType;
   }

   public void setAssociationMembershipType(String associationMembershipType)
   {
      this.associationMembershipType = associationMembershipType;
   }

   public boolean isIgnoreMappedMembershipType()
   {
      return ignoreMappedMembershipType;
   }

   public void setIgnoreMappedMembershipType(boolean ignoreMappedMembershipType)
   {
      this.ignoreMappedMembershipType = ignoreMappedMembershipType;
   }

   public boolean isUseJTA()
   {
      return useJTA;
   }

   public void setUseJTA(boolean useJTA)
   {
      this.useJTA = useJTA;
   }

   public String getSlashReplacement()
   {
      return slashReplacement;
   }

   public void setSlashReplacement(String slashReplacement)
   {
      this.slashReplacement = slashReplacement;
   }

   public boolean isSortGroups()
   {
      return sortGroups;
   }

   public void setSortGroups(boolean sortGroups)
   {
      this.sortGroups = sortGroups;
   }

   public boolean isSortMemberships()
   {
      return sortMemberships;
   }

   public void setSortMemberships(boolean sortMemberships)
   {
      this.sortMemberships = sortMemberships;
   }
}

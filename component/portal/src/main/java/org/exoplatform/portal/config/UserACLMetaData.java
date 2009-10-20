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

/**
 * A metadata class to describe security configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserACLMetaData
{

   /** . */
   private String superUser;

   /** . */
   private String guestsGroups;

   /** . */
   private String navigationCreatorMembershipType;

   /** . */
   private String portalCreateGroups;

   public String getSuperUser()
   {
      return superUser;
   }

   public void setSuperUser(String superUser)
   {
      this.superUser = superUser;
   }

   public String getGuestsGroups()
   {
      return guestsGroups;
   }

   public void setGuestsGroups(String guestsGroups)
   {
      this.guestsGroups = guestsGroups;
   }

   public String getNavigationCreatorMembershipType()
   {
      return navigationCreatorMembershipType;
   }

   public void setNavigationCreatorMembershipType(String navigationCreatorMembershipType)
   {
      this.navigationCreatorMembershipType = navigationCreatorMembershipType;
   }

   public String getPortalCreateGroups()
   {
      return portalCreateGroups;
   }

   public void setPortalCreateGroups(String portalCreateGroups)
   {
      this.portalCreateGroups = portalCreateGroups;
   }
}

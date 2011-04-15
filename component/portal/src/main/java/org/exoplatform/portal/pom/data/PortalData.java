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
package org.exoplatform.portal.pom.data;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalData extends ModelData
{

   /** . */
   private final PortalKey key;

   /** . */
   private final String locale;

   /** . */
   private final List<String> accessPermissions;

   /** . */
   private final String editPermission;

   /** . */
   private final Map<String, String> properties;

   /** . */
   private final String skin;

   /** . */
   private final ContainerData portalLayout;
   
   private final String label;
   
   private final String description;
   
   public PortalData(
      String storageId,
      String name,
      String type,
      String locale,
      String label,
      String description,
      List<String> accessPermissions,
      String editPermission,
      Map<String, String> properties,
      String skin,
      ContainerData portalLayout)
   {
      super(storageId, null);

      //
      this.key = new PortalKey(type, name);
      this.locale = locale;
      this.label = label;
      this.description = description;
      this.accessPermissions = accessPermissions;
      this.editPermission = editPermission;
      this.properties = properties;
      this.skin = skin;
      this.portalLayout = portalLayout;
   }

   public PortalKey getKey()
   {
      return key;
   }

   public String getName()
   {
      return key.getId();
   }

   public String getType()
   {
      return key.getType();
   }

   public String getLocale()
   {
      return locale;
   }

   public List<String> getAccessPermissions()
   {
      return accessPermissions;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public Map<String, String> getProperties()
   {
      return properties;
   }

   public String getSkin()
   {
      return skin;
   }

   public ContainerData getPortalLayout()
   {
      return portalLayout;
   }

   public String getDescription()
   {
      return description;
   }

   public String getLabel()
   {
      return label;
   }
}

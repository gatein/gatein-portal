/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.portal.pom.registry;

import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NodeMapping;
import org.chromattic.api.annotations.Property;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Workspace;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@NodeMapping(name = "mop:content")
public abstract class ContentDefinition
{

   @Id
   public abstract String getId();

   @Name
   public abstract String getName();

   @Property(name = "displayname")
   public abstract String getDisplayName();

   public abstract void setDisplayName(String displayName);

   @Property(name = "description")
   public abstract String getDescription();

   public abstract void setDescription(String description);

   @Property(name = "creationdate")
   public abstract Date getCreationDate();

   public abstract void setCreationDate(Date date);

   @Property(name = "lastmodificationdate")
   public abstract Date getLastModificationDate();

   public abstract void setLastModificationDate(Date date);

   @Property(name = "accesspermissions")
   public abstract List<String> getAccessPermissions();

   public abstract void setAccessPermissions(List<String> accessPermissions);

   @ManyToOne
   public abstract CategoryDefinition getCategory();

   public Customization getCustomization()
   {
      CategoryDefinition category = getCategory();
      Workspace workspace = category.session.getWorkspace();
      String name = getName();
      return workspace.getCustomization(name);
   }

   

   

}

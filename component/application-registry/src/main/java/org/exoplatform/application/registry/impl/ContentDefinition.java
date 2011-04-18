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
package org.exoplatform.application.registry.impl;

import org.chromattic.api.annotations.*;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Workspace;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "app:application")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("app")
public abstract class ContentDefinition
{

   @Id
   public abstract String getId();

   @Name
   public abstract String getName();

   @Property(name = "gtn:name")
   public abstract String getDisplayName();

   public abstract void setDisplayName(String displayName);

   @Property(name = "gtn:description")
   public abstract String getDescription();

   public abstract void setDescription(String description);

   @Property(name = "app:creationdate")
   public abstract Date getCreationDate();

   public abstract void setCreationDate(Date date);

   @Property(name = "app:lastmodificationdate")
   public abstract Date getLastModificationDate();

   public abstract void setLastModificationDate(Date date);

   @Property(name = "gtn:access-permissions")
   public abstract List<String> getAccessPermissions();

   public abstract void setAccessPermissions(List<String> accessPermissions);

   @ManyToOne
   public abstract CategoryDefinition getCategory();

   public Customization getCustomization()
   {
      CategoryDefinition category = getCategory();
      POMSession session = category.registry.mopManager.getSession();
      Workspace workspace = session.getWorkspace();
      String name = getName();
      return workspace.getCustomizationContext().getCustomization(name);
   }

   

   

}

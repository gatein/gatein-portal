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
package org.exoplatform.application.registry.mop;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NodeMapping;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Property;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.Workspace;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@NodeMapping(name = "mop:contentcategory")
public abstract class CategoryDefinition
{

   /** The injected workspace. */
   public MOPApplicationRegistryService registry;

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

   @OneToMany
   public abstract List<ContentDefinition> getContentList();

   @OneToMany
   public abstract Map<String, ContentDefinition> getContentMap();

   @Create
   public abstract ContentDefinition create();

   /**
    * Create a content definition for the target content.
    *
    * @param definitionName the definition name
    * @param contentType the target content type
    * @param contentId the target content id
    * @return the content definion
    */
   public ContentDefinition createContent(
      String definitionName,
      ContentType<?> contentType,
      String contentId)
   {
      if (definitionName == null)
      {
         throw new NullPointerException("No null definition name accepted");
      }
      if (contentType  == null)
      {
         throw new NullPointerException("No null content type accepted");
      }
      if (contentId  == null)
      {
         throw new NullPointerException("No null content id accepted");
      }

      //
      POMSession session = registry.mopManager.getSession();

      //
      Workspace workspace = session.getWorkspace();

      //
      Customization customization = workspace.getCustomization(definitionName);

      //
      if (customization == null)
      {
         workspace.customize(definitionName, contentType, contentId, null);
      }
      else if (customization.getContentId().equals(contentId))
      {
         // Do nothing here
      }
      else
      {
         throw new IllegalArgumentException("Cannot create a content with a content id " + contentId + 
            " with an existing different content id " + customization.getContentId());
      }

      //
      ContentDefinition content = create();

      //
      Map<String, ContentDefinition> contents = getContentMap();
      contents.put(definitionName, content);

      //
      return content;
   }
}

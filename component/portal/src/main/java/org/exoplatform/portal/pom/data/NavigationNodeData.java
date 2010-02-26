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

import org.exoplatform.portal.mop.Visibility;

import java.util.Date;
import java.util.List;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
public class NavigationNodeData extends NavigationNodeContainerData
{

   /** . */
   private final String uri;

   /** . */
   private final String label;

   /** . */
   private final String icon;

   /** . */
   private final String name;

   /** . */
   private final Date startPublicationDate;

   /** . */
   private final Date endPublicationDate;

   /** . */
   private final Visibility visibility;

   /** . */
   private final String pageReference;

   public NavigationNodeData(
      String uri,
      String label,
      String icon,
      String name,
      Date startPublicationDate,
      Date endPublicationDate,
      Visibility visibility,
      String pageReference,
      List<NavigationNodeData> children)
   {
      this(null, uri, label, icon, name, startPublicationDate, endPublicationDate, visibility, pageReference, children);
   }

   public NavigationNodeData(
      String storageId,
      String uri,
      String label,
      String icon,
      String name,
      Date startPublicationDate,
      Date endPublicationDate,
      Visibility visibility,
      String pageReference,
      List<NavigationNodeData> children)
   {
      super(storageId, children);

      //
      this.uri = uri;
      this.label = label;
      this.icon = icon;
      this.name = name;
      this.startPublicationDate = startPublicationDate;
      this.endPublicationDate = endPublicationDate;
      this.visibility = visibility;
      this.pageReference = pageReference;
   }
   public String getURI()
   {
      return uri;
   }

   public String getLabel()
   {
      return label;
   }

   public String getIcon()
   {
      return icon;
   }

   public String getName()
   {
      return name;
   }

   public Date getStartPublicationDate()
   {
      return startPublicationDate;
   }

   public Date getEndPublicationDate()
   {
      return endPublicationDate;
   }

   public Visibility getVisibility()
   {
      return visibility;
   }

   public String getPageReference()
   {
      return pageReference;
   }
}

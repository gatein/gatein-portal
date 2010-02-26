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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DashboardData extends ContainerData
{

   public DashboardData(
      String storageId,
      String id,
      String name,
      String icon,
      String template,
      String factoryId,
      String title,
      String description,
      String width,
      String height,
      List<String> accessPermissions,
      List<ComponentData> children)
   {
      super(
         storageId,
         id,
         name,
         icon,
         template,
         factoryId,
         title,
         description,
         width,
         height,
         accessPermissions,
         children);
   }

   /** . */
   static final DashboardData INITIAL_DASHBOARD;

   static
   {
      List<ComponentData> children = new ArrayList<ComponentData>();
      for (int i = 0; i < 3; i++)
      {
         ContainerData row = new ContainerData(
            null,
            null,
            null,
            null,
            "classpath:groovy/dashboard/webui/component/UIContainer.gtmpl",
            null,
            null,
            null,
            null,
            null,
            Collections.<String>emptyList(),
            Collections.<ComponentData>emptyList());
         children.add(row);
      }

      INITIAL_DASHBOARD = new DashboardData(
         null,
         null,
         null,
         null,
         "classpath:groovy/dashboard/webui/component/UIColumnContainer.gtmpl",
         null,
         null,
         null,
         null,
         null,
         Collections.<String>emptyList(),
         Collections.unmodifiableList(children)
      );
   }

}

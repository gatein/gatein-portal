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

package org.exoplatform.portal.config.model;

import org.exoplatform.portal.pom.data.NavigationNodeContainerData;
import org.exoplatform.portal.pom.data.NavigationNodeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageNodeContainer extends ModelObject
{

   public PageNodeContainer(String storageId)
   {
      super(storageId);
   }

   public PageNodeContainer()
   {
   }

   public abstract List<PageNode> getNodes();

   protected List<NavigationNodeData> buildNavigationChildren()
   {
      List<PageNode> nodes = getNodes();
      if (nodes != null)
      {
         ArrayList<NavigationNodeData> children = new ArrayList<NavigationNodeData>();
         for (int i = 0;i < nodes.size();i++)
         {
            PageNode node = nodes.get(i);
            NavigationNodeData child = node.build();
            children.add(child);
         }
         return Collections.unmodifiableList(children);
      }
      else
      {
         return Collections.emptyList();
      }
   }

   public abstract NavigationNodeContainerData build();

}

/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.webui.core;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIFilterableTree.gtmpl", events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class))
@Serialized
public class UIFilterableTree extends UITree
{

   private TreeNodeFilter nodeFilter;
   
   public UIFilterableTree() throws Exception
   {
      super();
   }
   
   public boolean displayThisNode(Object nodeObject, WebuiRequestContext context)
   {
      if(nodeFilter == null)
      {
         return true;
      }
      return !nodeFilter.filterThisNode(nodeObject, context);
   }
   
   public void setTreeNodeFilter(TreeNodeFilter _nodeFilter)
   {
      this.nodeFilter = _nodeFilter;
   }
   
   public static interface TreeNodeFilter
   {
      public boolean filterThisNode(Object nodeObject, WebuiRequestContext context);
   }
}

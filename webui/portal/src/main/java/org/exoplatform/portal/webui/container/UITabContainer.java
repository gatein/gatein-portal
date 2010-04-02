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

package org.exoplatform.portal.webui.container;

import org.exoplatform.portal.webui.container.UIContainerActionListener.EditContainerActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.List;

/**
 * May 19, 2006
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UITabContainer.gtmpl", events = {
   @EventConfig(listeners = EditContainerActionListener.class),
   @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIContainer.deleteContainer"),
   @EventConfig(listeners = UITabContainer.SelectTabActionListener.class)})
public class UITabContainer extends UIContainer
{
   public static final String TAB_CONTAINER = "TabContainer";
   
   public UITabContainer()
   {
      super();
   }
   
   static public class SelectTabActionListener extends EventListener<UITabContainer>
   {
      public void execute(Event<UITabContainer> event) throws Exception
      {
         String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
         UITabContainer container = event.getSource();
         UIComponent goal = container.findComponentById(objectId);
         if (goal == null)
         {
            return;
         }
         UITabContainer parent = goal.getParent();
         List<UIComponent> children = parent.getChildren();
         for (UIComponent child : children)
         {
            if (child.getId().equals(objectId))
            {
               child.setRendered(true);
               continue;
            }
            child.setRendered(false);
         }
      }
   }
}

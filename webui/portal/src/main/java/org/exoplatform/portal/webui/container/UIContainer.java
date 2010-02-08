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
import org.exoplatform.portal.webui.portal.UIPortalComponent;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.List;

/**
 * May 19, 2006
 */
@ComponentConfigs({
   @ComponentConfig(events = {@EventConfig(listeners = UIContainerActionListener.EditContainerActionListener.class),
      @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIContainer.deleteContainer")}),
   @ComponentConfig(id = "TabContainer", template = "system:/groovy/portal/webui/container/UITabContainer.gtmpl", events = {
      @EventConfig(listeners = EditContainerActionListener.class),
      @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIContainer.deleteContainer"),
      @EventConfig(listeners = UIContainer.SelectTabActionListener.class)})})
public class UIContainer extends UIPortalComponent
{

   /** Storage id. */
   private String storageId;

   protected String icon;

   protected String description;

   public UIContainer()
   {
   }

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String s)
   {
      icon = s;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String desc)
   {
      this.description = desc;
   }

   static public class SelectTabActionListener extends EventListener<UIContainer>
   {
      public void execute(Event<UIContainer> event) throws Exception
      {
         String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
         UIContainer container = event.getSource();
         UIComponent goal = container.findComponentById(objectId);
         if (goal == null)
         {
            return;
         }
         UIContainer parent = goal.getParent();
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

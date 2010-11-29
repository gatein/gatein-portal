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

package org.exoplatform.webui.core;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * May 7, 2006
 * 
 * The decorator of a component 
 */
@ComponentConfig(lifecycle = UIComponentDecorator.UIComponentDecoratorLifecycle.class)
@Serialized
public class UIComponentDecorator extends UIComponent
{
   /**
    * The component being decorated
    */
   protected UIComponent uicomponent_;

   public UIComponent getUIComponent()
   {
      return uicomponent_;
   }

   public UIComponent setUIComponent(UIComponent uicomponent)
   {
      UIComponent oldOne = uicomponent_;
      if (uicomponent_ != null)
         uicomponent_.setParent(null);
      uicomponent_ = uicomponent;
      if (uicomponent_ != null)
      {
         UIComponent oldParent = uicomponent_.getParent();
         if (oldParent != null && oldParent != this && oldParent instanceof UIComponentDecorator)
         {
            ((UIComponentDecorator)oldParent).setUIComponent(null);
         }
         uicomponent_.setParent(this);
      }
      return oldOne;
   }

   @SuppressWarnings("unchecked")
   public <T extends UIComponent> T findComponentById(String id)
   {
      if (getId().equals(id))
         return (T)this;
      if (uicomponent_ == null)
         return null;
      return (T)uicomponent_.findComponentById(id);
   }

   public <T extends UIComponent> T findFirstComponentOfType(Class<T> type)
   {
      if (type.isInstance(this))
         return type.cast(this);
      if (uicomponent_ == null)
         return null;
      return uicomponent_.findFirstComponentOfType(type);
   }

   public <T> void findComponentOfType(List<T> list, Class<T> type)
   {
      if (type.isInstance(this))
         list.add(type.cast(this));
      if (uicomponent_ == null)
         return;
      uicomponent_.findComponentOfType(list, type);
   }

   public void renderChildren() throws Exception
   {
      if (uicomponent_ == null)
         return;
      uicomponent_.processRender((WebuiRequestContext)WebuiRequestContext.getCurrentInstance());
   }

   static public class UIComponentDecoratorLifecycle extends Lifecycle<UIComponentDecorator>
   {

      public void processRender(UIComponentDecorator uicomponent, WebuiRequestContext context) throws Exception
      {
         context.getWriter().append("<div class=\"").append(uicomponent.getId()).append("\" id=\"").append(
            uicomponent.getId()).append("\">");
         if (uicomponent.uicomponent_ != null)
         {
            uicomponent.uicomponent_.processRender(context);
         }
         context.getWriter().append("</div>");
      }
   }
}
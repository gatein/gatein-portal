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

package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormPageIterator;
import org.exoplatform.webui.form.UIFormTableInputSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tung.Pham
 *          tung.pham@exoplatform.com
 * Aug 20, 2007  
 */
@ComponentConfig(template = "app:/groovy/applicationregistry/webui/component/UIFormTableIteratorInputSet.gtmpl")
@Serialized
public class UIFormTableIteratorInputSet extends UIFormTableInputSet
{

   UIFormPageIterator uiIterator_;

   public UIFormTableIteratorInputSet() throws Exception
   {
      uiIterator_ = createUIComponent(UIFormPageIterator.class, null, null);
      addChild(uiIterator_);
   }

   public UIFormPageIterator getUIFormPageIterator()
   {
      return uiIterator_;
   }

   @SuppressWarnings("unchecked")
   public UIComponent findComponentById(String lookupId)
   {
      if (uiIterator_.getId().equals(lookupId))
         return uiIterator_;
      return super.findComponentById(lookupId);
   }

   @SuppressWarnings("unchecked")
   public void processDecode(WebuiRequestContext context) throws Exception
   {
      List<UIComponent> children = uiIterator_.getCurrentPageData();
      for (UIComponent child : children)
      {
         List<UIFormInputBase> inputs = new ArrayList<UIFormInputBase>();
         child.findComponentOfType(inputs, UIFormInputBase.class);
         for (UIFormInputBase input : inputs)
         {
            String inputValue = context.getRequestParameter(input.getId());
            if (inputValue == null || inputValue.trim().length() == 0)
            {
               inputValue = context.getRequestParameter(input.getName());
            }
            input.decode(inputValue, context);
         }
         child.processDecode(context);
      }
   }
}

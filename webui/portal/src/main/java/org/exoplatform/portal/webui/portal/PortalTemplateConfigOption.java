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

package org.exoplatform.portal.webui.portal;

import org.exoplatform.webui.core.model.SelectItemOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Dung Ha
 *          ha.pham@exoplatform.com
 * May 11, 2007  
 */
public class PortalTemplateConfigOption extends SelectItemOption<String>
{

   private List<String> accessGroup_;

   public PortalTemplateConfigOption(String label, String value, String desc, String icon) throws Exception
   {
      super(label, value, desc, icon);
      accessGroup_ = new ArrayList<String>();
   }

   public List<String> getGroups()
   {
      return accessGroup_;
   }

   public PortalTemplateConfigOption addGroup(String accessGroup)
   {
      accessGroup_.add(accessGroup);
      return this;
   }
}

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

package org.exoplatform.portal.account;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 */

import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;

@ComponentConfig(template = "system:/groovy/portal/webui/portal/UIAccountSettingForm.gtmpl", events = {@EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class)})
public class UIAccountSetting extends UIContainer
{

   final static public String[] ACTIONS = {"Close"};

   public String[] getActions()
   {
      return ACTIONS;
   }

   public UIAccountSetting() throws Exception
   {
      addChild(UIAccountProfiles.class, null, null);
      addChild(UIAccountChangePass.class, null, null).setRendered(false);
   }
}
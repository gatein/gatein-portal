/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.sample.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Nguyen Duc Khoi
 * khoi.nguyen@exoplatform.com Mar 12, 2010
 */

@ComponentConfig(template = "app:/groovy/webui/component/UISampleTOC.gtmpl", events = {@EventConfig(listeners = UISampleTOC.ClickNodeActionListener.class)})
public class UISampleTOC extends UIContainer
{

   public static class ClickNodeActionListener extends EventListener<UISampleTOC>
   {
      @Override
      public void execute(Event<UISampleTOC> event) throws Exception
      {
         int nodeType = -1;
         try
         {
            nodeType = Integer.valueOf(event.getRequestContext().getRequestParameter(OBJECTID));
         }
         catch (Exception ex)
         {
         }

         ((UISamplePortlet)event.getSource().getParent()).showUIComponent(nodeType);
      }
   }

}

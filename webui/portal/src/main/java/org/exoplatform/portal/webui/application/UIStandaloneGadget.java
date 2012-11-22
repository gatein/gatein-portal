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

package org.exoplatform.portal.webui.application;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "system:/groovy/portal/webui/application/UIStandaloneGadget.gtmpl", events = { @EventConfig(listeners = UIStandaloneGadget.SaveUserPrefActionListener.class) })
public class UIStandaloneGadget extends UIGadget {

    @Override
    public boolean isLossData() {
        DataStorage ds = getApplicationComponent(DataStorage.class);
        try {
            ds.getId(getState());
        } catch (Exception e) {
            return true;
        }
        return super.isLossData();
    }

    public static class SaveUserPrefActionListener extends EventListener<UIStandaloneGadget> {
        public void execute(Event<UIStandaloneGadget> event) throws Exception {
            UIStandaloneGadget uiGadget = event.getSource();

            try {
                uiGadget.addUserPref(event.getRequestContext().getRequestParameter("userPref"));
                event.getRequestContext().setResponseComplete(true);
            } catch (Exception e) {
                event.getRequestContext().addUIComponentToUpdateByAjax(uiGadget.<UIComponent> getParent());
            }
        }
    }
}

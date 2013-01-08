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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL Author : tam.nguyen tamndrok@gmail.com July, 14, 2009
 */
@ComponentConfig(template = "system:/groovy/webui/core/UILazyTabPane.gtmpl", events = { @EventConfig(listeners = UILazyTabPane.SelectTabActionListener.class) })
public class UILazyTabPane extends UIContainer {
    private static String selectedTabId = "";

    public String getSelectedTabId() {
        return selectedTabId;
    }

    public void setSelectedTab(String renderTabId) {
        selectedTabId = renderTabId;
    }

    public void setSelectedTab(int index) {
        selectedTabId = ((UIComponent) getChild(index - 1)).getId();
    }

    public static class SelectTabActionListener extends EventListener<UILazyTabPane> {
        public void execute(Event<UILazyTabPane> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            String renderTab = context.getRequestParameter(UIComponent.OBJECTID);

            UILazyTabPane uicomp = event.getSource();

            UIComponent uiChild = uicomp.getChildById(renderTab);
            if (renderTab == null)
                return;
            selectedTabId = renderTab;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiChild);
        }
    }
}

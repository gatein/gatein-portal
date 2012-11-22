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

import java.util.List;

import org.exoplatform.webui.config.Event;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL Author : Tran The Trong trongtt@gmail.com January 18, 2007
 *
 * A component that creates a popup menu that appears when a right click event fires
 */
@ComponentConfig()
public class UIRightClickPopupMenu extends UIComponent {
    /**
     * The list of actions available in the popup menu
     */
    private String[] actions_;

    public UIRightClickPopupMenu() {
    }

    public String[] getActions() {
        if (actions_ != null)
            return actions_;
        List<Event> events = config.getEvents();
        actions_ = new String[events.size()];
        for (int i = 0; i < actions_.length; i++) {
            actions_[i] = events.get(i).getName();
        }
        return actions_;
    }

    public void setActions(String[] action) {
        this.actions_ = action;
    }

    public CharSequence getJSOnclickShowPopup(String objId, String actions) {
        StringBuilder jsOnclick = new StringBuilder("onmousedown=\"eXo.webui.UIRightClickPopupMenu");
        jsOnclick.append(".clickRightMouse(event, this, '").append(getId()).append('\'');
        // if(objId != null) {
        // objId = objId.replaceAll("'", "\\'") ;
        // objId = objId.replaceAll("\"", "\\\"") ;
        // try {
        // objId = URLEncoder.encode(objId, "utf-8");
        // }catch (Exception e) {
        // System.err.println(e.toString());
        // }
        jsOnclick.append(",'").append(objId).append("'");
        // }
        if (actions != null)
            jsOnclick.append(",'").append(actions).append("'");
        jsOnclick.append(");\"");
        return jsOnclick;
    }
}

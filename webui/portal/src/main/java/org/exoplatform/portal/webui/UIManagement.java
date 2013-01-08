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

package org.exoplatform.portal.webui;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SARL Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Dec 1, 2006
 */
public abstract class UIManagement extends UIContainer {

    protected ManagementMode mode_ = ManagementMode.EDIT;

    public static enum ManagementMode {
        EDIT, BROWSE
    }

    public ManagementMode getMode() {
        return mode_;
    }

    public abstract void setMode(ManagementMode mode, Event<? extends UIComponent> event);

}

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

package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

import javax.portlet.WindowState;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net May 8, 2006
 */
public class UIPageLifecycle extends Lifecycle<UIPage> {

    public void processRender(UIPage uiPage, WebuiRequestContext context) throws Exception {
        uiPage.normalizePortletWindowStates();

        if (uiPage.getMaximizedUIPortlet() != null) {
            UIPortlet uiPortlet = uiPage.getMaximizedUIPortlet();
            uiPortlet.setCurrentWindowState(WindowState.MAXIMIZED);
            uiPortlet.processRender(context);
            return;
        }
        super.processRender(uiPage, context);
    }

}

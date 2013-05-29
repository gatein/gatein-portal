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

package org.exoplatform.toolbar.webui.component;

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.gatein.web.security.impersonation.ImpersonatedIdentity;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh tan.pham@exoplatform.com May 27, 2009
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIStarToolBarPortlet.gtmpl")
public class UIStarToolbarPortlet extends UIPortletApplication {

    public UIStarToolbarPortlet() throws Exception {
    }

    public String getLogoutMessageKey() {
        Identity identity = ConversationState.getCurrent().getIdentity();
        if (identity instanceof ImpersonatedIdentity) {
            return "UIStarToolbarPortlet.item.FinishImpersonation";
        } else {
            return "UIStarToolbarPortlet.item.Logout";
        }
    }
}

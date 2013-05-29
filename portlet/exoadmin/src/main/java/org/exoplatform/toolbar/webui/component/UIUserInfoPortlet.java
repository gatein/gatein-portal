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

import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.gatein.web.security.impersonation.ImpersonatedIdentity;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/admintoolbar/webui/component/UIUserInfoPortlet.gtmpl")
public class UIUserInfoPortlet extends UIPortletApplication {

    public UIUserInfoPortlet() throws Exception {
    }

    public String getUserDisplayName() {
        ConversationState state = ConversationState.getCurrent();
        User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);

        Identity identity = state.getIdentity();
        if (identity instanceof ImpersonatedIdentity) {
            String adminUsername = ((ImpersonatedIdentity) identity).getParentConversationState().getIdentity().getUserId();
            return user.getFullName() + " (" + adminUsername + ")";
        } else {
            return user.getFullName();
        }
    }
}

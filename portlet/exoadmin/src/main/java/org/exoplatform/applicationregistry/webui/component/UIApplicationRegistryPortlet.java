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

package org.exoplatform.applicationregistry.webui.component;

import java.util.ArrayList;

import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.ViewChildActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/applicationregistry/webui/component/UIApplicationRegistryPortlet.gtmpl", events = { @EventConfig(listeners = ViewChildActionListener.class) })
@Serialized
public class UIApplicationRegistryPortlet extends UIPortletApplication {

    public UIApplicationRegistryPortlet() throws Exception {
        addChild(UIApplicationOrganizer.class, null, null).setRendered(true);
        addChild(UIPortletManagement.class, null, null).setRendered(false);
        addChild(UIGadgetManagement.class, null, null).setRendered(false);
        addChild(UIApplicationRegistryEditMode.class, null, null).setRendered(false);
    }

    public static void setPermissionToEveryone(Application app) {
        ArrayList<String> defaultPermission = new ArrayList<String>();
        defaultPermission.add(UserACL.EVERYONE);

        app.setAccessPermissions(defaultPermission);
    }

    public static void setPermissionToAdminGroup(Application app) {
        ArrayList<String> permissions = new ArrayList<String>();
        UserACL acl = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);
        MembershipEntry anyOfAdminGroup = new MembershipEntry(acl.getAdminGroups());
        permissions.add(anyOfAdminGroup.toString());
        app.setAccessPermissions(permissions);
    }
}

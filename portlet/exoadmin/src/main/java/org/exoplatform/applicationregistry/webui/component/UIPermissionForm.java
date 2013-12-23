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
import java.util.Calendar;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.organization.UIListPermissionSelector;

/**
 * Created by The eXo Platform SARL Author : Nguyen Thi Hoa hoa.nguyen@exoplatform.com Sep 26, 2006
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
        @EventConfig(listeners = UIPermissionForm.SelectMembershipActionListener.class),
        @EventConfig(listeners = UIPermissionForm.DeleteActionListener.class),
        @EventConfig(listeners = UIPermissionForm.ChangePublicModeActionListener.class) })
@Serialized
public class UIPermissionForm extends UIForm {

    private Application application_;

    public UIPermissionForm() throws Exception {
        UIListPermissionSelector selector = addChild(UIListPermissionSelector.class, null, "UIListPermissionSelector");
        selector.configure(WebuiRequestContext.generateUUID("UIListPermissionSelector"), "accessPermissions");
        setActions(new String[] {});
    }

    public void setValue(Application app) throws Exception {
        application_ = app;
        ArrayList<String> accessPermissions = application_.getAccessPermissions();
        if (accessPermissions != null) {
            String[] per = new String[accessPermissions.size()];
            getChild(UIListPermissionSelector.class).setValue(accessPermissions.toArray(per));
        }
    }

    public Application getApplication() {
        return application_;
    }

    public void save() throws Exception {
        UIListPermissionSelector uiListPermissionSelector = getChild(UIListPermissionSelector.class);
        ArrayList<String> pers = new ArrayList<String>();
        if (uiListPermissionSelector.getValue() != null)
            for (String per : uiListPermissionSelector.getValue())
                pers.add(per);
        application_.setAccessPermissions(pers);
        ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);
        application_.setModifiedDate(Calendar.getInstance().getTime());
        WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
        if (service.getApplication(application_.getId()) == null) {
            UIApplication uiApp = ctx.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("application.msg.changeNotExist", null));
            return;
        }
        service.update(application_);
        Application selectedApplication = getApplication();
        UIApplicationOrganizer uiApplicationOrganizer = getAncestorOfType(UIApplicationOrganizer.class);
        ApplicationCategory selectedCategory = uiApplicationOrganizer.getSelectedCategory();
        uiApplicationOrganizer.reload();
        uiApplicationOrganizer.setSelectedCategory(selectedCategory);
        uiApplicationOrganizer.setSelectedApplication(selectedApplication);
    }

    public static class SelectMembershipActionListener extends EventListener<UIPermissionForm> {

        public void execute(Event<UIPermissionForm> event) throws Exception {
            UIPermissionForm uiPermissionForm = event.getSource();
            uiPermissionForm.save();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionForm.getParent());
        }

    }

    public static class DeleteActionListener extends EventListener<UIPermissionForm> {

        public void execute(Event<UIPermissionForm> event) throws Exception {
            UIPermissionForm uiPermissionForm = event.getSource();
            uiPermissionForm.save();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionForm.getParent());
        }

    }

    public static class ChangePublicModeActionListener extends EventListener<UIPermissionForm> {

        public void execute(Event<UIPermissionForm> event) throws Exception {
            UIPermissionForm uiPermissionForm = event.getSource();
            uiPermissionForm.save();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionForm.getParent());
        }

    }

}

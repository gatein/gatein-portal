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
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.NotHTMLTagValidator;
import org.exoplatform.webui.form.validator.NullFieldValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL Author : Nguyen Viet Chung nguyenchung136@yahoo.com Jul 28, 2006
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIApplicationForm.SaveActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIApplicationForm.CancelActionListener.class) })
@Serialized
public class UIApplicationForm extends UIForm {

    private Application application_;

    public UIApplicationForm() throws Exception {
        addUIFormInput(new UIFormStringInput("applicationName", "applicationName", null).addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 3, 30).addValidator(NameValidator.class));
        addUIFormInput(new UIFormStringInput("displayName", "displayName", null).addValidator(StringLengthValidator.class, 3,
                30).addValidator(NotHTMLTagValidator.class));
        addUIFormInput(new UIFormTextAreaInput("description", "description", null).addValidator(NullFieldValidator.class)
                .addValidator(NotHTMLTagValidator.class));
    }

    public void setValues(Application app) throws Exception {
        application_ = app;
        if (application_ == null) {
            getUIStringInput("applicationName").setReadOnly(false);
            return;
        }
        getUIStringInput("applicationName").setReadOnly(true);
        invokeGetBindingBean(app);
    }

    public Application getApplication() {
        return application_;
    }

    public static class SaveActionListener extends EventListener<UIApplicationForm> {
        public void execute(Event<UIApplicationForm> event) throws Exception {
            UIApplicationForm uiForm = event.getSource();
            WebuiRequestContext ctx = event.getRequestContext();
            UIApplicationOrganizer uiOrganizer = uiForm.getParent();
            ApplicationRegistryService service = uiForm.getApplicationComponent(ApplicationRegistryService.class);
            Application application = uiForm.getApplication();
            /**
             * Apps IDs have "/" characters.
             * We need to workaround them in the markup to be W3C compliant.
             *
             * "_slash_" -> "/"
             */
            String selectedAppId = application.getId();
            selectedAppId = selectedAppId.replaceAll("_slash_", "/");
            if (service.getApplication(selectedAppId) == null) {
                UIApplication uiApp = ctx.getUIApplication();
                uiApp.addMessage(new ApplicationMessage("application.msg.changeNotExist", null));
                uiOrganizer.reload();
                uiOrganizer.setSelectedCategory(application.getCategoryName());
                ctx.addUIComponentToUpdateByAjax(uiOrganizer);
                return;
            }
            uiForm.invokeSetBindingBean(application);
            application.setModifiedDate(Calendar.getInstance().getTime());
            String displayName = application.getDisplayName();
            if (displayName == null || displayName.trim().length() < 1) {
                application.setDisplayName(application.getApplicationName());
            }
            service.update(application);
            ApplicationCategory selectedCat = uiOrganizer.getSelectedCategory();
            uiOrganizer.reload();
            uiOrganizer.setSelectedCategory(selectedCat);
            uiOrganizer.setSelectedApplication(application);
            ctx.addUIComponentToUpdateByAjax(uiOrganizer);
        }

    }

    public static class CancelActionListener extends EventListener<UIApplicationForm> {
        public void execute(Event<UIApplicationForm> event) throws Exception {
            UIApplicationForm uiForm = event.getSource();
            UIApplicationOrganizer uiOrganizer = uiForm.getParent();
            uiOrganizer.setSelectedApplication(uiOrganizer.getSelectedApplication());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiOrganizer);
        }
    }

}

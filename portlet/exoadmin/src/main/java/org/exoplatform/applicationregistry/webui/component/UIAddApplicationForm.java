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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.applicationregistry.webui.Util;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.SerializablePageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormPageIterator;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTableInputSet;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.federation.FederatingPortletInvoker;

/** Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Jul 10, 2008 */
@ComponentConfig(template = "system:/groovy/webui/form/UIForm.gtmpl", lifecycle = UIFormLifecycle.class, events = {
        @EventConfig(listeners = UIAddApplicationForm.ChangeTypeActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIAddApplicationForm.AddActionListener.class),
        @EventConfig(listeners = UIAddApplicationForm.CancelActionListener.class, phase = Phase.DECODE) })
@Serialized
public class UIAddApplicationForm extends UIForm {

    public static final String FIELD_NAME = "displayName";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_APPLICATION = "application";

    static final String[] TABLE_COLUMNS = { "input", "label", "description" };

    private List<Application> applications_ = new ArrayList<Application>();

    public UIAddApplicationForm() throws Exception {
        addUIFormInput(new UIFormStringInput(FIELD_NAME, null, null).addValidator(StringLengthValidator.class, 3, 30));
        List<SelectItemOption<String>> types = new ArrayList<SelectItemOption<String>>(3);
        types.add(new SelectItemOption<String>(ApplicationType.PORTLET.getName()));
        types.add(new SelectItemOption<String>(ApplicationType.GADGET.getName()));
        types.add(new SelectItemOption<String>(ApplicationType.WSRP_PORTLET.getName()));
        UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_TYPE, null, types);
        uiSelectBox.setOnChange("ChangeType");
        addUIFormInput(uiSelectBox);
        String tableName = getClass().getSimpleName();
        UIFormTableIteratorInputSet uiTableInputSet = createUIComponent(UIFormTableIteratorInputSet.class, null, null);
        uiTableInputSet.setName(tableName);
        uiTableInputSet.setId(tableName);
        uiTableInputSet.setColumns(TABLE_COLUMNS);
        addChild(uiTableInputSet);
        setApplicationList(ApplicationType.PORTLET.getName());
        setActions(new String[] { "Add", "Cancel" });
    }

    public List<Application> getApplications() {
        return applications_;
    }

    public void setApplicationList(String type) throws Exception {
        applications_.clear();
        applications_ = getApplicationByType(type);
        Collections.sort(applications_, new Util.ApplicationComparator());
        setup();
    }

    private void setup() {
        List<UIFormInputSet> uiInputSetList = new ArrayList<UIFormInputSet>();
        UIFormTableInputSet uiTableInputSet = getChild(UIFormTableInputSet.class);
        int i = 0;
        for (Application app : applications_) {
            UIFormInputSet uiInputSet = new UIFormInputSet(app.getId());
            ArrayList<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(5);
            options.add(new SelectItemOption<String>("", String.valueOf(i)));
            UIFormRadioBoxInput uiRadioInput = new UIFormRadioBoxInput(FIELD_APPLICATION, "", options);
            // TODO review
            if (i == 0) {
                uiRadioInput.setValue(options.get(0).getValue());
            }
            // ----------------------------------------------
            uiInputSet.addChild(uiRadioInput);
            UIFormInputInfo uiInfo = new UIFormInputInfo("label", null, app.getDisplayName());
            uiInputSet.addChild(uiInfo);
            uiInfo = new UIFormInputInfo("description", null, app.getDescription());
            uiInputSet.addChild(uiInfo);
            uiTableInputSet.addChild(uiInputSet);
            uiInputSetList.add(uiInputSet);
            i++;
        }
        UIFormPageIterator uiIterator = uiTableInputSet.getChild(UIFormPageIterator.class);
        SerializablePageList<UIFormInputSet> pageList = new SerializablePageList<UIFormInputSet>(UIFormInputSet.class,
                uiInputSetList, 10);
        uiIterator.setPageList(pageList);
    }

    private List<Application> getApplicationByType(String typeName) throws Exception {
        ApplicationType type = ApplicationType.getType(typeName);
        if (ApplicationType.PORTLET == type) {
            return createApplicationsFromPortlets(false);
        } else if (ApplicationType.WSRP_PORTLET == type) {
            return createApplicationsFromPortlets(true);
        } else if (ApplicationType.GADGET == type) {
            GadgetRegistryService gadgetService = getApplicationComponent(GadgetRegistryService.class);
            List<Gadget> gadgets = gadgetService.getAllGadgets();
            List<Application> applications = new ArrayList<Application>(gadgets.size());
            for (Gadget gadget : gadgets) {
                Application app = new Application();
                app.setApplicationName(gadget.getName());
                app.setType(ApplicationType.GADGET);
                app.setDisplayName(gadget.getTitle());
                app.setContentId(gadget.getName());
                String description = (gadget.getDescription() == null || gadget.getDescription().length() < 1) ? gadget
                        .getName() : gadget.getDescription();
                app.setDescription(description);
                app.setAccessPermissions(new ArrayList<String>());
                applications.add(app);
            }
            return applications;
        }

        return Collections.emptyList();
    }

    private List<Application> createApplicationsFromPortlets(boolean remote) throws PortletInvokerException {
        ExoContainer manager = ExoContainerContext.getCurrentContainer();

        FederatingPortletInvoker portletInvoker = (FederatingPortletInvoker) manager
                .getComponentInstance(FederatingPortletInvoker.class);
        Set<Portlet> portlets = remote ? portletInvoker.getRemotePortlets() : portletInvoker.getLocalPortlets();
        List<Application> applications = new ArrayList<Application>(portlets.size());
        for (Portlet portlet : portlets) {
            PortletInfo info = portlet.getInfo();

            LocalizedString descriptionLS = info.getMeta().getMetaValue(MetaInfo.DESCRIPTION);
            LocalizedString displayNameLS = info.getMeta().getMetaValue(MetaInfo.DISPLAY_NAME);

            String portletName = info.getName();
            Application app = new Application();
            app.setApplicationName(portletName);
            // app.setApplicationGroup(info.getApplicationName());
            ApplicationType appType;
            String contentId;
            String displayName = Util.getLocalizedStringValue(displayNameLS, portletName);
            if (remote) {
                appType = ApplicationType.WSRP_PORTLET;
                contentId = portlet.getContext().getId();
                displayName += ApplicationRegistryService.REMOTE_DISPLAY_NAME_SUFFIX; // add remote to display name to make it
                                                                                      // more obvious that the portlet is remote
            } else {
                appType = ApplicationType.PORTLET;
                contentId = info.getApplicationName() + "/" + info.getName();
            }
            app.setType(appType);
            app.setDisplayName(displayName);
            app.setDescription(Util.getLocalizedStringValue(descriptionLS, portletName));
            app.setAccessPermissions(new ArrayList<String>());
            app.setContentId(contentId);
            applications.add(app);
        }

        return applications;
    }

    public static class ChangeTypeActionListener extends EventListener<UIAddApplicationForm> {

        public void execute(Event<UIAddApplicationForm> event) throws Exception {
            UIAddApplicationForm uiForm = event.getSource();
            String type = uiForm.getUIFormSelectBox(UIAddApplicationForm.FIELD_TYPE).getValue();
            uiForm.setApplicationList(type);
            uiForm.getChild(UIFormTableIteratorInputSet.class).setRendered(true);
            if (uiForm.getApplications().size() == 0) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIAddApplicationForm.msg.typeNoApps", null));
                uiForm.getChild(UIFormTableIteratorInputSet.class).setRendered(false);
            }
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }

    }

    public static class AddActionListener extends EventListener<UIAddApplicationForm> {

        public void execute(Event<UIAddApplicationForm> event) throws Exception {
            UIAddApplicationForm uiForm = event.getSource();
            UIApplicationOrganizer uiOrganizer = uiForm.getParent();
            WebuiRequestContext ctx = event.getRequestContext();
            ApplicationRegistryService appRegService = uiForm.getApplicationComponent(ApplicationRegistryService.class);
            ApplicationCategory selectedCate = uiOrganizer.getSelectedCategory();
            if (appRegService.getApplicationCategory(selectedCate.getName()) == null) {
                uiOrganizer.reload();
                UIApplication uiApp = ctx.getUIApplication();
                uiApp.addMessage(new ApplicationMessage("category.msg.changeNotExist", null));
                ctx.addUIComponentToUpdateByAjax(uiOrganizer);
                return;
            }

            if (uiForm.getApplications().size() == 0) {
                ctx.getUIApplication().addMessage(new ApplicationMessage("UIAddApplicationForm.msg.appNotExists", null));
                ctx.addUIComponentToUpdateByAjax(uiOrganizer);
                return;
            }
            UIFormRadioBoxInput uiRadio = uiForm.getUIInput("application");
            String displayName = uiForm.getUIStringInput(FIELD_NAME).getValue();
            Application tmp = uiForm.getApplications().get(Integer.parseInt(uiRadio.getValue()));

            // check portet name is exist
            for (Application application : appRegService.getApplications(selectedCate)) {
                if (application.getContentId().equals(tmp.getContentId())) {
                    ctx.getUIApplication().addMessage(new ApplicationMessage("UIAddApplicationForm.msg.PortletExist", null));
                    return;
                }
            }

            Application app = cloneApplication(tmp);
            UIApplicationRegistryPortlet.setPermissionToAdminGroup(app);

            if (displayName != null && displayName.trim().length() > 0) {
                app.setDisplayName(displayName);
            }

            appRegService.save(selectedCate, app);
            uiOrganizer.reload();
            uiOrganizer.setSelectedCategory(selectedCate.getName());
            uiOrganizer.selectApplication(app.getApplicationName());
            ctx.addUIComponentToUpdateByAjax(uiOrganizer);
        }

        private Application cloneApplication(Application app) {
            Application newApp = new Application();
            newApp.setApplicationName(app.getApplicationName());
            newApp.setDisplayName(app.getDisplayName());
            newApp.setType(app.getType());
            newApp.setDescription(app.getDescription());
            newApp.setAccessPermissions(app.getAccessPermissions());
            newApp.setContentId(app.getContentId());
            return newApp;
        }

    }

    public static class CancelActionListener extends EventListener<UIAddApplicationForm> {

        public void execute(Event<UIAddApplicationForm> event) throws Exception {
            UIApplicationOrganizer uiOrganizer = event.getSource().getParent();
            uiOrganizer.setSelectedApplication(uiOrganizer.getSelectedApplication());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiOrganizer);

        }

    }

}

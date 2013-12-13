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

package org.exoplatform.portal.webui.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/*
 * Created by The eXo Platform SAS
 * Author : tam.nguyen
 *          tamndrok@gmail.com
 * June 11, 2009
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIPageNavigationForm.SaveActionListener.class),
        @EventConfig(listeners = UIPageNavigationForm.CloseActionListener.class, phase = Phase.DECODE, name = "ClosePopup") })
public class UIPageNavigationForm extends UIForm {

    private UserNavigation userNav;

    private String ownerId;

    private String ownerType;

    private String priority;

    public UIPageNavigationForm() {

    }

    public void addFormInput() {
        List<SelectItemOption<String>> priorties = new ArrayList<SelectItemOption<String>>();
        int priority = Integer.parseInt(getPriority());
        if (priority == PageNavigation.UNDEFINED_PRIORITY) {
            ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
            String undefined = resourceBundle.getString("UIPageNavigationForm.priority.undefined");
            priorties.add(new SelectItemOption<String>(undefined, getPriority()));
        }
        for (int i = 1; i < 11; i++) {
            priorties.add(new SelectItemOption<String>(String.valueOf(i), String.valueOf(i)));
        }
        if (priority > 10) {
            priorties.add(new SelectItemOption<String>(getPriority(), getPriority()));
        }

        addUIFormInput(new UIFormStringInput("ownerType", "ownerType", getOwnerType()).setReadOnly(true)).addUIFormInput(
                new UIFormStringInput("ownerId", "ownerId", ownerId).setReadOnly(true)).addUIFormInput(
                new UIFormSelectBox("priority", null, priorties).setValue(getPriority()));
    }

    public void setValues(UserNavigation userNavigation) throws Exception {
        setUserNav(userNavigation);
        invokeGetBindingBean(userNavigation);
        removeChildById("ownerId");
        UIFormStringInput ownerId = new UIFormStringInput("ownerId", "ownerId", userNavigation.getKey().getName());
        ownerId.setReadOnly(true);
        ownerId.setParent(this);
        getChildren().add(1, ownerId);
        UIFormSelectBox uiSelectBox = findComponentById("priority");
        uiSelectBox.setValue(String.valueOf(userNavigation.getPriority()));
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    public void setUserNav(UserNavigation pageNav_) {
        this.userNav = pageNav_;
    }

    public UserNavigation getUserNav() {
        return userNav;
    }

    public static class SaveActionListener extends EventListener<UIPageNavigationForm> {
        public void execute(Event<UIPageNavigationForm> event) throws Exception {
            UIPageNavigationForm uiForm = event.getSource();
            UserNavigation userNav = uiForm.getUserNav();

            // Check existed
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UserPortal userPortal = prContext.getUserPortalConfig().getUserPortal();

            userNav = userPortal.getNavigation(userNav.getKey());

            if (userNav == null) {
                UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
                uiPortalApp.addMessage(new ApplicationMessage("UINavigationManagement.msg.NavigationNotExistAnymore", null));
                UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
                prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
                prContext.setFullRender(true);
                UIPopupWindow uiPopup = uiForm.getParent();
                uiPopup.setShow(false);
                return;
            }

            UIFormSelectBox uiSelectBox = uiForm.findComponentById("priority");
            int priority = Integer.parseInt(uiSelectBox.getValue());

            // update navigation
            NavigationService service = uiForm.getApplicationComponent(NavigationService.class);
            NavigationContext ctx = service.loadNavigation(userNav.getKey());
            ctx.setState(new NavigationState(priority));
            service.saveNavigation(ctx);

            UIPopupWindow uiPopup = uiForm.getParent();
            uiPopup.setShow(false);
            UIComponent opener = uiPopup.getParent();

            ActionResponse response = event.getRequestContext().getResponse();
            response.setEvent(new QName("NavigationChange"), null);

            WebuiRequestContext pcontext = event.getRequestContext();
            pcontext.addUIComponentToUpdateByAjax(opener);
        }
    }

    public static class CloseActionListener extends EventListener<UIPageNavigationForm> {
        public void execute(Event<UIPageNavigationForm> event) throws Exception {
            UIPageNavigationForm uiForm = event.getSource();
            uiForm.<UIComponent> getParent().broadcast(event, Phase.ANY);
        }
    }
}

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

package org.exoplatform.webui.organization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.SerializablePageList;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserACL.Permission;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormGrid;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormPageIterator;
import org.exoplatform.webui.form.UIFormPopupWindow;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.Validator;

/** Created by The eXo Platform SARL Author : Pham Dung Ha ha.pham@exoplatform.com May 7, 2007o */
@ComponentConfig(template = "system:/groovy/organization/webui/component/UIListPermissionSelector.gtmpl", events = {
        @EventConfig(phase = Phase.DECODE, listeners = UIListPermissionSelector.DeleteActionListener.class, confirm = "UIAccessGroup.deleteAccessGroup"),
        @EventConfig(phase = Phase.DECODE, listeners = UIPermissionSelector.SelectMembershipActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIListPermissionSelector.ChangePublicModeActionListener.class) })
@Serialized
public class UIListPermissionSelector extends UISelector<String[]> {
    private boolean publicMode_ = false;

    public UIListPermissionSelector() throws Exception {
        UICheckBoxInput uiPublicMode = new UICheckBoxInput("publicMode", null, false);
        addChild(uiPublicMode);
        UIFormGrid uiGrid = addChild(UIFormGrid.class, null, "PermissionGrid");
        uiGrid.setLabel("UIListPermissionSelector");
        uiGrid.configure("expression", new String[] { "groupId", "membership" }, new String[] { "Delete" });
        UIFormPageIterator uiIterator = (UIFormPageIterator) uiGrid.getUIPageIterator();
        uiIterator
                .setPageList(new SerializablePageList<Permission>(Permission.class, Collections.<Permission> emptyList(), 10));
        addChild(uiIterator);
        uiIterator.setRendered(false);
        UIFormPopupWindow uiPopup = addChild(UIFormPopupWindow.class, null, null);
        uiPopup.setWindowSize(540, 0);

        UIGroupMembershipSelector uiMembershipSelector = createUIComponent(UIGroupMembershipSelector.class, null, null);
        uiMembershipSelector.setId("ListPermissionSelector");
        uiMembershipSelector.getChild(UITree.class).setId("TreeListPermissionSelector");
        uiMembershipSelector.getChild(UIBreadcumbs.class).setId("BreadcumbsListPermissionSelector");
        uiPopup.setUIComponent(uiMembershipSelector);
    }

    public void configure(String iname, String bfield) {
        setName(iname);
        setId(iname);
        setBindingField(bfield);
        UICheckBoxInput uiPublicMode = getChild(UICheckBoxInput.class);
        uiPublicMode.setOnChange("ChangePublicMode", iname);
        UIFormPopupWindow uiPopup = getChild(UIFormPopupWindow.class);
        String prefix = WebuiRequestContext.stripUUIDSuffix(iname);
        uiPopup.setId(prefix + "Popup"+ iname.substring(prefix.length()));
    }

    private boolean existsPermission(List<?> list, Permission permission) {
        for (Object ele : list) {
            Permission per = (Permission) ele;
            if (per.getExpression().equals(permission.getExpression())) {
                return true;
            }
        }
        return false;
    }

    public void clearGroups() {
        UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
        uiIterator.setPageList(new SerializablePageList<Permission>(Permission.class, new ArrayList<Permission>(), 10));
    }

    @SuppressWarnings("unchecked")
    public String[] getValue() throws Exception {
        if (publicMode_) {
            return new String[] { UserACL.EVERYONE };
        }
        UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
        List<Object> values = uiIterator.getPageList().getAll();
        String[] expPermissions = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Permission permission = (Permission) values.get(i);
            expPermissions[i] = permission.getExpression();
        }
        return expPermissions;
    }

    public UIListPermissionSelector setValue(String[] permissions) throws Exception {
        List<Permission> list = new ArrayList<Permission>();
        setPublicMode(false);
        for (String exp : permissions) {
            if (UserACL.EVERYONE.equals(exp)) {
                UIFormGrid uiGrid = getChild(UIFormGrid.class);
                uiGrid.setRendered(false);
                setPublicMode(true);
                break;
            }
            if (exp.trim().length() < 1) {
                continue;
            }
            Permission permission = new Permission();
            permission.setPermissionExpression(exp);
            if (existsPermission(list, permission)) {
                continue;
            }
            list.add(permission);
        }
        UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
        uiIterator.setPageList(new SerializablePageList<Permission>(Permission.class, list, 10));
        return this;
    }

    @SuppressWarnings("unchecked")
    public void removePermission(String exp) throws Exception {
        List<Object> list = new ArrayList<Object>();
        UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
        list.addAll(uiIterator.getPageList().getAll());
        for (Object ele : list) {
            Permission permission = (Permission) ele;
            if (permission.getExpression().equals(exp)) {
                list.remove(ele);
                break;
            }
        }
        uiIterator.setPageList(new SerializablePageList(Permission.class, list, 10));
    }

    @SuppressWarnings("unchecked")
    public void setMembership(String groupId, String membershipType) throws Exception {
        if (groupId.trim().length() < 1 || membershipType.trim().length() < 1) {
            return;
        }
        Permission permission = new Permission();
        permission.setExpression(membershipType + ":" + groupId);
        permission.setGroupId(groupId);
        permission.setMembership(membershipType);
        List<Object> list = new ArrayList<Object>();
        UIPageIterator uiIterator = getChild(UIGrid.class).getUIPageIterator();
        list.addAll(uiIterator.getPageList().getAll());
        if (existsPermission(list, permission)) {
            return;
        }
        list.add(permission);
        uiIterator.setPageList(new SerializablePageList(Permission.class, list, 10));
    }

    public Class<String[]> getTypeValue() {
        return String[].class;
    }

    public String getLabel(String id) {
        String label = null;
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ResourceBundle res = context.getApplicationResourceBundle();
        String key = "UIListPermissionSelector.label." + id;
        try {
            label = res.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("\nkey: " + key);
        }
        return label;
    }

    public boolean isPublicMode() {
        return publicMode_;
    }

    public void setPublicMode(boolean mode) {
        publicMode_ = mode;
        UICheckBoxInput uiPublicMode = getChildById("publicMode");
        uiPublicMode.setChecked(publicMode_);
        UIFormGrid uiGrid = getChild(UIFormGrid.class);
        uiGrid.setRendered(!publicMode_);
        if (publicMode_) {
            uiGrid.getUIPageIterator().setPageList(
                    new SerializablePageList<Permission>(Permission.class, new ArrayList<Permission>(), 10));
        }
    }

    public static class DeleteActionListener extends EventListener<UIListPermissionSelector> {
        public void execute(Event<UIListPermissionSelector> event) throws Exception {
            String permission = event.getRequestContext().getRequestParameter(OBJECTID);
            UIListPermissionSelector uiPermissions = event.getSource();
            UIPageIterator pageIterator = uiPermissions.getChild(UIFormGrid.class).getUIPageIterator();
            int currentPage = pageIterator.getCurrentPage();
            uiPermissions.removePermission(permission);
            UIContainer uiParent = uiPermissions.getParent();
            uiParent.setRenderedChild(UIListPermissionSelector.class);
            UIForm uiForm = uiPermissions.getAncestorOfType(UIForm.class);
            uiForm.broadcast(event, event.getExecutionPhase());
            while (currentPage > pageIterator.getAvailablePage()) {
                currentPage--;
            }
            pageIterator.setCurrentPage(currentPage);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
        }
    }

    // static public class CloseActionListener extends EventListener<UIPopupWindow> {
    // public void execute(Event<UIPopupWindow> event) throws Exception {
    // // UIPopupWindow uiPopupWindow = event.getSource();
    // System.out.println("\n\n\n+++++++++++++++++++>>>>>>>>>>>>>>>>>> HUN");
    // }
    // }

    public static class ChangePublicModeActionListener extends EventListener<UIListPermissionSelector> {
        public void execute(Event<UIListPermissionSelector> event) throws Exception {
            UIListPermissionSelector uicom = event.getSource();
            UICheckBoxInput uiPublicModeInput = uicom.getChildById("publicMode");
            uicom.setPublicMode(uiPublicModeInput.isChecked());
            uicom.setRendered(true);
            UIForm uiForm = uicom.getAncestorOfType(UIForm.class);
            UIPermissionSelector uiPermission = uiForm.findFirstComponentOfType(UIPermissionSelector.class);
            if (uiPermission != null) {
                uiPermission.setRendered(false);
            }

            // julien: UIForm cannot be null otherwise the uiForm.findFirstComponentOfType would have thrown an NPE
            uiForm.broadcast(event, event.getExecutionPhase());
            // event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
            event.getRequestContext().addUIComponentToUpdateByAjax(uicom);
        }

    }

    public static class EmptyIteratorValidator implements Validator, Serializable {
        public void validate(UIFormInput uiInput) throws Exception {
            UIListPermissionSelector uiInputContainer = (UIListPermissionSelector) uiInput;
            if (uiInputContainer.isPublicMode()) {
                return;
            }
            UIFormPageIterator uiInputIterator = uiInputContainer.findFirstComponentOfType(UIFormPageIterator.class);
            if (uiInputIterator.getAvailable() < 1) {
                String[] args = { "UITabPane.title.UIListPermissionSelector" };
                throw new MessageException(new ApplicationMessage("EmptyIteratorValidator.msg.empty", args,
                        ApplicationMessage.INFO));
            }
        }

    }

}

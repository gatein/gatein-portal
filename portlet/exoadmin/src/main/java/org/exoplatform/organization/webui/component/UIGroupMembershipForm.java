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

package org.exoplatform.organization.webui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL Author : chungnv nguyenchung136@yahoo.com Jun 23, 2006 1:55:22 PM
 */
@ComponentConfigs({
        @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/groovy/organization/webui/component/UIGroupMembershipForm.gtmpl", events = {
                @EventConfig(listeners = UIGroupMembershipForm.SaveActionListener.class),
                @EventConfig(phase = Phase.DECODE, listeners = UIGroupMembershipForm.SearchUserActionListener.class),
                @EventConfig(listeners = UIGroupMembershipForm.RefreshActionListener.class, phase = Phase.DECODE) }),

        @ComponentConfig(type = UIPopupWindow.class, id = "SearchUser", template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = {
                @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup"),
                @EventConfig(listeners = UIGroupMembershipForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE),
                @EventConfig(listeners = UIGroupMembershipForm.AddActionListener.class, name = "Add", phase = Phase.DECODE) }) })
@Serialized
public class UIGroupMembershipForm extends UIForm {

    private List<SelectItemOption<String>> listOption = new ArrayList<SelectItemOption<String>>();

    static final String USER_NAME = "username";

    public UIGroupMembershipForm() throws Exception {
        /*
         * addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).addValidator(MandatoryValidator.class)
         * .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d._\\-\\s*,\\s*]+$",
         * "UIGroupMembershipForm.msg.Invalid-char"));
         */
        addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).addValidator(MandatoryValidator.class));
        addUIFormInput(new UIFormSelectBox("membership", "membership", listOption).setSize(1));
        UIPopupWindow searchUserPopup = addChild(UIPopupWindow.class, "SearchUser", "SearchUser");
        searchUserPopup.setWindowSize(640, 0);
        // UIListUsers listUsers = createUIComponent(UIListUsers.class, null, "ListUserForSearch");
        // searchUserPopup.setUIComponent(listUsers);
        // UIGrid grid = listUsers.findFirstComponentOfType(UIGrid.class);
        // grid.setId("SearchUserGrid");
        // grid.configure(grid.getBeanIdField(), grid.getBeanFields(), new String[]{"SelectUser"});
        // grid.getUIPageIterator().setId("SearchUserPageIterator");
        //
        // listUsers.getChild(UISearchForm.class).setId("SearchUserForm");
        loadData();
    }

    public String getUserName() {
        return getUIStringInput(USER_NAME).getValue();
    }

    public String getMembership() {
        return getUIStringInput("membership").getValue();
    }

    private void loadData() throws Exception {
        listOption.clear();
        OrganizationService service = getApplicationComponent(OrganizationService.class);
        List<MembershipType> memberships = (List<MembershipType>) service.getMembershipTypeHandler().findMembershipTypes();
        Collections.sort(memberships, new Comparator<MembershipType>() {
            @Override
            public int compare(MembershipType o1, MembershipType o2) {
                return (o1.getName()).compareTo(o2.getName());
            }
        });
        for (MembershipType mt : memberships) {
            listOption.add(new SelectItemOption<String>(mt.getName(), mt.getName(), mt.getDescription()));
        }
    }

    public void setUserName(String userName) {
        getUIStringInput(USER_NAME).setValue(userName);
    }

    @SuppressWarnings("unchecked")
    public void removeOptionMembershipType(MembershipType membership) {
        for (SelectItemOption op : listOption) {
            if (op.getLabel().equals(membership.getName())) {
                listOption.remove(op);
                break;
            }
        }
    }

    public String event(String eventName, String comId, String beanId) throws Exception {
        UIComponent component = findComponentById(comId);
        if (component == null)
            return super.event(eventName, comId, beanId);
        return component.event(eventName, beanId);
    }

    public void addOptionMembershipType(MembershipType membership) {
        SelectItemOption<String> option = new SelectItemOption<String>(membership.getName(), membership.getName(),
                membership.getDescription());
        listOption.add(option);
    }

    public static class AddActionListener extends EventListener<UIUserSelector> {
        public void execute(Event<UIUserSelector> event) throws Exception {
            UIUserSelector uiForm = event.getSource();
            UIGroupMembershipForm uiParent = uiForm.getAncestorOfType(UIGroupMembershipForm.class);
            uiParent.setUserName(uiForm.getSelectedUsers());
            UIPopupWindow uiPopup = uiParent.getChild(UIPopupWindow.class);
            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiParent);
        }
    }

    public static class CloseActionListener extends EventListener<UIUserSelector> {
        public void execute(Event<UIUserSelector> event) throws Exception {
            UIUserSelector uiForm = event.getSource();
            UIGroupMembershipForm uiParent = uiForm.getAncestorOfType(UIGroupMembershipForm.class);
            UIPopupWindow uiPopup = uiParent.getChild(UIPopupWindow.class);
            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiParent);
        }
    }

    @Override
    public void begin() throws Exception {
        // TODO Auto-generated method stub
        super.begin();
    }

    public static class SaveActionListener extends EventListener<UIGroupMembershipForm> {
        public void execute(Event<UIGroupMembershipForm> event) throws Exception {
            UIGroupMembershipForm uiForm = event.getSource();
            UIUserInGroup userInGroup = uiForm.getParent();
            OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
            MembershipHandler memberShipHandler = service.getMembershipHandler();
            UIApplication uiApp = event.getRequestContext().getUIApplication();

            Group group = userInGroup.getSelectedGroup();
            MembershipType membershipType = service.getMembershipTypeHandler().findMembershipType(uiForm.getMembership());
            if (group == null) {
                uiApp.addMessage(new ApplicationMessage("UIGroupMembershipForm.msg.group-not-select", null));
                return;
            }

            // add new
            List<String> userNames = Arrays.asList(uiForm.getUserName().trim().split("\\s*,\\s*"));
            if (new HashSet<String>(userNames).size() != userNames.size()) {
                uiApp.addMessage(new ApplicationMessage("UIGroupMembershipForm.msg.duplicate-user", null));
                return;
            }

            // check user
            boolean check = false;
            String listNotExist = null;
            for (String username : userNames) {
                if (username == null || username.trim().length() == 0)
                    continue;
                User user = service.getUserHandler().findUserByName(username);
                if (user == null) {
                    check = true;
                    if (listNotExist == null)
                        listNotExist = username;
                    else
                        listNotExist += ", " + username;
                }
            }
            if (check) {
                ApplicationMessage msg = new ApplicationMessage("UIGroupMembershipForm.msg.user-not-exist",
                        new String[] { listNotExist });
                msg.setArgsLocalized(false);
                uiApp.addMessage(msg);
                return;
            }

            // check membership
            String listUserMembership = null;
            for (String username : userNames) {
                if (username == null || username.trim().length() == 0)
                    continue;
                Membership membership = memberShipHandler.findMembershipByUserGroupAndType(username, group.getId(),
                        membershipType.getName());
                if (membership != null) {
                    check = true;
                    if (listUserMembership == null)
                        listUserMembership = username;
                    else
                        listUserMembership += ", " + username;
                }
            }
            if (check) {
                uiApp.addMessage(new ApplicationMessage("UIGroupMembershipForm.msg.membership-exist", new String[] {
                        listUserMembership, group.getGroupName() }));
                return;
            }

            for (String username : userNames) {
                if (username == null || username.trim().length() == 0)
                    continue;
                User user = service.getUserHandler().findUserByName(username);
                memberShipHandler.linkMembership(user, group, membershipType, true);
            }
            userInGroup.refresh();
            uiForm.reset();
        }
    }

    public static class SearchUserActionListener extends EventListener<UIGroupMembershipForm> {
        public void execute(Event<UIGroupMembershipForm> event) throws Exception {
            UIGroupMembershipForm uiGroupForm = event.getSource();
            UIPopupWindow searchUserPopup = uiGroupForm.getChild(UIPopupWindow.class);
            UIUserSelector userSelector = uiGroupForm.createUIComponent(UIUserSelector.class, null, null);
            userSelector.setShowSearchGroup(false);
            String userName = uiGroupForm.getUIStringInput(USER_NAME).getValue();
            userSelector.getUIStringInput(UIUserSelector.FIELD_KEYWORD).setValue(userName);
            searchUserPopup.setUIComponent(userSelector);
            searchUserPopup.setShow(true);

            // TODO tamnd PORTAL-3010 searching a user in the add member form does not work.
            String filter = userSelector.getUIFormSelectBox(UIUserSelector.FIELD_FILTER).getValue();
            String groupId = userSelector.getSelectedGroup();

            userSelector.search(userName, filter, groupId);
            // modified by Pham Dinh Tan
            // UIListUsers form = (UIListUsers) searchUserPopup.getUIComponent();
            // String name = uiGroupForm.getUIStringInput("username").getValue();
            // UISearchForm uiSearchForm = form.getUISearchForm();
            // uiSearchForm.getUIStringInput("searchTerm").setValue(name);
            // uiSearchForm.getUIFormSelectBox("searchOption").setValue(UIListUsers.USER_NAME);
            // form.quickSearch(uiSearchForm.getQuickSearchInputSet();
            // Query query = new Query();
            // String name = uiGroupForm.getUIStringInput("username").getValue();
            // if(name == null || name.length() < 1){ name = "*"; }
            // else { name = "*" + name + "*"; }
            // query.setUserName(name) ;
            // form.getUISearchForm().getUIStringInput("searchTerm").setValue(name);
            // form.search(query );
        }
    }

    public static class RefreshActionListener extends EventListener<UIGroupMembershipForm> {
        public void execute(Event<UIGroupMembershipForm> event) throws Exception {
            UIGroupMembershipForm uiForm = event.getSource();
            uiForm.loadData();
        }
    }
}

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
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UISearch;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL Author : chungnv nguyenchung136@yahoo.com Jun 23, 2006 10:07:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
        @EventConfig(listeners = UIListUsers.DisableEnableUserActionListener.class),
        @EventConfig(listeners = UIListUsers.ConfirmCloseActionListener.class),
        @EventConfig(listeners = UIListUsers.AbortCloseActionListener.class),
        @EventConfig(listeners = UIListUsers.ViewUserInfoActionListener.class),
        @EventConfig(listeners = UIListUsers.SelectUserActionListener.class),
        @EventConfig(listeners = UIListUsers.DeleteUserActionListener.class, confirm = "UIListUsers.deleteUser") })
@Serialized
public class UIListUsers extends UISearch {

    public static final String USER_NAME = "userName";

    public static final String LAST_NAME = "lastName";

    public static final String FIRST_NAME = "firstName";

    public static final String EMAIL = "email";

    public static final String USER_STATUS_FILTER = "userStatusFilter";

    private static final String[] USER_BEAN_FIELD = { USER_NAME, LAST_NAME, FIRST_NAME, EMAIL };

    private static final String[] USER_ACTION = { "ViewUserInfo", "DeleteUser" };

    private static final List<SelectItemOption<String>> OPTIONS_ = Collections.unmodifiableList(Arrays.asList(
            new SelectItemOption<String>(USER_NAME, USER_NAME), new SelectItemOption<String>(LAST_NAME, LAST_NAME),
            new SelectItemOption<String>(FIRST_NAME, FIRST_NAME), new SelectItemOption<String>(EMAIL, EMAIL)));

    private static final List<SelectItemOption<String>> USER_STATUS_OPTIONS = Collections.unmodifiableList(Arrays.asList(
                new SelectItemOption<String>("Enabled", UserStatus.ENABLED.name()),
                new SelectItemOption<String>("Disabled", UserStatus.DISABLED.name()),
                new SelectItemOption<String>("All", UserStatus.BOTH.name())
            ));

    private OrganizationService orgService;
    private Query lastQuery_;
    private UserStatus statusFilter = UserStatus.ENABLED;

    private String userSelected_;

    private UIGridUsers grid_;

    public UIListUsers() throws Exception {
        super(OPTIONS_);

        UIFormInputSet inputSet = getUISearchForm().getQuickSearchInputSet();

        boolean showDisableUserFilterCheckbox = true;
        orgService = this.getApplicationComponent(OrganizationService.class);
        if(orgService instanceof PicketLinkIDMOrganizationServiceImpl
                && !((PicketLinkIDMOrganizationServiceImpl) orgService).getConfiguration().isFilterDisabledUsersInQueries()) {
            showDisableUserFilterCheckbox = false;
        }

        if(showDisableUserFilterCheckbox) {
            UIFormSelectBox selectBox = new UIFormSelectBox("UIListUsers-" + USER_STATUS_FILTER, null, USER_STATUS_OPTIONS);
            selectBox.setValue(UserStatus.ENABLED.name());
            selectBox.setId("UIListUsers-" + USER_STATUS_FILTER);
            inputSet.addChild(selectBox);
        }

        grid_ = addChild(UIGridUsers.class, null, "UIListUsersGird");
        grid_.configure(USER_NAME, USER_BEAN_FIELD, USER_ACTION);
        grid_.getUIPageIterator().setId("UIListUsersIterator");
        grid_.getUIPageIterator().setParent(this);

        UIConfirmation uiConfirmation = addChild(UIConfirmation.class, null, null);
        uiConfirmation.setCaller(this);
        createActionConfirms(uiConfirmation);
    }

    /**
     * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
     */
    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        int curPage = grid_.getUIPageIterator().getCurrentPage();
        if (lastQuery_ == null)
            lastQuery_ = new Query();
        search(lastQuery_);
        grid_.getUIPageIterator().setCurrentPage(curPage);
        grid_.getUIPageIterator().getCurrentPageData();
        super.processRender(context);
    }

    public void setUserSelected(String userName) {
        userSelected_ = userName;
    }

    public String getUserSelected() {
        return userSelected_;
    }

    public void search(Query query) {
        lastQuery_ = query;
        grid_.getUIPageIterator().setPageList(new FindUsersPageList(query, 10, statusFilter));
    }

    public void quickSearch(UIFormInputSet quickSearchInput) throws Exception {
        Query query = new Query();
        UIFormStringInput input = (UIFormStringInput) quickSearchInput.getChild(0);
        UIFormSelectBox select = (UIFormSelectBox) quickSearchInput.getChild(1);
        String name = input.getValue();
        if (name != null && !(name = name.trim()).equals("")) {
            if (name.indexOf("*") < 0) {
                if (name.charAt(0) != '*')
                    name = "*" + name;
                if (name.charAt(name.length() - 1) != '*')
                    name += "*";
            }
            name = name.replace('?', '_');
            String selectBoxValue = select.getValue();
            if (selectBoxValue.equals(USER_NAME))
                query.setUserName(name);
            if (selectBoxValue.equals(LAST_NAME))
                query.setLastName(name);
            if (selectBoxValue.equals(FIRST_NAME))
                query.setFirstName(name);
            if (selectBoxValue.equals(EMAIL))
                query.setEmail(name);
        }

        //Fetch user status
        UIFormSelectBox selectBox = getUISearchForm().getQuickSearchInputSet()
                .getChildById("UIListUsers-" + USER_STATUS_FILTER);
        if(selectBox != null) {
            String status = selectBox.getValue();
            statusFilter = UserStatus.valueOf(status);
        } else {
            statusFilter = UserStatus.BOTH;
        }

        search(query);

        if (getChild(UIGridUsers.class).getUIPageIterator().getAvailable() == 0) {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.empty", null));
        }
    }

    @SuppressWarnings("unused")
    public void advancedSearch(UIFormInputSet advancedSearchInput) {
    }

    public void showConfirmWindow(String message) {
        UIConfirmation uiConfirmation = getChild(UIConfirmation.class);
        uiConfirmation.setMessage(message);
        ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiConfirmation);
    }

    public void createActionConfirms(UIConfirmation uiConfirmation) {
        ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
        String yes = resourceBundle.getString("UIEditInlineWorkspace.confirm.yes");
        String no = resourceBundle.getString("UIEditInlineWorkspace.confirm.no");

        List<UIConfirmation.ActionConfirm> actionConfirms = new ArrayList<UIConfirmation.ActionConfirm>();
        actionConfirms.add(new UIConfirmation.ActionConfirm("ConfirmClose", yes));
        actionConfirms.add(new UIConfirmation.ActionConfirm("AbortClose", no));
        uiConfirmation.setActions(actionConfirms);
    }

    private boolean executeDisableUser(Event<UIListUsers> event, String userName) throws Exception {
        if(userName == null) {
            return false;
        }

        UIListUsers uiListUser = event.getSource();
        OrganizationService service = uiListUser.getApplicationComponent(OrganizationService.class);

        User user = service.getUserHandler().findUserByName(userName, UserStatus.BOTH);

        if(user == null) {
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIListUsers.msg.user-is-deleted", null, ApplicationMessage.WARNING));
            return false;
        }

        UserACL userACL = uiListUser.getApplicationComponent(UserACL.class);
        if (userACL.getSuperUser().equals(userName) && user.isEnabled()) {
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIListUsers.msg.DisableSuperUser", new String[] { userName },
                    ApplicationMessage.WARNING));
            return false;
        }

        service.getUserHandler().setEnabled(userName, !user.isEnabled(), true);
        return true;
    }

    public static class ViewUserInfoActionListener extends EventListener<UIListUsers> {
        public void execute(Event<UIListUsers> event) throws Exception {
            String username = event.getRequestContext().getRequestParameter(OBJECTID);
            UIListUsers uiListUsers = event.getSource();
            OrganizationService service = uiListUsers.getApplicationComponent(OrganizationService.class);
            User user = service.getUserHandler().findUserByName(username, UserStatus.BOTH);
            if (user == null) {
                UIApplication uiApplication = event.getRequestContext().getUIApplication();
                uiApplication.addMessage(new ApplicationMessage("UIListUsers.msg.user-is-deleted", null,
                        ApplicationMessage.WARNING));
                return;
            }
            if (!user.isEnabled()) {
                UIApplication uiApplication = event.getRequestContext().getUIApplication();
                uiApplication.addMessage(new ApplicationMessage("UIListUsers.msg.user-is-disabled", null,
                        ApplicationMessage.WARNING));
                return;
            }
            uiListUsers.setRendered(false);
            UIUserManagement uiUserManager = uiListUsers.getParent();
            UIUserInfo uiUserInfo = uiUserManager.getChild(UIUserInfo.class);
            uiUserInfo.setUser(username);
            uiUserInfo.setRendered(true);

            UIComponent uiToUpdateAjax = uiListUsers.getAncestorOfType(UIUserManagement.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiToUpdateAjax);
        }
    }

    public static class DeleteUserActionListener extends EventListener<UIListUsers> {
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers uiListUser = event.getSource();
            String userName = event.getRequestContext().getRequestParameter(OBJECTID);
            OrganizationService service = uiListUser.getApplicationComponent(OrganizationService.class);
            UserACL userACL = uiListUser.getApplicationComponent(UserACL.class);
            if (userACL.getSuperUser().equals(userName)) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIListUsers.msg.DeleteSuperUser", new String[] { userName },
                        ApplicationMessage.WARNING));
                return;
            }
            UIPageIterator pageIterator = uiListUser.getChild(UIGridUsers.class).getUIPageIterator();
            int currentPage = pageIterator.getCurrentPage();
            service.getUserHandler().removeUser(userName, true);
            uiListUser.search(uiListUser.lastQuery_);
            while (currentPage > pageIterator.getAvailablePage())
                currentPage--;
            pageIterator.setCurrentPage(currentPage);
            UIComponent uiToUpdateAjax = uiListUser.getAncestorOfType(UIUserManagement.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiToUpdateAjax);
        }
    }

    public static class SelectUserActionListener extends EventListener<UIListUsers> {
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers uiListUser = event.getSource();
            String userName = event.getRequestContext().getRequestParameter(OBJECTID);
            UIPopupWindow popup = uiListUser.getAncestorOfType(UIPopupWindow.class);
            popup.setShow(false);
            UIGroupMembershipForm groupMembershipForm = popup.getParent();
            groupMembershipForm.setUserName(userName);
            event.getRequestContext().addUIComponentToUpdateByAjax(groupMembershipForm);
        }
    }

    public static class DisableEnableUserActionListener extends EventListener<UIListUsers> {
        @Override
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers uiListUser = event.getSource();
            String userName = event.getRequestContext().getRequestParameter(OBJECTID);
            OrganizationService service = uiListUser.getApplicationComponent(OrganizationService.class);

            if(userName != null && userName.equals(event.getRequestContext().getRemoteUser())) {
                //Need to confirm to disable current user
                //Current user will be disable on confirmed
                ResourceBundle resourceBundle = event.getRequestContext().getApplicationResourceBundle();
                String disableMessage = resourceBundle.getString("UIListUsers.msg.DisableYourSelf");
                uiListUser.showConfirmWindow(disableMessage);
                return;
            }

            uiListUser.executeDisableUser(event, userName);
        }
    }

    public static class ConfirmCloseActionListener extends EventListener<UIListUsers> {

        @Override
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers uiListUsers = event.getSource();

            UIConfirmation uiConfirmation = uiListUsers.getChild(UIConfirmation.class);
            uiConfirmation.createEvent("Close", event.getExecutionPhase(), event.getRequestContext()).broadcast();

            //Disable current user
            String userName = event.getRequestContext().getRemoteUser();
            if(userName != null) {
                if(uiListUsers.executeDisableUser(event, userName)) {
                    LogoutControl.wantLogout();
                }
            }
            UIComponent uiToUpdateAjax = uiListUsers.getAncestorOfType(UIUserManagement.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiToUpdateAjax);
        }
    }

    public static class AbortCloseActionListener extends EventListener<UIListUsers> {

        @Override
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers uiListUsers = event.getSource();
            UIConfirmation uiConfirmation = uiListUsers.getChild(UIConfirmation.class);
            uiConfirmation.createEvent("Close", event.getExecutionPhase(), event.getRequestContext()).broadcast();
        }
    }
}

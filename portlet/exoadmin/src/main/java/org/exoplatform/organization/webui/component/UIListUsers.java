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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIGrid;
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
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL Author : chungnv nguyenchung136@yahoo.com Jun 23, 2006 10:07:15 AM
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
        @EventConfig(listeners = UIListUsers.DisableEnableUserActionListener.class),
        @EventConfig(listeners = UIListUsers.ShowDisabledUserActionListener.class),
        @EventConfig(listeners = UIListUsers.ViewUserInfoActionListener.class),
        @EventConfig(listeners = UIListUsers.SelectUserActionListener.class),
        @EventConfig(listeners = UIListUsers.DeleteUserActionListener.class, confirm = "UIListUsers.deleteUser") })
@Serialized
public class UIListUsers extends UISearch {

    public static final String USER_NAME = "userName";

    public static final String LAST_NAME = "lastName";

    public static final String FIRST_NAME = "firstName";

    public static final String EMAIL = "email";

    public static final String SHOW_DISABLED_USER = "showDisabledUser";

    private static boolean enabledOnly = true;

    private static final String[] USER_BEAN_FIELD = { USER_NAME, LAST_NAME, FIRST_NAME, EMAIL };

    private static final String[] USER_ACTION = { "DisableEnableUser", "ViewUserInfo", "DeleteUser" };

    private static final List<SelectItemOption<String>> OPTIONS_ = Collections.unmodifiableList(Arrays.asList(
            new SelectItemOption<String>(USER_NAME, USER_NAME), new SelectItemOption<String>(LAST_NAME, LAST_NAME),
            new SelectItemOption<String>(FIRST_NAME, FIRST_NAME), new SelectItemOption<String>(EMAIL, EMAIL)));

    private OrganizationService orgService;
    private Query lastQuery_;

    private String userSelected_;

    private UIGrid grid_;

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
            UICheckBoxInput disabledUserFilter = new UICheckBoxInput(SHOW_DISABLED_USER, null, false);
            disabledUserFilter.setId("UIListUsers-" + SHOW_DISABLED_USER);
            disabledUserFilter.setOnChange("ShowDisabledUser");
            inputSet.addChild(disabledUserFilter);
        }

        grid_ = addChild(UIGrid.class, null, "UIListUsersGird");
        grid_.configure(USER_NAME, USER_BEAN_FIELD, USER_ACTION);
        grid_.getUIPageIterator().setId("UIListUsersIterator");
        grid_.getUIPageIterator().setParent(this);
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
        grid_.getUIPageIterator().setPageList(new FindUsersPageList(query, 10, enabledOnly));
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
        search(query);

        if (getChild(UIGrid.class).getUIPageIterator().getAvailable() == 0) {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.empty", null));
        }
    }

    @SuppressWarnings("unused")
    public void advancedSearch(UIFormInputSet advancedSearchInput) {
    }

    public static class ViewUserInfoActionListener extends EventListener<UIListUsers> {
        public void execute(Event<UIListUsers> event) throws Exception {
            String username = event.getRequestContext().getRequestParameter(OBJECTID);
            UIListUsers uiListUsers = event.getSource();
            OrganizationService service = uiListUsers.getApplicationComponent(OrganizationService.class);
            User user = service.getUserHandler().findUserByName(username, false);
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
            UIPageIterator pageIterator = uiListUser.getChild(UIGrid.class).getUIPageIterator();
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

            User user = service.getUserHandler().findUserByName(userName, false);
            if(user == null) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIListUsers.msg.user-is-deleted", null, ApplicationMessage.WARNING));
                return;
            }

            UserACL userACL = uiListUser.getApplicationComponent(UserACL.class);
            if (userACL.getSuperUser().equals(userName) && user.isEnabled()) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIListUsers.msg.DisableSuperUser", new String[] { userName },
                        ApplicationMessage.WARNING));
                return;
            }

            if (user.isEnabled()) {
                service.getUserHandler().setEnabled(userName, false, true);
            } else {
                service.getUserHandler().setEnabled(userName, true, true);
            }
        }
    }

    public static class ShowDisabledUserActionListener extends EventListener<UIListUsers> {
        @Override
        public void execute(Event<UIListUsers> event) throws Exception {
            UIListUsers listUsers = event.getSource();
            UICheckBoxInput checkbox = (UICheckBoxInput) listUsers.getUISearchForm().getQuickSearchInputSet()
                    .getChildById("UIListUsers-" + SHOW_DISABLED_USER);
            enabledOnly = checkbox.isChecked() ? false : true;
        }
    }
}

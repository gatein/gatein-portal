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

package org.exoplatform.navigation.webui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.exoplatform.navigation.webui.TreeNode;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.navigation.UIAddGroupNavigation;
import org.exoplatform.portal.webui.navigation.UIPageNavigationForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS
 * Author : tam.nguyen
 *          tamndrok@gmail.com
 * May 28, 2009
 */
@ComponentConfigs({
        @ComponentConfig(template = "app:/groovy/navigation/webui/component/UIGroupNavigationManagement.gtmpl", events = {
                @EventConfig(listeners = UIGroupNavigationManagement.EditNavigationActionListener.class),
                @EventConfig(listeners = UIGroupNavigationManagement.EditPropertiesActionListener.class),
                @EventConfig(listeners = UIGroupNavigationManagement.AddNavigationActionListener.class),
                @EventConfig(listeners = UIGroupNavigationManagement.DeleteNavigationActionListener.class, confirm = "UIGroupNavigationManagement.Delete.Confirm") }),
        @ComponentConfig(id = "UIGroupNavigationGrid", type = UIRepeater.class, template = "app:/groovy/navigation/webui/component/UINavigationGrid.gtmpl"),
        @ComponentConfig(type = UIPageNodeForm.class, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
                @EventConfig(listeners = UIPageNodeForm.SaveActionListener.class),
                @EventConfig(listeners = UIGroupNavigationManagement.BackActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.ChangeLanguageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchLabelModeActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchVisibleActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.ClearPageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.CreatePageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SelectTabActionListener.class, phase = Phase.DECODE) }),
        @ComponentConfig(type = UIPopupWindow.class, template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = @EventConfig(listeners = UIGroupNavigationManagement.CloseActionListener.class, name = "ClosePopup")) })
public class UIGroupNavigationManagement extends UIContainer {

    private UINavigationManagement naviManager;

    // set navigationScope to GrandChildren for default value
    private Scope navigationScope = Scope.GRANDCHILDREN;

    public UIGroupNavigationManagement() throws Exception {
        UIVirtualList virtualList = addChild(UIVirtualList.class, null, "GroupNavigationList");
        UIRepeater repeater = createUIComponent(UIRepeater.class, "UIGroupNavigationGrid", null);
        virtualList.setUIComponent(repeater);
        UIPopupWindow editNavigation = addChild(UIPopupWindow.class, null, null);
        editNavigation.setId(editNavigation.getId() + "-" + UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public void loadNavigations() throws Exception {
        UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();

        List<UserNavigation> allNavs = userPortal.getNavigations();
        final List<UserNavigation> groupNav = new ArrayList<UserNavigation>();
        for (UserNavigation nav : allNavs) {
            if (nav.getKey().getType().equals(SiteType.GROUP) && nav.isModifiable()) {
                groupNav.add(nav);
            }
        }

        //
        UIVirtualList virtualList = getChild(UIVirtualList.class);
        final int pageSize = 4;
        Iterator<List<?>> source = new Iterator<List<?>>() {
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < groupNav.size();
            }

            @Override
            public List<UserNavigation> next() {
                if (hasNext()) {
                    List<UserNavigation> list = new ArrayList<UserNavigation>(pageSize);
                    for (int i = currentIndex; i < currentIndex + pageSize; i++) {
                        if (i < groupNav.size()) {
                            UserNavigation u = groupNav.get(i);
                            list.add(u);
                        } else {
                            break;
                        }
                    }

                    //
                    currentIndex += pageSize;
                    return list;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        virtualList.dataBind(source);
        virtualList.setAutoAdjustHeight(true);
    }

    public void setScope(Scope scope) {
        this.navigationScope = scope;
    }

    public Scope getScope() {
        return this.navigationScope;
    }

    /**
     * User has right to add navigation to a group in below cases
     *
     * 1. He/She is member of admin groups
     *
     * 2. He/She is manager of the group
     *
     * @param
     * @return
     */
    private boolean userHasRightToAddNavigation() {
        PortalRequestContext pcontext = Util.getPortalRequestContext();
        String remoteUser = pcontext.getRemoteUser();
        if (remoteUser == null) {
            return false;
        }

        UserACL userACL = this.getApplicationComponent(UserACL.class);
        if (userACL.isUserInGroup(userACL.getAdminGroups())) {
            return true;
        }

        OrganizationService orgService = this.getApplicationComponent(OrganizationService.class);
        try {
            Collection<?> groups = orgService.getGroupHandler().findGroupByMembership(remoteUser, userACL.getMakableMT());
            return groups != null && groups.size() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public abstract static class BaseEditAction extends EventListener<UIGroupNavigationManagement> {
        public void execute(Event<UIGroupNavigationManagement> event) throws Exception {
            UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
            UIGroupNavigationManagement uicomp = event.getSource();
            WebuiRequestContext context = event.getRequestContext();
            UIApplication uiApplication = context.getUIApplication();

            // get navigation id
            String groupName = event.getRequestContext().getRequestParameter(OBJECTID);
            SiteKey siteKey = SiteKey.group(groupName);

            // check edit permission, ensure that user has edit permission on that
            // navigation
            UserACL userACL = uicomp.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermissionOnNavigation(siteKey)) {
                uiApplication
                        .addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.Invalid-editPermission", null));
                return;
            }

            UserNavigation navigation = userPortal.getNavigation(siteKey);
            if (navigation == null) {
                uiApplication.addMessage(new ApplicationMessage("UIGroupNavigationManagement.msg.navigation-not-exist", null));
                UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
                uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
                return;
            }

            doEdit(navigation, event);
        }

        protected abstract void doEdit(UserNavigation navigation, Event<UIGroupNavigationManagement> event) throws Exception;
    }

    public static class EditNavigationActionListener extends BaseEditAction {
        @Override
        protected void doEdit(UserNavigation nav, Event<UIGroupNavigationManagement> event) throws Exception {
            UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
            UIGroupNavigationManagement uicomp = event.getSource();
            SiteKey siteKey = nav.getKey();

            UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);
            UINavigationManagement naviManager = popUp.createUIComponent(UINavigationManagement.class, null, null, popUp);
            uicomp.naviManager = naviManager;

            naviManager.setSiteKey(siteKey);

            UINavigationNodeSelector selector = naviManager.getChild(UINavigationNodeSelector.class);
            selector.setEdittedNavigation(nav);
            selector.setUserPortal(userPortal);
            selector.setScope(uicomp.getScope());
            selector.initTreeData();

            popUp.setUIComponent(naviManager);
            popUp.setWindowSize(400, 400);
            popUp.setShowMask(true);
            popUp.setShow(true);
        }
    }

    public static class EditPropertiesActionListener extends BaseEditAction {
        @Override
        protected void doEdit(UserNavigation navigation, Event<UIGroupNavigationManagement> event) throws Exception {
            UIGroupNavigationManagement uicomp = event.getSource();
            SiteKey siteKey = navigation.getKey();

            // open a add navigation popup
            UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);
            UIPageNavigationForm pageNavigation = popUp.createUIComponent(UIPageNavigationForm.class, null, null, popUp);
            pageNavigation.setOwnerId(siteKey.getName());
            pageNavigation.setOwnerType(siteKey.getTypeName());
            pageNavigation.setPriority(String.valueOf(navigation.getPriority()));
            pageNavigation.addFormInput();
            pageNavigation.setUserNav(navigation);
            popUp.setUIComponent(pageNavigation);
            popUp.setWindowSize(600, 400);
            popUp.setShowMask(true);
            popUp.setShow(true);
        }
    }

    public static class DeleteNavigationActionListener extends BaseEditAction {
        @Override
        protected void doEdit(UserNavigation navigation, Event<UIGroupNavigationManagement> event) throws Exception {
            UIGroupNavigationManagement uicomp = event.getSource();
            NavigationService service = uicomp.getApplicationComponent(NavigationService.class);

            NavigationContext ctx = service.loadNavigation(navigation.getKey());
            if (ctx != null) {
                service.destroyNavigation(ctx);
            }

            //
            event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);
            // Update UserToolbarGroupPortlet
            UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
            uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
        }
    }

    public static class AddNavigationActionListener extends EventListener<UIGroupNavigationManagement> {
        public void execute(Event<UIGroupNavigationManagement> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            // UIGroupNavigationManagement uicomp = event.getSource();
            UIPortalApplication uiApp = (UIPortalApplication) prContext.getUIApplication();
            // UIGroupNavigationPortlet uiPortlet = (UIGroupNavigationPortlet) uicomp.getParent();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

            UIAddGroupNavigation uiNewPortal = uiMaskWS.createUIComponent(UIAddGroupNavigation.class, null, null);
            uiMaskWS.setUIComponent(uiNewPortal);
            uiMaskWS.setShow(true);
            prContext.addUIComponentToUpdateByAjax(uiMaskWS);

            // If other users has add or remove group navigation, we need to refresh this portlet
            UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
            uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
        }
    }

    public static class BackActionListener extends EventListener<UIPageNodeForm> {

        public void execute(Event<UIPageNodeForm> event) throws Exception {
            UIPageNodeForm uiPageNodeForm = event.getSource();
            UIGroupNavigationManagement uiGroupNavigation = uiPageNodeForm.getAncestorOfType(UIGroupNavigationManagement.class);
            UINavigationManagement navigationManager = uiGroupNavigation.naviManager;

            UINavigationNodeSelector selector = navigationManager.getChild(UINavigationNodeSelector.class);
            TreeNode selectedParent = (TreeNode) uiPageNodeForm.getSelectedParent();
            selector.selectNode(selectedParent);

            UIPopupWindow uiNavigationPopup = uiGroupNavigation.getChild(UIPopupWindow.class);
            uiNavigationPopup.setUIComponent(navigationManager);
            uiNavigationPopup.setWindowSize(400, 400);
            uiNavigationPopup.setRendered(true);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigationPopup.getParent());

            TreeNode pageNode = uiPageNodeForm.getPageNode();
            if (pageNode != null) {
                selector.getUserNodeLabels().put(pageNode.getId(), pageNode.getI18nizedLabels());
            }
            selector.createEvent("NodeModified", Phase.PROCESS, event.getRequestContext()).broadcast();
        }

    }

    public static class CloseActionListener extends UIPopupWindow.CloseActionListener {
        public void execute(Event<UIPopupWindow> event) throws Exception {
            UIPopupWindow popWindow = event.getSource();
            popWindow.setUIComponent(null);

            UIGroupNavigationManagement grpMan = popWindow.getAncestorOfType(UIGroupNavigationManagement.class);
            grpMan.naviManager = null;

            super.execute(event);
        }
    }
}

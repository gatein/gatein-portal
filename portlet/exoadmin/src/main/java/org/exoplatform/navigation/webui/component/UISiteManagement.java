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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.navigation.webui.TreeNode;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.EditLevel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.ComponentTab;
import org.exoplatform.util.ReflectionUtil;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({
        @ComponentConfig(template = "app:/groovy/navigation/webui/component/UISiteManagement.gtmpl", events = {
                @EventConfig(name = "EditPortalLayout", listeners = UISiteManagement.EditPortalLayoutActionListener.class),
                @EventConfig(listeners = UISiteManagement.EditNavigationActionListener.class),
                @EventConfig(listeners = UISiteManagement.DeletePortalActionListener.class, confirm = "UIPortalBrowser.deletePortal") }),
        @ComponentConfig(type = UIPageNodeForm.class, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
                @EventConfig(listeners = UIPageNodeForm.SaveActionListener.class),
                @EventConfig(listeners = UIPageNodeForm.ChangeLanguageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UISiteManagement.BackActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchLabelModeActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SwitchVisibleActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.ClearPageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.CreatePageActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageNodeForm.SelectTabActionListener.class, phase = Phase.DECODE) }),
        @ComponentConfig(type = UIPopupWindow.class, template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = @EventConfig(listeners = UISiteManagement.CloseActionListener.class, name = "ClosePopup")) })
public class UISiteManagement extends UIContainer {

    public static String[] ACTIONS = { "EditNavigation", "DeletePortal", "EditPortalLayout" };

    private LazyPageList<PortalConfig> pageList;

    private UINavigationManagement naviManager;

    // set navigationScope to GrandChildren for default value
    private Scope navigationScope = Scope.GRANDCHILDREN;

    public UISiteManagement() throws Exception {
        UIPopupWindow editNavigation = addChild(UIPopupWindow.class, null, null);
        editNavigation.setWindowSize(400, 400);
        editNavigation.setId(editNavigation.getId() + "-" + UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public List<PortalConfig> getPortalConfigs() throws Exception {
        return pageList.getAll();
    }

    public String[] getActions() {
        return ACTIONS;
    }

    public Object getFieldValue(Object bean, String field) throws Exception {
        Method method = ReflectionUtil.getGetBindingMethod(bean, field);
        return method.invoke(bean, ReflectionUtil.EMPTY_ARGS);
    }

    @SuppressWarnings("unchecked")
    public void loadPortalConfigs() throws Exception {
        DataStorage service = getApplicationComponent(DataStorage.class);

        Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class);
        LazyPageList<PortalConfig> temp = service.find(query, new Comparator<PortalConfig>() {
            public int compare(PortalConfig pconfig1, PortalConfig pconfig2) {
                return pconfig1.getName().toLowerCase().compareTo(pconfig2.getName().toLowerCase());
            }
        });

        final ArrayList<PortalConfig> tempArrayList = new ArrayList<PortalConfig>();
        if (temp != null) {
            tempArrayList.addAll(temp.getAll());
        }

        // Get portals without edit permission
        UserACL userACL = getApplicationComponent(UserACL.class);
        Iterator<PortalConfig> iterPortals = tempArrayList.iterator();
        PortalConfig portalConfig;
        while (iterPortals.hasNext()) {
            portalConfig = iterPortals.next();
            if (!userACL.hasEditPermission(portalConfig)) {
                iterPortals.remove();
            }
        }

        this.pageList = new LazyPageList<PortalConfig>(new ListAccess<PortalConfig>() {

            public int getSize() throws Exception {
                return tempArrayList.size();
            }

            public PortalConfig[] load(int index, int length) throws Exception {
                PortalConfig[] pcs = new PortalConfig[tempArrayList.size()];

                if (index < 0) {
                    throw new IllegalArgumentException("Illegal index: index must be a positive number");
                }

                if (length < 0) {
                    throw new IllegalArgumentException("Illegal length: length must be a positive number");
                }

                if (index + length > tempArrayList.size()) {
                    throw new IllegalArgumentException(
                            "Illegal index or length: sum of the index and the length cannot be greater than the list size");
                }

                for (int i = 0; i < length; i++) {
                    pcs[i] = tempArrayList.get(i + index);
                }

                return pcs;
            }

        }, 10);

    }

    public void setScope(Scope scope) {
        this.navigationScope = scope;
    }

    public Scope getScope() {
        return this.navigationScope;
    }

    private boolean stillKeptInPageList(String portalName) throws Exception {
        List<PortalConfig> portals = this.getPortalConfigs();
        for (PortalConfig p : portals) {
            if (p.getName().equals(portalName))
                return true;
        }
        return false;
    }

    public static class DeletePortalActionListener extends EventListener<UISiteManagement> {
        public void execute(Event<UISiteManagement> event) throws Exception {
            UISiteManagement uicomp = event.getSource();
            String portalName = event.getRequestContext().getRequestParameter(OBJECTID);

            UserPortalConfigService service = uicomp.getApplicationComponent(UserPortalConfigService.class);
            String defaultPortalName = service.getDefaultPortal();

            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();

            if (defaultPortalName.equals(portalName)) {
                uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.delete-default-portal",
                        new String[] { defaultPortalName }, ApplicationMessage.WARNING));
                return;
            }

            DataStorage dataStorage = uicomp.getApplicationComponent(DataStorage.class);
            UserACL acl = uicomp.getApplicationComponent(UserACL.class);

            PortalConfig pConfig = dataStorage.getPortalConfig(portalName);
            if (pConfig != null) {
                if (acl.hasPermission(pConfig)) {
                    service.removeUserPortalConfig(portalName);
                } else {
                    uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-deletePermission",
                            new String[] { pConfig.getName() }));
                    return;
                }
            } else {
                if (uicomp.stillKeptInPageList(portalName)) {
                    uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
                            new String[] { portalName }));
                }
                return;
            }

            if (pConfig == null && !Util.getUIPortal().getName().equals(portalName)) {
                uiPortalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-deletePermission",
                        new String[] { portalName }));
                return;
            }

            if (pConfig == null || Util.getUIPortal().getName().equals(portalName)) {
                HttpServletRequest request = prContext.getRequest();
                LogoutControl.wantLogout();
                event.getRequestContext().sendRedirect(request.getContextPath());
                return;
            }

            event.getRequestContext().addUIComponentToUpdateByAjax(uicomp);
        }
    }

    public static class EditPortalLayoutActionListener extends EventListener<UISiteManagement> {
        public void execute(Event<UISiteManagement> event) throws Exception {
            UISiteManagement uicomp = event.getSource();
            String portalName = event.getRequestContext().getRequestParameter(OBJECTID);
            UserPortalConfigService service = uicomp.getApplicationComponent(UserPortalConfigService.class);
            DataStorage dataStorage = uicomp.getApplicationComponent(DataStorage.class);
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication portalApp = (UIPortalApplication) prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = portalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

            PortalConfig pConfig = dataStorage.getPortalConfig(portalName);

            if (pConfig == null) {
                portalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
                        new String[] { portalName }));
                uiWorkingWS.updatePortletsByName("UserToolbarSitePortlet");
                return;
            }

            UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermission(pConfig)) {
                portalApp.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-editPermission",
                        new String[] { pConfig.getName() }));
                return;
            }

            // UIEditInlineWorkspace uiEditWS = uiWorkingWS.addChild(UIEditInlineWorkspace.class, null,
            // UIPortalApplication.UI_EDITTING_WS_ID);
            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChildById(UIPortalApplication.UI_EDITTING_WS_ID);
            UIPortalComposer uiComposer = uiEditWS.getComposer().setRendered(true);
            uiComposer.setEditted(false);
            uiComposer.setCollapse(false);
            uiComposer.setShowControl(true);
            uiComposer.setComponentConfig(UIPortalComposer.class, null);
            uiComposer.setId(UIPortalComposer.UIPORTAL_COMPOSER);

            UIPortal uiPortal = Util.getUIPortal();
            uiWorkingWS.setBackupUIPortal(uiPortal);

            UIPortal editPortal = uiWorkingWS.createUIComponent(UIPortal.class, null, null);
            PortalDataMapper.toUIPortal(editPortal, pConfig);
            uiEditWS.setUIComponent(editPortal);

            // Check if edit current portal
            if (uiPortal.getSiteKey().equals(editPortal.getSiteKey())) {
                // editPortal.setSelectedNode(uiPortal.getSelectedNode());
                // editPortal.setNavigation(uiPortal.getNavigation());
                // editPortal.setSelectedPath(uiPortal.getSelectedPath());
                editPortal.setNavPath(uiPortal.getNavPath());
                UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
                siteBody.setUIComponent(null);
            }

            editPortal.refreshUIPage();
            portalApp.setDefaultEditMode(ComponentTab.APPLICATIONS, EditLevel.EDIT_SITE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_EDITTING_WS_ID);

            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            prContext.ignoreAJAXUpdateOnPortlets(true);
        }
    }

    public static class EditNavigationActionListener extends EventListener<UISiteManagement> {

        public void execute(Event<UISiteManagement> event) throws Exception {
            UISiteManagement uicomp = event.getSource();
            String portalName = event.getRequestContext().getRequestParameter(OBJECTID);
            WebuiRequestContext context = event.getRequestContext();
            UIApplication uiApplication = context.getUIApplication();

            // Minh Hoang TO: User could edit navigation if he/she has edit permissions on PortalConfig. That is not
            // at all logical and should be modified after release 3.1 GA
            UserPortalConfigService configService = uicomp.getApplicationComponent(UserPortalConfigService.class);
            UserPortalConfig userPortalConfig = configService.getUserPortalConfig(portalName, context.getRemoteUser(),
                    PortalRequestContext.USER_PORTAL_CONTEXT);
            if (userPortalConfig == null) {
                uiApplication.addMessage(new ApplicationMessage("UISiteManagement.msg.portal-not-exist",
                        new String[] { portalName }));
                UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChildById(
                        UIPortalApplication.UI_WORKING_WS_ID);
                uiWorkingWS.updatePortletsByName("UserToolbarSitePortlet");
                return;
            }

            UserACL userACL = uicomp.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermission(userPortalConfig.getPortalConfig())) {
                uiApplication.addMessage(new ApplicationMessage("UISiteManagement.msg.Invalid-editPermission", null));
                return;
            }

            // Minh Hoang TO: For release 3.1, Edit Permission check would be rollback to former checks on PortalConfig
            /*
             * if (edittedNavigation == null) { uiApplication.addMessage(new
             * ApplicationMessage("UISiteManagement.msg.portal-not-exist", new String[]{portalName})); return; }
             *
             * UserACL userACL = uicomp.getApplicationComponent(UserACL.class); if
             * (!userACL.hasEditPermission(edittedNavigation)) { uiApplication.addMessage(new
             * ApplicationMessage("UISiteManagement.msg.Invalid-editPermission", null));; return; }
             */

            UIPopupWindow popUp = uicomp.getChild(UIPopupWindow.class);
            UINavigationManagement naviManager = popUp.createUIComponent(UINavigationManagement.class, null, null, popUp);
            uicomp.naviManager = naviManager;

            naviManager.setSiteKey(SiteKey.portal(portalName));

            UserPortal userPortal = userPortalConfig.getUserPortal();
            UserNavigation edittedNavigation = userPortal.getNavigation(SiteKey.portal(portalName));

            UINavigationNodeSelector selector = naviManager.getChild(UINavigationNodeSelector.class);
            selector.setScope(uicomp.getScope());
            selector.setEdittedNavigation(edittedNavigation);
            selector.setUserPortal(userPortal);
            selector.initTreeData();
            popUp.setUIComponent(naviManager);
            popUp.setShowMask(true);
            popUp.setShow(true);
            popUp.setWindowSize(400, 400);
        }
    }

    public static class BackActionListener extends EventListener<UIPageNodeForm> {

        public void execute(Event<UIPageNodeForm> event) throws Exception {
            UIPageNodeForm uiPageNodeForm = event.getSource();
            UISiteManagement uiSiteManagement = uiPageNodeForm.getAncestorOfType(UISiteManagement.class);
            UINavigationManagement navigationManager = uiSiteManagement.naviManager;

            UINavigationNodeSelector selector = navigationManager.getChild(UINavigationNodeSelector.class);
            TreeNode selectedParent = (TreeNode) uiPageNodeForm.getSelectedParent();
            selector.setScope(uiSiteManagement.getScope());
            selector.selectNode(selectedParent);

            WebuiRequestContext context = event.getRequestContext();
            UIPopupWindow uiNavigationPopup = uiSiteManagement.getChild(UIPopupWindow.class);
            uiNavigationPopup.setUIComponent(navigationManager);
            uiNavigationPopup.setWindowSize(400, 400);
            context.addUIComponentToUpdateByAjax(uiNavigationPopup.getParent());

            TreeNode pageNode = uiPageNodeForm.getPageNode();
            if (pageNode != null) {
                selector.getUserNodeLabels().put(pageNode.getId(), pageNode.getI18nizedLabels());
            }
            selector.createEvent("NodeModified", Phase.PROCESS, context).broadcast();
        }

    }

    public static class CloseActionListener extends UIPopupWindow.CloseActionListener {
        public void execute(Event<UIPopupWindow> event) throws Exception {
            UIPopupWindow popWindow = event.getSource();
            popWindow.setUIComponent(null);

            UISiteManagement siteMan = popWindow.getAncestorOfType(UISiteManagement.class);
            siteMan.naviManager = null;

            super.execute(event);
        }
    }

}

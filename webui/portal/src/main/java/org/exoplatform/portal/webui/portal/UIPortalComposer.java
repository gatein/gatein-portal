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

package org.exoplatform.portal.webui.portal;

import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.StaleModelException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UIPageForm;
import org.exoplatform.portal.webui.page.UIPagePreview;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.EditLevel;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/** Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Jun 10, 2009 */
@ComponentConfigs({
        @ComponentConfig(template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
                @EventConfig(name = "ViewProperties", listeners = UIPortalComposer.ViewSitePropertiesActionListener.class),
                @EventConfig(listeners = UIPortalComposer.CloseComposerActionListener.class),
                @EventConfig(name = "Abort", listeners = UIPortalComposer.AbortSiteEditionActionListener.class),
                @EventConfig(name = "Finish", listeners = UIPortalComposer.FinishSiteEditionActionListener.class),
                @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
                @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
                @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class) }),
        @ComponentConfig(id = UIPortalComposer.UIPAGE_EDITOR, template = "app:/groovy/portal/webui/portal/UIPortalComposer.gtmpl", events = {
                @EventConfig(name = "ViewProperties", listeners = UIPortalComposer.ViewPagePropertiesActionListener.class),
                @EventConfig(listeners = UIPortalComposer.CloseComposerActionListener.class),
                @EventConfig(name = "Abort", listeners = UIPortalComposer.AbortPageEditionActionListener.class),
                @EventConfig(name = "Finish", listeners = UIPortalComposer.FinishPageEditionActionListener.class),
                @EventConfig(name = "Back", listeners = UIPortalComposer.BackActionListener.class),
                @EventConfig(listeners = UIPortalComposer.SwitchModeActionListener.class),
                @EventConfig(listeners = UIPortalComposer.ChangeEdittedStateActionListener.class),
                @EventConfig(listeners = UIPortalComposer.ToggleActionListener.class) }),
        @ComponentConfig(id = "UIPortalComposerTab", type = UITabPane.class, template = "app:/groovy/portal/webui/portal/UIPortalComposerContent.gtmpl", events = { @EventConfig(listeners = UIPortalComposer.SelectTabActionListener.class) }) })
public class UIPortalComposer extends UIContainer {
    public static final String UIPORTAL_COMPOSER = "UIPortalComposer";

    public static final String UIPAGE_EDITOR = "UIPageEditor";

    private boolean isEditted = false;

    private boolean isCollapsed = false;

    private boolean isShowControl = true;

    public UIPortalComposer() throws Exception {
        UITabPane uiTabPane = addChild(UITabPane.class, "UIPortalComposerTab", null);
        uiTabPane.addChild(UIApplicationList.class, null, null).setRendered(true);
        uiTabPane.addChild(UIContainerList.class, null, null);
        uiTabPane.setSelectedTab(1);
    }

    public int getPortalMode() {
        return getAncestorOfType(UIPortalApplication.class).getModeState();
    }

    /**
     * Returns <code>true</code> there was at least one change has been done in this edition time.
     *
     * <p>
     * This value is used in the template of this component to notice the user if there was something has been changed
     *
     * @return
     */
    public boolean isEditted() {
        return isEditted;
    }

    public void setEditted(boolean b) {
        isEditted = b;
    }

    /**
     * Return a value of <code>boolean</code> to tell current state of the composer
     *
     * @return <code>true</code> if the composer is collapsed currently
     */
    public boolean isCollapse() {
        return isCollapsed;
    }

    public void setCollapse(boolean isCollapse) {
        this.isCollapsed = isCollapse;
    }

    public boolean isShowControl() {
        return isShowControl;
    }

    public void setShowControl(boolean state) {
        isShowControl = state;
    }

    /**
     * Return true if the edition is in the page creation wizard
     *
     * @return
     */
    private boolean isUsedInWizard() {
        UIWorkingWorkspace uiWorkingWS = getAncestorOfType(UIWorkingWorkspace.class);
        UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
        UIComponent uicomponent = uiToolPanel.getUIComponent();
        if (uicomponent != null && uicomponent instanceof UIWizard) {
            return true;
        }
        return false;
    }

    /**
     * Perform saving changes of the edition of SiteConfig into database
     *
     * @throws Exception if there is anything wrong in saving process
     */
    private void save() throws Exception {
        PortalRequestContext prContext = Util.getPortalRequestContext();
        UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
        UIWorkingWorkspace uiWorkingWS = uiPortalApp.findFirstComponentOfType(UIWorkingWorkspace.class);
        UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
        UIPortal editPortal = (UIPortal) uiEditWS.getUIComponent();
        UIPortal uiPortal = Util.getUIPortal();
        String remoteUser = prContext.getRemoteUser();
        String portalName = prContext.getPortalOwner();

        PortalConfig portalConfig = (PortalConfig) PortalDataMapper.buildModelObject(editPortal);
        DataStorage dataStorage = getApplicationComponent(DataStorage.class);
        UserACL acl = getApplicationComponent(UserACL.class);

        if (!isPortalExist(editPortal)) {
            return;
        }

        SkinService skinService = getApplicationComponent(SkinService.class);
        skinService.invalidatePortalSkinCache(editPortal.getName(), editPortal.getSkin());
        try {
            dataStorage.save(portalConfig);
        } catch (StaleModelException ex) {
            // Temporary solution for concurrency-related issue. The StaleModelException should be
            // caught in the ApplicationLifecycle
            rebuildUIPortal(uiPortalApp, editPortal, dataStorage);
        }
        prContext.getUserPortalConfig().setPortalConfig(portalConfig);
        PortalConfig pConfig = dataStorage.getPortalConfig(portalName);
        if (pConfig != null) {
            editPortal.setModifiable(acl.hasEditPermission(pConfig));
        } else {
            editPortal.setModifiable(false);
        }
        LocaleConfigService localeConfigService = uiPortalApp.getApplicationComponent(LocaleConfigService.class);
        LocaleConfig localeConfig = localeConfigService.getLocaleConfig(portalConfig.getLocale());
        if (localeConfig == null) {
            localeConfig = localeConfigService.getDefaultLocaleConfig();
        }
        // TODO dang.tung - change layout when portal get language from UIPortal
        // (user and browser not support)
        // ----------------------------------------------------------------------------------------------------
        String portalAppLanguage = prContext.getLocale().getLanguage();
        OrganizationService orgService = getApplicationComponent(OrganizationService.class);
        UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(remoteUser);
        String userLanguage = userProfile != null ? userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE) : null;
        String browserLanguage = prContext.getRequest().getLocale().getLanguage();

        // in case: edit current portal, set skin and language for uiPortalApp
        if (uiPortal == null) {
            if (!portalAppLanguage.equals(userLanguage) && !portalAppLanguage.equals(browserLanguage)) {
                prContext.setLocale(localeConfig.getLocale());
                // editPortal.refreshNavigation(localeConfig.getLocale());
                // uiPortalApp.localizeNavigations();
            }
            uiPortalApp.setSkin(editPortal.getSkin());
        }
        prContext.refreshResourceBundle();
    }

    private void rebuildUIPortal(UIPortalApplication uiPortalApp, UIPortal uiPortal, DataStorage storage) throws Exception {
        PortalConfig portalConfig = storage.getPortalConfig(uiPortal.getSiteType().getName(), uiPortal.getName());
        UserPortalConfig userPortalConfig = Util.getPortalRequestContext().getUserPortalConfig();
        userPortalConfig.setPortalConfig(portalConfig);
        uiPortal.getChildren().clear();
        PortalDataMapper.toUIPortal(uiPortal, userPortalConfig.getPortalConfig());

        uiPortalApp.putCachedUIPortal(uiPortal);

    }

    /**
     * Check the <code>editPortal</code> whether it is existing in database or not
     *
     * @param editPortal
     * @return
     * @throws Exception
     */
    private boolean isPortalExist(UIPortal editPortal) throws Exception {
        String remoteUser = Util.getPortalRequestContext().getRemoteUser();

        String portalOwner = null;
        if (editPortal.getSiteType().equals(SiteType.PORTAL)) {
            portalOwner = editPortal.getName();
        } else {
            portalOwner = Util.getPortalRequestContext().getPortalOwner();
        }

        UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);

        return configService.getUserPortalConfig(portalOwner, remoteUser) != null;
    }

    /**
     * Updates the availability children of the UIEditInlineWorkspace except to the UIPortalComposer
     *
     * @throws Exception
     */
    public void updateWorkspaceComponent() throws Exception {
        UIPortalApplication uiApp = Util.getUIPortalApplication();
        WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
        UIEditInlineWorkspace uiEditWS = uiApp.findFirstComponentOfType(UIEditInlineWorkspace.class);
        List<UIComponent> children = uiEditWS.getChildren();
        for (UIComponent child : children) {
            if (!child.isRendered() || child.getClass().equals(UIPortalComposer.class)) {
                continue;
            }
            rcontext.addUIComponentToUpdateByAjax(child);
        }
        int portalMode = uiApp.getModeState();
        if (portalMode != UIPortalApplication.NORMAL_MODE) {
            switch (portalMode) {
                case UIPortalApplication.APP_BLOCK_EDIT_MODE:
                case UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE:
                    Util.showComponentEditInBlockMode();
                    break;
                case UIPortalApplication.APP_VIEW_EDIT_MODE:
                case UIPortalApplication.CONTAINER_VIEW_EDIT_MODE:
                    Util.showComponentEditInViewMode();
                    break;
            }
        }
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
        int portalMode = uiPortalApp.getModeState();
        if (portalMode != UIPortalApplication.NORMAL_MODE) {
            /* ppalaga is quite sure that changing UIPortalApplication.modeState during render is
             * not a good idea because there might have happened other state changes in event
             * handlers that those handlers rely on as not changing again during the same request.
             * Moreover, ppalaga could not find an execution path where this piece of code was
             * important, therefore, he bravely commented it out.
             *
             * If you decide that this code is missing, please test New Page Wizard with distinct
             * gatein.portal.pageEditor.defaultEditMode and gatein.portal.pageEditor.fullPreview values
             * and do not forget to check going back and forward in the wizard.
             *
             * The original code:
             *
             * UITabPane uiTabPane = this.getChild(UITabPane.class);
             * UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
             * if (uiComponent instanceof UIApplicationList) {
             *     uiPortalApp.setDefaultEditMode(ComponentTab.APPLICATIONS, uiPortalApp.getEditLevel());
             * } else if (uiComponent instanceof UIContainerList) {
             *     uiPortalApp.setDefaultEditMode(ComponentTab.CONTAINERS, uiPortalApp.getEditLevel());
             * }
             */
            switch (portalMode) {
                case UIPortalApplication.APP_VIEW_EDIT_MODE:
                case UIPortalApplication.CONTAINER_VIEW_EDIT_MODE:
                    Util.showComponentEditInViewMode();
                    break;
                case UIPortalApplication.APP_BLOCK_EDIT_MODE:
                case UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE:
                    Util.showComponentEditInBlockMode();
                    break;
                default:
                    break;
            }
        }
        super.processRender(context);
    }

    public static class ViewSitePropertiesActionListener extends EventListener<UIPortalComposer> {

        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIComponent temp = null;
            UIPortal uiPortal = null;
            String portalOwner = null;
            UIEditInlineWorkspace uiEditWS = event.getSource().getAncestorOfType(UIEditInlineWorkspace.class);
            temp = uiEditWS.getUIComponent();
            if (temp != null && (temp instanceof UIPortal)) {
                uiPortal = (UIPortal) temp;
                if (uiPortal.getSiteType().equals(SiteType.PORTAL)) {
                    portalOwner = uiPortal.getName();
                } else {
                    portalOwner = Util.getPortalRequestContext().getPortalOwner();
                }
            } else {
                uiPortal = Util.getUIPortal();
                portalOwner = Util.getPortalRequestContext().getPortalOwner();
            }

            UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            UIPortalForm portalForm = uiMaskWS.createUIComponent(UIPortalForm.class, null, "UIPortalForm");
            portalForm.setPortalOwner(portalOwner);
            portalForm.setBindingBean();
            if (SiteType.USER.equals(uiPortal.getSiteType())) {
                portalForm.removeChildById("PermissionSetting");
            }
            uiMaskWS.setWindowSize(700, -1);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
        }
    }

    public static class AbortSiteEditionActionListener extends EventListener<UIPortalComposer> {

        protected void closeComposer(UIPortalApplication uiPortalApp, UIWorkingWorkspace uiWorkingWS) {

            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.ignoreAJAXUpdateOnPortlets(true);

            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
            uiEditWS.getComposer().setEditted(false);
            uiEditWS.setRendered(false);
            uiEditWS.setUIComponent(null);

            UITabPane tabPane = uiEditWS.getComposer().getChild(UITabPane.class);
            tabPane.setSelectedTab(1);
        }

        protected void fireChangeNode(WebuiRequestContext prContext, UIPortalApplication uiPortalApp, UIPortal uiPortal) throws Exception {
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

            UserNode currentNode = uiPortal.getSelectedUserNode();
            SiteKey siteKey = currentNode.getNavigation().getKey();
            PageNodeEvent<UIPortalApplication> pnevent = new PageNodeEvent<UIPortalApplication>(uiPortalApp,
                    PageNodeEvent.CHANGE_NODE, siteKey, currentNode.getURI());
            uiPortalApp.broadcast(pnevent, Event.Phase.PROCESS);


            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            Util.updatePortalMode();
        }

        public void execute(Event<UIPortalComposer> event) throws Exception {

            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

            UIPortal currentPortal = uiWorkingWS.restoreUIPortal();
            uiPortalApp.refreshCachedUI();

            closeComposer(uiPortalApp, uiWorkingWS);
            fireChangeNode(prContext, uiPortalApp, currentPortal);
        }
    }

    /**
     * Listens the <code>save</code> action of the composer while editing SiteConfig
     *
     * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
     * @version $Revision$
     */
    public static class FinishSiteEditionActionListener extends EventListener<UIPortalComposer> {

        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalComposer uiComposer = event.getSource();
            uiComposer.save();
            uiComposer.setEditted(false);
            //Reset tab pane state
            UITabPane tabPane = uiComposer.getChild(UITabPane.class);
            tabPane.setSelectedTab(1);
            PortalRequestContext prContext = Util.getPortalRequestContext();

            UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
            UIPortal editPortal = (UIPortal) uiEditWS.getUIComponent();

            UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
            UIPortal uiPortal = (UIPortal) siteBody.getUIComponent();

            if (uiPortal == null) {
                siteBody.setUIComponent(editPortal);
            }
            uiEditWS.setRendered(false);
            uiEditWS.setUIComponent(null);
            uiPortal = (UIPortal) siteBody.getUIComponent();

            uiPortalApp.setSessionOpen(PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive()));
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            prContext.ignoreAJAXUpdateOnPortlets(true);

            if (uiComposer.isPortalExist(editPortal)) {
                DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
                PortalConfig pConfig = storage.getPortalConfig(uiPortal.getSiteKey().getTypeName(), uiPortal.getSiteKey()
                        .getName());
                if (pConfig != null) {
                    prContext.getUserPortalConfig().setPortalConfig(pConfig);
                }

                // Update the cache of UIPortal from UIPortalApplication
                uiPortalApp.refreshCachedUI();

                UserNode currentNode = uiPortal.getSelectedUserNode();
                SiteKey siteKey = currentNode.getNavigation().getKey();
                PageNodeEvent<UIPortalApplication> pnevent = new PageNodeEvent<UIPortalApplication>(uiPortalApp,
                        PageNodeEvent.CHANGE_NODE, siteKey, currentNode.getURI());
                uiPortalApp.broadcast(pnevent, Event.Phase.PROCESS);

                prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
                Util.updatePortalMode();
            } else {
                if (editPortal.getName().equals(prContext.getPortalOwner())) {
                    HttpServletRequest request = prContext.getRequest();
                    LogoutControl.wantLogout();
                    prContext.setResponseComplete(true);
                    prContext.getResponse().sendRedirect(request.getContextPath());
                    return;
                } else {
                    UIApplication uiApp = prContext.getUIApplication();
                    uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null));
                    prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
                }
            }
        }

    }

    public static class SelectTabActionListener extends UITabPane.SelectTabActionListener {
        public void execute(Event<UITabPane> event) throws Exception {
            super.execute(event);
            UITabPane uiTabPane = event.getSource();
            UIComponent uiComponent = uiTabPane.getChildById(uiTabPane.getSelectedTabId());
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
            int portalMode = uiPortalApp.getModeState();

            if (uiComponent instanceof UIApplicationList) { // Swicth to Porlets Tab
                if (portalMode % 2 == 0) {
                    uiPortalApp.setModeState(UIPortalApplication.APP_VIEW_EDIT_MODE);
                } else {
                    uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
                }
            } else if (uiComponent instanceof UIContainerList) { // Swicth to
                                                                 // Containers Tab
                if (portalMode % 2 == 0) {
                    uiPortalApp.setModeState(UIPortalApplication.CONTAINER_VIEW_EDIT_MODE);
                } else {
                    uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
                }
            }
        }
    }

    public static class SwitchModeActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
            int portalMode = uiPortalApp.getModeState();

            switch (portalMode) {
                case UIPortalApplication.APP_BLOCK_EDIT_MODE:
                    uiPortalApp.setModeState(UIPortalApplication.APP_VIEW_EDIT_MODE);
                    break;
                case UIPortalApplication.APP_VIEW_EDIT_MODE:
                    uiPortalApp.setModeState(UIPortalApplication.APP_BLOCK_EDIT_MODE);
                    break;
                case UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE:
                    uiPortalApp.setModeState(UIPortalApplication.CONTAINER_VIEW_EDIT_MODE);
                    break;
                case UIPortalApplication.CONTAINER_VIEW_EDIT_MODE:
                    uiPortalApp.setModeState(UIPortalApplication.CONTAINER_BLOCK_EDIT_MODE);
                    break;
                default:
                    uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
                    return;
            }

            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);

            UIComponent editComponent = uiEditWS.getUIComponent();
            UIPortal editPortal = null;
            if (editComponent instanceof UIPortal) {
                editPortal = (UIPortal) uiEditWS.getUIComponent();
            } else if (editComponent instanceof UIPageCreationWizard) {
                UIPageCreationWizard uiWizard = (UIPageCreationWizard) uiEditWS.getUIComponent();
                UIPagePreview uiPagePreview = uiWizard.getChild(UIPagePreview.class);
                UIComponent uiPreviewComponent = uiPagePreview.getUIComponent();
                if (uiPreviewComponent instanceof UIPortal) {
                    editPortal = (UIPortal) uiPreviewComponent;
                }
            }
            if (editPortal != null) {
                editPortal.setHeaderAndFooterRendered(UIPage.isFullPreviewInPageEditor() || uiPortalApp.getEditLevel() == EditLevel.EDIT_SITE);
            }

            event.getSource().updateWorkspaceComponent();
            Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
        }
    }

    public static class ChangeEdittedStateActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) {
            UIPortalComposer uiComposer = event.getSource();
            uiComposer.setEditted(true);
        }
    }

    public static class ToggleActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalComposer uiComposer = event.getSource();
            uiComposer.setCollapse(!uiComposer.isCollapse());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiComposer);
        }
    }

    public static class ViewPagePropertiesActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
            UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
            UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
            UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            UIPageForm uiPageForm = uiPortalApp.createUIComponent(UIPageForm.class, null, null);

            UIPage uiPage = uiToolPanel.findFirstComponentOfType(UIPage.class);
            uiPageForm.buildForm(uiPage);
            uiMaskWS.setUIComponent(uiPageForm);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
        }
    }

    public static class CloseComposerActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalComposer uiPortalComposer = event.getSource();
            UIEditInlineWorkspace uiEditInlineWorkspace = uiPortalComposer.getAncestorOfType(UIEditInlineWorkspace.class);
            if (uiPortalComposer.isEditted()) {
                ResourceBundle resourceBundle = event.getRequestContext().getApplicationResourceBundle();
                String closeMessage = resourceBundle.getString("UIEditInlineWorkspace.confirm.close");

                uiEditInlineWorkspace.showConfirmWindow(closeMessage);
            } else {
                Event<UIComponent> abortEvent = uiPortalComposer.createEvent("Abort", event.getExecutionPhase(),
                        event.getRequestContext());
                abortEvent.broadcast();
            }
        }
    }

    public static class AbortPageEditionActionListener extends AbortSiteEditionActionListener {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

            try {
                uiWorkingWS.restoreUIPortal();
            } catch (IllegalStateException e) {
                /* It is expected that in some cases, there will be nothing to restore. */
            }

            UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
            uiToolPanel.setUIComponent(null);
            UIPortal uiPortal = uiPortalApp.getCurrentSite();
            uiPortal.setRenderSibling(UIPortal.class);

            uiPortalApp.refreshCachedUI();

            closeComposer(uiPortalApp, uiWorkingWS);
            fireChangeNode(prContext, uiPortalApp, uiPortal);
        }
    }

    /**
     * This action listener is for the page edition
     *
     * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
     * @version $Revision$
     */
    public static class FinishPageEditionActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
            UIEditInlineWorkspace editInlineWS = event.getSource().getParent();
            UIWorkingWorkspace uiWorkingWS = editInlineWS.getParent();
            //Reset tab pane state
            UITabPane tabPane = event.getSource().getChild(UITabPane.class);
            tabPane.setSelectedTab(1);
            UIPortal uiPortal = uiPortalApp.getCurrentSite();
            UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
            Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);

            UIPage uiPage = uiToolPanel.findFirstComponentOfType(UIPage.class);
            Page page = (Page) PortalDataMapper.buildModelObject(uiPage);
            String pageId = page.getPageId();

            UserPortalConfigService portalConfigService = uiWorkingWS.getApplicationComponent(UserPortalConfigService.class);

            /* Put the portal back in place as we have removed it in
             * org.exoplatform.portal.webui.workspace.UIMainActionListener.EditInlineActionListener.execute(Event<UIWorkingWorkspace>) */
            UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
            UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
            UIComponent editComponent = uiEditWS.getUIComponent();
            if (editComponent instanceof UIPortal) {
                UIPortal editPortal = (UIPortal) editComponent;
                editPortal.setHeaderAndFooterRendered(true);
                siteBody.setUIComponent(editPortal);
            }

            /*
             * if it is a edition of the current page and it is not available to current remote user anymore.
             */
            PageKey pageKey = PageKey.parse(pageId);
            if (page.getStorageId() != null && portalConfigService.getPageService().loadPage(pageKey) == null) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.PageNotExist", new String[] { pageId },
                        ApplicationMessage.WARNING));
                uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
                uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
                Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);

                UserNode currentNode = uiPortal.getSelectedUserNode();
                SiteKey siteKey = currentNode.getNavigation().getKey();
                PageNodeEvent<UIPortalApplication> pnevent = new PageNodeEvent<UIPortalApplication>(uiPortalApp,
                        PageNodeEvent.CHANGE_NODE, siteKey, currentNode.getURI());
                uiPortalApp.broadcast(pnevent, Event.Phase.PROCESS);

                Util.updatePortalMode();
                return;
            }
            UIPortalComposer composer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
            composer.setEditted(false);

            // If it is a page creation wizard
            if (composer.isUsedInWizard()) {
                UIWizard wizard = (UIWizard) uiToolPanel.getUIComponent();
                int step = wizard.getCurrentStep();
                step++;
                Event<UIComponent> uiEvent = wizard.createEvent("ViewStep" + step, Phase.PROCESS, event.getRequestContext());
                uiEvent.broadcast();
                return;
            }

            // Perform model update
            DataStorage dataService = uiWorkingWS.getApplicationComponent(DataStorage.class);
            PageService pageService = uiWorkingWS.getApplicationComponent(PageService.class);
            try {
                PageState pageState = PageUtils.toPageState(page);
                pageService.savePage(new PageContext(pageKey, pageState));
                dataService.save(page);
            } catch (StaleModelException ex) {
                // Temporary solution to concurrency-related issue
                // This catch block should be put in an appropriate ApplicationLifecyclec
            }
            uiToolPanel.setUIComponent(null);

            // Invalidate UI cached
            uiPortalApp.refreshCachedUI();

            if (PortalProperties.SESSION_ALWAYS.equals(uiPortal.getSessionAlive())) {
                uiPortalApp.setSessionOpen(true);
            } else {
                uiPortalApp.setSessionOpen(false);
            }
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
            Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);

            UserNode currentNode = uiPortal.getSelectedUserNode();
            PageNodeEvent<UIPortalApplication> pnevent = new PageNodeEvent<UIPortalApplication>(uiPortalApp,
                    PageNodeEvent.CHANGE_NODE, currentNode.getNavigation().getKey(), currentNode.getURI());
            uiPortalApp.broadcast(pnevent, Event.Phase.PROCESS);

            Util.updatePortalMode();
        }
    }

    public static class BackActionListener extends EventListener<UIPortalComposer> {
        public void execute(Event<UIPortalComposer> event) throws Exception {
            UIPortalComposer composer = event.getSource();
            if (composer.isUsedInWizard()) {
                UIWorkingWorkspace uiWorkingWS = composer.getAncestorOfType(UIWorkingWorkspace.class);
                UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
                UIWizard wizard = (UIWizard) uiToolPanel.getUIComponent();
                int step = wizard.getCurrentStep();
                step--;
                Event<UIComponent> uiEvent = wizard.createEvent("ViewStep" + step, Phase.PROCESS, event.getRequestContext());
                uiEvent.broadcast();
            }
        }
    }
}

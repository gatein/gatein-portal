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

package org.exoplatform.portal.webui.workspace;

import java.lang.reflect.Method;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.portal.UIPortalForm;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.ComponentTab;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.EditLevel;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.EditMode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 *
 * Author : Pham Thanh Tung thanhtungty@gmail.com May 5, 2009
 */
public class UIMainActionListener {
    public static class PageCreationWizardActionListener extends EventListener<UIWorkingWorkspace> {
        public void execute(Event<UIWorkingWorkspace> event) throws Exception {
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            UIPortal uiPortal = Util.getUIPortal();
            UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);

            UserNavigation currNav = uiPortal.getUserNavigation();
            if (currNav == null) {
                uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.navigation.deleted", null));
                return;
            }

            if (!currNav.isModifiable()) {
                uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-CreatePage-Permission", null));
                return;
            }

            // Should renew the selectedNode. Don't reuse the cached selectedNode
            UserNode selectedNode = Util.getUIPortal().getSelectedUserNode();
            UserNodeFilterConfig filterConfig = createFilterConfig();
            UserNode resolvedNode = resolveNode(selectedNode, filterConfig);
            if (resolvedNode == null) {
                resolvedNode = resolveNode(selectedNode, null);

                WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
                if (resolvedNode == null) {
                    context.getUIApplication().addMessage(new ApplicationMessage("UIPortalManagement.msg.node.deleted", null));
                } else {
                    context.getUIApplication().addMessage(new ApplicationMessage("UIPortalManagement.msg.node.permission", null));
                }
                event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
                return;
            }

            uiApp.setDefaultEditMode(ComponentTab.APPLICATIONS, EditLevel.EDIT_PAGE);
            uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);

            UIPortalComposer portalComposer = uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class);
            portalComposer.setRendered(false);
            portalComposer.setComponentConfig(UIPortalComposer.class, UIPortalComposer.UIPAGE_EDITOR);
            portalComposer.setId(UIPortalComposer.UIPAGE_EDITOR);
            portalComposer.setShowControl(true);
            portalComposer.setEditted(true);
            portalComposer.setCollapse(false);

            UIPortalToolPanel uiToolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
            uiToolPanel.setShowMaskLayer(false);
            uiToolPanel.setWorkingComponent(UIPageCreationWizard.class, null);
            UIPageCreationWizard uiWizard = (UIPageCreationWizard) uiToolPanel.getUIComponent();
            uiWizard.configure(resolvedNode);

            UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
            uiPageSetInfo.setShowPublicationDate(false);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingWS);
        }

        private UserNode resolveNode(UserNode selectedNode, UserNodeFilterConfig filterConfig) {
            UserNavigation currNav = selectedNode.getNavigation();
            UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
            if (currNav.getKey().getType().equals(SiteType.USER)) {
                return userPortal.getNode(currNav, Scope.CHILDREN, filterConfig, null);
            } else {
                return userPortal.resolvePath(currNav, filterConfig, selectedNode.getURI());
            }
        }

        private UserNodeFilterConfig createFilterConfig() {
            UserNodeFilterConfig.Builder filterConfigBuilder = UserNodeFilterConfig.builder();
            filterConfigBuilder.withReadWriteCheck();
            return filterConfigBuilder.build();
        }
    }

    public static class EditInlineActionListener extends EventListener<UIWorkingWorkspace> {

        protected boolean authorizeEditSite(UIPortal currentPortal, UIPortalApplication portalApp) {
            UserACL userACL = portalApp.getApplicationComponent(UserACL.class);
            if (userACL.hasEditPermissionOnPortal(currentPortal.getSiteType().getName(), currentPortal.getName(),
                    currentPortal.getEditPermission())) {
                return true;
            } else {
                portalApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-EditLayout-Permission",
                        new String[] { currentPortal.getName() }));
                return false;
            }
        }


        protected boolean authorizeEditPage(UIPortal currentPortal, UIPortalApplication portalApp) {
            return true;
        }

        protected void configureComposer(UIPortalComposer uiComposer) {
            uiComposer.setComponentConfig(UIPortalComposer.class, null);
            uiComposer.setId(UIPortalComposer.UIPORTAL_COMPOSER);
        }

        public void execute(Event<UIWorkingWorkspace> event) throws Exception {
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            UIPortalApplication portalApp = (UIPortalApplication) pcontext.getUIApplication();
            UIPortal currentPortal = portalApp.getCurrentSite();
            UIWorkingWorkspace uiWorkingWS = event.getSource();

            if (authorizeEditSite(currentPortal, portalApp)) {
                DataStorage dataStorage = portalApp.getApplicationComponent(DataStorage.class);
                PortalConfig portalConfig = dataStorage.getPortalConfig(pcontext.getSiteType().getName(), pcontext.getSiteName());
                UIPortal transientPortal = uiWorkingWS.createUIComponent(UIPortal.class, null, null);
                PortalDataMapper.toUIPortal(transientPortal, portalConfig);
                transientPortal.setNavPath(currentPortal.getNavPath());
                transientPortal.refreshUIPage();

                if (authorizeEditPage(currentPortal, portalApp)) {
                    uiWorkingWS.setBackupUIPortal(currentPortal);
                    portalApp.setDefaultEditMode(ComponentTab.APPLICATIONS, getEditLevel());

                    configurePortal(portalApp, transientPortal);

                    UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
                    uiEditWS.setUIComponent(transientPortal);
                    UISiteBody siteBody = uiWorkingWS.findFirstComponentOfType(UISiteBody.class);
                    siteBody.setUIComponent(null);

                    UIPortalComposer uiComposer = uiEditWS.getComposer().setRendered(true);
                    configureComposer(uiComposer);
                    uiComposer.setShowControl(true);
                    uiComposer.setEditted(false);
                    uiComposer.setCollapse(false);

                    uiWorkingWS.setRenderedChild(UIEditInlineWorkspace.class);
                    pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
                    pcontext.ignoreAJAXUpdateOnPortlets(true);
                }
            }
        }


        /**
         * @param transientPortal
         */
        protected void configurePortal(UIPortalApplication portalApp, UIPortal transientPortal) {
        }


        protected EditLevel getEditLevel() {
            return EditLevel.EDIT_SITE;
        }
    }

    public static class EditPageInFullPreviewActionListener extends EditInlineActionListener {

        /**
         * @see org.exoplatform.portal.webui.workspace.UIMainActionListener.EditInlineActionListener#authorizeEdit(org.exoplatform.portal.webui.portal.UIPortal, org.exoplatform.portal.webui.workspace.UIPortalApplication)
         */
        @Override
        public boolean authorizeEditSite(UIPortal currentPortal, UIPortalApplication portalApp) {
            return true;
        }

        /**
         * @see org.exoplatform.portal.webui.workspace.UIMainActionListener.EditInlineActionListener#authorizeEditPage(org.exoplatform.portal.webui.portal.UIPortal, org.exoplatform.portal.webui.workspace.UIPortalApplication)
         */
        @Override
        public boolean authorizeEditPage(UIPortal currentPortal, UIPortalApplication portalApp) {
            // check edit permission for page
            UserACL userACL = currentPortal.getApplicationComponent(UserACL.class);
            UIPage uiPage = currentPortal.findFirstComponentOfType(UIPage.class);
            SiteKey siteKey = uiPage.getSiteKey();
            if (userACL.hasEditPermissionOnPage(siteKey.getTypeName(), siteKey.getName(), uiPage.getEditPermission())) {
                return true;
            } else {
                portalApp.addMessage(
                        new ApplicationMessage("UIPortalManagement.msg.Invalid-EditPage-Permission", null));
                return false;
            }
        }

        protected void configureComposer(UIPortalComposer uiComposer) {
            uiComposer.setComponentConfig(UIPortalComposer.class, UIPortalComposer.UIPAGE_EDITOR);
            uiComposer.setId(UIPortalComposer.UIPAGE_EDITOR);
        }

        /**
         * @see org.exoplatform.portal.webui.workspace.UIMainActionListener.EditInlineActionListener#getEditLevel()
         */
        @Override
        protected EditLevel getEditLevel() {
            return EditLevel.EDIT_PAGE;
        }

        /* (non-Javadoc)
         * @see org.exoplatform.portal.webui.workspace.UIMainActionListener.EditInlineActionListener#adjustPortal(org.exoplatform.portal.webui.portal.UIPortal)
         */
        @Override
        protected void configurePortal(UIPortalApplication portalApp, UIPortal transientPortal) {
            if (portalApp.getEditMode() == EditMode.BLOCK) {
                transientPortal.setHeaderAndFooterRendered(false);
            }
        }

    }

    public static class CreatePortalActionListener extends EventListener<UIWorkingWorkspace> {
        public void execute(Event<UIWorkingWorkspace> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
            UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
            if (!userACL.hasCreatePortalPermission()) {
                uiApp.addMessage(new ApplicationMessage("UIPortalBrowser.msg.Invalid-createPermission", null));
                return;
            }
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class, "CreatePortal", "UIPortalForm");
            uiNewPortal.initPortalTemplateTab();
            uiMaskWS.setUIComponent(uiNewPortal);
            uiMaskWS.setShow(true);
            prContext.addUIComponentToUpdateByAjax(uiMaskWS);
        }
    }

    public static class EditBackgroundActionListener extends EventListener<UIWorkingWorkspace> {
        private Log log = ExoLogger.getExoLogger(this.getClass());

        @Override
        public void execute(Event<UIWorkingWorkspace> event) throws Exception {

            UIWorkingWorkspace workingWorkspace = event.getSource();
            UIPage uiPage = workingWorkspace.findFirstComponentOfType(UIPage.class);

            Method showEditBackgroundPopupMethod = null;
            try {
                if (uiPage == null) {
                    return;
                }
                showEditBackgroundPopupMethod = uiPage.getClass().getDeclaredMethod("showEditBackgroundPopup",
                        WebuiRequestContext.class);
            } catch (NoSuchMethodException ex) {
                log.warn(ex.getMessage(), ex);
            }
            if (showEditBackgroundPopupMethod != null) {
                showEditBackgroundPopupMethod.invoke(uiPage, event.getRequestContext());
            }
        }
    }

}

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

package org.exoplatform.portal.webui.page;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.navigation.UIPageNodeSelector;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/** Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 23, 2006 */
@ComponentConfigs(@ComponentConfig(template = "system:/groovy/webui/core/UIWizard.gtmpl", events = {
        @EventConfig(listeners = UIPageCreationWizard.ViewStep1ActionListener.class),
        @EventConfig(listeners = UIPageCreationWizard.ViewStep2ActionListener.class),
        @EventConfig(listeners = UIPageCreationWizard.ViewStep3ActionListener.class),
        @EventConfig(listeners = UIPageCreationWizard.ViewStep4ActionListener.class),
        @EventConfig(listeners = UIPageWizard.AbortActionListener.class) }))
public class UIPageCreationWizard extends UIPageWizard {

    public static final int FIRST_STEP = 1;

    public static final int SECOND_STEP = 2;

    public static final int THIRD_STEP = 3;

    public static final int NUMBER_OF_STEPs = 3;

    public UIPageCreationWizard() throws Exception {
        addChild(UIWizardPageSetInfo.class, null, null).setRendered(false);
        addChild(UIWizardPageSelectLayoutForm.class, null, null).setRendered(false);
        addChild(UIPagePreview.class, null, null).setRendered(false);
        setNumberSteps(NUMBER_OF_STEPs);
        viewStep(FIRST_STEP);
        setShowWelcomeComponent(false);
    }

    public void configure(UserNode node) throws Exception {
        UIPageNodeSelector nodeSelector = findFirstComponentOfType(UIPageNodeSelector.class);
        nodeSelector.configure(node);
        if (node.getNavigation().getKey().getType().equals(SiteType.USER)) {
            nodeSelector.setRendered(false);
        }
    }

    private UserNode saveData() throws Exception {
        UIPagePreview uiPagePreview = getChild(UIPagePreview.class);
        UIPage uiPage = (UIPage) uiPagePreview.getUIComponent();

        UIWizardPageSetInfo uiPageInfo = getChild(UIWizardPageSetInfo.class);
        UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
        UserNode selectedNode = uiNodeSelector.getSelectedNode();

        Page page = (Page) PortalDataMapper.buildModelObject(uiPage);
        UserNode createdNode = uiPageInfo.createUserNode(selectedNode);

        createdNode.setPageRef(page.getPageKey());

        //
        PageService pageService = getApplicationComponent(PageService.class);
        PageState pageState = new PageState(page.getTitle(), page.getDescription(), page.isShowMaxWindow(),
                page.getFactoryId(), page.getAccessPermissions() != null ? Arrays.asList(page.getAccessPermissions()) : null,
                page.getEditPermission());
        pageService.savePage(new PageContext(page.getPageKey(), pageState));

        //
        DataStorage dataService = getApplicationComponent(DataStorage.class);
        dataService.save(page);

        UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
        userPortal.saveNode(selectedNode, null);

        DescriptionService descriptionService = getApplicationComponent(DescriptionService.class);
        Map<Locale, Described.State> descriptions = new HashMap<Locale, Described.State>();
        Map<String, String> cachedLabels = uiPageInfo.getCachedLabels();

        for (String strLocale : cachedLabels.keySet()) {
            Locale locale;
            if (strLocale.contains("_")) {
                String[] arr = strLocale.split("_");
                if (arr.length > 2) {
                    locale = new Locale(arr[0], arr[1], arr[2]);
                } else {
                    locale = new Locale(arr[0], arr[1]);
                }
            } else {
                locale = new Locale(strLocale);
            }

            descriptions.put(locale, new Described.State(cachedLabels.get(strLocale), null));
        }

        descriptionService.setDescriptions(createdNode.getId(), descriptions);
        return createdNode;
    }

    /**
     * Returns <code>true</code> if the creating node is existing already. Otherwise it returns <code>false</code>
     *
     * @return true if the creating node is existing, otherwise it's false
     * @throws Exception
     */
    private boolean isSelectedNodeExist() {
        UIWizardPageSetInfo uiPageSetInfo = getChild(UIWizardPageSetInfo.class);
        String pageName = uiPageSetInfo.getUIStringInput(UIWizardPageSetInfo.PAGE_NAME).getValue();
        UserNode selectedPageNode = uiPageSetInfo.getSelectedPageNode();
        if (selectedPageNode.getChild(pageName) != null) {
            return true;
        }
        return false;
    }

    public static class ViewStep1ActionListener extends EventListener<UIPageWizard> {
        public void execute(Event<UIPageWizard> event) throws Exception {
            UIPageWizard uiWizard = event.getSource();

            uiWizard.updateWizardComponent();
            uiWizard.viewStep(FIRST_STEP);

            uiWizard.setShowActions(true);

            UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
            uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
        }
    }

    public static class ViewStep2ActionListener extends EventListener<UIPageCreationWizard> {
        public void execute(Event<UIPageCreationWizard> event) throws Exception {
            UIPageCreationWizard uiWizard = event.getSource();
            uiWizard.setShowActions(true);
            UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
            UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
            uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
            uiWizard.viewStep(SECOND_STEP);

            if (uiWizard.getSelectedStep() < SECOND_STEP) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep", null));
                uiWizard.viewStep(FIRST_STEP);
                uiWizard.updateWizardComponent();
                return;
            }

            if (uiWizard.isSelectedNodeExist()) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
                uiWizard.viewStep(FIRST_STEP);
                uiWizard.updateWizardComponent();
                return;
            }

            UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
            UIPageNodeSelector uiNodeSelector = uiPageSetInfo.getChild(UIPageNodeSelector.class);
            uiWizard.updateWizardComponent();
            UserNavigation navigation = uiNodeSelector.getNavigation();
            if (navigation == null) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.notSelectedPageNavigation",
                        new String[] {}));
                uiWizard.viewStep(FIRST_STEP);
                return;
            }

            if (uiPageSetInfo.getUICheckBoxInput(UIWizardPageSetInfo.VISIBLE).isChecked()
                    && uiPageSetInfo.getUICheckBoxInput(UIWizardPageSetInfo.SHOW_PUBLICATION_DATE).isChecked()) {

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                Date currentDate = currentCalendar.getTime();

                Calendar startCalendar = uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.START_PUBLICATION_DATE)
                        .getCalendar();
                Date startDate = (startCalendar != null) ? startCalendar.getTime() : currentDate;
                Calendar endCalendar = uiPageSetInfo.getUIFormDateTimeInput(UIWizardPageSetInfo.END_PUBLICATION_DATE)
                        .getCalendar();
                Date endDate = (endCalendar != null) ? endCalendar.getTime() : null;

                // Case 1: current date after start date
                if (currentDate.after(startDate)) {
                    Object[] args = {};
                    uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeStartDate", args,
                            ApplicationMessage.WARNING));
                    uiWizard.viewStep(FIRST_STEP);
                    return;

                    // Case 2: start date after end date
                } else if ((endCalendar != null) && (startCalendar != null) && (startDate.after(endDate))) {
                    Object[] args = {};
                    uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.startDateBeforeEndDate", args,
                            ApplicationMessage.WARNING));
                    uiWizard.viewStep(FIRST_STEP);
                    return;

                    // Case 3: start date is null and current date after end date
                } else if ((endCalendar != null) && (currentDate.after(endDate))) {
                    Object[] args = {};
                    uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeEndDate", args,
                            ApplicationMessage.WARNING));
                    uiWizard.viewStep(FIRST_STEP);
                    return;
                }
            }

            // Update the last value of selected locale to cached labels
            UIFormStringInput label = uiPageSetInfo.getUIStringInput(UIWizardPageSetInfo.I18N_LABEL);
            uiPageSetInfo.updateCachedLabels(uiPageSetInfo.getSelectedLocale(), label.getValue());
        }

    }

    public static class ViewStep3ActionListener extends EventListener<UIPageCreationWizard> {

        private void setDefaultPermission(Page page, SiteKey siteKey) {
            UIPortal uiPortal = Util.getUIPortal();
            if (SiteType.PORTAL.equals(siteKey.getType())) {
                page.setAccessPermissions(uiPortal.getAccessPermissions());
                page.setEditPermission(uiPortal.getEditPermission());
            } else if (SiteType.GROUP.equals(siteKey.getType())) {
                UserACL acl = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);
                String siteName = siteKey.getName().startsWith("/") ? siteKey.getName() : "/" + siteKey.getName();
                page.setAccessPermissions(new String[] { "*:" + siteName });
                page.setEditPermission(acl.getMakableMT() + ":" + siteName);
            }
        }

        public void execute(Event<UIPageCreationWizard> event) throws Exception {
            UIPageCreationWizard uiWizard = event.getSource();
            uiWizard.setShowActions(false);
            UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
            UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
            WebuiRequestContext context = Util.getPortalRequestContext();

            if (uiWizard.isSelectedNodeExist()) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
                uiWizard.viewStep(FIRST_STEP);
                uiWizard.updateWizardComponent();
                return;
            }
            uiWizard.viewStep(THIRD_STEP);

            if (uiWizard.getSelectedStep() < THIRD_STEP) {
                uiWizard.setShowActions(true);
                uiWizard.updateWizardComponent();
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.StepByStep", null));
                return;
            }

            uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(true);

            UIPageTemplateOptions uiPageTemplateOptions = uiWizard.findFirstComponentOfType(UIPageTemplateOptions.class);
            UIWizardPageSetInfo uiPageInfo = uiWizard.getChild(UIWizardPageSetInfo.class);

            UIPageNodeSelector uiNodeSelector = uiPageInfo.getChild(UIPageNodeSelector.class);
            UserNavigation pageNavi = uiNodeSelector.getNavigation();
            String ownerType = pageNavi.getKey().getTypeName();
            String ownerId = pageNavi.getKey().getName();

            UIFormStringInput pageName = uiPageInfo.getUIStringInput(UIWizardPageSetInfo.PAGE_NAME);
            Page page = uiPageTemplateOptions.createPageFromSelectedOption(ownerType, ownerId);
            page.setName("page" + page.hashCode());
            String pageId = ownerType + "::" + ownerId + "::" + page.getName();

            // check page is exist
            PageService pageService = uiWizard.getApplicationComponent(PageService.class);
            if (pageService.loadPage(PageKey.parse(pageId)) != null) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
                uiWizard.viewStep(FIRST_STEP);
                uiWizard.updateWizardComponent();
            }
            page.setModifiable(true);

            // Set default permissions on the page
            setDefaultPermission(page, pageNavi.getKey());

            if (page.getTitle() == null || page.getTitle().trim().length() == 0) {
                page.setTitle(pageName.getValue());
            }

            UIPagePreview uiPagePreview = uiWizard.getChild(UIPagePreview.class);

            UIPageFactory clazz = UIPageFactory.getInstance(page.getFactoryId());
            UIPage uiPage = clazz.createUIPage(context);

            PortalDataMapper.toUIPage(uiPage, page);
            uiPagePreview.setUIComponent(uiPage);

            uiWizard.updateWizardComponent();
            uiPageTemplateOptions.setSelectedOption(null);
        }
    }

    public static class ViewStep4ActionListener extends EventListener<UIPageCreationWizard> {
        public void execute(Event<UIPageCreationWizard> event) throws Exception {
            UIPageCreationWizard uiWizard = event.getSource();
            uiWizard.setShowActions(true);
            UIPortalApplication uiPortalApp = uiWizard.getAncestorOfType(UIPortalApplication.class);
            UIWorkingWorkspace uiWorkingWS = uiWizard.getAncestorOfType(UIWorkingWorkspace.class);
            uiWorkingWS.findFirstComponentOfType(UIPortalComposer.class).setRendered(false);
            if (uiWizard.isSelectedNodeExist()) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageCreationWizard.msg.NameNotSame", null));
                uiWizard.viewStep(FIRST_STEP);
                uiWizard.updateWizardComponent();
                return;
            }
            uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
            uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);

            PortalRequestContext pcontext = Util.getPortalRequestContext();

            try {
                UserNode newNode = uiWizard.saveData();
                NodeURL nodeURL = pcontext.createURL(NodeURL.TYPE).setNode(newNode);
                UIPortalToolPanel toolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
                toolPanel.setUIComponent(null);
                uiWizard.updateUIPortal(event);
                pcontext.sendRedirect(nodeURL.toString());
            } catch (NavigationServiceException ex) {
                pcontext.getUIApplication().addMessage(
                        new ApplicationMessage("UIPageCreationWizard.msg." + ex.getError().name(), null));
            }
        }
    }

}

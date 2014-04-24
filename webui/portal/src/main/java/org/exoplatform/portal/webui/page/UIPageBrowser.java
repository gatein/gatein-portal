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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfigs({
        @ComponentConfig(template = "system:/groovy/portal/webui/page/UIPageBrowser.gtmpl", events = {
                @EventConfig(listeners = UIPageBrowser.DeleteActionListener.class, confirm = "UIPageBrowse.deletePage"),
                @EventConfig(listeners = UIPageBrowser.EditInfoActionListener.class),
                @EventConfig(listeners = UIPageBrowser.AddNewActionListener.class) }),
        @ComponentConfig(id = "UIBrowserPageForm", type = UIPageForm.class, lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
                @EventConfig(listeners = UIPageBrowser.SavePageActionListener.class, name = "Save"),
                @EventConfig(listeners = UIPageForm.ChangeOwnerTypeActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageForm.ChangeOwnerIdActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPageForm.SelectMembershipActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE) }, initParams = @ParamConfig(name = "PageTemplate", value = "system:/WEB-INF/conf/uiconf/portal/webui/page/PageTemplate.groovy")),
        @ComponentConfig(type = UIFormInputSet.class, id = "PermissionSetting", template = "system:/groovy/webui/core/UITabSelector.gtmpl", events = { @EventConfig(listeners = UIFormInputSet.SelectComponentActionListener.class) }) })
@Serialized
public class UIPageBrowser extends UIContainer {

    public static final String[] BEAN_FIELD = { "pageId", "title", "accessPermissions", "editPermission" };

    public static final String[] ACTIONS = { "EditInfo", "Delete" };

    private boolean showAddNewPage = false;

    protected String pageSelectedId_;

    private List<SelectItemOption<String>> OPTIONS = new ArrayList<SelectItemOption<String>>(2);

    private Query<Page> lastQuery_;

    public UIPageBrowser() throws Exception {
        WebuiRequestContext contextui = WebuiRequestContext.getCurrentInstance();
        ResourceBundle res = contextui.getApplicationResourceBundle();
        OPTIONS.add(new SelectItemOption<String>(res.getString("UIPageSearchForm.label.option.portal"), "portal"));
        OPTIONS.add(new SelectItemOption<String>(res.getString("UIPageSearchForm.label.option.group"), "group"));

        UIPageSearchForm uiSearchForm = addChild(UIPageSearchForm.class, null, null);
        uiSearchForm.setOptions(OPTIONS);
        uiSearchForm.setId("UIPageSearchForm");
        UIRepeater uiRepeater = createUIComponent(UIRepeater.class, null, null);
        uiRepeater.configure("pageId", BEAN_FIELD, ACTIONS);

        lastQuery_ = new Query<Page>(null, null, null, null, Page.class);
        lastQuery_.setOwnerType(OPTIONS.get(0).getValue());

        UIVirtualList virtualList = addChild(UIVirtualList.class, null, null);
        virtualList.setUIComponent(uiRepeater);
    }

    public Query<Page> getLastQuery() {
        return lastQuery_;
    }

    public Query<Page> getQuery(UIFormInputSet searchInputs) {
        Query<Page> query = new Query<Page>(null, null, null, null, Page.class);
        UIFormStringInput titleInput = (UIFormStringInput) searchInputs.getChild(0);
        UIFormStringInput siteNameInput = (UIFormStringInput) searchInputs.getChild(1);
        UIFormSelectBox select = (UIFormSelectBox) searchInputs.getChild(2);

        String siteName = siteNameInput.getValue();
        String title = titleInput.getValue();
        String ownerType = select.getValue();
        if (title != null) {
            query.setTitle(title.trim());
        }
        if (siteName != null && !siteName.trim().equals("")) {
            query.setOwnerId(siteName.trim());
        }

        query.setOwnerType(ownerType);
        query.setName(null);

        return query;
    }

    /**
     * Update data feed in UIRepeater with a given query. Returns false if no result is found, true other wise
     *
     * @param query
     * @return
     * @throws Exception
     */
    public boolean feedDataWithQuery(Query<Page> query) throws Exception {
        lastQuery_ = query;

        UIVirtualList virtualList = getChild(UIVirtualList.class);
        if (lastQuery_ == null) {
            lastQuery_ = new Query<Page>(null, null, null, null, Page.class);
        }

        // virtualList.dataBind(new PageQueryAccessList(lastQuery_, 10));
        virtualList.dataBind(new PageIterator(lastQuery_.getOwnerType(), lastQuery_.getOwnerId(), lastQuery_.getName(),
                lastQuery_.getTitle(), 10));

        //
        UIRepeater repeater = (UIRepeater) virtualList.getRepeater();
        if (repeater.hasNext()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show a popup informing that no result available for the last query
     *
     */
    public static void showNoResultMessagePopup() {
        UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.empty", null));
    }

    public void quickSearch(UIFormInputSet quickSearchInput) throws Exception {
        lastQuery_ = this.getQuery(quickSearchInput);
        boolean dataAvailable = feedDataWithQuery(lastQuery_);
        if (!dataAvailable) {
            showNoResultMessagePopup();
        }

        if (this.<UIComponent> getParent() instanceof UIPopupWindow) {
            UIPopupWindow popupWindow = getParent();
            popupWindow.setShow(true);
        }
    }

    public boolean isShowAddNewPage() {
        return showAddNewPage;
    }

    public void setShowAddNewPage(boolean showAddNewPage) {
        this.showAddNewPage = showAddNewPage;
    }

    public void processDecode(WebuiRequestContext context) throws Exception {
        super.processDecode(context);
        UIForm uiForm = getAncestorOfType(UIForm.class);
        String action = null;
        if (uiForm != null) {
            action = uiForm.getSubmitAction();
        } else {
            action = context.getRequestParameter(UIForm.ACTION);
        }
        if (action == null)
            return;
        Event<UIComponent> event = createEvent(action, Event.Phase.PROCESS, context);
        if (event != null)
            event.broadcast();
    }

    public void advancedSearch(UIFormInputSet advancedSearchInput) {
    }

    public static class DeleteActionListener extends EventListener<UIPageBrowser> {
        public void execute(Event<UIPageBrowser> event) throws Exception {
            UIPageBrowser uiPageBrowser = event.getSource();
            WebuiRequestContext context = event.getRequestContext();
            String id = context.getRequestParameter(OBJECTID);

            UserPortalConfigService service = uiPageBrowser.getApplicationComponent(UserPortalConfigService.class);
            UIApplication uiApp = context.getUIApplication();
            PageContext page = (id != null) ? service.getPageService().loadPage(PageKey.parse(id)) : null;

            if (page == null) {
                uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.PageNotExist", new String[] { id }, 1));
                return;
            }

            UserACL userACL = uiPageBrowser.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermission(page)) {
                uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.delete.NotDelete", new String[] { id }, 1));
                return;
            }

            UIPortal uiPortal = Util.getUIPortal();
            UserNode userNode = uiPortal.getSelectedUserNode();
            boolean isDeleteCurrentPage = page.getKey().equals(userNode.getPageRef());

            service.getPageService().destroyPage(page.getKey());
            // Minh Hoang TO: The cached UIPage objects corresponding to removed Page should be removed here.
            // As we have multiple UIPortal, which means multiple caches of UIPage. It 's unwise to garbage
            // all UIPage caches at once. Better solution is to clear UIPage on browsing to PageNode having Page
            // removed

            if (isDeleteCurrentPage) {
                SiteKey siteKey = userNode.getNavigation().getKey();
                PageNodeEvent<UIPortalApplication> pnevent = new PageNodeEvent<UIPortalApplication>(
                        Util.getUIPortalApplication(), PageNodeEvent.CHANGE_NODE, siteKey, userNode.getURI());
                uiPortal.broadcast(pnevent, Phase.PROCESS);
            } else {
                boolean dataAvailable = uiPageBrowser.feedDataWithQuery(uiPageBrowser.getLastQuery());
                if (!dataAvailable) {
                    showNoResultMessagePopup();
                }
                event.getRequestContext().addUIComponentToUpdateByAjax(uiPageBrowser);
            }
        }

        /**
         *
         * This method remove User Page node that reference to page. If page is exist, remove User Page node. If page is not
         * exist, do nothing.
         *
         * @param page the page is referenced by User Page node
         * @param event
         * @throws Exception any exception
         */
        private void removePageNode(PageContext page, Event<UIPageBrowser> event) throws Exception {
            PortalRequestContext prc = Util.getPortalRequestContext();
            UserPortal userPortal = prc.getUserPortalConfig().getUserPortal();

            UserNavigation userNav = userPortal.getNavigation(SiteKey.user(event.getRequestContext().getRemoteUser()));
            UserNode rootNode = userPortal.getNode(userNav, Scope.CHILDREN, null, null);
            if (rootNode == null) {
                return;
            }

            for (UserNode userNode : rootNode.getChildren()) {
                if (page.getKey().equals(userNode.getPageRef())) {
                    // Remove pageNode
                    rootNode.removeChild(userNode.getName());
                    userPortal.saveNode(rootNode, null);

                    // Update navigation and UserToolbarGroupPortlet

                    String pageRef = page.getKey().format();
                    if (pageRef != null && pageRef.length() > 0) {
                        // Remove from cache
                        UIPortal uiPortal = Util.getUIPortal();
                        uiPortal.clearUIPage(pageRef);
                    }

                    // Update UserToolbarDashboardPortlet
                    ActionResponse actResponse = event.getRequestContext().getResponse();
                    actResponse.setEvent(new QName("NavigationChange"), userNode.getName());
                    return;
                }
            }
        }
    }

    public static class EditInfoActionListener extends EventListener<UIPageBrowser> {
        public void execute(Event<UIPageBrowser> event) throws Exception {
            UIPageBrowser uiPageBrowser = event.getSource();
            WebuiRequestContext context = event.getRequestContext();
            PortalRequestContext pcontext = (PortalRequestContext) context.getParentAppRequestContext();
            UIPortalApplication uiPortalApp = (UIPortalApplication) pcontext.getUIApplication();
            String id = context.getRequestParameter(OBJECTID);
            UserPortalConfigService service = uiPageBrowser.getApplicationComponent(UserPortalConfigService.class);

            // Check existence of the page
            PageContext pageContext = (id != null) ? service.getPage(PageKey.parse(id)) : null;
            if (pageContext == null) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.PageNotExist", new String[] { id }, 1));
                return;
            }

            // Check current user 's permissions on the page
            UserACL userACL = uiPageBrowser.getApplicationComponent(UserACL.class);
            if (!userACL.hasEditPermission(pageContext)) {
                uiPortalApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.edit.NotEditPage", new String[] { id }, 1));
                return;
            }

            // Need this code to override editpage action in extension project
            UIPageFactory clazz = UIPageFactory.getInstance(pageContext.getState().getFactoryId());
            UIPage uipage = clazz.createUIPage(null);
            Page page = service.getDataStorage().getPage(id);
            pageContext.update(page);
            uipage.switchToEditMode(page);
        }
    }

    public static class AddNewActionListener extends EventListener<UIPageBrowser> {
        public void execute(Event<UIPageBrowser> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiApp = (UIPortalApplication) prContext.getUIApplication();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

            boolean hasPermission = false;
            String currentUser = prContext.getRemoteUser();
            if (currentUser != null) {
                UIPageForm uiPageForm = uiMaskWS.createUIComponent(UIPageForm.class, "UIBrowserPageForm", "UIPageForm");
                uiPageForm.buildForm(null);

                UIFormSelectBox slcOwnerType = uiPageForm.getUIFormSelectBox(UIPageForm.OWNER_TYPE);
                List<SelectItemOption<String>> types = slcOwnerType.getOptions();
                for (int i = 0; i < types.size(); i++) {
                    if (PortalConfig.USER_TYPE.equals(types.get(i).getValue())) {
                        types.remove(types.get(i));
                        break;
                    }
                }

                if (!types.isEmpty()) {
                    hasPermission = true;

                    slcOwnerType.setOptions(types);
                    Event<UIComponent> slcEvent = uiPageForm.createEvent("ChangeOwnerType", Phase.DECODE, event.getRequestContext());
                    slcEvent.broadcast();

                    uiMaskWS.setUIComponent(uiPageForm);
                    uiMaskWS.setShow(true);
                    prContext.addUIComponentToUpdateByAjax(uiMaskWS);
                }
            }

            if (!hasPermission) {
                uiMaskWS.setUIComponent(null);
                uiApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-CreatePage-Permission", null));
            }
        }
    }

    public static class SavePageActionListener extends UIPageForm.SaveActionListener {
        public void execute(Event<UIPageForm> event) throws Exception {
            UIPageForm uiPageForm = event.getSource();
            UIPortalApplication uiPortalApp = uiPageForm.getAncestorOfType(UIPortalApplication.class);
            PortalRequestContext pcontext = Util.getPortalRequestContext();
            UIPage uiPage = uiPageForm.getUIPage();
            Page page = new Page();
            uiPageForm.invokeSetBindingBean(page);
            DataStorage dataService = uiPageForm.getApplicationComponent(DataStorage.class);
            // create new page
            if (uiPage == null) {
                PageService pageService = uiPageForm.getApplicationComponent(PageService.class);
                PageContext existPage = pageService.loadPage(page.getPageKey());
                if (existPage != null) {
                    uiPortalApp.addMessage(new ApplicationMessage("UIPageForm.msg.sameName", null));
                    return;
                }
                page.setModifiable(true);
                if (page.getChildren() == null) {
                    page.setChildren(new ArrayList<ModelObject>());
                }

                //
                PageState pageState = PageUtils.toPageState(page);
                pageService.savePage(new PageContext(page.getPageKey(), pageState));

                //
                dataService.save(page);
                postSave(uiPortalApp, pcontext);
                return;
            }

            page.setOwnerType(uiPage.getSiteKey().getTypeName());

            List<UIPortlet<?, ?>> uiPortlets = new ArrayList<UIPortlet<?, ?>>();
            findAllPortlet(uiPortlets, uiPage);
            ArrayList<ModelObject> applications = new ArrayList<ModelObject>();
            for (UIPortlet uiPortlet : uiPortlets) {
                applications.add(PortalDataMapper.buildModelObject(uiPortlet));
            }

            List<UIComponent> uiChildren = uiPage.getChildren();
            if (uiChildren == null)
                return;
            ArrayList<ModelObject> children = new ArrayList<ModelObject>();
            for (UIComponent child : uiChildren) {
                ModelObject component = PortalDataMapper.buildModelObject(child);
                if (component != null)
                    children.add(component);
            }
            page.setChildren(children);
            uiPage.getChildren().clear();

            PortalDataMapper.toUIPage(uiPage, page);

            // if (page.getTemplate() == null) page.setTemplate(uiPage.getTemplate());
            if (page.getChildren() == null)
                page.setChildren(new ArrayList<ModelObject>());
        }

        private void postSave(UIPortalApplication uiPortalApp, WebuiRequestContext context) throws Exception {
            UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            uiMaskWS.createEvent("Close", Phase.DECODE, context).broadcast();

            UIWorkingWorkspace uiWorkWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            uiWorkWS.updatePortletsByName("PageManagementPortlet");
        }
    }
}

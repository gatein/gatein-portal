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

package org.exoplatform.dashboard.webui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 *
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 * Aug 10, 2009
 */
@ComponentConfig(template = "app:/groovy/dashboard/webui/component/UITabPaneDashboard.gtmpl", events = {
        @EventConfig(confirm = "UITabPaneDashboard.msg.deleteTab", name = "DeleteTab", listeners = UITabPaneDashboard.DeleteTabActionListener.class),
        @EventConfig(name = "AddDashboard", listeners = UITabPaneDashboard.AddDashboardActionListener.class),
        @EventConfig(name = "SwitchShowedTabRange", listeners = UITabPaneDashboard.SwitchShowedTabRangeActionListener.class),
        @EventConfig(name = "RenameTabLabel", listeners = UITabPaneDashboard.RenameTabLabelActionListener.class),
        @EventConfig(name = "PermuteTab", listeners = UITabPaneDashboard.PermuteTabActionListener.class) })
public class UITabPaneDashboard extends UIContainer {

    private static Log logger = ExoLogger.getExoLogger(UITabPaneDashboard.class);

    private int startShowIndex;

    private int endShowIndex;

    private int tabNbs;

    private UserPortalConfigService configService;

    private DataStorage dataService;

    private UIPortal uiPortal;

    private static final int MAX_SHOWED_TAB_NUMBER = 6;

    public static final String PAGE_TEMPLATE = "dashboard";

    private final UserNodeFilterConfig filterConfig;
    private static final Scope TAB_PANE_DASHBOARD_SCOPE = Scope.CHILDREN;

    public UITabPaneDashboard() {
        configService = getApplicationComponent(UserPortalConfigService.class);
        dataService = getApplicationComponent(DataStorage.class);
        uiPortal = Util.getUIPortal();

        UserNodeFilterConfig.Builder scopeBuilder = UserNodeFilterConfig.builder();
        scopeBuilder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
        scopeBuilder.withTemporalCheck();
        filterConfig = scopeBuilder.build();
    }

    public int getCurrentNumberOfTabs() throws Exception {
        return getSameSiblingsNode().size();
    }

    public int getStartShowIndex() {
        return this.startShowIndex;
    }

    public int getEndShowIndex() {
        if (this.endShowIndex > 0) {
            return this.endShowIndex;
        } else {
            return Math.min(this.tabNbs, this.startShowIndex + MAX_SHOWED_TAB_NUMBER);
        }
    }

    public UserNode getParentTab() throws Exception {
        UserPortal userPortal = getUserPortal();
        UserNode selectedNode = uiPortal.getSelectedUserNode();
        UserNode currParent = selectedNode.getParent();

        UserNode parent = null;
        if ("".equals(currParent.getURI())) {
            parent = userPortal.getNode(currParent.getNavigation(), TAB_PANE_DASHBOARD_SCOPE, filterConfig, null);
        } else {
            parent = userPortal.resolvePath(currParent.getNavigation(), filterConfig, currParent.getURI());
        }

        if (parent != null) {
            try {
                userPortal.updateNode(parent, TAB_PANE_DASHBOARD_SCOPE, null);
            } catch (NavigationServiceException e) {
                parent = null;
            }
        }

        //
        return parent;
    }

    public Collection<UserNode> getSameSiblingsNode() throws Exception {
        UserNode parentTab = getParentTab();

        if (parentTab == null) {
            return Collections.emptyList();
        }
        return parentTab.getChildren();
    }

    public UserNavigation getCurrentUserNavigation() {
        UserPortal userPortal = getUserPortal();
        WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
        return userPortal.getNavigation(SiteKey.user(rcontext.getRemoteUser()));
    }

    private UserPortal getUserPortal() {
        PortalRequestContext prc = Util.getPortalRequestContext();
        return prc.getUserPortalConfig().getUserPortal();
    }

    /**
     * Remove node specified by nodeName and returns the node to switch to
     *
     * @param nodeName - name of the Node that will be remove
     * @return return the node that should be selected after remove node
     */
    public UserNode removePageNode(String nodeName) {
        try {
            UserNode parentNode = getParentTab();
            if (parentNode == null || parentNode.getChild(nodeName) == null) {
                return null;
            }

            UserNode tobeRemoved = parentNode.getChild(nodeName);
            UserNode prevNode = null;

            if (parentNode.getChildrenCount() >= 2) {
                for (UserNode child : parentNode.getChildren()) {
                    if (child.getName().equals(nodeName)) {
                        parentNode.removeChild(nodeName);
                        break;
                    }
                    prevNode = child;
                }

                PageKey pageRef = tobeRemoved.getPageRef();
                if (pageRef != null) {
                    PageContext page = configService.getPageService().loadPage(pageRef);
                    if (page != null) {
                        configService.getPageService().destroyPage(pageRef);
                    }
                    UIPortal uiPortal = Util.getUIPortal();
                    // Remove from cache
                    uiPortal.setUIPage(pageRef.format(), null);
                }
                getUserPortal().saveNode(parentNode, null);
            } else {
                getAncestorOfType(UIApplication.class).addMessage(
                        new ApplicationMessage("UITabPaneDashboard.msg.cannotDeleteLastTab", null));
                return null;
            }

            UserNode selectedNode = uiPortal.getSelectedUserNode();
            if (nodeName.equals(selectedNode.getName())) {
                selectedNode = prevNode != null ? prevNode : parentNode.getChildren().iterator().next();
            }
            return selectedNode;
        } catch (Exception ex) {
            return null;
        }
    }

    public UserNode createNewPageNode(String nodeLabel) {
        try {
            UserNavigation userNav = getCurrentUserNavigation();
            UserNode parentNode = getParentTab();
            if (userNav == null || parentNode == null) {
                return null;
            }

            String uniqueNodeName = buildName(nodeLabel);
            SiteKey siteKey = userNav.getKey();
            Page page = configService.createPageTemplate(UITabPaneDashboard.PAGE_TEMPLATE, siteKey.getTypeName(),
                    siteKey.getName());
            page.setTitle(HTMLEntityEncoder.getInstance().encode(nodeLabel));
            page.setName(uniqueNodeName + page.hashCode());

            //
            PageKey pageKey = new PageKey(siteKey, page.getName());
            PageState pageState = PageUtils.toPageState(page);
            configService.getPageService().savePage(new PageContext(pageKey, pageState));

            //
            dataService.save(page);

            if (parentNode.getChild(uniqueNodeName) != null) {
                uniqueNodeName = uniqueNodeName + "_" + System.currentTimeMillis();
            }

            UserNode tabNode = parentNode.addChild(uniqueNodeName);
            tabNode.setLabel(nodeLabel);
            tabNode.setPageRef(page.getPageKey());

            getUserPortal().saveNode(parentNode, null);

            return tabNode;
        } catch (Exception ex) {
            logger.info("Could not create page template", ex);
            return null;
        }
    }

    private boolean validateName(String label) {
        if (label == null || label.length() < 1) {
            return false;
        }
        label = label.trim();
        if (label.length() < 1 || Character.isDigit(label.charAt(0)) || label.charAt(0) == '-') {
            return false;
        }
        return true;
    }

    private String buildName(String label) {
        StringBuffer nodeName = new StringBuffer();
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-') {
                nodeName.append(c);
                continue;
            }
            nodeName.append("_");
        }
        return nodeName.toString();
    }

    public UserNode renamePageNode(String nodeName, String newNodeLabel) {
        try {
            UserNode parentNode = getParentTab();
            if (parentNode == null || parentNode.getChild(nodeName) == null) {
                return null;
            }

            String newNodeName = buildName(newNodeLabel);
            if (parentNode.getChild(newNodeName) != null) {
                newNodeName = newNodeName + "_" + System.currentTimeMillis();
            }

            UserNode renamedNode = parentNode.getChild(nodeName);
            renamedNode.setName(newNodeName);

            // Distinguish between "Extended label mode" and basic label mode.
            // In "Extended label mode" node.getLabel() == null.
            if (renamedNode.getLabel() != null) {
                renamedNode.setLabel(newNodeLabel);
            } else {
                renamedNode.setResolvedLabel(newNodeLabel);
            }

            // Rename the page only in basic label mode.
            // For "Extended label mode" use page editor.
            if (renamedNode.getPageRef() != null && renamedNode.getLabel() != null) {
                PageContext page = configService.getPageService().loadPage(renamedNode.getPageRef());
                if (page != null) {
                    PageState state = page.getState();
                    String encodedLabel = HTMLEntityEncoder.getInstance().encode(newNodeLabel);
                    page.setState(state.builder().displayName(encodedLabel).build());
                    configService.getPageService().savePage(page);
                    // Update UIPage cache on UIPortal.
                    uiPortal.setUIPage(renamedNode.getPageRef().format(), null);
                }
            }

            getUserPortal().saveNode(parentNode, null);
            return renamedNode;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Exchange two nodes under user navigation
     *
     * @param firstIndex
     * @param secondIndex
     */
    public boolean permutePageNode(int firstIndex, int secondIndex) throws Exception {
        UserNode parentNode = getParentTab();
        List<UserNode> siblings = new ArrayList<UserNode>(getSameSiblingsNode());
        if (firstIndex == secondIndex) {
            return false;
        }

        try {
            UserNode firstNode = siblings.get(firstIndex);
            parentNode.addChild(secondIndex, firstNode);

            getUserPortal().saveNode(parentNode, null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static class DeleteTabActionListener extends EventListener<UITabPaneDashboard> {
        public void execute(Event<UITabPaneDashboard> event) throws Exception {
            UITabPaneDashboard source = event.getSource();
            PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
            String nodeName = context.getRequestParameter(UIComponent.OBJECTID);
            UserNode nextNode = source.getFirstAvailableNode();
            UserNode selectedNode = source.removePageNode(nodeName);

            // If the node is removed successfully, then redirect to the node specified by tab on the left
            if (selectedNode != null) {
                nextNode = selectedNode;
            }

            PortalRequestContext prContext = Util.getPortalRequestContext();
            NodeURL nodeURL = prContext.createURL(NodeURL.TYPE).setNode(nextNode);
            context.sendRedirect(nodeURL.toString());
        }
    }

    public static class AddDashboardActionListener extends EventListener<UITabPaneDashboard> {
        public void execute(Event<UITabPaneDashboard> event) throws Exception {
            UITabPaneDashboard tabPane = event.getSource();
            PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
            String newTabLabel = context.getRequestParameter(UIComponent.OBJECTID);
            UserNode node = tabPane.getFirstAvailableNode();
            if (!tabPane.validateName(newTabLabel)) {
                Object[] args = { newTabLabel };
                context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
            } else {
                UserNode createdNode = tabPane.createNewPageNode(newTabLabel);
                if (createdNode != null) {
                    node = createdNode;
                }
            }

            PortalRequestContext prContext = Util.getPortalRequestContext();
            NodeURL nodeURL = prContext.createURL(NodeURL.TYPE).setNode(node);
            context.sendRedirect(nodeURL.toString());
        }
    }

    public static class SwitchShowedTabRangeActionListener extends EventListener<UITabPaneDashboard> {
        public void execute(Event<UITabPaneDashboard> event) throws Exception {
        }
    }

    public static class RenameTabLabelActionListener extends EventListener<UITabPaneDashboard> {

        public static final String RENAMED_TAB_LABEL_PARAMETER = "newTabLabel";

        public void execute(Event<UITabPaneDashboard> event) throws Exception {
            UITabPaneDashboard tabPane = event.getSource();
            PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
            UIApplication rootUI = context.getUIApplication();

            String newTabLabel = context.getRequestParameter(RENAMED_TAB_LABEL_PARAMETER);
            UserNode node = tabPane.getFirstAvailableNode();
            if (!tabPane.validateName(newTabLabel)) {
                Object[] args = { newTabLabel };
                rootUI.addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
            } else {
                String nodeName = context.getRequestParameter(UIComponent.OBJECTID);
                UserNode renamedNode = tabPane.renamePageNode(nodeName, newTabLabel);
                if (renamedNode != null) {
                    node = renamedNode;
                }
            }
            PortalRequestContext prContext = Util.getPortalRequestContext();
            NodeURL nodeURL = prContext.createURL(NodeURL.TYPE).setNode(node);
            context.sendRedirect(nodeURL.toString());
        }
    }

    public static class PermuteTabActionListener extends EventListener<UITabPaneDashboard> {

        public static final String TARGETED_TAB_PARAMETER = "targetedTab";

        public void execute(Event<UITabPaneDashboard> event) throws Exception {
            UITabPaneDashboard tabPane = event.getSource();
            WebuiRequestContext context = event.getRequestContext();
            int dragingTabIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
            int targetedTabIndex = Integer.parseInt(context.getRequestParameter(TARGETED_TAB_PARAMETER));

            // If two nodes are permuted, then update the tab pane
            if (tabPane.permutePageNode(dragingTabIndex, targetedTabIndex)) {
                context.addUIComponentToUpdateByAjax(tabPane);
            }
        }
    }

    /**
     * Return the current node uri, if it's been deleted, return first sibling node uri if there is no node remain, return
     * default path
     *
     * @throws Exception
     */
    public UserNode getFirstAvailableNode() throws Exception {
        UserNode parentTab = getParentTab();
        if (parentTab != null) {
            UserNode currNode = Util.getUIPortal().getSelectedUserNode();
            if (parentTab.getChildren().size() == 0 && parentTab.getURI() != null) {
                return parentTab;
            }

            if (parentTab.getChild(currNode.getName()) != null) {
                return currNode;
            } else {
                return parentTab.getChild(0);
            }
        }

        return getUserPortal().getDefaultPath(null);
    }
}

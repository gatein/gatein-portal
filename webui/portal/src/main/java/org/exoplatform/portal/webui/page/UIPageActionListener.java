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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Just a class that contains the Page related action listeners
 *
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
public class UIPageActionListener {
    /** . */
    private static final Logger log = LoggerFactory.getLogger(UIPageActionListener.class);

    public static class ChangeNodeActionListener extends EventListener<UIPortalApplication> {
        public void execute(Event<UIPortalApplication> event) throws Exception {
            PortalRequestContext pcontext = PortalRequestContext.getCurrentInstance();
            UserPortal userPortal = pcontext.getUserPortalConfig().getUserPortal();
            UIPortalApplication uiPortalApp = event.getSource();
            UIPortal showedUIPortal = uiPortalApp.getCurrentSite();

            UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
            builder.withReadCheck();

            PageNodeEvent<UIPortalApplication> pageNodeEvent = (PageNodeEvent<UIPortalApplication>) event;
            String nodePath = pageNodeEvent.getTargetNodeUri();

            UserNode targetNode = null;
            SiteKey siteKey = pageNodeEvent.getSiteKey();
            if (siteKey != null) {
                if (pcontext.getRemoteUser() == null
                        && (siteKey.getType().equals(SiteType.GROUP) || siteKey.getType().equals(SiteType.USER))) {
                    NavigationService service = uiPortalApp.getApplicationComponent(NavigationService.class);
                    NavigationContext navContext = service.loadNavigation(siteKey);
                    if (navContext != null) {
                        uiPortalApp.setLastRequestNavData(null);
                        pcontext.requestAuthenticationLogin();
                        return;
                    }
                }

                UserNavigation navigation = userPortal.getNavigation(siteKey);
                if (navigation != null) {
                    targetNode = userPortal.resolvePath(navigation, builder.build(), nodePath);
                    if (targetNode == null) {
                        // If unauthenticated users have no permission on PORTAL node and URL is valid, they will be required to
                        // login
                        if (pcontext.getRemoteUser() == null && siteKey.getType().equals(SiteType.PORTAL)) {
                            targetNode = userPortal.resolvePath(navigation, null, nodePath);
                            if (targetNode != null) {
                                uiPortalApp.setLastRequestNavData(null);
                                pcontext.requestAuthenticationLogin();
                                return;
                            }
                        } else {
                            // If path to node is invalid, get the default node instead of.
                            targetNode = userPortal.getDefaultPath(navigation, builder.build());
                        }
                    }
                }

                if (targetNode != null) {
                    // let's check if this node can be shown at this time
                    long now = System.currentTimeMillis();
                    long startPublicationTime = targetNode.getStartPublicationTime();
                    long endPublicationTime = targetNode.getEndPublicationTime();
                    boolean restrictOutsidePublicationWindow = targetNode.isRestrictOutsidePublicationWindow();

                    if (shouldRestrictAccess(restrictOutsidePublicationWindow, now, startPublicationTime, endPublicationTime)) {
                        if (log.isInfoEnabled()) {
                            log.info("User "
                                    +pcontext.getRemoteUser()
                                    +" has tried to access a node ("
                                    +targetNode.getURI()
                                    +") which is outside of the publishing window and is restricted.");
                        }
                        targetNode = userPortal.getDefaultPath(navigation, builder.build());
                    }
                }
            }

            if (targetNode == null) {
                targetNode = userPortal.getDefaultPath(builder.build());
                if (targetNode == null) {
                    if (showedUIPortal != null) {
                        UIPageBody uiPageBody = showedUIPortal.findFirstComponentOfType(UIPageBody.class);
                        uiPageBody.setUIComponent(null);
                    }
                    return;
                }
            }

            UserNavigation targetNav = targetNode.getNavigation();
            UserNode currentNavPath = null;
            if (showedUIPortal != null) {
                currentNavPath = showedUIPortal.getNavPath();
            }

            if (currentNavPath != null && currentNavPath.getNavigation().getKey().equals(targetNav.getKey())) {
                // Case 1: Both navigation type and id are not changed, but current page node is changed and it is not a first
                // request.
                if (!currentNavPath.getURI().equals(targetNode.getURI())) {
                    showedUIPortal.setNavPath(targetNode);
                }
            } else {
                // Case 2: Either navigation type or id has been changed
                // First, we try to find a cached UIPortal
                UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
                uiWorkingWS.setRenderedChild(UIPortalApplication.UI_VIEWING_WS_ID);
                uiPortalApp.setModeState(UIPortalApplication.NORMAL_MODE);
                showedUIPortal = uiPortalApp.getCachedUIPortal(targetNav.getKey());
                if (showedUIPortal != null) {
                    showedUIPortal.setNavPath(targetNode);
                    uiPortalApp.setCurrentSite(showedUIPortal);

                    DataStorage storageService = uiPortalApp.getApplicationComponent(DataStorage.class);
                    PortalConfig associatedPortalConfig = storageService.getPortalConfig(targetNav.getKey().getTypeName(),
                            targetNav.getKey().getName());
                    UserPortalConfig userPortalConfig = pcontext.getUserPortalConfig();

                    // Update layout-related data on UserPortalConfig
                    userPortalConfig.setPortalConfig(associatedPortalConfig);
                } else {
                    showedUIPortal = buildUIPortal(targetNav.getKey(), uiPortalApp, pcontext.getUserPortalConfig());
                    if (showedUIPortal == null) {
                        return;
                    }
                    showedUIPortal.setNavPath(targetNode);
                    uiPortalApp.setCurrentSite(showedUIPortal);
                    uiPortalApp.putCachedUIPortal(showedUIPortal);
                }
            }

            showedUIPortal.refreshUIPage();
            pcontext.setFullRender(true);
            pcontext.addUIComponentToUpdateByAjax(uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID));
        }

        protected static boolean shouldRestrictAccess(boolean restrictOutsidePublicationWindow, long now, long startPublicationTime, long endPublicationTime) {
            return ((restrictOutsidePublicationWindow && startPublicationTime > now) ||
                    (restrictOutsidePublicationWindow && endPublicationTime < now && endPublicationTime > 0));
        }

        private UIPortal buildUIPortal(SiteKey siteKey, UIPortalApplication uiPortalApp, UserPortalConfig userPortalConfig)
                throws Exception {
            DataStorage storage = uiPortalApp.getApplicationComponent(DataStorage.class);
            if (storage == null) {
                return null;
            }
            PortalConfig portalConfig = storage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
            Container layout = portalConfig.getPortalLayout();
            if (layout != null) {
                userPortalConfig.setPortalConfig(portalConfig);
            }
            UIPortal uiPortal = uiPortalApp.createUIComponent(UIPortal.class, null, null);

            // Reset selected navigation on userPortalConfig
            PortalDataMapper.toUIPortal(uiPortal, userPortalConfig.getPortalConfig());
            return uiPortal;
        }
    }

    public static class DeleteGadgetActionListener extends EventListener<UIPage> {
        public void execute(Event<UIPage> event) throws Exception {
            WebuiRequestContext pContext = event.getRequestContext();
            String id = pContext.getRequestParameter(UIComponent.OBJECTID);
            UIPage uiPage = event.getSource();
            List<UIGadget> uiWidgets = new ArrayList<UIGadget>();
            uiPage.findComponentOfType(uiWidgets, UIGadget.class);
            for (UIGadget uiWidget : uiWidgets) {
                if (uiWidget.getId().equals(id)) {
                    uiPage.getChildren().remove(uiWidget);
                    String userName = pContext.getRemoteUser();
                    if (userName != null && userName.trim().length() > 0) {
                        // Julien : commented as normally removing the gadget should
                        // remove the state associated with it
                        // in the MOP
                        // UserGadgetStorage widgetDataService =
                        // uiPage.getApplicationComponent(UserGadgetStorage.class) ;
                        // widgetDataService.delete(userName,
                        // uiWidget.getApplicationName(), uiWidget.getId()) ;
                    }
                    if (uiPage.isModifiable()) {
                        Page page = (Page) PortalDataMapper.buildModelObject(uiPage);
                        if (page.getChildren() == null) {
                            page.setChildren(new ArrayList<ModelObject>());
                        }

                        //
                        PageService pageService = uiPage.getApplicationComponent(PageService.class);
                        PageState pageState = PageUtils.toPageState(page);
                        pageService.savePage(new PageContext(page.getPageKey(), pageState));

                        //
                        DataStorage dataService = uiPage.getApplicationComponent(DataStorage.class);
                        dataService.save(page);
                    }
                    break;
                }
            }
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            pcontext.ignoreAJAXUpdateOnPortlets(false);
            pcontext.setResponseComplete(true);
            pcontext.getWriter().write(EventListener.RESULT_OK);
        }
    }

    public static class RemoveChildActionListener extends EventListener<UIPage> {
        public void execute(Event<UIPage> event) throws Exception {
            UIPage uiPage = event.getSource();
            String id = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            if (uiPage.isModifiable()) {
                uiPage.removeChildById(id);
                Page page = (Page) PortalDataMapper.buildModelObject(uiPage);
                if (page.getChildren() == null) {
                    page.setChildren(new ArrayList<ModelObject>());
                }

                //
                PageService pageService = uiPage.getApplicationComponent(PageService.class);
                PageState pageState = PageUtils.toPageState(page);
                pageService.savePage(new PageContext(page.getPageKey(), pageState));

                //
                DataStorage dataService = uiPage.getApplicationComponent(DataStorage.class);
                dataService.save(page);

                //
                pcontext.ignoreAJAXUpdateOnPortlets(false);
                pcontext.setResponseComplete(true);
                pcontext.getWriter().write(EventListener.RESULT_OK);
            } else {
                org.exoplatform.webui.core.UIApplication uiApp = pcontext.getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIPage.msg.EditPermission.null", null));
            }
        }
    }
}

/*
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

package org.exoplatform.portal.application;

import java.util.Arrays;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserSiteLifeCycle implements ApplicationLifecycle<PortalRequestContext> {

    /** . */
    private final Logger log = LoggerFactory.getLogger(UserSiteLifeCycle.class);

    private static final String DEFAULT_TAB_NAME = "Tab_Default";

    private static final String PAGE_TEMPLATE = "dashboard";

    public void onInit(Application app) throws Exception {
    }

    public void onStartRequest(Application app, PortalRequestContext context) throws Exception {
        String userName = context.getRemoteUser();
        if (userName != null && SiteType.USER == context.getSiteType() && userName.equals(context.getSiteName())) {
            DataStorage storage = (DataStorage) PortalContainer.getComponent(DataStorage.class);
            UserPortalConfigService configService = (UserPortalConfigService) PortalContainer
                    .getComponent(UserPortalConfigService.class);
            PortalConfig portalConfig = storage.getPortalConfig("user", userName);

            //
            if (portalConfig == null) {
                log.debug("About to create user site for user " + userName);
                configService.createUserSite(userName);
                // BZ 1059036 - Force the flushing of JCR.
                RequestLifeCycle.end();
                RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
            }

            UserPortalConfig userPortalConfig = context.getUserPortalConfig();
            UserPortal userPortal = userPortalConfig.getUserPortal();
            SiteKey siteKey = context.getSiteKey();
            UserNavigation nav = userPortal.getNavigation(siteKey);

            try {
                UserNode rootNode = userPortal.getNode(nav, Scope.CHILDREN, UserNodeFilterConfig.builder().build(), null);
                if (rootNode.getChildren().size() < 1) {
                    // TODO: Retrieve tab name from request
                    Page page = configService.createPageTemplate(PAGE_TEMPLATE, siteKey.getTypeName(), siteKey.getName());
                    page.setName(DEFAULT_TAB_NAME);
                    page.setTitle(DEFAULT_TAB_NAME);

                    //
                    PageState pageState = PageUtils.toPageState(page);
                    configService.getPageService().savePage(new PageContext(page.getPageKey(), pageState));

                    //
                    storage.save(page);

                    //
                    UserNode tabNode = rootNode.addChild(DEFAULT_TAB_NAME);
                    tabNode.setLabel(DEFAULT_TAB_NAME);
                    tabNode.setPageRef(PageKey.parse(page.getPageId()));

                    userPortal.saveNode(tabNode, null);
                }
            } catch (Exception ex) {
                log.warn("Navigation " + nav.getKey().getName() + " does not exist!", ex);
            }

        }
    }

    public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) {
    }

    public void onEndRequest(Application app, PortalRequestContext context) throws Exception {
    }

    public void onDestroy(Application app) throws Exception {
    }
}

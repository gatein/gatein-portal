/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.header;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.security.sso.SSOHelper;
import org.exoplatform.webui.organization.OrganizationUtils;
import org.exoplatform.portal.webui.util.Util;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class HeaderBean {

    private static final String GROUP_NAVIGATION_NODE = "groupnavigation";
    private static final String REGISTER_NODE = "register";

    private final SSOHelper ssoHelper;
    protected int nodeLevel;

    public HeaderBean(int nodeLevel) {
        this.nodeLevel = nodeLevel;
        ssoHelper = (SSOHelper) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SSOHelper.class);
    }

    public void setNodeLevel(int nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public int getNodeLevel() {
        return nodeLevel;
    }

    public String generateLoginLink() {
        if (ssoHelper != null && ssoHelper.isSSOEnabled()) {
            PortalRequestContext pContext = Util.getPortalRequestContext();
            String ssoRedirectURL = pContext.getRequest().getContextPath() + ssoHelper.getSSORedirectURLSuffix();
            return ssoRedirectURL;
        } else {
            return "#";
        }
    }

    public String generateLoginAction(String defaultAction) {
        if (ssoHelper != null && ssoHelper.isSSOEnabled()) {
            return "#";
        } else {
            return defaultAction;
        }
    }

    public String generateRegisterLink() {
        PortalRequest portalRequest = PortalRequest.getInstance();
        String portalURL = portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId());
        return portalURL + "/" + REGISTER_NODE;
    }

    public String generateHomePageLink() throws Exception {
        PortalRequest portalRequest = PortalRequest.getInstance();
        String portalURL = portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId());
        return portalURL;
    }

    public String generateDashboardLink() throws Exception {
        PortalRequest portalRequest = PortalRequest.getInstance();
        return portalRequest.getURIResolver().resolveURI(new SiteId(portalRequest.getUser()));
    }

    public String generateGroupPagesLink() {
        Navigation navigation = PortalRequest.getInstance().getNavigation();
        Node navigationNode = navigation.getNode(GROUP_NAVIGATION_NODE);

        if (navigationNode != null) {
            return navigationNode.getURI().toString();
        } else {
            return null;
        }
    }

    public Map<String, NodeBean> getGroupNodes() throws Exception {

        Map<String, NodeBean> navNodes = new HashMap<String, NodeBean>();

        PortalRequest portalRequest = PortalRequest.getInstance();

        SiteQuery.Builder siteQueryBulder = new SiteQuery.Builder();
        SiteQuery siteQuery = siteQueryBulder.withSiteTypes(SiteType.SPACE).includeEmptySites(false).build();
        List<Site> groupSites = PortalRequest.getInstance().getPortal().findSites(siteQuery);

        for (Site site : groupSites) {
            // check permissions and handle the special 'guest' site
            if (portalRequest.getPortal().hasPermission(portalRequest.getUser(), site.getAccessPermission())
                    && !site.getName().equals("/platform/guests")) {
                Navigation siteNavigation = portalRequest.getPortal().getNavigation(site.getId());
                Node node = siteNavigation.getRootNode(Nodes.visitNodes(this.nodeLevel));
                if (node.isVisible()) {
                    String groupLabel = OrganizationUtils.getGroupLabel(siteNavigation.getSiteId().getName().toString());
                    NodeBean nodeBean = new NodeBean(node, site.getId(), true);
                    navNodes.put(groupLabel, nodeBean);
                }
            }
        }

        return navNodes;
    }

}

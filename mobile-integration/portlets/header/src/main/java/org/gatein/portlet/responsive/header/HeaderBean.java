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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.security.sso.SSOHelper;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.organization.OrganizationUtils;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class HeaderBean {

    private static final String GROUP_NAVIGATION_NODE = "groupnavigation";

    private final SSOHelper ssoHelper;

    public HeaderBean() {
        ssoHelper = (SSOHelper) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SSOHelper.class);
    }

    public String generateLoginLink(String defaultAction) {
        if (ssoHelper != null) {
            PortalRequestContext pContext = Util.getPortalRequestContext();
            String ssoRedirectURL = pContext.getRequest().getContextPath() + ssoHelper.getSSORedirectURLSuffix();
            return ssoRedirectURL;
        } else {
            return defaultAction;
        }
    }

    public String generateRegisterLink() {
        PortalRequestContext pContext = Util.getPortalRequestContext();
        NavigationResource resource = new NavigationResource(SiteType.PORTAL, pContext.getPortalOwner(), "register");
        return resource.getNodeURI();
    }

    public String generateHomePageLink() throws Exception {
        PortalRequestContext pContext = Util.getPortalRequestContext();
        NodeURL nodeURL = pContext.createURL(NodeURL.TYPE).setResource(
                new NavigationResource(SiteType.PORTAL, pContext.getPortalOwner(), null));
        return nodeURL.toString();
    }

    public String generateDashboardLink() throws Exception {
        PortalRequestContext pContext = Util.getPortalRequestContext();
        NodeURL nodeURL = pContext.createURL(NodeURL.TYPE);
        nodeURL.setResource(new NavigationResource(SiteType.USER, pContext.getRemoteUser(), null));
        return nodeURL.toString();
    }

    public String generateGroupPagesLink() {
        PortalRequestContext pContext = Util.getPortalRequestContext();
        NodeURL nodeURL = pContext.createURL(NodeURL.TYPE);
        nodeURL.setResource(new NavigationResource(SiteType.PORTAL, pContext.getPortalOwner(), GROUP_NAVIGATION_NODE));
        return nodeURL.toString();
    }

    public Map<String, List<Node>> getGroupNodes() throws Exception {
        Map<String, List<Node>> navNodes = new HashMap<String, List<Node>>();

        PortalRequestContext pContext = Util.getPortalRequestContext();
        UserPortal userPortal = pContext.getUserPortal();

        List<UserNavigation> groupNavigations = getGroupNavigations(userPortal);

        for (UserNavigation groupNavigation : groupNavigations) {
            String groupName = OrganizationUtils.getGroupLabel(groupNavigation.getKey().getName());
            String groupTitle = groupName;

            UserNode userNode = userPortal.getNode(groupNavigation, Scope.ALL, getFilter(), null);
            List<Node> nodes = new ArrayList<Node>();
            for (UserNode childnode : userNode.getChildren()) {
                Node node = getNode(childnode);
                nodes.add(node);
            }

            navNodes.put(groupTitle, nodes);
        }
        return navNodes;
    }

    public Node getNode(UserNode userNode) {
        Node node;
        if (userNode.getPageRef() != null) {
            NodeURL nodeURL = Util.getPortalRequestContext().createURL(NodeURL.TYPE);
            nodeURL.setResource(new NavigationResource(userNode));

            node = new Node(userNode.getEncodedResolvedLabel(), nodeURL.toString());
        } else {
            node = new Node(userNode.getEncodedResolvedLabel(), null);
        }

        List<Node> children = new ArrayList<Node>();
        for (UserNode childNode : userNode.getChildren()) {
            children.add(getNode(childNode));
        }
        node.setChildren(children);

        return node;
    }

    protected List<UserNavigation> getGroupNavigations(UserPortal userPortal) {
        List<UserNavigation> userNavigations = userPortal.getNavigations();

        // TODO: Can we not just access the Group Navigations directly?
        List<UserNavigation> groupNavigations = new ArrayList<UserNavigation>();
        for (UserNavigation userNavigation : userNavigations) {
            if (userNavigation.getKey().getType().equals(SiteType.GROUP)) {
                groupNavigations.add(userNavigation);
            }
        }

        return groupNavigations;
    }

    protected UserNodeFilterConfig getFilter() {
        UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
        builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
        builder.withTemporalCheck();
        return builder.build();
    }
}

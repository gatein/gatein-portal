/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2013, Red Hat Middleware, LLC, and individual                    *
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.site.SiteId;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

public abstract class NodePortlet extends GenericPortlet {

    private static final Logger log = LoggerFactory.getLogger(NodePortlet.class);

    private final String EMPTY_CATEGORY_PROPERTY_NAME = "ShowEmptyCategories";
    public static final String NODE_RESOURCE_ID = "node";

    /**
     * The serveResource method is used for handling AJAX requests. It's used for the rendering of sub-menus. Anytime users
     * clicks on the menu item, the URI parameter is passed to the serveResource method. This parameter contains the URI of the
     * node which sub-menu is about to be rendered.
     *
     * @see javax.portlet.GenericPortlet#serveResource(javax.portlet.ResourceRequest, javax.portlet.ResourceResponse)
     *
     * @param request the resource request
     * @param response the resource response
     * @throws PortletException if the portlet has problems fulfilling the rendering request
     * @throws IOException if the streaming causes an I/O problem
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String resourceID = request.getResourceID();

        if (resourceID != null && resourceID.equals(NODE_RESOURCE_ID)) {
            String siteId = request.getParameter("siteId");
            Navigation navigation = PortalRequest.getInstance().getPortal().getNavigation(SiteId.fromString(siteId));

            String chosenNodeURI = request.getParameter("uri");

            Node chosenNode = navigation.getNode(NodePath.fromString(chosenNodeURI), Nodes.visitNodes(getNodeLevel(request)));

            boolean showEmptyCategory = getShowEmptyCategory(request);

            NodeBean chosenNodeBean = generateNodeBean(chosenNode, SiteId.fromString(siteId), showEmptyCategory);

            request.setAttribute("showEmptyCategory", showEmptyCategory);
            request.setAttribute("parentNode", chosenNodeBean);

            PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/node.jsp");
            prd.include(request, response);
        }
    }

    protected NodeBean generateNodeBean(Node node, SiteId siteId, boolean showEmptyCategory) {
        NodeBean nodeBean = new NodeBean(node, siteId);
        List<NodeBean> nodeChildrenList = getChildren(node, siteId, false, showEmptyCategory);
        if (!showEmptyCategory) {
            nodeBean.setEmptyCategory(isEmptyCategory(node));
        }
        nodeBean.setChildren(nodeChildrenList);
        return nodeBean;
    }

    private boolean isEmptyCategory(Node n) {

        if (n.getPageId() != null) {
            return false;

        } else {
            Navigation navigation = PortalRequest.getInstance().getNavigation();
            Iterator<Node> iterator = n.iterator();

            while (iterator.hasNext()) {
                Node child = navigation.getNode(iterator.next().getNodePath(), Nodes.visitNodes(1));

                if (child != null && !isEmptyCategory(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<NodeBean> getChildren(Node node, SiteId siteId, boolean firstActive, boolean showEmptyCategory) {
        List<NodeBean> nodes = new ArrayList<NodeBean>();

        boolean firstActiveSet = false;

        Iterator<Node> nodeIterator = node.iterator();

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.next();
            NodeBean childNodeBean = new NodeBean(childNode, siteId);

            if (firstActive && !firstActiveSet) {
                childNodeBean.setActive(true);
                firstActiveSet = true;
            }

            if (!showEmptyCategory) {
                childNodeBean.setEmptyCategory(isEmptyCategory(childNode));
            }

            nodes.add(childNodeBean);
        }
        return nodes;
    }

    protected boolean getShowEmptyCategory(PortletRequest request) {
        PortletPreferences portletPreferences = request.getPreferences();
        String showEmptyCategoryStringValue = portletPreferences.getValue(EMPTY_CATEGORY_PROPERTY_NAME, "false");
        return Boolean.valueOf(showEmptyCategoryStringValue);
    }

    protected abstract int getNodeLevel(PortletRequest request);
}

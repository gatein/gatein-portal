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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.navigation.Visibility;
import org.gatein.api.site.SiteId;

public class NodeBean {

    private final String EMPTY_CATEGORY_PROPERTY_NAME = "ShowEmptyCategories";

    private Node node;

    private SiteId siteId;

    List<NodeBean> children;

    protected boolean showEmptyCategories = false;

    public List<NodeBean> getChildren() {
        return children;
    }

    public void setChildren(List<NodeBean> children) {
        this.children = children;
    }

    private boolean emptyCategory;

    public void setEmptyCategory(boolean emptyCategory) {
        this.emptyCategory = emptyCategory;
    }

    public boolean isEmptyCategory() {
        return emptyCategory;
    }

    /**
     * Flag marking currently accessed node. If the page within the node is accessed in the browser, the node state should be
     * set to active.
     */
    private boolean active = false;

    /**
     * Returns true, if the current node is a system node. A system node is a node with visibility status of value
     * {@code Visibility.Status.SYSTEM}.
     *
     * @return true, if the current node is a system node.
     */
    public boolean isSystem() {
        return node.getVisibility().getStatus().equals(Visibility.Status.SYSTEM);
    }

    /**
     * Constructs a new {@link NavigationNodeBean} encapsulating the {@link org.gatein.api.navigation.Node} instance specified
     * in the parameter.
     *
     * @param node A node to be encapsulated by this NavigationNodeBean.
     */
    public NodeBean(Node node, SiteId siteId) {
        this(node, siteId, false);
    }

    public NodeBean(Node node, SiteId siteId, Boolean showEmptyCategories) {
        this.node = node;
        this.siteId = siteId;
        this.showEmptyCategories = showEmptyCategories;

        List<NodeBean> nodeChildrenList = getChildren(node, siteId, false, showEmptyCategories);
        if (!showEmptyCategories) {
            setEmptyCategory(isEmptyCategory(node));
        }
        setChildren(nodeChildrenList);
    }

    /**
     * Sets the current {@link NavigationNodeBean} node active state. The active state indicates that this bean is being
     * currently accessed in the user interface.
     *
     * @param active A boolean value.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the current NavigationNodeBean active state.
     *
     * @see #setActive(boolean)
     * @return The current NavigationNodeBean active state.
     */
    public boolean isActive() {
        NodePath currentPath = PortalRequest.getInstance().getNodePath();
        NodePath nodePath = node.getNodePath();

        if (!active) {
            active = (nodePath != null) ? nodePath.equals(currentPath) : false;
        }

        return active;
    }

    /**
     * Returns the {@link String} value of path to the current NavigationNodeBean.
     *
     * @return The {@link String} value of path to the current NavigationNodeBean.
     */
    public String getPath() {
        return this.node.getNodePath().toString();
    }

    public String getSiteId() {
        return this.siteId.toString();
    }

    /**
     * Returns true if current node has a page assigned to it, false otherwise. Node doesn't have to have the page assigned.
     * Node without any pages can serve as "categories".
     *
     * @return true if current node has a page assigned to it, false otherwise.
     */
    public boolean isPage() {
        return node.getPageId() != null;
    }

    /**
     * Returns true if parent node contains one or more children nodes.
     *
     * @return true if current node children count is higher than 0 (it contains any children nodes), false otherwise.
     */
    public boolean isParent() {
        return node.getChildCount() > 0;
    }

    /**
     * Returns the display name of the encapsulated node.
     *
     * @return The display name of the encapsulated node.
     */
    public String getName() {
        return node.getDisplayName();
    }

    /**
     * Returns the URI string of the encapsulated node.
     *
     * @return The URI string of the encapsulated node.
     */
    public String getURI() {
        return node.getURI();
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

}

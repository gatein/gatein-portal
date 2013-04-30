/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.portlet.responsive.navigation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Visibility;

/**
 *
 * {@link NavigationNodeBean} is representing a single navigation node. It basically encapsulates
 * the {@link org.gatein.api.navigation.Node} from the GateIn Portal API.
 *
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class NavigationNodeBean {

    private Node node;

    List<NavigationNodeBean> children;

    public List<NavigationNodeBean> getChildren(){
        return children;
    }

    public void setChildren(List<NavigationNodeBean> children){
        this.children = children;
    }

    private boolean emptyCategory;

    public void setEmptyCategory(boolean emptyCategory){
        this.emptyCategory = emptyCategory;
    }

    public boolean isEmptyCategory(){
        return emptyCategory;
    }

    /**
     * Flag marking currently accessed node. If the page within the node is accessed in the browser, the node state should
     * be set to active.
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
    public NavigationNodeBean(Node node) {
        this.node = node;
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
    public String getPath(){
        return this.node.getNodePath().toString();
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
}
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
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class NavigationNodeBean {

    private Node node;

    /*
     * Flag marking currently accessed node
     */
    private boolean active = false;

    private boolean firstActive = false;

    public boolean isSystem() {
        return node.getVisibility().getStatus().equals(Visibility.Status.SYSTEM);
    }

    public NavigationNodeBean(Node node) {
        this.node = node;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        NodePath currentPath = PortalRequest.getInstance().getNodePath();
        NodePath nodePath = node.getNodePath();

        if (!active) {
            active = (nodePath != null) ? nodePath.equals(currentPath) : false;
        }

        return active;
    }

    public boolean isPage() {
        return node.getPageId() != null;
    }

    /*
     * Parent node contains one or more children nodes
     */
    public boolean isParent() {
        return !getChildren().isEmpty();
    }

    /*
     * Parent node contains one or more renderable (containing page or children) children nodes
     */
    public boolean isMenuCategory() {
        for (NavigationNodeBean child : getChildren()) {
            if (child.isMenuCategory() || child.isPage()) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return node.getDisplayName();
    }

    public String getURI() {
        return node.getURI();
    }

    public void setFirstActive(){
        firstActive = true;
    }

    public List<NavigationNodeBean> getChildren() {
        List<NavigationNodeBean> nodes = new ArrayList<NavigationNodeBean>();

        boolean firstActiveSet = false;

        Iterator<Node> nodeIterator = node.iterator();

        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.next();
            NavigationNodeBean childNodeBean = new NavigationNodeBean(childNode);

            if (firstActive && !firstActiveSet){
                childNodeBean.setActive(true);
                firstActiveSet = true;
            }
            nodes.add(childNodeBean);
        }

        return nodes;
    }
}
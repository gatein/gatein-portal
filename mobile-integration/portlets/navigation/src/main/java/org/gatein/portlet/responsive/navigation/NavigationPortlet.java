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

import java.io.IOException;
import java.util.List;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequestDispatcher;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;

/**
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class NavigationPortlet extends GenericPortlet {

    NavigationNodeBean navigationRootNodeBean;

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        PortalRequest portalRequest = PortalRequest.getInstance();

        Navigation navigation = portalRequest.getNavigation();
        Node rootNode = navigation.getRootNode(Nodes.ALL);

        navigationRootNodeBean = new NavigationNodeBean(rootNode);

        /* Setting the 1st node to be active when accesing the root node "/" */
        List<NavigationNodeBean> rootNodeChildrenList = navigationRootNodeBean.getChildren();

        if (!rootNodeChildrenList.isEmpty() && portalRequest.getNodePath().equals(NodePath.root())){
            navigationRootNodeBean.setFirstActive();
        }

        request.setAttribute("navigationRootNode", navigationRootNodeBean);
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/navigation.jsp");
        prd.include(request, response);
    }
}

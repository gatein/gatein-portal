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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.PortletRequestDispatcher;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;

/**
 * Responsive version of the navigation portlet implemented using the GateIn navigation API.
 *
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class NavigationPortlet extends GenericPortlet {

    private String EMPTY_CATEGORY_PROPERTY_NAME = "ShowEmptyCategories";

    /**
     * Method responsible for the VIEW mode of the navigation portlet.
     * This method passes the navigationRootNodeBean as an attribute to the JSP page.
     * The navigationRootNodeBean is the root node of the navigation and contains main menu (top-menu) elements (Home and Sitemap
     * by default) as children nodes.
     *
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     * @param request the portlet request
     * @param response the render response
     * @throws PortletException if the portlet cannot fulfilling the request
     * @throws IOException if the streaming causes an I/O problem
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        PortalRequest portalRequest = PortalRequest.getInstance();

        Navigation navigation = PortalRequest.getInstance().getNavigation();

        // Diving two levels so the information about children count of children nodes is available
        Node rootNode = navigation.getRootNode(Nodes.visitNodes(2)).filter().showDefault();

        // The root navigation bean contains the top-menu elements (Home and Sitemap by default) as its direct children nodes.
        NavigationNodeBean  navigationRootNodeBean = new NavigationNodeBean(rootNode);

        boolean showEmptyCategory = getShowEmptyCategory(request);

        if (!showEmptyCategory) {
            navigationRootNodeBean.setEmptyCategory(isEmptyCategory(rootNode));
        }

        /* Setting the 1st node to be active when accesing the root node "/" */
        boolean isRootNode = portalRequest.getNodePath().equals(NodePath.root());

        List<NavigationNodeBean> rootNodeChildrenList = getChildren(rootNode, isRootNode, showEmptyCategory);

        navigationRootNodeBean.setChildren(rootNodeChildrenList);

        request.setAttribute("showEmptyCategory", showEmptyCategory);
        request.setAttribute("navigationRootNode", navigationRootNodeBean);

        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/navigation.jsp");
        prd.include(request, response);
    }

    /**
     * The serveResource method is used for handling AJAX requests. It's used for the rendering of sub-menus. Anytime
     * users clicks on the menu item, the URI parameter is passed to the serveResource method. This parameter contains the URI
     * of the node which sub-menu is about to be rendered.
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

        Navigation navigation = PortalRequest.getInstance().getNavigation();

        String chosenNodeURI = request.getParameter("uri");

        Node chosenNode = navigation.getNode(NodePath.fromString(chosenNodeURI), Nodes.visitNodes(2)).filter().showDefault();

        NavigationNodeBean chosenNodeBean = new NavigationNodeBean(chosenNode);

        boolean showEmptyCategory = getShowEmptyCategory(request);

        List<NavigationNodeBean> nodeChildrenList = getChildren(chosenNode, false, showEmptyCategory);

        if (!showEmptyCategory) {
            chosenNodeBean.setEmptyCategory(isEmptyCategory(chosenNode));
        }

        chosenNodeBean.setChildren(nodeChildrenList);

        request.setAttribute("showEmptyCategory", showEmptyCategory);
        request.setAttribute("parentNode", chosenNodeBean);

        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/node.jsp");
        prd.include(request, response);
    }


    private boolean isEmptyCategory(Node n){

        if (n.getPageId() != null) {
            return false;

        } else {
            Navigation navigation = PortalRequest.getInstance().getNavigation();
            Iterator<Node> iterator = n.iterator();

            while (iterator.hasNext()){
                Node child = navigation.getNode(iterator.next().getNodePath(), Nodes.visitNodes(1)).filter().showDefault();

                if (!isEmptyCategory(child)){
                    return false;
                }
            }
        }

        return true;
    }

    private List<NavigationNodeBean> getChildren(Node node, boolean firstActive, boolean showEmptyCategory) {
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

            if (!showEmptyCategory) {
                childNodeBean.setEmptyCategory(isEmptyCategory(childNode));
            }

            nodes.add(childNodeBean);
        }

        return nodes;
    }

    private boolean getShowEmptyCategory(PortletRequest request){
        PortletPreferences portletPreferences = request.getPreferences();
        String showEmptyCategoryStringValue = portletPreferences.getValue(EMPTY_CATEGORY_PROPERTY_NAME, "false");
        return Boolean.valueOf(showEmptyCategoryStringValue);
    }
}
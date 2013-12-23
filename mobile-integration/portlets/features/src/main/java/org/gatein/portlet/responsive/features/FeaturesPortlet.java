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
package org.gatein.portlet.responsive.features;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class FeaturesPortlet extends GenericPortlet {

    public static final String FEATURE_PAGE_ATTRIBUTE = "featureLink";

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        String featureLink = null;
        if (request.getPreferences().getValue("features.node", null) != null) {
            String featurePageName = request.getPreferences().getValue("features.node", null);
            featureLink = generateFeaturePageLink(featurePageName);
        }

        if (featureLink != null) {
            request.setAttribute(FEATURE_PAGE_ATTRIBUTE, featureLink);
        }

        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/features.jsp");
        prd.include(request, response);
    }

    protected String generateFeaturePageLink(String featurePageName) {
        if (featurePageName != null) {
            Node featureNode = PortalRequest.getInstance().getNavigation().getNode(NodePath.fromString(featurePageName));
            if (featureNode != null) {
                return featureNode.getURI();
            }
        }
        return null;
    }

}
